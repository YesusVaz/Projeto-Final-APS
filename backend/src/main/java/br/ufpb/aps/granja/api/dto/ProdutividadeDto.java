package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.model.ProdutividadeLote;

/** Campos nulos = ainda nao ha dados pra calcular. */
public record ProdutividadeDto(long loteId, String codigoLote, String especie, int totalEntrado, int saldo, int mortes,
                               double taxaDeMortalidade, int racaoConsumidaKg, Double ganhoDePesoKg, Double gmdMedio,
                               Double conversaoAlimentar, Integer ovosTotal, Double taxaMediaDePostura,
                               Double racaoPorDuzia) {

    public static ProdutividadeDto de(ProdutividadeLote p) {
        return new ProdutividadeDto(p.loteId(), p.codigoLote(), p.especie().name(), p.totalEntrado(), p.saldo(),
                p.mortes(), p.taxaDeMortalidade(), p.racaoConsumidaKg(), p.ganhoDePesoKg(), p.gmdMedio(),
                p.conversaoAlimentar(), p.ovosTotal(), p.taxaMediaDePostura(), p.racaoPorDuzia());
    }
}
