package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.CategoriaInsumo;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Insumo;
import br.ufpb.aps.granja.model.MovimentoEstoque;
import br.ufpb.aps.granja.model.OrigemMovimento;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.TipoMovimentoEstoque;
import br.ufpb.aps.granja.repository.InsumoRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public final class SqliteInsumoRepository implements InsumoRepository {

    private static final String COLUNAS = "id, nome, categoria, unidade, nivel_minimo, saldo, ativo";

    private final Jdbc jdbc;

    public SqliteInsumoRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
    }

    @Override
    public Insumo inserir(String nome, CategoriaInsumo categoria, String unidade, Quantidade nivelMinimo) {
        long id = jdbc.inserir("gravar o insumo",
                "INSERT INTO insumo (nome, categoria, unidade, nivel_minimo, saldo, ativo) VALUES (?, ?, ?, ?, 0, 1)",
                nome, categoria.name(), unidade, nivelMinimo.valor());
        return Insumo.reconstituir(id, nome, categoria, unidade, nivelMinimo, Quantidade.ZERO, true);
    }

    @Override
    public Optional<Insumo> buscarPorId(long id) {
        return jdbc.buscar("ler o insumo", "SELECT " + COLUNAS + " FROM insumo WHERE id = ?",
                SqliteInsumoRepository::mapear, id);
    }

    @Override
    public List<Insumo> listar() {
        return jdbc.listar("listar os insumos", "SELECT " + COLUNAS + " FROM insumo ORDER BY id",
                SqliteInsumoRepository::mapear);
    }

    @Override
    public void atualizar(Insumo insumo) {
        jdbc.executar("atualizar o insumo", "UPDATE insumo SET saldo = ?, ativo = ? WHERE id = ?",
                insumo.saldo().valor(), Jdbc.comoInteiro(insumo.ativo()), insumo.id());
    }

    @Override
    public void registrarMovimento(MovimentoEstoque movimento) {
        jdbc.executar("gravar o movimento de estoque",
                "INSERT INTO movimento_estoque (insumo_id, tipo, quantidade, codigo, origem, lote_id, data_hora) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                movimento.insumoId(), movimento.tipo().name(), movimento.quantidade().valor(),
                movimento.codigo().valor(), movimento.origem().name(), movimento.loteId(),
                movimento.dataHora().toString());
    }

    @Override
    public List<MovimentoEstoque> extrato(long insumoId) {
        return jdbc.listar("ler o extrato do insumo",
                "SELECT id, insumo_id, tipo, quantidade, codigo, origem, lote_id, data_hora "
                        + "FROM movimento_estoque WHERE insumo_id = ? ORDER BY id",
                SqliteInsumoRepository::mapearMovimento, insumoId);
    }

    private static Insumo mapear(ResultSet rs) throws SQLException {
        return Insumo.reconstituir(
                rs.getLong("id"),
                rs.getString("nome"),
                CategoriaInsumo.valueOf(rs.getString("categoria")),
                rs.getString("unidade"),
                Quantidade.de(rs.getInt("nivel_minimo")),
                Quantidade.de(rs.getInt("saldo")),
                rs.getInt("ativo") == 1);
    }

    private static MovimentoEstoque mapearMovimento(ResultSet rs) throws SQLException {
        long lote = rs.getLong("lote_id");
        Long loteId = rs.wasNull() ? null : lote;
        return new MovimentoEstoque(
                rs.getLong("id"),
                rs.getLong("insumo_id"),
                TipoMovimentoEstoque.valueOf(rs.getString("tipo")),
                Quantidade.de(rs.getInt("quantidade")),
                new CodigoLancamento(rs.getString("codigo")),
                OrigemMovimento.valueOf(rs.getString("origem")),
                loteId,
                LocalDateTime.parse(rs.getString("data_hora")));
    }
}
