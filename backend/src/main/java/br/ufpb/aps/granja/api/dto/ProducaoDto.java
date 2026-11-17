package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.model.RegistroProducao;
import br.ufpb.aps.granja.model.TipoProducao;

/**
 * Postura: {@code valor} = ovos, {@code indicador} = taxa de postura em %.
 * Pesagem: {@code valor} = peso medio em gramas, {@code indicador} = GMD em g/dia (nulo na primeira).
 */
public record ProducaoDto(long id, long loteId, String tipo, String data, int valor, Integer vivos,
                          Double indicador, String unidadeIndicador, String codigo) {

    public static ProducaoDto de(RegistroProducao r) {
        return new ProducaoDto(r.id(), r.loteId(), r.tipo().name(), r.data().toString(), r.valor(), r.vivos(),
                r.indicador(), r.tipo() == TipoProducao.POSTURA ? "%" : "g/dia", r.codigo().valor());
    }
}
