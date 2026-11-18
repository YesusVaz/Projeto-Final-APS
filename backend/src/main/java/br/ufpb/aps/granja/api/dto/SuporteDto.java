package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.model.Chamado;
import br.ufpb.aps.granja.model.CopiaDeSeguranca;

/** Respostas dos chamados de suporte (UC30) e dos backups (UC28). */
public final class SuporteDto {

    private SuporteDto() {
    }

    public record ChamadoDto(long id, long solicitanteId, String solicitante, String categoria, String assunto,
                             String descricao, String status, String abertoEm, String resposta, String atualizadoEm) {
        public static ChamadoDto de(Chamado c) {
            return new ChamadoDto(c.id(), c.solicitanteId(), c.solicitanteNome(), c.categoria().name(), c.assunto(),
                    c.descricao(), c.status().name(), c.abertoEm().toString(), c.resposta(), c.atualizadoEm().toString());
        }
    }

    public record BackupDto(String nome, long tamanhoBytes, String criadoEm) {
        public static BackupDto de(CopiaDeSeguranca c) {
            return new BackupDto(c.nome(), c.tamanhoBytes(), c.criadaEm().toString());
        }
    }
}
