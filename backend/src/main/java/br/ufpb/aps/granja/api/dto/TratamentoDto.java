package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.model.Tratamento;

public record TratamentoDto(long id, long ocorrenciaId, long insumoId, int quantidade, String data, String codigo,
                            String observacao) {

    public static TratamentoDto de(Tratamento t) {
        return new TratamentoDto(t.id(), t.ocorrenciaId(), t.insumoId(), t.quantidade().valor(), t.data().toString(),
                t.codigo().valor(), t.observacao());
    }
}
