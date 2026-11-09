
package br.com.smartprod.producao;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.smartprod.usuario.Usuario;
import br.com.smartprod.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class OrdemProducaoService {

    private final OrdemProducaoRepository repository;
    private final UsuarioRepository usuarioRepository;

    public OrdemProducaoService(
            OrdemProducaoRepository repository,
            UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<OrdemProducao> listarTodas() {
        return repository.findAllByOrderByHorarioEntregaAsc();
    }

    @Transactional(readOnly = true)
    public List<OrdemProducao> listarPorStatus(StatusOrdemProducao status) {
        return repository.findByStatusOrderByHorarioEntregaAsc(status);
    }

    @Transactional(readOnly = true)
    public OrdemProducao buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Ordem de produção não encontrada."));
    }

    @Transactional(readOnly = true)
    public Usuario buscarUsuarioAtivo(Long id) {
        return usuarioRepository.findById(id)
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuário não encontrado ou inativo."));
    }

    @Transactional
    public OrdemProducao iniciar(Long id) {
        OrdemProducao ordem = buscarPorId(id);
        ordem.iniciar();
        return ordem;
    }

    @Transactional
    public OrdemProducao concluir(Long id, Integer quantidadeProduzida) {
        OrdemProducao ordem = buscarPorId(id);
        ordem.concluir(quantidadeProduzida);
        return ordem;
    }
}
