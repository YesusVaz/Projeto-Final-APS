package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.model.RegistroAlimentacao;

public record AlimentacaoDto(Long id, long loteId, long insumoId, int quantidadeKg, String data, String codigo) {

    public static AlimentacaoDto de(RegistroAlimentacao registro) {
        return new AlimentacaoDto(registro.id(), registro.loteId(), registro.insumoId(),
                registro.quantidadeKg().valor(), registro.data().toString(), registro.codigo().valor());
    }
}
