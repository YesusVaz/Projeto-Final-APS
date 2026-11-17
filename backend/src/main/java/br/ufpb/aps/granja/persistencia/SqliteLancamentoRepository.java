package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.excecao.LancamentoDuplicadoException;
import br.ufpb.aps.granja.repository.LancamentoRepository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public final class SqliteLancamentoRepository implements LancamentoRepository {

    private final Banco banco;

    public SqliteLancamentoRepository(Banco banco) {
        this.banco = banco;
    }

    /**
     * RN22 pela chave primaria: o INSERT OR IGNORE nao grava nada se o codigo ja existe,
     * a linha afetada da zero. Assim quem barra e o banco, nao so o codigo Java.
     */
    @Override
    public void registrar(CodigoLancamento codigo, LocalDateTime dataHora) {
        try (PreparedStatement ps = banco.conexao().prepareStatement(
                "INSERT OR IGNORE INTO lancamento (codigo, data_hora) VALUES (?, ?)")) {
            ps.setString(1, codigo.valor());
            ps.setString(2, dataHora.toString());
            if (ps.executeUpdate() == 0) {
                throw new LancamentoDuplicadoException(codigo.valor());
            }
        } catch (SQLException e) {
            throw new ErroDePersistencia("registrar o lançamento", e);
        }
    }
}
