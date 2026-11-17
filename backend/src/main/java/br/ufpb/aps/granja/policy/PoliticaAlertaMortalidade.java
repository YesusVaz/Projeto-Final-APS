package br.ufpb.aps.granja.policy;

/**
 * Strategy (RN24): como classificar a mortalidade do lote. O servico so depende desta interface;
 * trocar a politica nao obriga a mexer no servico.
 */
public interface PoliticaAlertaMortalidade {
    NivelAlerta avaliar(ResumoMortalidade resumo);
}
