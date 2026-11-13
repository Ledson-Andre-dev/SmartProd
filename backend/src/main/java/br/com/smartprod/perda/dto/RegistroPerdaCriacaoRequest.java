
package br.com.smartprod.perda.dto;

import java.math.BigDecimal;

import br.com.smartprod.insumo.UnidadeMedida;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistroPerdaCriacaoRequest(

        @NotBlank @Size(max = 120) String etapa,

        @NotNull @DecimalMin("0.0001") @Digits(integer = 8, fraction = 4) BigDecimal quantidade,

        @NotNull UnidadeMedida unidade,

        @NotBlank @Size(max = 500) String motivo) {
}
