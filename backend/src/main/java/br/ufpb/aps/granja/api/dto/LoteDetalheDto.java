package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.service.LoteService;

import java.util.List;

/** Lote com historico e o resultado da checagem de consistencia (RN06r). */
public record LoteDetalheDto(long id, String codigo, String especie, int quantidadeInicial, int saldo, String status,
                             boolean consistente, List<MovimentoLoteDto> historico) {

    public static LoteDetalheDto de(LoteService.ConsultaLote consulta) {
        LoteDto lote = LoteDto.de(consulta.lote());
        return new LoteDetalheDto(lote.id(), lote.codigo(), lote.especie(), lote.quantidadeInicial(), lote.saldo(),
                lote.status(), consulta.consistente(),
                consulta.historico().stream().map(MovimentoLoteDto::de).toList());
    }
}
