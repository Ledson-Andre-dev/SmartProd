package br.com.smartprod.producao.dto;

import java.time.Instant;

import br.com.smartprod.producao.OrdemProducao;
import br.com.smartprod.producao.StatusOrdemProducao;

public record OrdemProducaoResponse(
        Long id,
        Integer quantidadeUnidades,
        Instant horarioEntrega,
        StatusOrdemProducao status,
        ResponsavelResponse responsavel) {

    public static OrdemProducaoResponse de(OrdemProducao ordem) {
        return new OrdemProducaoResponse(
                ordem.getId(),
                ordem.getQuantidadeUnidades(),
                ordem.getHorarioEntrega(),
                ordem.getStatus(),
                new ResponsavelResponse(
                        ordem.getResponsavel().getId(),
                        ordem.getResponsavel().getNome()
                )
        );
    }

    public record ResponsavelResponse(
            Long id,
            String nome) {
    }
}