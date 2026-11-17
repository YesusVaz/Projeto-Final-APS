package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.policy.AlertaMortalidade;

public record AlertaDto(long loteId, String codigoLote, String nivel, String dataHora) {

    public static AlertaDto de(AlertaMortalidade alerta) {
        return new AlertaDto(alerta.loteId(), alerta.codigoLote(), alerta.nivel().name(),
                alerta.dataHora().toString());
    }
}
