package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.repository.AuditoriaRepository;

public record AuditoriaDto(long id, String categoria, String detalhe, String dataHora) {

    public static AuditoriaDto de(AuditoriaRepository.Entrada entrada) {
        return new AuditoriaDto(entrada.id(), entrada.categoria(), entrada.detalhe(), entrada.dataHora().toString());
    }
}
