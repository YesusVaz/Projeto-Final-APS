package br.ufpb.aps.granja.repository;

import br.ufpb.aps.granja.policy.ConfiguracaoAlerta;

import java.util.Optional;

/** Contrato da configuracao de alerta. So existe uma. */
public interface ConfiguracaoAlertaRepository {

    Optional<ConfiguracaoAlerta> buscar();

    void salvar(ConfiguracaoAlerta configuracao);
}
