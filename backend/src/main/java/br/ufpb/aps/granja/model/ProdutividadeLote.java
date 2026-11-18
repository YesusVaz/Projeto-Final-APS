package br.ufpb.aps.granja.model;

/**
 * UC18: numeros de desempenho de um lote. Campos que dependem de dados que ainda nao existem (duas pesagens,
 * uma postura) vem nulos, e a tela mostra "sem dados" em vez de inventar um numero.
 * Frango de corte: ganho de peso, GMD medio e conversao alimentar (RN20). Poedeira: ovos, taxa media e racao por duzia.
 */
public record ProdutividadeLote(long loteId, String codigoLote, Especie especie, int totalEntrado, int saldo,
                                int mortes, double taxaDeMortalidade, int racaoConsumidaKg,
                                Double ganhoDePesoKg, Double gmdMedio, Double conversaoAlimentar,
                                Integer ovosTotal, Double taxaMediaDePostura, Double racaoPorDuzia) {
}
