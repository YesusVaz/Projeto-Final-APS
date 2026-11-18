package br.ufpb.aps.granja.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static br.ufpb.aps.granja.model.Acao.*;

/**
 * Perfis herdados do AveTrack. As permissoes seguem os atores de cada caso de uso (Gestor pode tudo).
 * A checagem roda no servico, nao so na tela.
 */
public enum Perfil {
    GESTOR(EnumSet.allOf(Acao.class)),
    TECNICO(EnumSet.of(CONSULTAR, CADASTRAR_LOTE, ENCERRAR_LOTE, REGISTRAR_BAIXA, REGISTRAR_ENTRADA_ANIMAIS, REGISTRAR_VACINACAO,
            REGISTRAR_OCORRENCIA, CONSULTAR_ALERTAS_SANITARIOS, REGISTRAR_PRODUCAO, REGISTRAR_ILUMINACAO, ABRIR_CHAMADO)),
    FUNCIONARIO(EnumSet.of(CONSULTAR, REGISTRAR_MORTALIDADE, MOVIMENTAR_ESTOQUE, REGISTRAR_ALIMENTACAO,
            REGISTRAR_PRODUCAO, REGISTRAR_HIGIENIZACAO, ABRIR_CHAMADO));

    private final Set<Acao> permitidas;

    Perfil(Set<Acao> permitidas) {
        this.permitidas = Collections.unmodifiableSet(permitidas);
    }

    public boolean permite(Acao acao) {
        return permitidas.contains(acao);
    }
}
