package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.model.Vacinacao;

public record VacinacaoDto(long id, long loteId, long insumoId, int doses, String data, String proximaDose,
                           String codigo, String observacao) {

    public static VacinacaoDto de(Vacinacao v) {
        return new VacinacaoDto(v.id(), v.loteId(), v.insumoId(), v.doses().valor(), v.data().toString(),
                v.proximaDose() == null ? null : v.proximaDose().toString(), v.codigo().valor(), v.observacao());
    }
}
