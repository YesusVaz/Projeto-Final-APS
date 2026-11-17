package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.CategoriaInsumo;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Insumo;
import br.ufpb.aps.granja.model.MovimentoEstoque;
import br.ufpb.aps.granja.model.OrigemMovimento;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.TipoMovimentoEstoque;
import br.ufpb.aps.granja.repository.InsumoRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SqliteInsumoRepository implements InsumoRepository {

    private static final String COLUNAS = "id, nome, categoria, unidade, nivel_minimo, saldo, ativo";

    private final Banco banco;

    public SqliteInsumoRepository(Banco banco) {
        this.banco = banco;
    }

    @Override
    public Insumo inserir(String nome, CategoriaInsumo categoria, String unidade, Quantidade nivelMinimo) {
        String sql = "INSERT INTO insumo (nome, categoria, unidade, nivel_minimo, saldo, ativo) VALUES (?, ?, ?, ?, 0, 1)";
        try (PreparedStatement ps = banco.conexao().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nome);
            ps.setString(2, categoria.name());
            ps.setString(3, unidade);
            ps.setInt(4, nivelMinimo.valor());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return Insumo.reconstituir(chaves.getLong(1), nome, categoria, unidade, nivelMinimo,
                        Quantidade.ZERO, true);
            }
        } catch (SQLException e) {
            throw new ErroDePersistencia("gravar o insumo", e);
        }
    }

    @Override
    public Optional<Insumo> buscarPorId(long id) {
        try (PreparedStatement ps = banco.conexao().prepareStatement("SELECT " + COLUNAS + " FROM insumo WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErroDePersistencia("ler o insumo", e);
        }
    }

    @Override
    public List<Insumo> listar() {
        try (PreparedStatement ps = banco.conexao().prepareStatement("SELECT " + COLUNAS + " FROM insumo ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            List<Insumo> insumos = new ArrayList<>();
            while (rs.next()) {
                insumos.add(mapear(rs));
            }
            return insumos;
        } catch (SQLException e) {
            throw new ErroDePersistencia("listar os insumos", e);
        }
    }

    @Override
    public void atualizar(Insumo insumo) {
        try (PreparedStatement ps = banco.conexao().prepareStatement("UPDATE insumo SET saldo = ?, ativo = ? WHERE id = ?")) {
            ps.setInt(1, insumo.saldo().valor());
            ps.setInt(2, insumo.ativo() ? 1 : 0);
            ps.setLong(3, insumo.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroDePersistencia("atualizar o insumo", e);
        }
    }

    @Override
    public void registrarMovimento(MovimentoEstoque movimento) {
        String sql = "INSERT INTO movimento_estoque (insumo_id, tipo, quantidade, codigo, origem, lote_id, data_hora) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = banco.conexao().prepareStatement(sql)) {
            ps.setLong(1, movimento.insumoId());
            ps.setString(2, movimento.tipo().name());
            ps.setInt(3, movimento.quantidade().valor());
            ps.setString(4, movimento.codigo().valor());
            ps.setString(5, movimento.origem().name());
            if (movimento.loteId() == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setLong(6, movimento.loteId());
            }
            ps.setString(7, movimento.dataHora().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroDePersistencia("gravar o movimento de estoque", e);
        }
    }

    @Override
    public List<MovimentoEstoque> extrato(long insumoId) {
        String sql = "SELECT id, insumo_id, tipo, quantidade, codigo, origem, lote_id, data_hora "
                + "FROM movimento_estoque WHERE insumo_id = ? ORDER BY id";
        try (PreparedStatement ps = banco.conexao().prepareStatement(sql)) {
            ps.setLong(1, insumoId);
            try (ResultSet rs = ps.executeQuery()) {
                List<MovimentoEstoque> movimentos = new ArrayList<>();
                while (rs.next()) {
                    long lote = rs.getLong("lote_id");
                    Long loteId = rs.wasNull() ? null : lote;
                    movimentos.add(new MovimentoEstoque(
                            rs.getLong("id"),
                            rs.getLong("insumo_id"),
                            TipoMovimentoEstoque.valueOf(rs.getString("tipo")),
                            Quantidade.de(rs.getInt("quantidade")),
                            new CodigoLancamento(rs.getString("codigo")),
                            OrigemMovimento.valueOf(rs.getString("origem")),
                            loteId,
                            LocalDateTime.parse(rs.getString("data_hora"))));
                }
                return movimentos;
            }
        } catch (SQLException e) {
            throw new ErroDePersistencia("ler o extrato do insumo", e);
        }
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
}
