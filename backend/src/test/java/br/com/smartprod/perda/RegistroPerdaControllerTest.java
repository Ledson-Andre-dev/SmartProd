
package br.com.smartprod.perda;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.com.smartprod.insumo.UnidadeMedida;
import br.com.smartprod.producao.OrdemProducao;
import br.com.smartprod.usuario.Perfil;
import br.com.smartprod.usuario.Usuario;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class RegistroPerdaControllerTest {

    @Mock
    private RegistroPerdaService service;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(new RegistroPerdaController(service))
                .setControllerAdvice(new RegistroPerdaExceptionHandler())
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void deveRegistrarPerdaComPost() throws Exception {
        RegistroPerda perda = criarPerda();

        when(service.registrar(
                10L,
                "Modelagem",
                new BigDecimal("2.5000"),
                UnidadeMedida.KG,
                "Massa danificada",
                3L))
                .thenReturn(perda);

        mockMvc.perform(post("/api/ordens-producao/10/perdas")
                .principal(usuarioAutenticado())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "etapa": "Modelagem",
                            "quantidade": 2.5000,
                            "unidade": "KG",
                            "motivo": "Massa danificada"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/ordens-producao/10/perdas/20"))
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.ordemProducaoId").value(10))
                .andExpect(jsonPath("$.etapa").value("Modelagem"))
                .andExpect(jsonPath("$.quantidade").value(2.5))
                .andExpect(jsonPath("$.unidade").value("KG"))
                .andExpect(jsonPath("$.motivo").value("Massa danificada"))
                .andExpect(jsonPath("$.registradoPorId").value(3))
                .andExpect(jsonPath("$.registradoPorNome")
                        .value("Funcionário Teste"))
                .andExpect(jsonPath("$.registradoEm").exists());

        verify(service).registrar(
                10L,
                "Modelagem",
                new BigDecimal("2.5000"),
                UnidadeMedida.KG,
                "Massa danificada",
                3L);
    }

    @Test
    void deveListarPerdasPorOrdem() throws Exception {
        RegistroPerda perda = criarPerda();

        when(service.listarPorOrdem(10L))
                .thenReturn(List.of(perda));

        mockMvc.perform(get("/api/ordens-producao/10/perdas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(20))
                .andExpect(jsonPath("$[0].etapa").value("Modelagem"))
                .andExpect(jsonPath("$[0].unidade").value("KG"))
                .andExpect(jsonPath("$[0].registradoPorId").value(3));

        verify(service).listarPorOrdem(10L);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverPerdas() throws Exception {
        when(service.listarPorOrdem(10L))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/ordens-producao/10/perdas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void naoDeveRegistrarPerdaComQuantidadeZero() throws Exception {
        mockMvc.perform(post("/api/ordens-producao/10/perdas")
                .principal(usuarioAutenticado())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "etapa": "Modelagem",
                            "quantidade": 0,
                            "unidade": "KG",
                            "motivo": "Teste"
                        }
                        """))
                .andExpect(status().isBadRequest());

        verify(service, never()).registrar(
                any(), any(), any(), any(), any(), any());
    }

    @Test
    void naoDeveRegistrarPerdaSemEtapa() throws Exception {
        mockMvc.perform(post("/api/ordens-producao/10/perdas")
                .principal(usuarioAutenticado())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "etapa": "",
                            "quantidade": 1.0000,
                            "unidade": "KG",
                            "motivo": "Teste"
                        }
                        """))
                .andExpect(status().isBadRequest());

        verify(service, never()).registrar(
                any(), any(), any(), any(), any(), any());
    }

    @Test
    void deveRetornar404QuandoOrdemNaoExistir() throws Exception {
        when(service.listarPorOrdem(99L))
                .thenThrow(new EntityNotFoundException(
                        "Ordem de produção não encontrada."));

        mockMvc.perform(get("/api/ordens-producao/99/perdas"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Ordem de produção não encontrada."));
    }

    @Test
    void deveRetornar409QuandoOrdemNaoEstiverEmAndamento()
            throws Exception {

        when(service.registrar(
                10L,
                "Modelagem",
                BigDecimal.ONE,
                UnidadeMedida.KG,
                "Massa danificada",
                3L))
                .thenThrow(new IllegalStateException(
                        "Perdas só podem ser registradas em ordens em andamento."));

        mockMvc.perform(post("/api/ordens-producao/10/perdas")
                .principal(usuarioAutenticado())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "etapa": "Modelagem",
                            "quantidade": 1,
                            "unidade": "KG",
                            "motivo": "Massa danificada"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("Perdas só podem ser registradas em ordens em andamento."));
    }

    private UsernamePasswordAuthenticationToken usuarioAutenticado() {
        return new UsernamePasswordAuthenticationToken(
                "3", null, List.of());
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

        ReflectionTestUtils.setField(ordem, "id", 10L);

        ordem.iniciar();

        RegistroPerda perda = new RegistroPerda(
                ordem,
                "Modelagem",
                new BigDecimal("2.5000"),
                UnidadeMedida.KG,
                "Massa danificada",
                usuario);

        ReflectionTestUtils.setField(perda, "id", 20L);

        return perda;
    }
}
