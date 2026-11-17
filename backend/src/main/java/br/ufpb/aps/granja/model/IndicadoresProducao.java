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

    private static double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
