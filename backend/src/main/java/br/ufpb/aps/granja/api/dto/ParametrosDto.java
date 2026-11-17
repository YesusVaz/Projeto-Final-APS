package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.model.ParametrosSistema;

public record ParametrosDto(int diasAvisoVacina, int diasOcorrenciaAberta) {

    public static ParametrosDto de(ParametrosSistema p) {
        return new ParametrosDto(p.diasAvisoVacina(), p.diasOcorrenciaAberta());
    }
}
