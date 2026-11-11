
package br.com.smartprod.producao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import br.com.smartprod.usuario.Perfil;
import br.com.smartprod.usuario.Usuario;

class OrdemProducaoTest {

    @Test
    void naoDeveCriarOrdemComQuantidadeZero() {
        assertThatThrownBy(() -> new OrdemProducao(
                0,
                java.time.Instant.now(),
                null,
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A quantidade de unidades deve ser maior que zero.");
    }

    @Test
    void naoDeveCriarOrdemComQuantidadeNegativa() {
        assertThatThrownBy(() -> new OrdemProducao(
                -5,
                java.time.Instant.now(),
                null,
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A quantidade de unidades deve ser maior que zero.");
    }

    @Test
    void naoDeveCriarOrdemSemHorarioEntrega() {
        assertThatThrownBy(() -> new OrdemProducao(
                10,
                null,
                null,
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O horário de entrega é obrigatório.");
    }

    @Test
    void naoDeveCriarOrdemSemUsuarios() {
        assertThatThrownBy(() -> new OrdemProducao(
                10,
                java.time.Instant.now(),
                null,
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O responsável e o criador da ordem são obrigatórios.");
    }

    @Test
    void deveIniciarOrdemPendente() {
        Usuario usuario = criarUsuario();

        OrdemProducao ordem = new OrdemProducao(
                50,
                java.time.Instant.now(),
                usuario,
                usuario);

        ordem.iniciar();

        assertThat(ordem.getStatus())
                .isEqualTo(StatusOrdemProducao.EM_ANDAMENTO);

        assertThat(ordem.getIniciadaEm())
                .isNotNull();
    }

    @Test
    void naoDeveIniciarOrdemMaisDeUmaVez() {
        Usuario usuario = criarUsuario();

        OrdemProducao ordem = new OrdemProducao(
                50,
                java.time.Instant.now(),
                usuario,
                usuario);

        ordem.iniciar();

        assertThatThrownBy(() -> ordem.iniciar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Somente ordens pendentes podem ser iniciadas.");
    }

    @Test
    void deveConcluirOrdemEmAndamento() {
        Usuario usuario = criarUsuario();

        OrdemProducao ordem = new OrdemProducao(
                50,
                java.time.Instant.now(),
                usuario,
                usuario);

        ordem.iniciar();
        ordem.concluir(45);

        assertThat(ordem.getStatus())
                .isEqualTo(StatusOrdemProducao.CONCLUIDA);

        assertThat(ordem.getQuantidadeProduzida())
                .isEqualTo(45);

        assertThat(ordem.getConcluidaEm())
                .isNotNull();
    }

    @Test
    void naoDeveConcluirOrdemPendente() {
        Usuario usuario = criarUsuario();

        OrdemProducao ordem = new OrdemProducao(
                50,
                java.time.Instant.now(),
                usuario,
                usuario);

        assertThatThrownBy(() -> ordem.concluir(45))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Somente ordens em andamento podem ser concluídas.");
    }

    @Test
    void naoDeveConcluirOrdemMaisDeUmaVez() {
        Usuario usuario = criarUsuario();

        OrdemProducao ordem = new OrdemProducao(
                50,
                java.time.Instant.now(),
                usuario,
                usuario);

        ordem.iniciar();
        ordem.concluir(45);

        assertThatThrownBy(() -> ordem.concluir(50))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Somente ordens em andamento podem ser concluídas.");
    }

    @Test
    void naoDeveConcluirComQuantidadeProduzidaNula() {
        Usuario usuario = criarUsuario();

        OrdemProducao ordem = new OrdemProducao(
                50,
                java.time.Instant.now(),
                usuario,
                usuario);

        ordem.iniciar();

        assertThatThrownBy(() -> ordem.concluir(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A quantidade produzida não pode ser nula ou negativa.");
    }

    @Test
    void naoDeveConcluirComQuantidadeProduzidaNegativa() {
        Usuario usuario = criarUsuario();

        OrdemProducao ordem = new OrdemProducao(
                50,
                java.time.Instant.now(),
                usuario,
                usuario);

        ordem.iniciar();

        assertThatThrownBy(() -> ordem.concluir(-5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A quantidade produzida não pode ser nula ou negativa.");
    }

    @Test
    void naoDeveIniciarOrdemConcluida() {
        Usuario usuario = criarUsuario();

        OrdemProducao ordem = new OrdemProducao(
                50,
                java.time.Instant.now(),
                usuario,
                usuario);

        ordem.iniciar();
        ordem.concluir(45);

        assertThatThrownBy(() -> ordem.iniciar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Somente ordens pendentes podem ser iniciadas.");

        assertThat(ordem.getStatus())
                .isEqualTo(StatusOrdemProducao.CONCLUIDA);
    }

    @Test
    void deveManterDadosDaOrdemAposTentativaDeAlteracao() {
        Usuario usuario = criarUsuario();

        OrdemProducao ordem = new OrdemProducao(
                50,
                java.time.Instant.now(),
                usuario,
                usuario);

        ordem.iniciar();
        ordem.concluir(45);

        assertThatThrownBy(() -> ordem.concluir(30))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ordem.getQuantidadeProduzida()).isEqualTo(45);
        assertThat(ordem.getStatus()).isEqualTo(StatusOrdemProducao.CONCLUIDA);
    }

    private Usuario criarUsuario() {
        return new Usuario(
                "Funcionário Teste",
                "funcionario@teste.com",
                "hash-teste",
                Perfil.FUNCIONARIO);
    }
}
