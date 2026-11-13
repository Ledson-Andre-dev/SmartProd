
package br.com.smartprod.perda;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.smartprod.perda.dto.RegistroPerdaCriacaoRequest;
import br.com.smartprod.perda.dto.RegistroPerdaResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ordens-producao/{ordemId}/perdas")
public class RegistroPerdaController {

    private final RegistroPerdaService service;

    public RegistroPerdaController(RegistroPerdaService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GERENTE', 'FUNCIONARIO')")
    public ResponseEntity<RegistroPerdaResponse> registrar(
            @PathVariable Long ordemId,
            @Valid @RequestBody RegistroPerdaCriacaoRequest request,
            Authentication authentication) {

        Long usuarioId = Long.valueOf(authentication.getName());

        RegistroPerda perda = service.registrar(
                ordemId,
                request.etapa(),
                request.quantidade(),
                request.unidade(),
                request.motivo(),
                usuarioId);

        RegistroPerdaResponse resposta = RegistroPerdaResponse.de(perda);

        return ResponseEntity
                .created(URI.create(
                        "/api/ordens-producao/" + ordemId
                                + "/perdas/" + perda.getId()))
                .body(resposta);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GERENTE', 'FUNCIONARIO')")
    public ResponseEntity<List<RegistroPerdaResponse>> listar(
            @PathVariable Long ordemId) {

        List<RegistroPerdaResponse> resposta = service.listarPorOrdem(ordemId)
                .stream()
                .map(RegistroPerdaResponse::de)
                .toList();

        return ResponseEntity.ok(resposta);
    }
}
