package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.service.LoteService;

/** UC05. {@code semanas} e so a idade em dias dividida por 7, arredondada pra baixo. */
public record IdadeLoteDto(long loteId, String codigo, String dataAlojamento, int idadeInicialDias, String data,
                           int idadeDias, int semanas) {

    public static IdadeLoteDto de(LoteService.IdadeLote idade) {
        return new IdadeLoteDto(idade.lote().id(), idade.lote().codigo(), idade.lote().dataAlojamento().toString(),
                idade.lote().idadeInicialDias(), idade.dia().toString(), idade.idadeDias(), idade.idadeDias() / 7);
    }
}
