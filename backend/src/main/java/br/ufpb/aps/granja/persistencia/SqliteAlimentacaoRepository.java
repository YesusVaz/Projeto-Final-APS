package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.RegistroAlimentacao;
import br.ufpb.aps.granja.repository.AlimentacaoRepository;

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
}
