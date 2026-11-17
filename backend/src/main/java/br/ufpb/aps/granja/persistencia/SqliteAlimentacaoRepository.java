package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.RegistroAlimentacao;
import br.ufpb.aps.granja.repository.AlimentacaoRepository;

import java.time.LocalDate;
import java.util.List;

public final class SqliteAlimentacaoRepository implements AlimentacaoRepository {

    private final Jdbc jdbc;

    public SqliteAlimentacaoRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
    }

    @Override
    public RegistroAlimentacao inserir(RegistroAlimentacao registro) {
        long id = jdbc.inserir("gravar a alimentação",
                "INSERT INTO registro_alimentacao (lote_id, insumo_id, quantidade_kg, data, codigo) "
                        + "VALUES (?, ?, ?, ?, ?)",
                registro.loteId(), registro.insumoId(), registro.quantidadeKg().valor(),
                registro.data().toString(), registro.codigo().valor());
        return new RegistroAlimentacao(id, registro.loteId(), registro.insumoId(),
                registro.quantidadeKg(), registro.data(), registro.codigo());
    }

    @Override
    public List<RegistroAlimentacao> listarPorLote(long loteId) {
        return jdbc.listar("listar a alimentação do lote",
                "SELECT id, lote_id, insumo_id, quantidade_kg, data, codigo FROM registro_alimentacao "
                        + "WHERE lote_id = ? ORDER BY data, id",
                rs -> new RegistroAlimentacao(rs.getLong("id"), rs.getLong("lote_id"), rs.getLong("insumo_id"),
                        Quantidade.de(rs.getInt("quantidade_kg")), LocalDate.parse(rs.getString("data")),
                        new CodigoLancamento(rs.getString("codigo"))),
                loteId);
    }
}
