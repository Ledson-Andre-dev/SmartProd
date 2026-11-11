
package br.com.smartprod.producao;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import br.com.smartprod.usuario.Perfil;
import br.com.smartprod.usuario.Usuario;
import br.com.smartprod.usuario.UsuarioRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrdemProducaoRepositoryTest {

    @Autowired
    private OrdemProducaoRepository repository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void deveBuscarOrdensDentroDoIntervaloDeData() {
        Usuario usuario = criarUsuario();

        LocalDate data = LocalDate.of(2026, 11, 10);
        Instant inicio = data.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant fim = data.plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        OrdemProducao ordem1 = criarOrdem(
                usuario, Instant.parse("2026-11-10T08:00:00Z"));

        OrdemProducao ordem2 = criarOrdem(
                usuario, Instant.parse("2026-11-10T15:00:00Z"));

        OrdemProducao ordem3 = criarOrdem(
                usuario, Instant.parse("2026-11-11T08:00:00Z"));

        repository.saveAllAndFlush(List.of(ordem1, ordem2, ordem3));

        List<OrdemProducao> resultado =
                repository
                        .findByHorarioEntregaGreaterThanEqualAndHorarioEntregaLessThanOrderByHorarioEntregaAsc(
                                inicio, fim);

        assertThat(resultado)
                .hasSize(2)
                .containsExactly(ordem1, ordem2);
    }

    @Test
    void deveBuscarOrdensPorStatusEDentroDoIntervaloDeData() {
        Usuario usuario = criarUsuario();

        LocalDate data = LocalDate.of(2026, 11, 10);
        Instant inicio = data.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant fim = data.plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        OrdemProducao pendente = criarOrdem(
                usuario, Instant.parse("2026-11-10T08:00:00Z"));

        OrdemProducao emAndamento = criarOrdem(
                usuario, Instant.parse("2026-11-10T15:00:00Z"));

        emAndamento.iniciar();

        repository.saveAllAndFlush(List.of(pendente, emAndamento));

        List<OrdemProducao> resultado =
                repository
                        .findByStatusAndHorarioEntregaGreaterThanEqualAndHorarioEntregaLessThanOrderByHorarioEntregaAsc(
                                StatusOrdemProducao.EM_ANDAMENTO, inicio, fim);

        assertThat(resultado)
                .hasSize(1)
                .containsExactly(emAndamento);
    }

    private Usuario criarUsuario() {
        return usuarioRepository.saveAndFlush(
                new Usuario(
                        "Funcionário Teste",
                        "funcionario@teste.com",
                        "hash-teste",
                        Perfil.FUNCIONARIO));
    }

    private OrdemProducao criarOrdem(
            Usuario usuario,
            Instant horarioEntrega) {

        return new OrdemProducao(
                50,
                horarioEntrega,
                usuario,
                usuario);
    }
}
