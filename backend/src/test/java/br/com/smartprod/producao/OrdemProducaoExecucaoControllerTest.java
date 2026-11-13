
package br.com.smartprod.producao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class OrdemProducaoExecucaoControllerTest {

    @Mock
    private OrdemProducaoExecucaoService service;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new OrdemProducaoExecucaoController(service))
                .setControllerAdvice(new OrdemProducaoExecucaoExceptionHandler())
                .build();
    }

    @Test
    void deveRetornar503QuandoExecucaoIndisponivel() throws Exception {
        when(service.buscarExecucao(1L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Dados de execução indisponíveis."));

        mockMvc.perform(get("/api/ordens-producao/1/execucao"))
                .andExpect(status().isServiceUnavailable());

        verify(service).buscarExecucao(1L);
    }

    @Test
    void deveRetornar404QuandoOrdemNaoExistir() throws Exception {
        when(service.buscarExecucao(99L))
                .thenThrow(new EntityNotFoundException(
                        "Ordem de produção não encontrada."));

        mockMvc.perform(get("/api/ordens-producao/99/execucao"))
                .andExpect(status().isNotFound());

        verify(service).buscarExecucao(99L);
    }
}
