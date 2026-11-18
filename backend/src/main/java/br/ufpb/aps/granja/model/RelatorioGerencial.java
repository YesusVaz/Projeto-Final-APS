package br.ufpb.aps.granja.model;

import java.time.LocalDate;
import java.util.List;

/**
 * UC25: relatorio do periodo. O financeiro e o do periodo. Os numeros de producao de cada lote valem desde o
 * alojamento, porque conversao alimentar e mortalidade so fazem sentido olhando o lote inteiro.
 */
public record RelatorioGerencial(LocalDate de, LocalDate ate, long receitasCentavos, long despesasCentavos,
                                 List<LinhaLote> lotes, List<InsumoAbaixoDoMinimo> insumosAbaixoDoMinimo) {

    public long resultadoCentavos() {
        return receitasCentavos - despesasCentavos;
    }

    public record LinhaLote(ProdutividadeLote produtividade, StatusLote status, long receitasCentavos,
                            long despesasCentavos) {
        public long resultadoCentavos() {
            return receitasCentavos - despesasCentavos;
        }
    }

    public record InsumoAbaixoDoMinimo(String nome, String unidade, int saldo, int nivelMinimo) {
    }
}
