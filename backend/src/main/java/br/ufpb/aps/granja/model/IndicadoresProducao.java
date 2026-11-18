package br.ufpb.aps.granja.model;

/** Contas da producao. Ficam no dominio pra serem testadas sem banco. Resultado com 2 casas. */
public final class IndicadoresProducao {

    private IndicadoresProducao() {
    }

    /** RN11: taxa de postura = ovos / aves vivas x 100. */
    public static double taxaDePostura(int ovos, int vivos) {
        if (vivos <= 0) {
            throw new IllegalArgumentException("O lote não tem aves vivas para calcular a postura.");
        }
        if (ovos < 0) {
            throw new IllegalArgumentException("A quantidade de ovos não pode ser negativa.");
        }
        if (ovos > vivos) {
            // cada ave bota no maximo um ovo por dia. mais que isso e erro de contagem
            throw new IllegalArgumentException("Foram informados mais ovos (" + ovos + ") do que aves vivas (" + vivos + ").");
        }
        return arredondar(ovos * 100.0 / vivos);
    }

    /** RN12: GMD = (peso atual - peso anterior) / dias entre as pesagens. Pode dar negativo se o lote perdeu peso. */
    public static double ganhoMedioDiario(int pesoAtual, int pesoAnterior, long dias) {
        if (dias <= 0) {
            throw new IllegalArgumentException("A pesagem precisa ser depois da anterior para calcular o GMD.");
        }
        return arredondar((pesoAtual - pesoAnterior) / (double) dias);
    }

    /** RN20: conversao alimentar = racao consumida / peso ganho, os dois em kg. Quanto menor, melhor. */
    public static double conversaoAlimentar(double racaoKg, double ganhoDePesoKg) {
        if (ganhoDePesoKg <= 0) {
            throw new IllegalArgumentException("O lote não ganhou peso para calcular a conversão alimentar.");
        }
        if (racaoKg < 0) {
            throw new IllegalArgumentException("A ração consumida não pode ser negativa.");
        }
        return arredondar(racaoKg / ganhoDePesoKg);
    }

    /** Quilos de racao gastos por duzia de ovos. */
    public static double racaoPorDuzia(double racaoKg, int ovos) {
        if (ovos <= 0) {
            throw new IllegalArgumentException("Não há ovos para calcular a ração por dúzia.");
        }
        return arredondar(racaoKg / (ovos / 12.0));
    }

    public static double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
