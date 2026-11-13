
package br.com.smartprod.producao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice(assignableTypes = OrdemProducaoExecucaoController.class)
public class OrdemProducaoExecucaoExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail tratarNaoEncontrado(
            EntityNotFoundException exception) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage());
    }
}
