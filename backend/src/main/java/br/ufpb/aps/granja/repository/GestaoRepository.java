package br.ufpb.aps.granja.repository;

import br.ufpb.aps.granja.model.PontoDia;

import java.time.LocalDate;
import java.util.List;

/** Consultas de leitura que somam por dia, de todos os lotes, pro painel e os graficos (UC23 e UC26). */
public interface GestaoRepository {

    /** Aves mortas por dia, so os dias que tiveram morte. */
    List<PontoDia> mortesPorDia(LocalDate de, LocalDate ate);

    /** Quilos de racao servidos por dia, so os dias que tiveram registro. */
    List<PontoDia> racaoPorDia(LocalDate de, LocalDate ate);

    /** Ovos coletados por dia, so os dias que tiveram registro. */
    List<PontoDia> ovosPorDia(LocalDate de, LocalDate ate);
}
