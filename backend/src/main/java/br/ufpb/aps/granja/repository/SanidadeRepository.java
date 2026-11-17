package br.ufpb.aps.granja.repository;

import br.ufpb.aps.granja.model.OcorrenciaSanitaria;
import br.ufpb.aps.granja.model.Tratamento;
import br.ufpb.aps.granja.model.Vacinacao;

import java.util.List;
import java.util.Optional;

/** Contrato de persistencia da sanidade: vacinas (UC11), ocorrencias e tratamentos (UC12). */
public interface SanidadeRepository {

    Vacinacao inserirVacinacao(Vacinacao vacinacao);

    List<Vacinacao> vacinacoesDoLote(long loteId);

    /** Todas, pra o calculo dos alertas sanitarios (UC24). */
    List<Vacinacao> listarVacinacoes();

    OcorrenciaSanitaria inserirOcorrencia(OcorrenciaSanitaria ocorrencia);

    Optional<OcorrenciaSanitaria> buscarOcorrencia(long id);

    /** Grava so status e data de encerramento. O resto nao muda. */
    void atualizarOcorrencia(OcorrenciaSanitaria ocorrencia);

    List<OcorrenciaSanitaria> ocorrenciasDoLote(long loteId);

    List<OcorrenciaSanitaria> listarOcorrencias();

    Tratamento inserirTratamento(Tratamento tratamento);

    List<Tratamento> tratamentosDaOcorrencia(long ocorrenciaId);
}
