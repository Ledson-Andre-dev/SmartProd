
package br.com.smartprod.producao.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record OrdemProducaoConclusaoRequest(
        @NotNull @PositiveOrZero Integer quantidadeProduzida) {
}
