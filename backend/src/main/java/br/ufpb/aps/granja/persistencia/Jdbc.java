package br.ufpb.aps.granja.persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cuida do ritual de JDBC dos repositorios: prepara o comando, liga os parametros, fecha tudo
 * e converte SQLException em ErroDePersistencia. O texto de {@code operacao} entra na mensagem de erro.
 */
final class Jdbc {

    @FunctionalInterface
    interface Mapeador<T> {
        T mapear(ResultSet rs) throws SQLException;
    }

    private final Banco banco;

    Jdbc(Banco banco) {
        this.banco = banco;
    }

    /** INSERT/UPDATE/DELETE. Devolve as linhas afetadas. */
    int executar(String operacao, String sql, Object... parametros) {
        try (PreparedStatement ps = banco.conexao().prepareStatement(sql)) {
            ligar(ps, parametros);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroDePersistencia(operacao, e);
        }
    }

    /** INSERT que devolve o id gerado. */
    long inserir(String operacao, String sql, Object... parametros) {
        try (PreparedStatement ps = banco.conexao().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ligar(ps, parametros);
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return chaves.getLong(1);
            }
        } catch (SQLException e) {
            throw new ErroDePersistencia(operacao, e);
        }
    }

    <T> List<T> listar(String operacao, String sql, Mapeador<T> mapeador, Object... parametros) {
        try (PreparedStatement ps = banco.conexao().prepareStatement(sql)) {
            ligar(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> itens = new ArrayList<>();
                while (rs.next()) {
                    itens.add(mapeador.mapear(rs));
                }
                return itens;
            }
        } catch (SQLException e) {
            throw new ErroDePersistencia(operacao, e);
        }
    }

    <T> Optional<T> buscar(String operacao, String sql, Mapeador<T> mapeador, Object... parametros) {
        try (PreparedStatement ps = banco.conexao().prepareStatement(sql)) {
            ligar(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapeador.mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErroDePersistencia(operacao, e);
        }
    }

    /** SELECT COUNT(*) e afins. */
    long contar(String operacao, String sql, Object... parametros) {
        return buscar(operacao, sql, rs -> rs.getLong(1), parametros).orElse(0L);
    }

    static int comoInteiro(boolean valor) {
        return valor ? 1 : 0;
    }

    private static void ligar(PreparedStatement ps, Object[] parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            ps.setObject(i + 1, parametros[i]);
        }
    }
}
