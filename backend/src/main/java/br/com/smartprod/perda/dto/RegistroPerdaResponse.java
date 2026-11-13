
package br.com.smartprod.perda.dto;

import java.math.BigDecimal;
import java.time.Instant;

import br.com.smartprod.insumo.UnidadeMedida;
import br.com.smartprod.perda.RegistroPerda;

public record RegistroPerdaResponse(
        Long id,
        Long ordemProducaoId,
        String etapa,
        BigDecimal quantidade,
        UnidadeMedida unidade,
        String motivo,
        Long registradoPorId,
        String registradoPorNome,
        Instant registradoEm) {

    public static RegistroPerdaResponse de(RegistroPerda perda) {
        return new RegistroPerdaResponse(
                perda.getId(),
                perda.getOrdemProducao().getId(),
                perda.getEtapa(),
                perda.getQuantidade(),
                perda.getUnidade(),
                perda.getMotivo(),
                perda.getRegistradoPor().getId(),
                perda.getRegistradoPor().getNome(),
                perda.getRegistradoEm());
    }
}
