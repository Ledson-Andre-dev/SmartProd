package br.com.smartprod.producao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = OrdemProducaoController.class)
public class OrdemProducaoExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail tratarEstadoInvalido(IllegalStateException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }
}