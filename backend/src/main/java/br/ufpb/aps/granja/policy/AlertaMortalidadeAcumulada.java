package br.ufpb.aps.granja.policy;

/** Politica 2: classifica pelo percentual acumulado desde a entrada do lote. */
public class AlertaMortalidadeAcumulada implements PoliticaAlertaMortalidade {

    private final LimitesDeAlerta limites;

    public AlertaMortalidadeAcumulada(double limiteAtencao, double limiteCritico) {
        this.limites = new LimitesDeAlerta(limiteAtencao, limiteCritico);
    }

    @Override
    public NivelAlerta avaliar(ResumoMortalidade resumo) {
        return limites.classificar(resumo.percentualAcumulado());
    }
}
