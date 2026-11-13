
package br.com.smartprod.perda;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.smartprod.insumo.UnidadeMedida;
import br.com.smartprod.producao.OrdemProducao;
import br.com.smartprod.producao.OrdemProducaoService;
import br.com.smartprod.usuario.Perfil;
import br.com.smartprod.usuario.Usuario;
import br.com.smartprod.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class RegistroPerdaServiceTest {

    @Mock
    private RegistroPerdaRepository repository;

    @Mock
    private OrdemProducaoService ordemProducaoService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private RegistroPerdaService service;

    @Test
    void deveRegistrarPerdaEmOrdemEmAndamento() {
        Usuario usuario = criarUsuario();
        OrdemProducao ordem = criarOrdem(usuario);
        ordem.iniciar();

        when(ordemProducaoService.buscarPorId(1L))
                .thenReturn(ordem);
        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(usuario));
        when(repository.save(any(RegistroPerda.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        RegistroPerda resultado = service.registrar(
                1L,
                "Modelagem",
                new BigDecimal("2.5000"),
                UnidadeMedida.KG,
                "Massa danificada",
                2L);

        assertThat(resultado.getOrdemProducao()).isSameAs(ordem);
        assertThat(resultado.getRegistradoPor()).isSameAs(usuario);
        assertThat(resultado.getEtapa()).isEqualTo("Modelagem");
        assertThat(resultado.getQuantidade())
                .isEqualByComparingTo("2.5000");
        assertThat(resultado.getUnidade()).isEqualTo(UnidadeMedida.KG);
        assertThat(resultado.getMotivo()).isEqualTo("Massa danificada");
        assertThat(resultado.getRegistradoEm()).isNotNull();

        ArgumentCaptor<RegistroPerda> captor = ArgumentCaptor.forClass(RegistroPerda.class);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(resultado);
    }

    @Test
    void naoDeveRegistrarPerdaEmOrdemPendente() {
        Usuario usuario = criarUsuario();
        OrdemProducao ordem = criarOrdem(usuario);

        when(ordemProducaoService.buscarPorId(1L))
                .thenReturn(ordem);

        assertThatThrownBy(() -> service.registrar(
                1L, "Preparo", BigDecimal.ONE,
                UnidadeMedida.KG, "Teste", 2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Perdas só podem ser registradas em ordens em andamento.");

        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveRegistrarPerdaEmOrdemConcluida() {
        Usuario usuario = criarUsuario();
        OrdemProducao ordem = criarOrdem(usuario);
        ordem.iniciar();
        ordem.concluir(50);

        when(ordemProducaoService.buscarPorId(1L))
                .thenReturn(ordem);

        assertThatThrownBy(() -> service.registrar(
                1L, "Forno", BigDecimal.ONE,
                UnidadeMedida.UN, "Produto queimado", 2L))
                .isInstanceOf(IllegalStateException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveRegistrarPerdaEmOrdemInexistente() {
        when(ordemProducaoService.buscarPorId(99L))
                .thenThrow(new EntityNotFoundException(
                        "Ordem de produção não encontrada."));

        assertThatThrownBy(() -> service.registrar(
                99L, "Preparo", BigDecimal.ONE,
                UnidadeMedida.KG, "Teste", 2L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveRegistrarPerdaComUsuarioInexistente() {
        Usuario usuario = criarUsuario();
        OrdemProducao ordem = criarOrdem(usuario);
        ordem.iniciar();

        when(ordemProducaoService.buscarPorId(1L))
                .thenReturn(ordem);
        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(
                1L, "Preparo", BigDecimal.ONE,
                UnidadeMedida.KG, "Teste", 2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Usuário não encontrado ou inativo.");

        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveRegistrarPerdaComUsuarioInativo() {
        Usuario usuario = criarUsuario();
        usuario.inativar();

        OrdemProducao ordem = criarOrdem(usuario);
        ordem.iniciar();

        when(ordemProducaoService.buscarPorId(1L))
                .thenReturn(ordem);
        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> service.registrar(
                1L, "Preparo", BigDecimal.ONE,
                UnidadeMedida.KG, "Teste", 2L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void deveListarPerdasPorOrdem() {
        Usuario usuario = criarUsuario();
        OrdemProducao ordem = criarOrdem(usuario);

        RegistroPerda perda = new RegistroPerda(
                ordem,
                "Modelagem",
                BigDecimal.ONE,
                UnidadeMedida.KG,
                "Massa danificada",
                usuario);

        when(ordemProducaoService.buscarPorId(1L))
                .thenReturn(ordem);
        when(repository.findByOrdemProducaoIdOrderByRegistradoEmDesc(1L))
                .thenReturn(List.of(perda));

        List<RegistroPerda> resultado = service.listarPorOrdem(1L);

        assertThat(resultado).containsExactly(perda);

        verify(repository)
                .findByOrdemProducaoIdOrderByRegistradoEmDesc(1L);
    }

    @Test
    void naoDeveListarPerdasDeOrdemInexistente() {
        when(ordemProducaoService.buscarPorId(99L))
                .thenThrow(new EntityNotFoundException(
                        "Ordem de produção não encontrada."));

        assertThatThrownBy(() -> service.listarPorOrdem(99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(repository, never())
                .findByOrdemProducaoIdOrderByRegistradoEmDesc(any());
    }

    private Usuario criarUsuario() {
        return new Usuario(
                "Funcionário Teste",
                "funcionario@teste.com",
                "hash-teste",
                Perfil.FUNCIONARIO);
    }

    private OrdemProducao criarOrdem(Usuario usuario) {
        return new OrdemProducao(
                50,
                Instant.now(),
                usuario,
                usuario);
    }
}
