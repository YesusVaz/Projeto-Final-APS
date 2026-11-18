package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.api.dto.SuporteDto.BackupDto;
import br.ufpb.aps.granja.service.BackupService;

import java.time.LocalDateTime;
import java.util.List;

/** Backup do banco (UC28): fazer uma copia, listar as copias e baixar uma delas. So o Gestor. */
final class BackupController implements Controller {

    private final BackupService backups;

    BackupController(BackupService backups) {
        this.backups = backups;
    }

    @Override
    public List<Rota> rotas() {
        return List.of(
                Rota.get("/api/backups", this::listar),
                Rota.post("/api/backups", this::criar),
                Rota.get("/api/backups/([^/]+)/arquivo", this::baixar));
    }

    private Resposta listar(Requisicao r) {
        return Resposta.ok(backups.listar(r.usuario()).stream().map(BackupDto::de).toList());
    }

    private Resposta criar(Requisicao r) {
        return Resposta.criado(BackupDto.de(backups.criar(r.usuario(), LocalDateTime.now())));
    }

    private Resposta baixar(Requisicao r) {
        String nome = r.parametros().get(0);
        return Resposta.arquivo(nome, "application/octet-stream", backups.ler(r.usuario(), nome, LocalDateTime.now()));
    }
}
