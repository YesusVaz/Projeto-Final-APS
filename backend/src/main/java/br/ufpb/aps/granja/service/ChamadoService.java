package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.evento.BarramentoEventos;
import br.ufpb.aps.granja.evento.OperacaoRealizada;
import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.CategoriaChamado;
import br.ufpb.aps.granja.model.Chamado;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.StatusChamado;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.model.excecao.NaoEncontradoException;
import br.ufpb.aps.granja.repository.ChamadoRepository;
import br.ufpb.aps.granja.repository.LancamentoRepository;
import br.ufpb.aps.granja.repository.UnidadeDeTrabalho;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UC30: qualquer usuario abre um chamado e acompanha os seus. O Gestor ve todos e responde.
 * Abrir usa o codigo de lancamento (RN22): o duplo clique nao abre dois chamados.
 */
public final class ChamadoService {

    private static final int ASSUNTO_MIN = 3;
    private static final int ASSUNTO_MAX = 80;
    private static final int DESCRICAO_MIN = 10;
    private static final int DESCRICAO_MAX = 1000;
    private static final int RESPOSTA_MAX = 1000;

    private final UnidadeDeTrabalho uow;
    private final ChamadoRepository chamados;
    private final LancamentoRepository lancamentos;
    private final BarramentoEventos eventos;

    public ChamadoService(UnidadeDeTrabalho uow, ChamadoRepository chamados, LancamentoRepository lancamentos,
                          BarramentoEventos eventos) {
        this.uow = uow;
        this.chamados = chamados;
        this.lancamentos = lancamentos;
        this.eventos = eventos;
    }

    public Chamado abrir(Usuario usuario, CategoriaChamado categoria, String assunto, String descricao,
                         CodigoLancamento codigo, LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.ABRIR_CHAMADO);
        String assuntoLimpo = texto(assunto, ASSUNTO_MIN, ASSUNTO_MAX, "O assunto");
        String descricaoLimpa = texto(descricao, DESCRICAO_MIN, DESCRICAO_MAX, "A descrição");
        Chamado gravado = uow.executar(() -> {
            lancamentos.registrar(codigo, agora);
            return chamados.inserir(new Chamado(null, usuario.id(), usuario.nome(), categoria, assuntoLimpo,
                    descricaoLimpa, StatusChamado.ABERTO, agora, null, agora, codigo));
        });
        eventos.publicar(new OperacaoRealizada("SUPORTE",
                "Chamado " + gravado.id() + " aberto por " + usuario.login() + ": " + assuntoLimpo, agora));
        return gravado;
    }

    /** Os chamados da propria pessoa. */
    public List<Chamado> meus(Usuario usuario) {
        Autorizacao.exigir(usuario, Acao.ABRIR_CHAMADO);
        return chamados.listarDoSolicitante(usuario.id());
    }

    public List<Chamado> todos(Usuario usuario) {
        Autorizacao.exigir(usuario, Acao.GERENCIAR_CHAMADOS);
        return chamados.listarTodos();
    }

    public Chamado responder(Usuario usuario, long id, StatusChamado status, String resposta, LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.GERENCIAR_CHAMADOS);
        if (resposta != null && resposta.trim().length() > RESPOSTA_MAX) {
            throw new IllegalArgumentException("A resposta pode ter até " + RESPOSTA_MAX + " caracteres.");
        }
        Chamado atualizado = uow.executar(() -> {
            Chamado atual = chamados.buscarPorId(id)
                    .orElseThrow(() -> new NaoEncontradoException("Chamado " + id + " não encontrado."));
            Chamado novo = atual.responder(status, resposta, agora);
            chamados.atualizar(novo);
            return novo;
        });
        eventos.publicar(new OperacaoRealizada("SUPORTE",
                "Chamado " + id + " agora está " + status.name().toLowerCase().replace('_', ' ') + " (por "
                        + usuario.login() + ")", agora));
        return atualizado;
    }

    private static String texto(String valor, int minimo, int maximo, String campo) {
        String limpo = valor == null ? "" : valor.trim();
        if (limpo.length() < minimo || limpo.length() > maximo) {
            throw new IllegalArgumentException(campo + " deve ter de " + minimo + " a " + maximo + " caracteres.");
        }
        return limpo;
    }
}
