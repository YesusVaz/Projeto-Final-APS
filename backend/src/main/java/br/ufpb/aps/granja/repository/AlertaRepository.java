package br.ufpb.aps.granja.repository;

import br.ufpb.aps.granja.policy.AlertaMortalidade;

import java.util.List;

/** Contrato de persistencia dos alertas gerados pelo Observer. */
public interface AlertaRepository {

    void registrar(AlertaMortalidade alerta);

    List<AlertaMortalidade> listar();
}
