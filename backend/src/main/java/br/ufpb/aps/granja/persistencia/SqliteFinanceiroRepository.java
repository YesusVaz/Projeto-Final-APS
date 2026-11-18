package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.CategoriaFinanceira;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.LancamentoFinanceiro;
import br.ufpb.aps.granja.repository.FinanceiroRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public final class SqliteFinanceiroRepository implements FinanceiroRepository {

    private static final String COLUNAS =
            "id, categoria, descricao, valor_centavos, data, lote_id, fornecedor_id, insumo_id, quantidade, codigo";

    private final Jdbc jdbc;

    public SqliteFinanceiroRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
    }

    @Override
    public LancamentoFinanceiro inserir(LancamentoFinanceiro l) {
        long id = jdbc.inserir("gravar o lançamento financeiro",
                "INSERT INTO lancamento_financeiro (tipo, categoria, descricao, valor_centavos, data, lote_id, "
                        + "fornecedor_id, insumo_id, quantidade, codigo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                l.tipo().name(), l.categoria().name(), l.descricao(), l.valorCentavos(), l.data().toString(),
                l.loteId(), l.fornecedorId(), l.insumoId(), l.quantidade(), l.codigo().valor());
        return new LancamentoFinanceiro(id, l.categoria(), l.descricao(), l.valorCentavos(), l.data(), l.loteId(),
                l.fornecedorId(), l.insumoId(), l.quantidade(), l.codigo());
    }

    @Override
    public List<LancamentoFinanceiro> listarPorPeriodo(LocalDate de, LocalDate ate) {
        return jdbc.listar("listar os lançamentos financeiros",
                "SELECT " + COLUNAS + " FROM lancamento_financeiro WHERE data >= ? AND data <= ? "
                        + "ORDER BY data DESC, id DESC",
                SqliteFinanceiroRepository::mapear, de.toString(), ate.toString());
    }

    private static LancamentoFinanceiro mapear(ResultSet rs) throws SQLException {
        long lote = rs.getLong("lote_id");
        Long loteOuNulo = rs.wasNull() ? null : lote;
        long fornecedor = rs.getLong("fornecedor_id");
        Long fornecedorOuNulo = rs.wasNull() ? null : fornecedor;
        long insumo = rs.getLong("insumo_id");
        Long insumoOuNulo = rs.wasNull() ? null : insumo;
        int quantidade = rs.getInt("quantidade");
        Integer quantidadeOuNula = rs.wasNull() ? null : quantidade;
        return new LancamentoFinanceiro(rs.getLong("id"), CategoriaFinanceira.valueOf(rs.getString("categoria")),
                rs.getString("descricao"), rs.getLong("valor_centavos"), LocalDate.parse(rs.getString("data")),
                loteOuNulo, fornecedorOuNulo, insumoOuNulo, quantidadeOuNula,
                new CodigoLancamento(rs.getString("codigo")));
    }
}
