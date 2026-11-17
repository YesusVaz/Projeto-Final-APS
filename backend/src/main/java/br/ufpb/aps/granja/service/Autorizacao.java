package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.model.excecao.AcessoNegadoException;

/** RN25: a permissao e checada no servico antes de qualquer efeito. Nao depende da tela. */
final class Autorizacao {

    private Autorizacao() {
    }

    static void exigir(Usuario usuario, Acao acao) {
        if (!usuario.podeExecutar(acao)) {
            throw new AcessoNegadoException(usuario.perfil().name(), acao.name());
        }
    }
}
