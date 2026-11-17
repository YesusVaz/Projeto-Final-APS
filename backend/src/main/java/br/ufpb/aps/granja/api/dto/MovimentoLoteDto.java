package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.model.MovimentoLote;

public record MovimentoLoteDto(Long id, String tipo, int quantidade, String codigo, String dataHora,
                               String observacao) {

    public static MovimentoLoteDto de(MovimentoLote movimento) {
        return new MovimentoLoteDto(movimento.id(), movimento.tipo().name(), movimento.quantidade().valor(),
                movimento.codigo().valor(), movimento.dataHora().toString(), movimento.observacao());
    }
}
