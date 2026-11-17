package br.ufpb.aps.granja.policy;

/** Criador concreto: faz a politica que classifica pelo percentual acumulado. */
public class CriadorAlertaAcumulado extends CriadorPoliticaAlerta {

    @Override
    protected PoliticaAlertaMortalidade criarPolitica(double limiteAtencao, double limiteCritico) {
        return new AlertaMortalidadeAcumulada(limiteAtencao, limiteCritico);
    }
}
