package br.ufpb.aps.granja.policy;

/** Criador concreto: faz a politica que classifica pelo percentual do dia. */
public class CriadorAlertaDiario extends CriadorPoliticaAlerta {

    @Override
    protected PoliticaAlertaMortalidade criarPolitica(double limiteAtencao, double limiteCritico) {
        return new AlertaMortalidadeDiaria(limiteAtencao, limiteCritico);
    }
}
