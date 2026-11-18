package br.ufpb.aps.granja.model;

import br.ufpb.aps.granja.model.excecao.RegraDeNegocioException;

import java.time.LocalDateTime;

/**
 * UC30: pedido de ajuda de qualquer usuario. O Gestor acompanha e responde. Imutavel: responder devolve um novo objeto.
 * O chamado so anda pra frente: ABERTO, EM_ATENDIMENTO, RESOLVIDO. Resolver exige uma resposta.
 */
public record Chamado(Long id, long solicitanteId, String solicitanteNome, CategoriaChamado categoria, String assunto,
                      String descricao, StatusChamado status, LocalDateTime abertoEm, String resposta,
                      LocalDateTime atualizadoEm, CodigoLancamento codigo) {

    public Chamado responder(StatusChamado novoStatus, String texto, LocalDateTime agora) {
        if (status == StatusChamado.RESOLVIDO) {
            throw new RegraDeNegocioException("Este chamado já foi resolvido e não pode mais ser alterado.");
        }
        if (novoStatus == StatusChamado.ABERTO) {
            throw new RegraDeNegocioException("Um chamado em andamento não volta para aberto.");
        }
        String resposta = texto == null || texto.isBlank() ? null : texto.trim();
        if (novoStatus == StatusChamado.RESOLVIDO && resposta == null) {
            throw new RegraDeNegocioException("Escreva a resposta antes de resolver o chamado.");
        }
        return new Chamado(id, solicitanteId, solicitanteNome, categoria, assunto, descricao, novoStatus, abertoEm,
                resposta != null ? resposta : this.resposta, agora, codigo);
    }

    public Chamado comId(long novoId) {
        return new Chamado(novoId, solicitanteId, solicitanteNome, categoria, assunto, descricao, status, abertoEm,
                resposta, atualizadoEm, codigo);
    }
}
