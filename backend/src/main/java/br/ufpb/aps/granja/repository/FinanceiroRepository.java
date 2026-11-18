package br.ufpb.aps.granja.repository;

import br.ufpb.aps.granja.model.LancamentoFinanceiro;

import java.time.LocalDate;
import java.util.List;

/** Contrato de persistencia das despesas e receitas (UC19 a UC22). */
public interface FinanceiroRepository {

    LancamentoFinanceiro inserir(LancamentoFinanceiro lancamento);

    /** Lancamentos com data entre {@code de} e {@code ate}, os dois dias incluidos. Do mais novo pro mais antigo. */
    List<LancamentoFinanceiro> listarPorPeriodo(LocalDate de, LocalDate ate);
}
