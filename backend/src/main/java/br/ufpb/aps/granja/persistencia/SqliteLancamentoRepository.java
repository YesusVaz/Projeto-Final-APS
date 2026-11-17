package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.excecao.LancamentoDuplicadoException;
import br.ufpb.aps.granja.repository.LancamentoRepository;

import java.time.LocalDateTime;

public final class SqliteLancamentoRepository implements LancamentoRepository {

    private final Jdbc jdbc;

    public SqliteLancamentoRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
    }

    /**
     * RN22 pela chave primaria: o INSERT OR IGNORE nao grava nada se o codigo ja existe,
     * a linha afetada da zero. Assim quem barra e o banco, nao so o codigo Java.
     */
    @Override
    public void registrar(CodigoLancamento codigo, LocalDateTime dataHora) {
        int gravadas = jdbc.executar("registrar o lançamento",
                "INSERT OR IGNORE INTO lancamento (codigo, data_hora) VALUES (?, ?)",
                codigo.valor(), dataHora.toString());
        if (gravadas == 0) {
            throw new LancamentoDuplicadoException(codigo.valor());
        }
    }
}
