
package br.com.smartprod.producao.dto;

public record ExecucaoEtapaResponse(
        Integer ordem,
        String titulo,
        String descricao,
        Integer duracaoSegundos) {
}
