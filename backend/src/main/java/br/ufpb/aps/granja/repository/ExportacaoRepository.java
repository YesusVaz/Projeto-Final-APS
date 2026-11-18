package br.ufpb.aps.granja.repository;

import br.ufpb.aps.granja.model.ConjuntoExportavel;
import br.ufpb.aps.granja.model.TabelaExportada;

import java.time.LocalDate;

/** Consultas de leitura que montam as tabelas do UC29. */
public interface ExportacaoRepository {

    /** Os conjuntos que nao usam periodo ignoram {@code de} e {@code ate}. */
    TabelaExportada consultar(ConjuntoExportavel conjunto, LocalDate de, LocalDate ate);
}
