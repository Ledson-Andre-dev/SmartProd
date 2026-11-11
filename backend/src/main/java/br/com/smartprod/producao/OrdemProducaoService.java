
package br.com.smartprod.producao;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.smartprod.usuario.Perfil;
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
    public Usuario buscarResponsavelAtivo(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuário responsável não encontrado ou inativo."));

        if (usuario.getPerfil() != Perfil.FUNCIONARIO) {
            throw new IllegalArgumentException(
                    "O responsável pela ordem deve ser um funcionário.");
        }

        return usuario;
    }

    @Transactional(readOnly = true)
    public Usuario buscarGerenteAtivo(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Gerente criador não encontrado ou inativo."));

        if (usuario.getPerfil() != Perfil.GERENTE) {
            throw new IllegalArgumentException(
                    "Somente um gerente pode criar uma ordem de produção.");
        }

        return usuario;
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
