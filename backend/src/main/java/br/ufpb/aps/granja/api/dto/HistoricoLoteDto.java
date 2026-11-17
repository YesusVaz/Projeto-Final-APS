package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.service.HistoricoLoteService;

import java.util.List;

/** UC17: lote com tudo que aconteceu com ele, em ordem. */
public record HistoricoLoteDto(LoteDto lote, boolean consistente, List<Item> itens) {

    public record Item(String data, String hora, String categoria, String descricao) {
    }

    public static HistoricoLoteDto de(HistoricoLoteService.HistoricoCompleto historico) {
        List<Item> itens = historico.itens().stream()
                .map(i -> new Item(i.data().toString(), i.hora() == null ? null : i.hora().withNano(0).toString(),
                        i.categoria(), i.descricao()))
                .toList();
        return new HistoricoLoteDto(LoteDto.de(historico.lote()), historico.consistente(), itens);
    }
}
