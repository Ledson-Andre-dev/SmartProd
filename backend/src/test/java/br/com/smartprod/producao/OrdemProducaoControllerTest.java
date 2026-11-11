
package br.com.smartprod.producao;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import br.com.smartprod.usuario.Perfil;
import br.com.smartprod.usuario.Usuario;

@ExtendWith(MockitoExtension.class)
class OrdemProducaoControllerTest {

    @Mock
    private OrdemProducaoService service;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(new OrdemProducaoController(service))
                .setControllerAdvice(new OrdemProducaoExceptionHandler())
                .setMessageConverters(
                        new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void deveListarTodasAsOrdens() throws Exception {
        OrdemProducao ordem = criarOrdem();

        when(service.listarPorFiltros(null, null))
                .thenReturn(List.of(ordem));

        mockMvc.perform(get("/api/ordens-producao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].status").value("PENDENTE"))
                .andExpect(jsonPath("$[0].responsavel.nome")
                        .value("Funcionário Teste"));

        verify(service).listarPorFiltros(null, null);
    }

    @Test
    void deveListarOrdensPorStatus() throws Exception {
        OrdemProducao ordem = criarOrdem();
        ordem.iniciar();

        when(service.listarPorFiltros(
                StatusOrdemProducao.EM_ANDAMENTO, null))
                .thenReturn(List.of(ordem));

        mockMvc.perform(get("/api/ordens-producao")
                .param("status", "EM_ANDAMENTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status")
                        .value("EM_ANDAMENTO"));

        verify(service).listarPorFiltros(
                StatusOrdemProducao.EM_ANDAMENTO, null);
    }

    @Test
    void deveListarOrdensPorData() throws Exception {
        LocalDate data = LocalDate.of(2026, 11, 10);

        when(service.listarPorFiltros(null, data))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/ordens-producao")
                .param("data", "2026-11-10"))
                .andExpect(status().isOk());

        verify(service).listarPorFiltros(null, data);
    }

    @Test
    void deveListarOrdensPorStatusEData() throws Exception {
        LocalDate data = LocalDate.of(2026, 11, 10);

        when(service.listarPorFiltros(
                StatusOrdemProducao.PENDENTE, data))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/ordens-producao")
                .param("status", "PENDENTE")
                .param("data", "2026-11-10"))
                .andExpect(status().isOk());

        verify(service).listarPorFiltros(
                StatusOrdemProducao.PENDENTE, data);
    }

    @Test
    void deveBuscarOrdemPorId() throws Exception {
        OrdemProducao ordem = criarOrdem();

        when(service.buscarPorId(10L))
                .thenReturn(ordem);

        mockMvc.perform(get("/api/ordens-producao/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.quantidadeUnidades").value(50))
                .andExpect(jsonPath("$.responsavel.id").value(3));

        verify(service).buscarPorId(10L);
    }

    @Test
    void deveIniciarOrdemComPatch() throws Exception {
        OrdemProducao ordem = criarOrdem();
        ordem.iniciar();

        when(service.iniciar(10L))
                .thenReturn(ordem);

        mockMvc.perform(patch("/api/ordens-producao/10/iniciar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("EM_ANDAMENTO"));

        verify(service).iniciar(10L);
    }

    @Test
    void deveConcluirOrdemComPatch() throws Exception {
        OrdemProducao ordem = criarOrdem();
        ordem.iniciar();
        ordem.concluir(45);

        when(service.concluir(10L, 45))
                .thenReturn(ordem);

        mockMvc.perform(patch("/api/ordens-producao/10/concluir")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "quantidadeProduzida": 45
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.quantidadeProduzida").value(45))
                .andExpect(jsonPath("$.concluidaEm").exists());

        verify(service).concluir(10L, 45);
    }

    private OrdemProducao criarOrdem() {
        Usuario usuario = new Usuario(
                "Funcionário Teste",
                "funcionario@teste.com",
                "hash-teste",
                Perfil.FUNCIONARIO);

        ReflectionTestUtils.setField(usuario, "id", 3L);

        OrdemProducao ordem = new OrdemProducao(
                50,
                Instant.parse("2026-11-10T12:00:00Z"),
                usuario,
                usuario);

        ReflectionTestUtils.setField(ordem, "id", 10L);

        return ordem;
    }
}
