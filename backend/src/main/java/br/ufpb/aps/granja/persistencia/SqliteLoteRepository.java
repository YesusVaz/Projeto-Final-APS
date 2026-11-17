package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.MovimentoLote;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.StatusLote;
import br.ufpb.aps.granja.model.TipoMovimentoLote;
import br.ufpb.aps.granja.repository.LoteRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public final class SqliteLoteRepository implements LoteRepository {

    private static final String COLUNAS = "id, codigo, especie, quantidade_inicial, data_alojamento, idade_inicial_dias, saldo, status";

    private final Jdbc jdbc;

    public SqliteLoteRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
    }

    @Override
    public Lote inserir(String codigo, Especie especie, Quantidade quantidadeInicial, LocalDate dataAlojamento,
                        int idadeInicialDias) {
        long id = jdbc.inserir("gravar o lote",
                "INSERT INTO lote (codigo, especie, quantidade_inicial, data_alojamento, idade_inicial_dias, saldo, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                codigo, especie.name(), quantidadeInicial.valor(), dataAlojamento.toString(), idadeInicialDias,
                quantidadeInicial.valor(), StatusLote.ATIVO.name());
        return Lote.reconstituir(id, codigo, especie, quantidadeInicial, dataAlojamento, idadeInicialDias,
                quantidadeInicial, StatusLote.ATIVO);
    }

    @Override
    public Optional<Lote> buscarPorId(long id) {
        return jdbc.buscar("ler o lote", "SELECT " + COLUNAS + " FROM lote WHERE id = ?",
                SqliteLoteRepository::mapear, id);
    }

    @Override
    public List<Lote> listar() {
        return jdbc.listar("listar os lotes", "SELECT " + COLUNAS + " FROM lote ORDER BY id",
                SqliteLoteRepository::mapear);
    }

    @Override
    public List<MovimentoLote> historico(long loteId) {
        return jdbc.listar("ler o histórico do lote",
                "SELECT id, lote_id, tipo, quantidade, codigo, data_hora, observacao "
                        + "FROM movimento_lote WHERE lote_id = ? ORDER BY id",
                SqliteLoteRepository::mapearMovimento, loteId);
    }

    @Override
    public void registrarMovimento(MovimentoLote movimento) {
        jdbc.executar("gravar o movimento do lote",
                "INSERT INTO movimento_lote (lote_id, tipo, quantidade, codigo, data_hora, observacao) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                movimento.loteId(), movimento.tipo().name(), movimento.quantidade().valor(),
                movimento.codigo().valor(), movimento.dataHora().toString(), movimento.observacao());
    }

    @Override
    public void atualizar(Lote lote) {
        jdbc.executar("atualizar o lote", "UPDATE lote SET saldo = ?, status = ? WHERE id = ?",
                lote.saldo().valor(), lote.status().name(), lote.id());
    }

    @Override
    public int contarPorAno(int ano) {
        return (int) jdbc.contar("contar os lotes do ano", "SELECT COUNT(*) FROM lote WHERE codigo LIKE ?",
                "LOT-" + ano + "-%");
    }

    private static Lote mapear(ResultSet rs) throws SQLException {
        return Lote.reconstituir(
                rs.getLong("id"),
                rs.getString("codigo"),
                Especie.valueOf(rs.getString("especie")),
                Quantidade.de(rs.getInt("quantidade_inicial")),
                LocalDate.parse(rs.getString("data_alojamento")),
                rs.getInt("idade_inicial_dias"),
                Quantidade.de(rs.getInt("saldo")),
                StatusLote.valueOf(rs.getString("status")));
    }

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
}
