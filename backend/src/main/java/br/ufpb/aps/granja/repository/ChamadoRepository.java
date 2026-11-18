package br.ufpb.aps.granja.repository;

import br.ufpb.aps.granja.model.Chamado;

import java.util.List;
import java.util.Optional;

/** Contrato de persistencia dos chamados de suporte (UC30). */
public interface ChamadoRepository {

    Chamado inserir(Chamado chamado);

    /** Os chamados de uma pessoa, do mais novo pro mais antigo. */
    List<Chamado> listarDoSolicitante(long solicitanteId);

    /** Todos os chamados: os que ainda nao foram resolvidos primeiro, depois os mais novos. */
    List<Chamado> listarTodos();

    Optional<Chamado> buscarPorId(long id);

    void atualizar(Chamado chamado);
}
