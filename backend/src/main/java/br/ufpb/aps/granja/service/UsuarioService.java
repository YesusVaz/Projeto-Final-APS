package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.evento.BarramentoEventos;
import br.ufpb.aps.granja.evento.OperacaoRealizada;
import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.model.excecao.NaoEncontradoException;
import br.ufpb.aps.granja.model.excecao.RegraDeNegocioException;
import br.ufpb.aps.granja.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UC01: gestao dos usuarios pelo gestor. Lista e ativa ou inativa (RN02).
 * Nao existe operacao que troque o login: ele fica igual pra sempre (RN01).
 */
public final class UsuarioService {

    private final UsuarioRepository usuarios;
    private final BarramentoEventos eventos;

    public UsuarioService(UsuarioRepository usuarios, BarramentoEventos eventos) {
        this.usuarios = usuarios;
        this.eventos = eventos;
    }

    public List<Usuario> listar(Usuario autor) {
        Autorizacao.exigir(autor, Acao.GERENCIAR_USUARIOS);
        return usuarios.listar();
    }

    /**
     * Ativa ou inativa. Inativado perde a sessao na proxima chamada, porque o token e conferido
     * contra o banco toda vez. O gestor nao pode se inativar, senao ninguem mais administra.
     */
    public Usuario alterarStatus(Usuario autor, long usuarioId, boolean ativo, LocalDateTime agora) {
        Autorizacao.exigir(autor, Acao.GERENCIAR_USUARIOS);
        Usuario alvo = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new NaoEncontradoException("Usuário " + usuarioId + " não encontrado."));
        if (!ativo && alvo.id().equals(autor.id())) {
            throw new RegraDeNegocioException("O gestor não pode inativar a própria conta.");
        }
        if (ativo) {
            alvo.ativar();
        } else {
            alvo.inativar();
        }
        usuarios.atualizar(alvo);
        eventos.publicar(new OperacaoRealizada("USUARIO", "Usuário " + alvo.login() + (ativo ? " ativado" : " inativado")
                + " por " + autor.login(), agora));
        return alvo;
    }
}
