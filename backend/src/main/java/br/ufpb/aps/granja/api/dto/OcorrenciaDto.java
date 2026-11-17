package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.model.OcorrenciaSanitaria;
import br.ufpb.aps.granja.service.SanidadeService;

import java.util.List;

/** {@code tratamentos} so vem preenchido na consulta de uma ocorrencia. Na lista fica nulo. */
public record OcorrenciaDto(long id, long loteId, String descricao, String dataAbertura, String status,
                            String dataEncerramento, List<TratamentoDto> tratamentos) {

    public static OcorrenciaDto de(OcorrenciaSanitaria o) {
        return new OcorrenciaDto(o.id(), o.loteId(), o.descricao(), o.dataAbertura().toString(), o.status().name(),
                o.dataEncerramento() == null ? null : o.dataEncerramento().toString(), null);
    }

    public static OcorrenciaDto de(SanidadeService.ConsultaOcorrencia consulta) {
        OcorrenciaDto base = de(consulta.ocorrencia());
        return new OcorrenciaDto(base.id(), base.loteId(), base.descricao(), base.dataAbertura(), base.status(),
                base.dataEncerramento(), consulta.tratamentos().stream().map(TratamentoDto::de).toList());
    }
}
