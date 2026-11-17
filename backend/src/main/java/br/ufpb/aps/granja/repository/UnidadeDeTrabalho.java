package br.ufpb.aps.granja.repository;

import java.util.function.Supplier;

/**
 * RN23, tudo ou nada: o trabalho roda numa transacao. Se der erro, nada do que gravou
 * fica no banco.
 */
public interface UnidadeDeTrabalho {
    <T> T executar(Supplier<T> trabalho);
}
