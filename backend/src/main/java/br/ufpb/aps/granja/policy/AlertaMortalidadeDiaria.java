package br.ufpb.aps.granja.policy;

/** Politica 1: classifica pelo percentual de mortes do dia. */
public class AlertaMortalidadeDiaria implements PoliticaAlertaMortalidade {

    private final LimitesDeAlerta limites;

    public AlertaMortalidadeDiaria(double limiteAtencao, double limiteCritico) {
        this.limites = new LimitesDeAlerta(limiteAtencao, limiteCritico);
    }

    @Override
    public NivelAlerta avaliar(ResumoMortalidade resumo) {
        return limites.classificar(resumo.percentualNoDia());
    }
}
