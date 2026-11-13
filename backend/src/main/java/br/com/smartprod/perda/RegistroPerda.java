
package br.com.smartprod.perda;

import java.math.BigDecimal;
import java.time.Instant;

import br.com.smartprod.insumo.UnidadeMedida;
import br.com.smartprod.producao.OrdemProducao;
import br.com.smartprod.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "registros_perda")
public class RegistroPerda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_producao_id", nullable = false)
    private OrdemProducao ordemProducao;

    @Column(nullable = false, length = 120)
    private String etapa;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal quantidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UnidadeMedida unidade;

    @Column(nullable = false, length = 500)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Usuario registradoPor;

    @Column(name = "registrado_em", nullable = false, updatable = false)
    private Instant registradoEm;

    protected RegistroPerda() {
    }

    public RegistroPerda(
            OrdemProducao ordemProducao,
            String etapa,
            BigDecimal quantidade,
            UnidadeMedida unidade,
            String motivo,
            Usuario registradoPor) {

        if (ordemProducao == null) {
            throw new IllegalArgumentException(
                    "A ordem de produção é obrigatória.");
        }

        if (etapa == null || etapa.isBlank()) {
            throw new IllegalArgumentException(
                    "A etapa é obrigatória.");
        }

        if (quantidade == null || quantidade.signum() <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade perdida deve ser maior que zero.");
        }

        if (unidade == null) {
            throw new IllegalArgumentException(
                    "A unidade de medida é obrigatória.");
        }

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException(
                    "O motivo é obrigatório.");
        }

        if (registradoPor == null) {
            throw new IllegalArgumentException(
                    "O usuário que registrou a perda é obrigatório.");
        }

        this.ordemProducao = ordemProducao;
        this.etapa = etapa.trim();
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.motivo = motivo.trim();
        this.registradoPor = registradoPor;
        this.registradoEm = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public OrdemProducao getOrdemProducao() {
        return ordemProducao;
    }

    public String getEtapa() {
        return etapa;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public UnidadeMedida getUnidade() {
        return unidade;
    }

    public String getMotivo() {
        return motivo;
    }

    public Usuario getRegistradoPor() {
        return registradoPor;
    }

    public Instant getRegistradoEm() {
        return registradoEm;
    }
}
