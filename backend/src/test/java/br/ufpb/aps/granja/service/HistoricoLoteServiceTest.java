package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.Cenario;
import br.ufpb.aps.granja.model.CategoriaInsumo;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.Insumo;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.MotivoEncerramento;
import br.ufpb.aps.granja.model.OcorrenciaSanitaria;
import br.ufpb.aps.granja.model.OrigemMovimento;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.TipoMovimentoEstoque;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static br.ufpb.aps.granja.Cenario.AGORA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** UC17: o historico junta tudo do lote em ordem de data. */
class HistoricoLoteServiceTest {

    private Cenario c;

    @BeforeEach
    void preparar() {
        c = new Cenario();
    }

    @AfterEach
    void fechar() {
        c.close();
    }

    @Test
    void historicoJuntaTodasAsCategoriasEmOrdem() {
        Lote lote = c.app.lotes().cadastrar(c.tecnico, codigo("L-1"), Especie.FRANGO_CORTE, Quantidade.de(100),
                LocalDate.of(2026, 11, 1), 1, AGORA);
        Insumo racao = insumo("Ração inicial", CategoriaInsumo.RACAO, "E-1");
        Insumo vacina = insumo("Marek", CategoriaInsumo.VACINA, "E-2");
        Insumo remedio = insumo("Enrofloxacina", CategoriaInsumo.MEDICAMENTO, "E-3");

        c.app.sanidade().registrarVacinacao(c.tecnico, lote.id(), vacina.id(), 100, LocalDate.of(2026, 11, 2),
                null, null, codigo("V-1"), AGORA);
        c.app.alimentacao().registrar(c.funcionario, lote.id(), racao.id(), 30, LocalDate.of(2026, 11, 3),
                codigo("A-1"), AGORA);
        OcorrenciaSanitaria o = c.app.sanidade().abrirOcorrencia(c.tecnico, lote.id(), "Tosse",
                LocalDate.of(2026, 11, 4), AGORA);
        c.app.sanidade().registrarTratamento(c.tecnico, o.id(), remedio.id(), 2, LocalDate.of(2026, 11, 5), null,
                codigo("T-1"), AGORA);
        c.app.producao().registrarPesagem(c.tecnico, lote.id(), 400, LocalDate.of(2026, 11, 6), codigo("G-1"), AGORA);
        c.app.lotes().registrarMortalidade(c.funcionario, lote.id(), 2, "calor", codigo("M-1"), AGORA);
        c.app.lotes().encerrar(c.tecnico, lote.id(), MotivoEncerramento.VENDA, codigo("F-1"), AGORA);

        HistoricoLoteService.HistoricoCompleto h = c.app.historico().historico(c.funcionario, lote.id());
        List<String> categorias = h.itens().stream().map(HistoricoLoteService.ItemHistorico::categoria).toList();

        assertTrue(h.consistente());
        // a entrada fica no dia do alojamento (01/11), antes de tudo, mesmo cadastrada no dia 17
        assertEquals(List.of("MOVIMENTO", "VACINACAO", "ALIMENTACAO", "OCORRENCIA", "TRATAMENTO", "PRODUCAO",
                "MOVIMENTO", "MOVIMENTO"), categorias);
        assertTrue(h.itens().get(0).descricao().startsWith("Entrada inicial"));
        assertEquals(LocalDate.of(2026, 11, 1), h.itens().get(0).data());
        assertTrue(h.itens().get(2).descricao().contains("Ração inicial"));
        assertTrue(h.itens().get(6).descricao().contains("calor"));
        assertTrue(h.itens().get(7).descricao().contains("venda"));
    }

    private Insumo insumo(String nome, CategoriaInsumo categoria, String codigoEntrada) {
        Insumo insumo = c.app.estoque().cadastrarInsumo(c.gestor, nome, categoria, "un", Quantidade.de(1));
        c.app.estoque().movimentar(c.gestor, insumo.id(), TipoMovimentoEstoque.ENTRADA, 200, OrigemMovimento.COMPRA,
                codigo(codigoEntrada), AGORA);
        return insumo;
    }

    private static CodigoLancamento codigo(String valor) {
        return new CodigoLancamento(valor);
    }
}
