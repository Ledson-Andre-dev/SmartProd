
package br.com.smartprod.producao;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.com.smartprod.producao.dto.OrdemProducaoExecucaoResponse;

@Service
public class OrdemProducaoExecucaoService {

    private final OrdemProducaoService ordemProducaoService;

    public OrdemProducaoExecucaoService(
            OrdemProducaoService ordemProducaoService) {
        this.ordemProducaoService = ordemProducaoService;
    }

    public OrdemProducaoExecucaoResponse buscarExecucao(Long ordemId) {

        // Confirma que a ordem existe.
        ordemProducaoService.buscarPorId(ordemId);

        // A integracao com fichas tecnicas ainda nao esta disponivel.
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Dados de execucao indisponiveis ate a integracao "
                        + "com as fichas tecnicas.");
    }
}
