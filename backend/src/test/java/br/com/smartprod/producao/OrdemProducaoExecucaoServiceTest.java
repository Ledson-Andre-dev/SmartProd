
package br.com.smartprod.producao;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import br.com.smartprod.usuario.Perfil;
import br.com.smartprod.usuario.Usuario;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class OrdemProducaoExecucaoServiceTest {

    @Mock
    private OrdemProducaoService ordemProducaoService;

    @InjectMocks
    private OrdemProducaoExecucaoService service;

    @Test
    void deveRetornar503QuandoIntegracaoNaoEstiverDisponivel() {
        Usuario usuario = new Usuario(
                "Funcionário Teste",
                "funcionario@teste.com",
                "hash-teste",
                Perfil.FUNCIONARIO);

        OrdemProducao ordem = new OrdemProducao(
                50,
                Instant.parse("2026-11-13T12:00:00Z"),
                usuario,
                usuario);

        when(ordemProducaoService.buscarPorId(1L))
                .thenReturn(ordem);

        assertThatThrownBy(() -> service.buscarExecucao(1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException erro = (ResponseStatusException) exception;

                    org.assertj.core.api.Assertions
                            .assertThat(erro.getStatusCode())
                            .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                });

        verify(ordemProducaoService).buscarPorId(1L);
    }

    @Test
    void deveRetornarErroQuandoOrdemNaoExistir() {
        when(ordemProducaoService.buscarPorId(99L))
                .thenThrow(new EntityNotFoundException(
                        "Ordem de produção não encontrada."));

        assertThatThrownBy(() -> service.buscarExecucao(99L))
                .isInstanceOf(EntityNotFoundException.class);

        verify(ordemProducaoService).buscarPorId(99L);
    }
}
