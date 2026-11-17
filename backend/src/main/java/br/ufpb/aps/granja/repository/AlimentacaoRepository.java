package br.ufpb.aps.granja.repository;

import br.ufpb.aps.granja.model.RegistroAlimentacao;

/** Contrato de persistencia da alimentacao (UC13). */
public interface AlimentacaoRepository {

    RegistroAlimentacao inserir(RegistroAlimentacao registro);
}
