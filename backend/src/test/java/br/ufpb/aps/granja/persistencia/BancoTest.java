package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.Quantidade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BancoTest {

    private static final LocalDate ALOJAMENTO = LocalDate.of(2026, 11, 17);

    @Test
    void bancoEmMemoriaComecaSemDados() {
        try (Banco banco = Banco.memoria()) {
            assertTrue(new SqliteLoteRepository(banco).listar().isEmpty());
        }
    }

    @Test
    void executarConfirmaQuandoOTrabalhoTermina() {
        try (Banco banco = Banco.memoria()) {
            SqliteLoteRepository lotes = new SqliteLoteRepository(banco);
            banco.executar(() -> lotes.inserir("LOT-2026-001", Especie.FRANGO_CORTE, Quantidade.de(10), ALOJAMENTO, 0));
            assertEquals(1, lotes.listar().size());
        }
    }

    @Test
    void executarDesfazTudoQuandoOTrabalhoLancaExcecao() {
        try (Banco banco = Banco.memoria()) {
            SqliteLoteRepository lotes = new SqliteLoteRepository(banco);
            assertThrows(IllegalStateException.class, () -> banco.executar(() -> {
                lotes.inserir("LOT-2026-001", Especie.POEDEIRA, Quantidade.de(10), ALOJAMENTO, 0);
                throw new IllegalStateException("falha no meio da operação");
            }));
            assertTrue(lotes.listar().isEmpty());
        }
    }

    @Test
    void arquivoPersisteOsDadosEntreAberturas(@TempDir Path pasta) {
        String caminho = pasta.resolve("granja.db").toString();
        try (Banco banco = Banco.arquivo(caminho)) {
            new SqliteLoteRepository(banco).inserir("LOT-2026-001", Especie.FRANGO_CORTE, Quantidade.de(10), ALOJAMENTO, 0);
        }
        try (Banco banco = Banco.arquivo(caminho)) {
            List<Lote> lotes = new SqliteLoteRepository(banco).listar();
            assertEquals(1, lotes.size());
            assertEquals("LOT-2026-001", lotes.get(0).codigo());
        }
    }
}
