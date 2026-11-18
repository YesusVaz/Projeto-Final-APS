package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.model.Higienizacao;
import br.ufpb.aps.granja.model.ProgramaIluminacao;

public final class ManejoDto {

    private ManejoDto() {
    }

    public record HigienizacaoDto(long id, long loteId, String tipo, String data, String observacao, String codigo) {
        public static HigienizacaoDto de(Higienizacao h) {
            return new HigienizacaoDto(h.id(), h.loteId(), h.tipo().name(), h.data().toString(), h.observacao(),
                    h.codigo().valor());
        }
    }

    public record ProgramaDto(long id, long loteId, int horasDeLuz, Integer intensidadeLux, String dataInicio,
                              String observacao, String codigo) {
        public static ProgramaDto de(ProgramaIluminacao p) {
            return new ProgramaDto(p.id(), p.loteId(), p.horasDeLuz(), p.intensidadeLux(), p.dataInicio().toString(),
                    p.observacao(), p.codigo().valor());
        }
    }
}
