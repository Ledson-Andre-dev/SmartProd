
package br.com.smartprod.producao;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@AutoConfigureMockMvc
class OrdemProducaoExecucaoSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrdemProducaoExecucaoService service;

    @Test
    @WithMockUser(roles = "GERENTE")
    void gerentePodeAcessarExecucao() throws Exception {
        when(service.buscarExecucao(1L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE));

        mockMvc.perform(get("/api/ordens-producao/1/execucao"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    void funcionarioPodeAcessarExecucao() throws Exception {
        when(service.buscarExecucao(1L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE));

        mockMvc.perform(get("/api/ordens-producao/1/execucao"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void usuarioNaoAutenticadoRecebe401() throws Exception {
        mockMvc.perform(get("/api/ordens-producao/1/execucao"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "OUTRO")
    void usuarioSemPermissaoRecebe403() throws Exception {
        mockMvc.perform(get("/api/ordens-producao/1/execucao"))
                .andExpect(status().isForbidden());
    }
}
