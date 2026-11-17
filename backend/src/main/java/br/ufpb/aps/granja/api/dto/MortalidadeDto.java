package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.service.LoteService;

/** Lote atualizado mais o nivel de alerta avaliado na mortalidade. */
public record MortalidadeDto(long id, String codigo, String especie, int quantidadeInicial, int saldo, String status,
                             String nivelAlerta) {

    public static MortalidadeDto de(LoteService.RegistroMortalidade registro) {
        LoteDto lote = LoteDto.de(registro.lote());
        return new MortalidadeDto(lote.id(), lote.codigo(), lote.especie(), lote.quantidadeInicial(), lote.saldo(),
                lote.status(), registro.nivel().name());
    }
}
