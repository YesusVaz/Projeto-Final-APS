package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.model.Lote;

public record LoteDto(long id, String codigo, String especie, int quantidadeInicial, int saldo, String status) {

    public static LoteDto de(Lote lote) {
        return new LoteDto(lote.id(), lote.codigo(), lote.especie().name(), lote.quantidadeInicial().valor(),
                lote.saldo().valor(), lote.status().name());
    }
}
