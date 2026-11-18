package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.Cenario;
import br.ufpb.aps.granja.model.ArquivoExportado;
import br.ufpb.aps.granja.model.CategoriaChamado;
import br.ufpb.aps.granja.model.CategoriaFinanceira;
import br.ufpb.aps.granja.model.Chamado;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.ConjuntoExportavel;
import br.ufpb.aps.granja.model.CopiaDeSeguranca;
import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.StatusChamado;
import br.ufpb.aps.granja.model.excecao.AcessoNegadoException;
import br.ufpb.aps.granja.model.excecao.LancamentoDuplicadoException;
import br.ufpb.aps.granja.model.excecao.NaoEncontradoException;
import br.ufpb.aps.granja.model.excecao.RegraDeNegocioException;
import br.ufpb.aps.granja.persistencia.Banco;
import br.ufpb.aps.granja.Composicao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

import static br.ufpb.aps.granja.Cenario.AGORA;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** UC28 (backup), UC29 (exportacao) e UC30 (chamado de suporte). */
class SuporteDadosTest {

    private static final LocalDate HOJE = AGORA.toLocalDate();

    @TempDir
    Path pasta;

    private Cenario c;
    private Composicao.Aplicacao app;

    @BeforeEach
    void preparar() {
        c = new Cenario();
        // o Cenario usa a pasta padrao; aqui a aplicacao e refeita com uma pasta de teste, no mesmo banco
        app = Composicao.criar(c.banco, pasta);
    }

    @AfterEach
    void fechar() {
        c.close();
    }

    private static CodigoLancamento cod(String v) {
        return new CodigoLancamento(v);
    }

    // ---------- UC30 ----------

    @Test
    void qualquerPerfilAbreChamadoEVeSoOsSeus() {
        Chamado doFuncionario = app.chamados().abrir(c.funcionario, CategoriaChamado.DUVIDA, "Como lançar ração?",
                "Não achei o botão de registrar a alimentação.", cod("CH-1"), AGORA);
        app.chamados().abrir(c.tecnico, CategoriaChamado.PROBLEMA, "Erro ao salvar", "A tela travou ao salvar o lote.", cod("CH-2"), AGORA);

        assertEquals(StatusChamado.ABERTO, doFuncionario.status());
        assertEquals(c.funcionario.nome(), doFuncionario.solicitanteNome());
        assertEquals(1, app.chamados().meus(c.funcionario).size());
        assertEquals(1, app.chamados().meus(c.tecnico).size());
        assertEquals(2, app.chamados().todos(c.gestor).size());
    }

    @Test
    void mesmoCodigoNaoAbreDoisChamados() {
        app.chamados().abrir(c.funcionario, CategoriaChamado.DUVIDA, "Dúvida", "Texto com mais de dez letras.", cod("CH-1"), AGORA);

        assertThrows(LancamentoDuplicadoException.class, () -> app.chamados()
                .abrir(c.funcionario, CategoriaChamado.DUVIDA, "Dúvida", "Texto com mais de dez letras.", cod("CH-1"), AGORA));
        assertEquals(1, app.chamados().todos(c.gestor).size());
    }

    @Test
    void assuntoEDescricaoTemTamanhoMinimoEMaximo() {
        assertThrows(IllegalArgumentException.class, () -> app.chamados()
                .abrir(c.gestor, CategoriaChamado.DUVIDA, "ab", "Descrição suficiente.", cod("A"), AGORA));
        assertThrows(IllegalArgumentException.class, () -> app.chamados()
                .abrir(c.gestor, CategoriaChamado.DUVIDA, "Assunto ok", "curta", cod("B"), AGORA));
        assertThrows(IllegalArgumentException.class, () -> app.chamados()
                .abrir(c.gestor, CategoriaChamado.DUVIDA, "Assunto ok", "x".repeat(1001), cod("C"), AGORA));
    }

    @Test
    void gestorRespondeEResolveMasNaoReabre() {
        Chamado aberto = app.chamados().abrir(c.funcionario, CategoriaChamado.PROBLEMA, "Não carrega",
                "A lista de lotes não carrega no celular.", cod("CH-1"), AGORA);

        Chamado andamento = app.chamados().responder(c.gestor, aberto.id(), StatusChamado.EM_ATENDIMENTO, "Estou vendo.", AGORA);
        assertEquals(StatusChamado.EM_ATENDIMENTO, andamento.status());
        assertThrows(RegraDeNegocioException.class, () -> app.chamados().responder(c.gestor, aberto.id(), StatusChamado.RESOLVIDO, "  ", AGORA));
        Chamado resolvido = app.chamados().responder(c.gestor, aberto.id(), StatusChamado.RESOLVIDO, "Corrigido.", AGORA);

        assertEquals(StatusChamado.RESOLVIDO, resolvido.status());
        assertEquals("Corrigido.", resolvido.resposta());
        assertThrows(RegraDeNegocioException.class, () -> app.chamados().responder(c.gestor, aberto.id(), StatusChamado.EM_ATENDIMENTO, "x", AGORA));
        assertThrows(NaoEncontradoException.class, () -> app.chamados().responder(c.gestor, 999, StatusChamado.RESOLVIDO, "x", AGORA));
    }

    @Test
    void soOGestorVeTodosOsChamadosEResponde() {
        Chamado aberto = app.chamados().abrir(c.tecnico, CategoriaChamado.SUGESTAO, "Ideia", "Poderia ter um modo escuro.", cod("CH-1"), AGORA);

        assertThrows(AcessoNegadoException.class, () -> app.chamados().todos(c.tecnico));
        assertThrows(AcessoNegadoException.class, () -> app.chamados().responder(c.funcionario, aberto.id(), StatusChamado.RESOLVIDO, "x", AGORA));
    }

    // ---------- UC29 ----------

    private String csv(ArquivoExportado a) {
        // tira o BOM
        return new String(a.conteudo(), 3, a.conteudo().length - 3, StandardCharsets.UTF_8);
    }

    @Test
    void exportaFinanceiroEmCsvParaOExcel() {
        app.financeiro().registrarDespesa(c.gestor, CategoriaFinanceira.ENERGIA_AGUA, "Luz; \"conta\" de novembro", 12_345, HOJE, null, cod("D-1"), AGORA);
        app.financeiro().registrarReceita(c.gestor, CategoriaFinanceira.VENDA_OVOS, "Ovos", 50_000, HOJE.minusDays(40), null, cod("R-1"), AGORA);

        ArquivoExportado arquivo = app.exportacao().exportar(c.gestor, ConjuntoExportavel.FINANCEIRO, HOJE.minusDays(10), HOJE, AGORA);

        assertEquals(1, arquivo.linhas());
        assertEquals("granja-financeiro-" + HOJE.minusDays(10) + "-a-" + HOJE + ".csv", arquivo.nome());
        assertEquals((byte) 0xEF, arquivo.conteudo()[0]);
        assertEquals("Data;Tipo;Categoria;Descrição;Valor (R$);Lote\r\n"
                + HOJE + ";Despesa;Energia e água;\"Luz; \"\"conta\"\" de novembro\";123,45;\r\n", csv(arquivo));
    }

    @Test
    void exportaLotesEEstoqueSemPrecisarDePeriodo() {
        c.app.lotes().cadastrar(c.tecnico, cod("L-1"), Especie.POEDEIRA, Quantidade.de(100), AGORA);

        ArquivoExportado lotes = app.exportacao().exportar(c.gestor, ConjuntoExportavel.LOTES, HOJE.plusDays(5), HOJE, AGORA);
        ArquivoExportado estoque = app.exportacao().exportar(c.gestor, ConjuntoExportavel.ESTOQUE, HOJE, HOJE, AGORA);

        assertEquals(1, lotes.linhas());
        assertTrue(csv(lotes).startsWith("Código;Espécie;Quantidade inicial;Alojamento;Idade inicial (dias);Saldo;Situação\r\n"));
        assertTrue(csv(lotes).contains(";Poedeira;100;"));
        assertEquals("granja-lotes-" + HOJE + ".csv", lotes.nome());
        assertEquals(0, estoque.linhas());
        assertTrue(csv(estoque).startsWith("Insumo;Categoria;"));
    }

    @Test
    void celulaQueParecesFormulaGanhaApostrofo() {
        app.financeiro().registrarDespesa(c.gestor, CategoriaFinanceira.OUTRA_DESPESA, "=HYPERLINK(\"http://x\")", 100, HOJE, null, cod("D-1"), AGORA);

        String saida = csv(app.exportacao().exportar(c.gestor, ConjuntoExportavel.FINANCEIRO, HOJE, HOJE, AGORA));

        assertTrue(saida.contains("\"'=HYPERLINK(\"\"http://x\"\")\""), saida);
    }

    @Test
    void exportacaoRecusaPeriodoInvertidoEPerfilSemAcesso() {
        assertThrows(IllegalArgumentException.class, () -> app.exportacao().exportar(c.gestor, ConjuntoExportavel.PRODUCAO, HOJE, HOJE.minusDays(1), AGORA));
        assertThrows(AcessoNegadoException.class, () -> app.exportacao().exportar(c.tecnico, ConjuntoExportavel.LOTES, HOJE, HOJE, AGORA));
        assertThrows(NaoEncontradoException.class, () -> ConjuntoExportavel.daChave("senhas"));
    }

    // ---------- UC28 ----------

    @Test
    void backupGeraUmBancoQueAbreELeOsMesmosDados() throws Exception {
        c.app.lotes().cadastrar(c.tecnico, cod("L-1"), Especie.FRANGO_CORTE, Quantidade.de(250), AGORA);

        CopiaDeSeguranca copia = app.backups().criar(c.gestor, AGORA);

        assertEquals("granja-20261117-090000.db", copia.nome());
        assertTrue(copia.tamanhoBytes() > 0);
        assertEquals(AGORA, copia.criadaEm());
        try (Connection conexao = DriverManager.getConnection("jdbc:sqlite:" + pasta.resolve(copia.nome()));
             ResultSet rs = conexao.createStatement().executeQuery("SELECT quantidade_inicial FROM lote")) {
            assertTrue(rs.next());
            assertEquals(250, rs.getInt(1));
        }
    }

    @Test
    void duasCopiasNoMesmoSegundoNaoSeSobrescrevemEListaVemDaMaisNovaParaAMaisAntiga() {
        CopiaDeSeguranca a = app.backups().criar(c.gestor, AGORA);
        CopiaDeSeguranca b = app.backups().criar(c.gestor, AGORA);
        CopiaDeSeguranca depois = app.backups().criar(c.gestor, AGORA.plusMinutes(5));

        assertEquals("granja-20261117-090000-2.db", b.nome());
        List<String> nomes = app.backups().listar(c.gestor).stream().map(CopiaDeSeguranca::nome).toList();
        assertEquals(List.of(depois.nome(), b.nome(), a.nome()), nomes);
    }

    @Test
    void baixarDevolveOArquivoEOutrosNomesSaoRecusados() throws Exception {
        CopiaDeSeguranca copia = app.backups().criar(c.gestor, AGORA);
        Files.writeString(pasta.resolve("segredo.txt"), "nao");

        assertArrayEquals(Files.readAllBytes(pasta.resolve(copia.nome())), app.backups().ler(c.gestor, copia.nome(), AGORA));
        assertThrows(NaoEncontradoException.class, () -> app.backups().ler(c.gestor, "../segredo.txt", AGORA));
        assertThrows(NaoEncontradoException.class, () -> app.backups().ler(c.gestor, "segredo.txt", AGORA));
        assertThrows(NaoEncontradoException.class, () -> app.backups().ler(c.gestor, "granja-20200101-000000.db", AGORA));
        assertFalse(app.backups().listar(c.gestor).stream().anyMatch(x -> x.nome().equals("segredo.txt")));
    }

    @Test
    void soOGestorMexeEmBackup() {
        assertThrows(AcessoNegadoException.class, () -> app.backups().criar(c.tecnico, AGORA));
        assertThrows(AcessoNegadoException.class, () -> app.backups().listar(c.funcionario));
        assertThrows(AcessoNegadoException.class, () -> app.backups().ler(c.tecnico, "granja-20261117-090000.db", AGORA));
    }

    @Test
    void listaVaziaQuandoAindaNaoHaPasta() {
        Banco outro = Banco.memoria();
        try {
            assertTrue(Composicao.criar(outro, pasta.resolve("nao-existe")).backups().listar(c.gestor).isEmpty());
        } finally {
            outro.close();
        }
    }
}
