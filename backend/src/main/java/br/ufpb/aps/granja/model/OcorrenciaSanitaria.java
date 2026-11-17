package br.ufpb.aps.granja.model;

import br.ufpb.aps.granja.model.excecao.OcorrenciaEncerradaException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * UC12: problema sanitario no lote (doenca, surto, sintoma). Fica aberta enquanto trata.
 * Encerrada nao recebe tratamento e nao reabre.
 */
public class OcorrenciaSanitaria {

    private final Long id;
    private final Long loteId;
    private final String descricao;
    private final LocalDate dataAbertura;
    private StatusOcorrencia status = StatusOcorrencia.ABERTA;
    private LocalDate dataEncerramento;

    public OcorrenciaSanitaria(Long id, Long loteId, String descricao, LocalDate dataAbertura) {
        this.id = id;
        this.loteId = loteId;
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição da ocorrência é obrigatória.");
        }
        this.descricao = descricao.trim();
        this.dataAbertura = Objects.requireNonNull(dataAbertura, "dataAbertura");
    }

    public static OcorrenciaSanitaria reconstituir(Long id, Long loteId, String descricao, LocalDate dataAbertura,
                                                   StatusOcorrencia status, LocalDate dataEncerramento) {
        OcorrenciaSanitaria ocorrencia = new OcorrenciaSanitaria(id, loteId, descricao, dataAbertura);
        ocorrencia.status = status;
        ocorrencia.dataEncerramento = dataEncerramento;
        return ocorrencia;
    }

    public void encerrar(LocalDate data) {
        exigirAberta();
        if (data.isBefore(dataAbertura)) {
            throw new IllegalArgumentException("A data de encerramento é anterior à abertura da ocorrência.");
        }
        status = StatusOcorrencia.ENCERRADA;
        dataEncerramento = data;
    }

    public void exigirAberta() {
        if (status != StatusOcorrencia.ABERTA) {
            throw new OcorrenciaEncerradaException(id);
        }
    }

    public boolean aberta() {
        return status == StatusOcorrencia.ABERTA;
    }

    public Long id() { return id; }
    public Long loteId() { return loteId; }
    public String descricao() { return descricao; }
    public LocalDate dataAbertura() { return dataAbertura; }
    public StatusOcorrencia status() { return status; }
    public LocalDate dataEncerramento() { return dataEncerramento; }
}
