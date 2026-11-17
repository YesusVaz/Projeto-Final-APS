package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.RegistroProducao;
import br.ufpb.aps.granja.model.TipoProducao;
import br.ufpb.aps.granja.repository.ProducaoRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public final class SqliteProducaoRepository implements ProducaoRepository {

    private static final String COLUNAS = "id, lote_id, tipo, data, valor, vivos, indicador, codigo";

    private final Jdbc jdbc;

    public SqliteProducaoRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
    }

    @Override
    public RegistroProducao inserir(RegistroProducao r) {
        long id = jdbc.inserir("gravar a produção",
                "INSERT INTO producao (lote_id, tipo, data, valor, vivos, indicador, codigo) VALUES (?, ?, ?, ?, ?, ?, ?)",
                r.loteId(), r.tipo().name(), r.data().toString(), r.valor(), r.vivos(), r.indicador(),
                r.codigo().valor());
        return new RegistroProducao(id, r.loteId(), r.tipo(), r.data(), r.valor(), r.vivos(), r.indicador(), r.codigo());
    }

    @Override
    public boolean existe(long loteId, TipoProducao tipo, LocalDate data) {
        return jdbc.contar("conferir a produção do dia",
                "SELECT COUNT(*) FROM producao WHERE lote_id = ? AND tipo = ? AND data = ?",
                loteId, tipo.name(), data.toString()) > 0;
    }

    @Override
    public List<RegistroProducao> listarPorLote(long loteId) {
        return jdbc.listar("listar a produção do lote",
                "SELECT " + COLUNAS + " FROM producao WHERE lote_id = ? ORDER BY data, id",
                SqliteProducaoRepository::mapear, loteId);
    }

    private static RegistroProducao mapear(ResultSet rs) throws SQLException {
        int vivos = rs.getInt("vivos");
        Integer vivosOuNulo = rs.wasNull() ? null : vivos;
        double indicador = rs.getDouble("indicador");
        Double indicadorOuNulo = rs.wasNull() ? null : indicador;
        return new RegistroProducao(rs.getLong("id"), rs.getLong("lote_id"), TipoProducao.valueOf(rs.getString("tipo")),
                LocalDate.parse(rs.getString("data")), rs.getInt("valor"), vivosOuNulo, indicadorOuNulo,
                new CodigoLancamento(rs.getString("codigo")));
    }
}
