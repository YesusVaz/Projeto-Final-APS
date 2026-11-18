package br.ufpb.aps.granja.model;

/** UC23: os numeros do dia a dia do gestor. "No mes" vai do dia 1 ate hoje. */
public record PainelGerencial(int lotesAtivos, int avesVivas, int mortesNoMes, int racaoNoMesKg, int ovosNoMes,
                              long receitasDoMesCentavos, long despesasDoMesCentavos, int insumosAbaixoDoMinimo) {

    public long resultadoDoMesCentavos() {
        return receitasDoMesCentavos - despesasDoMesCentavos;
    }
}
