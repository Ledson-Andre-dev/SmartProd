
package br.com.smartprod.producao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdemProducaoRepository
        extends JpaRepository<OrdemProducao, Long> {

    List<OrdemProducao> findByStatus(StatusOrdemProducao status);

    List<OrdemProducao> findByResponsavelId(Long responsavelId);

    List<OrdemProducao> findAllByOrderByHorarioEntregaAsc();

    List<OrdemProducao> findByStatusOrderByHorarioEntregaAsc(
            StatusOrdemProducao status);
}
