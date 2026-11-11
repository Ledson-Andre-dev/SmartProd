package br.com.smartprod.producao.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrdemProducaoCriacaoRequest(

        @NotNull @Positive Long fichaTecnicaId,

        @NotNull @Positive Integer quantidadeUnidades,

        @NotNull Instant horarioEntrega,

        @NotNull @Positive Long responsavelId

) {
}