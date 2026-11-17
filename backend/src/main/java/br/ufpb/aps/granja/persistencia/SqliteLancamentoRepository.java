package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.excecao.LancamentoDuplicadoException;
import br.ufpb.aps.granja.repository.LancamentoRepository;

<<<<<<< HEAD
=======
import java.sql.PreparedStatement;
import java.sql.SQLException;
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
import java.time.LocalDateTime;

public final class SqliteLancamentoRepository implements LancamentoRepository {

<<<<<<< HEAD
    private final Jdbc jdbc;

    public SqliteLancamentoRepository(Banco banco) {
        this.jdbc = new Jdbc(banco);
=======
    private final Banco banco;

    public SqliteLancamentoRepository(Banco banco) {
        this.banco = banco;
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
    }

    /**
     * RN22 pela chave primaria: o INSERT OR IGNORE nao grava nada se o codigo ja existe,
     * a linha afetada da zero. Assim quem barra e o banco, nao so o codigo Java.
     */
    @Override
    public void registrar(CodigoLancamento codigo, LocalDateTime dataHora) {
<<<<<<< HEAD
        int gravadas = jdbc.executar("registrar o lançamento",
                "INSERT OR IGNORE INTO lancamento (codigo, data_hora) VALUES (?, ?)",
                codigo.valor(), dataHora.toString());
        if (gravadas == 0) {
            throw new LancamentoDuplicadoException(codigo.valor());
=======
        try (PreparedStatement ps = banco.conexao().prepareStatement(
                "INSERT OR IGNORE INTO lancamento (codigo, data_hora) VALUES (?, ?)")) {
            ps.setString(1, codigo.valor());
            ps.setString(2, dataHora.toString());
            if (ps.executeUpdate() == 0) {
                throw new LancamentoDuplicadoException(codigo.valor());
            }
        } catch (SQLException e) {
            throw new ErroDePersistencia("registrar o lançamento", e);
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
        }
    }
}
