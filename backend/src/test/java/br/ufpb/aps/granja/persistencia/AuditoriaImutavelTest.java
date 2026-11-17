package br.ufpb.aps.granja.persistencia;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RN15: nem com SQL direto da pra alterar ou apagar o log. Quem barra e o trigger do banco. */
class AuditoriaImutavelTest {

    @Test
    void updateEDeleteNoLogSaoBarradosPeloBanco() throws SQLException {
        try (Banco banco = Banco.memoria()) {
            SqliteAuditoriaRepository auditoria = new SqliteAuditoriaRepository(banco);
            auditoria.registrar("TESTE", "linha original", LocalDateTime.of(2026, 11, 17, 9, 0));

            try (Statement st = banco.conexao().createStatement()) {
                SQLException update = assertThrows(SQLException.class,
                        () -> st.executeUpdate("UPDATE auditoria SET detalhe = 'adulterado'"));
                assertTrue(update.getMessage().contains("imutável"));
                assertThrows(SQLException.class, () -> st.executeUpdate("DELETE FROM auditoria"));
            }

            assertEquals(1, auditoria.listar().size());
            assertEquals("linha original", auditoria.listar().get(0).detalhe());
        }
    }
}
