package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.model.AlertaSanitario;

public record AlertaSanitarioDto(String tipo, long loteId, String codigoLote, String descricao, String referencia) {

    public static AlertaSanitarioDto de(AlertaSanitario a) {
        return new AlertaSanitarioDto(a.tipo().name(), a.loteId(), a.codigoLote(), a.descricao(),
                a.referencia().toString());
    }
}
