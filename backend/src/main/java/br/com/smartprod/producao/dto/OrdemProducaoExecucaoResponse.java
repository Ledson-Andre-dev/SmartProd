
package br.com.smartprod.producao.dto;

import java.util.List;

import br.com.smartprod.producao.StatusOrdemProducao;

public record OrdemProducaoExecucaoResponse(
        Long ordemProducaoId,
        Long fichaTecnicaId,
        String nomeProduto,
        Integer quantidadeUnidades,
        StatusOrdemProducao status,
        List<ExecucaoIngredienteResponse> ingredientes,
        List<ExecucaoEtapaResponse> etapas) {
}
