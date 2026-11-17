package br.ufpb.aps.granja.persistencia;

import br.ufpb.aps.granja.Cenario;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.MovimentoLote;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.excecao.LancamentoDuplicadoException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static br.ufpb.aps.granja.Cenario.AGORA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsistenciaBancoTest {

    @Test
    void saldoGravadoBateComOHistoricoDepoisDeVariasOperacoes() {
        try (Cenario c = new Cenario()) {
            Lote lote = c.app.lotes().cadastrar(c.tecnico, new CodigoLancamento("L-1"),
                    Especie.FRANGO_CORTE, Quantidade.de(100), AGORA);
            c.app.lotes().registrarMortalidade(c.funcionario, lote.id(), 3, null, new CodigoLancamento("M-1"), AGORA);
            c.app.lotes().registrarMortalidade(c.funcionario, lote.id(), 2, null, new CodigoLancamento("M-2"), AGORA);

            // le do banco de novo com repositorio novo, nada de reaproveitar objeto da memoria.
            SqliteLoteRepository relido = new SqliteLoteRepository(c.banco);
            Lote doBanco = relido.buscarPorId(lote.id()).orElseThrow();
            List<MovimentoLote> historico = relido.historico(lote.id());

            assertEquals(95, doBanco.saldo().valor());
            assertEquals(3, historico.size());
            assertTrue(doBanco.consistenteCom(historico));
        }
    }

    @Test
    void mesmoCodigoDeLancamentoNaoSeRepeteNoBanco() {
        try (Cenario c = new Cenario()) {
            SqliteLancamentoRepository lancamentos = new SqliteLancamentoRepository(c.banco);
            lancamentos.registrar(new CodigoLancamento("X-1"), AGORA);
            assertThrows(LancamentoDuplicadoException.class,
                    () -> lancamentos.registrar(new CodigoLancamento("X-1"), AGORA));
        }
    }
}
