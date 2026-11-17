package br.ufpb.aps.granja.policy;

/**
 * Factory Method (GoF). O criador tem a operacao {@link #configurar} e deixa
 * as subclasses escolhem a politica concreta (Strategy) em {@link #criarPolitica}.
 * Quem configura recebe sempre a interface {@link PoliticaAlertaMortalidade}. O servico nao conhece
 * a AlertaMortalidadeDiaria nem a AlertaMortalidadeAcumulada.
 */
public abstract class CriadorPoliticaAlerta {

    /** Metodo de fabrica: cada subclasse decide qual politica concreta criar. */
    protected abstract PoliticaAlertaMortalidade criarPolitica(double limiteAtencao, double limiteCritico);

    /** Monta a politica configurada pelo Gestor (RN18, RN24). */
    public final PoliticaAlertaMortalidade configurar(double limiteAtencao, double limiteCritico) {
        return criarPolitica(limiteAtencao, limiteCritico);
    }
}
