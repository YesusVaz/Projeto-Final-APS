package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.CategoriaChamado;
import br.ufpb.aps.granja.model.Chamado;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.StatusChamado;
import br.ufpb.aps.granja.repository.ChamadoRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public final class SqliteChamadoRepository implements ChamadoRepository {

    private static final String SELECIONAR = "SELECT c.id, c.solicitante_id, u.nome AS solicitante_nome, c.categoria, "
            + "c.assunto, c.descricao, c.status, c.aberto_em, c.resposta, c.atualizado_em, c.codigo "
            + "FROM chamado c JOIN usuario u ON u.id = c.solicitante_id ";

    private final Jdbc jdbc;

    public SqliteChamadoRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
    }

    @Override
    public Chamado inserir(Chamado c) {
        long id = jdbc.inserir("gravar o chamado",
                "INSERT INTO chamado (solicitante_id, categoria, assunto, descricao, status, aberto_em, resposta, "
                        + "atualizado_em, codigo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                c.solicitanteId(), c.categoria().name(), c.assunto(), c.descricao(), c.status().name(),
                c.abertoEm().toString(), c.resposta(), c.atualizadoEm().toString(), c.codigo().valor());
        return c.comId(id);
    }

    @Override
    public List<Chamado> listarDoSolicitante(long solicitanteId) {
        return jdbc.listar("listar os chamados da pessoa", SELECIONAR + "WHERE c.solicitante_id = ? "
                + "ORDER BY c.aberto_em DESC, c.id DESC", SqliteChamadoRepository::mapear, solicitanteId);
    }

    @Override
    public List<Chamado> listarTodos() {
        return jdbc.listar("listar os chamados", SELECIONAR
                + "ORDER BY CASE c.status WHEN 'RESOLVIDO' THEN 1 ELSE 0 END, c.aberto_em DESC, c.id DESC",
                SqliteChamadoRepository::mapear);
    }

    @Override
    public Optional<Chamado> buscarPorId(long id) {
        return jdbc.buscar("buscar o chamado", SELECIONAR + "WHERE c.id = ?", SqliteChamadoRepository::mapear, id);
    }

    @Override
    public void atualizar(Chamado c) {
        jdbc.executar("atualizar o chamado", "UPDATE chamado SET status = ?, resposta = ?, atualizado_em = ? WHERE id = ?",
                c.status().name(), c.resposta(), c.atualizadoEm().toString(), c.id());
    }

    private static Chamado mapear(ResultSet rs) throws SQLException {
        return new Chamado(rs.getLong("id"), rs.getLong("solicitante_id"), rs.getString("solicitante_nome"),
                CategoriaChamado.valueOf(rs.getString("categoria")), rs.getString("assunto"), rs.getString("descricao"),
                StatusChamado.valueOf(rs.getString("status")), LocalDateTime.parse(rs.getString("aberto_em")),
                rs.getString("resposta"), LocalDateTime.parse(rs.getString("atualizado_em")),
                new CodigoLancamento(rs.getString("codigo")));
    }
}
