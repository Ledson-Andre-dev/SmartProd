
package br.com.smartprod.perda;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.smartprod.insumo.UnidadeMedida;
import br.com.smartprod.producao.OrdemProducao;
import br.com.smartprod.usuario.Perfil;
import br.com.smartprod.usuario.Usuario;

@SpringBootTest
@AutoConfigureMockMvc
class RegistroPerdaSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistroPerdaService service;

    @Test
    @WithMockUser(roles = "GERENTE")
    void gerentePodeConsultarPerdas() throws Exception {
        when(service.listarPorOrdem(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/ordens-producao/1/perdas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "FUNCIONARIO")
    void funcionarioPodeConsultarPerdas() throws Exception {
        when(service.listarPorOrdem(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/ordens-producao/1/perdas"))
                .andExpect(status().isOk());
    }

    @Test
    void usuarioNaoAutenticadoRecebe401() throws Exception {
        mockMvc.perform(get("/api/ordens-producao/1/perdas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "OUTRO")
    void usuarioSemPermissaoNaoPodeConsultarPerdas() throws Exception {
        mockMvc.perform(get("/api/ordens-producao/1/perdas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "3", roles = "GERENTE")
    void gerentePodeRegistrarPerda() throws Exception {
        when(service.registrar(
                eq(1L),
                eq("Modelagem"),
                eq(new BigDecimal("1.0000")),
                eq(UnidadeMedida.KG),
                eq("Massa danificada"),
                eq(3L)))
                .thenReturn(criarPerda());

        mockMvc.perform(post("/api/ordens-producao/1/perdas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonValido()))
                .andExpect(status().isCreated());

        verify(service).registrar(
                1L,
                "Modelagem",
                new BigDecimal("1.0000"),
                UnidadeMedida.KG,
                "Massa danificada",
                3L);
    }

    @Test
    @WithMockUser(username = "4", roles = "FUNCIONARIO")
    void funcionarioPodeRegistrarPerda() throws Exception {
        when(service.registrar(
                eq(1L),
                eq("Modelagem"),
                eq(new BigDecimal("1.0000")),
                eq(UnidadeMedida.KG),
                eq("Massa danificada"),
                eq(4L)))
                .thenReturn(criarPerda());

        mockMvc.perform(post("/api/ordens-producao/1/perdas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonValido()))
                .andExpect(status().isCreated());

        verify(service).registrar(
                1L,
                "Modelagem",
                new BigDecimal("1.0000"),
                UnidadeMedida.KG,
                "Massa danificada",
                4L);
    }

    @Test
    void usuarioNaoAutenticadoNaoPodeRegistrarPerda() throws Exception {
        mockMvc.perform(post("/api/ordens-producao/1/perdas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonValido()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "OUTRO")
    void usuarioSemPermissaoNaoPodeRegistrarPerda() throws Exception {
        mockMvc.perform(post("/api/ordens-producao/1/perdas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonValido()))
                .andExpect(status().isForbidden());
    }

    private String jsonValido() {
        return """
                {
                    "etapa": "Modelagem",
                    "quantidade": 1.0000,
                    "unidade": "KG",
                    "motivo": "Massa danificada"
                }
                """;
    }

    private RegistroPerda criarPerda() {
        Usuario usuario = new Usuario(
                "Funcionário Teste",
                "funcionario@teste.com",
                "hash-teste",
                Perfil.FUNCIONARIO);

        ReflectionTestUtils.setField(usuario, "id", 3L);

        OrdemProducao ordem = new OrdemProducao(
                50,
                Instant.parse("2026-11-13T12:00:00Z"),
                usuario,
                usuario);

        ReflectionTestUtils.setField(ordem, "id", 1L);
        ordem.iniciar();

        RegistroPerda perda = new RegistroPerda(
                ordem,
                "Modelagem",
                new BigDecimal("1.0000"),
                UnidadeMedida.KG,
                "Massa danificada",
                usuario);

        ReflectionTestUtils.setField(perda, "id", 20L);

        return perda;
    }
}
