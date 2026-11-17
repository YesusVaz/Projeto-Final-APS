package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.model.Insumo;

public record InsumoDto(long id, String nome, String categoria, String unidade, int nivelMinimo, int saldo,
                        boolean ativo, boolean abaixoDoMinimo) {

    public static InsumoDto de(Insumo insumo) {
        return new InsumoDto(insumo.id(), insumo.nome(), insumo.categoria().name(), insumo.unidade(),
                insumo.nivelMinimo().valor(), insumo.saldo().valor(), insumo.ativo(), insumo.abaixoDoMinimo());
    }
}
