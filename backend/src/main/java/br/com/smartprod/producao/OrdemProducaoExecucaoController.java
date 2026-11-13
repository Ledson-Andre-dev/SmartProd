
package br.com.smartprod.producao;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.smartprod.producao.dto.OrdemProducaoExecucaoResponse;

@RestController
@RequestMapping("/api/ordens-producao")
public class OrdemProducaoExecucaoController {

    private final OrdemProducaoExecucaoService service;

    public OrdemProducaoExecucaoController(
            OrdemProducaoExecucaoService service) {
        this.service = service;
    }

    @GetMapping("/{id}/execucao")
    @PreAuthorize("hasAnyRole('GERENTE', 'FUNCIONARIO')")
    public ResponseEntity<OrdemProducaoExecucaoResponse> buscarExecucao(
            @PathVariable Long id) {

        return ResponseEntity.ok(service.buscarExecucao(id));
    }
}
