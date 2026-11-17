package br.ufpb.aps.granja.repository;

import br.ufpb.aps.granja.model.ParametrosSistema;

import java.util.Optional;

/** Contrato dos parametros do sistema (UC27). So existe uma linha. */
public interface ParametrosRepository {

    Optional<ParametrosSistema> buscar();

    void salvar(ParametrosSistema parametros);
}
