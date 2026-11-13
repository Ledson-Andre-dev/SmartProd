
package br.com.smartprod.perda;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.smartprod.insumo.UnidadeMedida;
import br.com.smartprod.producao.OrdemProducao;
import br.com.smartprod.producao.OrdemProducaoRepository;
import br.com.smartprod.usuario.Perfil;
import br.com.smartprod.usuario.Usuario;
import br.com.smartprod.usuario.UsuarioRepository;
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RegistroPerdaRepositoryTest {

    @Autowired
    private RegistroPerdaRepository repository;

    @Autowired
    private OrdemProducaoRepository ordemRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void deveSalvarEConsultarPerdaPorOrdem() {
        Usuario usuario = criarUsuario("usuario1@teste.com");
        OrdemProducao ordem = criarOrdem(usuario);

        RegistroPerda perda = criarPerda(
                ordem, usuario, "Modelagem", "Massa danificada");

        repository.saveAndFlush(perda);

        List<RegistroPerda> resultado =
                repository.findByOrdemProducaoIdOrderByRegistradoEmDesc(
                        ordem.getId());

        assertThat(resultado)
                .hasSize(1)
                .containsExactly(perda);

        assertThat(resultado.get(0).getQuantidade())
                .isEqualByComparingTo("2.5000");
    }

    @Test
    void deveConsultarSomentePerdasDaOrdemInformada() {
        Usuario usuario = criarUsuario("usuario2@teste.com");

        OrdemProducao ordem1 = criarOrdem(usuario);
        OrdemProducao ordem2 = criarOrdem(usuario);

        RegistroPerda perda1 = criarPerda(
                ordem1, usuario, "Preparo", "Ingrediente perdido");

        RegistroPerda perda2 = criarPerda(
                ordem2, usuario, "Forno", "Produto queimado");

        repository.saveAllAndFlush(List.of(perda1, perda2));

        List<RegistroPerda> resultado =
                repository.findByOrdemProducaoIdOrderByRegistradoEmDesc(
                        ordem1.getId());

        assertThat(resultado).containsExactly(perda1);
    }

    @Test
    void deveOrdenarPerdasDaMaisRecenteParaMaisAntiga() {
        Usuario usuario = criarUsuario("usuario3@teste.com");
        OrdemProducao ordem = criarOrdem(usuario);

        RegistroPerda antiga = criarPerda(
                ordem, usuario, "Preparo", "Primeira perda");

        RegistroPerda recente = criarPerda(
                ordem, usuario, "Modelagem", "Segunda perda");

        ReflectionTestUtils.setField(
                antiga,
                "registradoEm",
                Instant.parse("2026-11-10T10:00:00Z"));

        ReflectionTestUtils.setField(
                recente,
                "registradoEm",
                Instant.parse("2026-11-10T11:00:00Z"));

        repository.saveAllAndFlush(List.of(antiga, recente));

        List<RegistroPerda> resultado =
                repository.findByOrdemProducaoIdOrderByRegistradoEmDesc(
                        ordem.getId());

        assertThat(resultado).containsExactly(recente, antiga);
    }

    @Test
    void naoDevePersistirQuantidadeNegativa() {
        Usuario usuario = criarUsuario("usuario4@teste.com");
        OrdemProducao ordem = criarOrdem(usuario);

        RegistroPerda perda = criarPerda(
                ordem, usuario, "Preparo", "Teste");

        ReflectionTestUtils.setField(
                perda, "quantidade", new BigDecimal("-1.0000"));

        assertThatThrownBy(() -> repository.saveAndFlush(perda))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Usuario criarUsuario(String email) {
        return usuarioRepository.saveAndFlush(
                new Usuario(
                        "Funcionário Teste",
                        email,
                        "hash-teste",
                        Perfil.FUNCIONARIO));
    }

    private OrdemProducao criarOrdem(Usuario usuario) {
        OrdemProducao ordem = new OrdemProducao(
                50,
                Instant.parse("2026-11-13T12:00:00Z"),
                usuario,
                usuario);

        ordem.iniciar();

        return ordemRepository.saveAndFlush(ordem);
    }

    private RegistroPerda criarPerda(
            OrdemProducao ordem,
            Usuario usuario,
            String etapa,
            String motivo) {

        return new RegistroPerda(
                ordem,
                etapa,
                new BigDecimal("2.5000"),
                UnidadeMedida.KG,
                motivo,
                usuario);
    }
}
