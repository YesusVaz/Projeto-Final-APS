package br.ufpb.aps.granja.repository;

import br.ufpb.aps.granja.model.CodigoLancamento;

import java.time.LocalDateTime;

/** Contrato dos codigos de lancamento, usados em toda operacao que mexe no saldo. */
public interface LancamentoRepository {

    /**
     * RN22: grava o codigo. Se ja existir, em qualquer operacao,
     * lanca LancamentoDuplicadoException e nada e aplicado.
     */
    void registrar(CodigoLancamento codigo, LocalDateTime dataHora);
}
