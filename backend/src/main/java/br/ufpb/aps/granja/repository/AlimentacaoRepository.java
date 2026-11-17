package br.ufpb.aps.granja.repository;

import br.ufpb.aps.granja.model.RegistroAlimentacao;

import java.util.List;

/** Contrato de persistencia da alimentacao (UC13). */
public interface AlimentacaoRepository {

    RegistroAlimentacao inserir(RegistroAlimentacao registro);

    /** Alimentacoes do lote em ordem. Entra no historico completo (UC17). */
    List<RegistroAlimentacao> listarPorLote(long loteId);
}
