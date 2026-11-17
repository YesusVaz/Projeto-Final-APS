package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.policy.ConfiguracaoAlerta;

public record ConfiguracaoAlertaDto(String criterio, double atencao, double critico) {

    public static ConfiguracaoAlertaDto de(ConfiguracaoAlerta configuracao) {
        return new ConfiguracaoAlertaDto(configuracao.criterio().name(), configuracao.atencao(),
                configuracao.critico());
    }
}
