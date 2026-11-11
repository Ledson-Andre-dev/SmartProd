
package br.com.smartprod.producao;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.smartprod.usuario.Perfil;
import br.com.smartprod.usuario.Usuario;
import br.com.smartprod.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class OrdemProducaoServiceTest {

    @Mock
    private OrdemProducaoRepository repository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private OrdemProducaoService service;

    @Test
    void deveListarTodasAsOrdens() {
        when(repository.findAllByOrderByHorarioEntregaAsc())
                .thenReturn(List.of());

        List<OrdemProducao> ordens = service.listarTodas();

        assertThat(ordens).isEmpty();

        verify(repository).findAllByOrderByHorarioEntregaAsc();
    }

    @Test
    void deveListarOrdensPorStatus() {
        Usuario usuario = criarUsuario();
        OrdemProducao ordem = criarOrdem(usuario);
        ordem.iniciar();

        when(repository.findByStatusOrderByHorarioEntregaAsc(
                StatusOrdemProducao.EM_ANDAMENTO))
                .thenReturn(List.of(ordem));

        List<OrdemProducao> ordens = service.listarPorStatus(StatusOrdemProducao.EM_ANDAMENTO);

        assertThat(ordens)
                .hasSize(1)
                .containsExactly(ordem);

        verify(repository).findByStatusOrderByHorarioEntregaAsc(
                StatusOrdemProducao.EM_ANDAMENTO);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremOrdensComStatus() {
        when(repository.findByStatusOrderByHorarioEntregaAsc(
                StatusOrdemProducao.CONCLUIDA))
                .thenReturn(List.of());

        List<OrdemProducao> ordens = service.listarPorStatus(StatusOrdemProducao.CONCLUIDA);

        assertThat(ordens).isEmpty();

        verify(repository).findByStatusOrderByHorarioEntregaAsc(
                StatusOrdemProducao.CONCLUIDA);
    }

    @Test
    void deveBuscarOrdemPorId() {
        Usuario usuario = criarUsuario();
        OrdemProducao ordem = criarOrdem(usuario);

        when(repository.findById(1L))
                .thenReturn(Optional.of(ordem));

        OrdemProducao resultado = service.buscarPorId(1L);

        assertThat(resultado).isSameAs(ordem);
    }

    @Test
    void naoDeveBuscarOrdemInexistente() {
        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Ordem de produção não encontrada.");
    }

    @Test
    void naoDeveBuscarResponsavelInativo() {
        Usuario usuario = criarUsuario();
        usuario.inativar();

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> service.buscarResponsavelAtivo(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Usuário responsável não encontrado ou inativo.");
    }

    @Test
    void deveIniciarOrdem() {
        Usuario usuario = criarUsuario();
        OrdemProducao ordem = criarOrdem(usuario);

        when(repository.findById(1L))
                .thenReturn(Optional.of(ordem));

        OrdemProducao resultado = service.iniciar(1L);

        assertThat(resultado.getStatus())
                .isEqualTo(StatusOrdemProducao.EM_ANDAMENTO);

        assertThat(resultado.getIniciadaEm()).isNotNull();
    }

    @Test
    void deveConcluirOrdem() {
        Usuario usuario = criarUsuario();
        OrdemProducao ordem = criarOrdem(usuario);
        ordem.iniciar();

        when(repository.findById(1L))
                .thenReturn(Optional.of(ordem));

        OrdemProducao resultado = service.concluir(1L, 45);

        assertThat(resultado.getStatus())
                .isEqualTo(StatusOrdemProducao.CONCLUIDA);

        assertThat(resultado.getQuantidadeProduzida())
                .isEqualTo(45);

        assertThat(resultado.getConcluidaEm()).isNotNull();
    }

    @Test
    void deveBuscarResponsavelFuncionarioAtivo() {
        Usuario usuario = criarUsuario();

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        Usuario resultado = service.buscarResponsavelAtivo(1L);

        assertThat(resultado).isSameAs(usuario);

        verify(usuarioRepository).findById(1L);
    }

    @Test
    void naoDeveBuscarResponsavelInexistente() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarResponsavelAtivo(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Usuário responsável não encontrado ou inativo.");
    }

    @Test
    void naoDevePermitirGerenteComoResponsavel() {
        Usuario gerente = new Usuario(
                "Gerente Teste",
                "gerente@teste.com",
                "hash-teste",
                Perfil.GERENTE);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(gerente));

        assertThatThrownBy(() -> service.buscarResponsavelAtivo(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O responsável pela ordem deve ser um funcionário.");
    }

    @Test
    void deveBuscarGerenteAtivo() {
        Usuario gerente = criarGerente();

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(gerente));

        Usuario resultado = service.buscarGerenteAtivo(2L);

        assertThat(resultado).isSameAs(gerente);

        verify(usuarioRepository).findById(2L);
    }

    @Test
    void naoDevePermitirFuncionarioComoCriador() {
        Usuario funcionario = criarUsuario();

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(funcionario));

        assertThatThrownBy(() -> service.buscarGerenteAtivo(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Somente um gerente pode criar uma ordem de produção.");
    }

    @Test
    void naoDeveBuscarGerenteInativo() {
        Usuario gerente = criarGerente();
        gerente.inativar();

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(gerente));

        assertThatThrownBy(() -> service.buscarGerenteAtivo(2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Gerente criador não encontrado ou inativo.");
    }

    @Test
    void naoDeveBuscarGerenteInexistente() {
        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarGerenteAtivo(2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Gerente criador não encontrado ou inativo.");
    }

    private OrdemProducao criarOrdem(Usuario usuario) {
        return new OrdemProducao(
                50,
                Instant.now(),
                usuario,
                usuario);
    }

    private Usuario criarUsuario() {
        return new Usuario(
                "Funcionário Teste",
                "funcionario@teste.com",
                "hash-teste",
                Perfil.FUNCIONARIO);
    }

    private Usuario criarGerente() {
        return new Usuario(
                "Gerente Teste",
                "gerente@teste.com",
                "hash-teste",
                Perfil.GERENTE);
    }
}
