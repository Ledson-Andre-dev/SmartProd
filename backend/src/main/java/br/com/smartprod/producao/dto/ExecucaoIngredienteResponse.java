
package br.com.smartprod.producao.dto;

import java.math.BigDecimal;

import br.com.smartprod.insumo.UnidadeMedida;

public record ExecucaoIngredienteResponse(
        Long insumoId,
        String nome,
        BigDecimal quantidade,
        UnidadeMedida unidade) {
}
