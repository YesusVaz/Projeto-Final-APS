package br.ufpb.aps.granja.policy;

/** Criterio e limites de alerta em vigor, em percentual. */
public record ConfiguracaoAlerta(CriterioAlerta criterio, double atencao, double critico) {
    public ConfiguracaoAlerta {
        if (criterio == null) {
            throw new IllegalArgumentException("Critério de alerta é obrigatório.");
        }
    }
}
