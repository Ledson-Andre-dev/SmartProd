package br.com.smartprod.producao;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

class OrdemProducaoExceptionHandlerTest {

    private final OrdemProducaoExceptionHandler handler =
            new OrdemProducaoExceptionHandler();

    @Test
    void deveRetornar409QuandoEstadoDaOrdemForInvalido() {
        IllegalStateException exception =
                new IllegalStateException("Somente ordens pendentes podem ser iniciadas.");

        ProblemDetail resposta = handler.tratarEstadoInvalido(exception);

        assertThat(resposta.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(resposta.getDetail())
                .isEqualTo("Somente ordens pendentes podem ser iniciadas.");
    }
}