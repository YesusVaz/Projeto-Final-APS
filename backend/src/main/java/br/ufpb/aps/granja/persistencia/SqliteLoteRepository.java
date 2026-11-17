package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.MovimentoLote;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.StatusLote;
import br.ufpb.aps.granja.model.TipoMovimentoLote;
import br.ufpb.aps.granja.repository.LoteRepository;

<<<<<<< HEAD
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
=======
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
import java.util.List;
import java.util.Optional;

public final class SqliteLoteRepository implements LoteRepository {

    private static final String COLUNAS = "id, codigo, especie, quantidade_inicial, saldo, status";

<<<<<<< HEAD
    private final Jdbc jdbc;

    public SqliteLoteRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
=======
    private final Banco banco;

    public SqliteLoteRepository(Banco banco) {
        this.banco = banco;
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }

    @Override
    public Lote inserir(String codigo, Especie especie, Quantidade quantidadeInicial) {
<<<<<<< HEAD
        long id = jdbc.inserir("gravar o lote",
                "INSERT INTO lote (codigo, especie, quantidade_inicial, saldo, status) VALUES (?, ?, ?, ?, ?)",
                codigo, especie.name(), quantidadeInicial.valor(), quantidadeInicial.valor(), StatusLote.ATIVO.name());
        return Lote.reconstituir(id, codigo, especie, quantidadeInicial, quantidadeInicial, StatusLote.ATIVO);
=======
        String sql = "INSERT INTO lote (codigo, especie, quantidade_inicial, saldo, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = banco.conexao().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, codigo);
            ps.setString(2, especie.name());
            ps.setInt(3, quantidadeInicial.valor());
            ps.setInt(4, quantidadeInicial.valor());
            ps.setString(5, StatusLote.ATIVO.name());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return Lote.reconstituir(chaves.getLong(1), codigo, especie, quantidadeInicial,
                        quantidadeInicial, StatusLote.ATIVO);
            }
        } catch (SQLException e) {
            throw new ErroDePersistencia("gravar o lote", e);
        }
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }

    @Override
    public Optional<Lote> buscarPorId(long id) {
<<<<<<< HEAD
        return jdbc.buscar("ler o lote", "SELECT " + COLUNAS + " FROM lote WHERE id = ?",
                SqliteLoteRepository::mapear, id);
=======
        try (PreparedStatement ps = banco.conexao().prepareStatement("SELECT " + COLUNAS + " FROM lote WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErroDePersistencia("ler o lote", e);
        }
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }

    @Override
    public List<Lote> listar() {
<<<<<<< HEAD
        return jdbc.listar("listar os lotes", "SELECT " + COLUNAS + " FROM lote ORDER BY id",
                SqliteLoteRepository::mapear);
=======
        try (PreparedStatement ps = banco.conexao().prepareStatement("SELECT " + COLUNAS + " FROM lote ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            List<Lote> lotes = new ArrayList<>();
            while (rs.next()) {
                lotes.add(mapear(rs));
            }
            return lotes;
        } catch (SQLException e) {
            throw new ErroDePersistencia("listar os lotes", e);
        }
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }

    @Override
    public List<MovimentoLote> historico(long loteId) {
<<<<<<< HEAD
        return jdbc.listar("ler o histórico do lote",
                "SELECT id, lote_id, tipo, quantidade, codigo, data_hora, observacao "
                        + "FROM movimento_lote WHERE lote_id = ? ORDER BY id",
                SqliteLoteRepository::mapearMovimento, loteId);
=======
        String sql = "SELECT id, lote_id, tipo, quantidade, codigo, data_hora, observacao "
                + "FROM movimento_lote WHERE lote_id = ? ORDER BY id";
        try (PreparedStatement ps = banco.conexao().prepareStatement(sql)) {
            ps.setLong(1, loteId);
            try (ResultSet rs = ps.executeQuery()) {
                List<MovimentoLote> movimentos = new ArrayList<>();
                while (rs.next()) {
                    movimentos.add(new MovimentoLote(
                            rs.getLong("id"),
                            rs.getLong("lote_id"),
                            TipoMovimentoLote.valueOf(rs.getString("tipo")),
                            Quantidade.de(rs.getInt("quantidade")),
                            new CodigoLancamento(rs.getString("codigo")),
                            LocalDateTime.parse(rs.getString("data_hora")),
                            rs.getString("observacao")));
                }
                return movimentos;
            }
        } catch (SQLException e) {
            throw new ErroDePersistencia("ler o histórico do lote", e);
        }
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }

    @Override
    public void registrarMovimento(MovimentoLote movimento) {
<<<<<<< HEAD
        jdbc.executar("gravar o movimento do lote",
                "INSERT INTO movimento_lote (lote_id, tipo, quantidade, codigo, data_hora, observacao) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                movimento.loteId(), movimento.tipo().name(), movimento.quantidade().valor(),
                movimento.codigo().valor(), movimento.dataHora().toString(), movimento.observacao());
=======
        String sql = "INSERT INTO movimento_lote (lote_id, tipo, quantidade, codigo, data_hora, observacao) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = banco.conexao().prepareStatement(sql)) {
            ps.setLong(1, movimento.loteId());
            ps.setString(2, movimento.tipo().name());
            ps.setInt(3, movimento.quantidade().valor());
            ps.setString(4, movimento.codigo().valor());
            ps.setString(5, movimento.dataHora().toString());
            ps.setString(6, movimento.observacao());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroDePersistencia("gravar o movimento do lote", e);
        }
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }

    @Override
    public void atualizar(Lote lote) {
<<<<<<< HEAD
        jdbc.executar("atualizar o lote", "UPDATE lote SET saldo = ?, status = ? WHERE id = ?",
                lote.saldo().valor(), lote.status().name(), lote.id());
=======
        try (PreparedStatement ps = banco.conexao().prepareStatement("UPDATE lote SET saldo = ?, status = ? WHERE id = ?")) {
            ps.setInt(1, lote.saldo().valor());
            ps.setString(2, lote.status().name());
            ps.setLong(3, lote.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroDePersistencia("atualizar o lote", e);
        }
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }

    @Override
    public int contarPorAno(int ano) {
<<<<<<< HEAD
        return (int) jdbc.contar("contar os lotes do ano", "SELECT COUNT(*) FROM lote WHERE codigo LIKE ?",
                "LOT-" + ano + "-%");
=======
        try (PreparedStatement ps = banco.conexao().prepareStatement("SELECT COUNT(*) FROM lote WHERE codigo LIKE ?")) {
            ps.setString(1, "LOT-" + ano + "-%");
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new ErroDePersistencia("contar os lotes do ano", e);
        }
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }

    private static Lote mapear(ResultSet rs) throws SQLException {
        return Lote.reconstituir(
                rs.getLong("id"),
                rs.getString("codigo"),
                Especie.valueOf(rs.getString("especie")),
                Quantidade.de(rs.getInt("quantidade_inicial")),
                Quantidade.de(rs.getInt("saldo")),
                StatusLote.valueOf(rs.getString("status")));
    }
<<<<<<< HEAD

    private static MovimentoLote mapearMovimento(ResultSet rs) throws SQLException {
        return new MovimentoLote(
                rs.getLong("id"),
                rs.getLong("lote_id"),
                TipoMovimentoLote.valueOf(rs.getString("tipo")),
                Quantidade.de(rs.getInt("quantidade")),
                new CodigoLancamento(rs.getString("codigo")),
                LocalDateTime.parse(rs.getString("data_hora")),
                rs.getString("observacao"));
    }
=======
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
}
