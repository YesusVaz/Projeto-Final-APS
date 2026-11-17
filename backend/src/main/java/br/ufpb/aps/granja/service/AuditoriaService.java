package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.repository.AuditoriaRepository;

import java.util.List;

/** UC03: so o gestor le o log. Ninguem altera nem apaga (RN15, garantido tambem por trigger no banco). */
public final class AuditoriaService {

    private final AuditoriaRepository auditoria;

    public AuditoriaService(AuditoriaRepository auditoria) {
        this.auditoria = auditoria;
    }

    public List<AuditoriaRepository.Entrada> listar(Usuario usuario) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR_AUDITORIA);
        return auditoria.listar();
    }
}
