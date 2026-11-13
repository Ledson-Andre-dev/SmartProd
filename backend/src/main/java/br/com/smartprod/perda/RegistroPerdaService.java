
package br.com.smartprod.perda;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.smartprod.insumo.UnidadeMedida;
import br.com.smartprod.producao.OrdemProducao;
import br.com.smartprod.producao.OrdemProducaoService;
import br.com.smartprod.producao.StatusOrdemProducao;
import br.com.smartprod.usuario.Usuario;
import br.com.smartprod.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class RegistroPerdaService {

    private final RegistroPerdaRepository repository;
    private final OrdemProducaoService ordemProducaoService;
    private final UsuarioRepository usuarioRepository;

    public RegistroPerdaService(
            RegistroPerdaRepository repository,
            OrdemProducaoService ordemProducaoService,
            UsuarioRepository usuarioRepository) {

        this.repository = repository;
        this.ordemProducaoService = ordemProducaoService;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public RegistroPerda registrar(
            Long ordemId,
            String etapa,
            BigDecimal quantidade,
            UnidadeMedida unidade,
            String motivo,
            Long usuarioId) {

        OrdemProducao ordem = ordemProducaoService.buscarPorId(ordemId);

        if (ordem.getStatus() != StatusOrdemProducao.EM_ANDAMENTO) {
            throw new IllegalStateException(
                    "Perdas só podem ser registradas em ordens em andamento.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuário não encontrado ou inativo."));

        RegistroPerda perda = new RegistroPerda(
                ordem,
                etapa,
                quantidade,
                unidade,
                motivo,
                usuario);

        return repository.save(perda);
    }

    @Transactional(readOnly = true)
    public List<RegistroPerda> listarPorOrdem(Long ordemId) {

        ordemProducaoService.buscarPorId(ordemId);

        return repository
                .findByOrdemProducaoIdOrderByRegistradoEmDesc(ordemId);
    }
}
