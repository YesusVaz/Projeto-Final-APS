package br.ufpb.aps.granja;

import br.ufpb.aps.granja.model.CategoriaChamado;
import br.ufpb.aps.granja.model.CategoriaFinanceira;
import br.ufpb.aps.granja.model.CategoriaInsumo;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.Fornecedor;
import br.ufpb.aps.granja.model.Insumo;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.MotivoEncerramento;
import br.ufpb.aps.granja.model.OcorrenciaSanitaria;
import br.ufpb.aps.granja.model.OrigemMovimento;
import br.ufpb.aps.granja.model.Perfil;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.StatusChamado;
import br.ufpb.aps.granja.model.TipoHigienizacao;
import br.ufpb.aps.granja.model.TipoMovimentoEstoque;
import br.ufpb.aps.granja.model.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Dados de exemplo para testar o sistema (liga com o argumento --demo ou a variavel GRANJA_DEMO=1).
 * Tudo passa pelos servicos de verdade, com as mesmas regras de sempre (saldo, perfil, codigo de lancamento),
 * so que com datas espalhadas pelos ultimos meses, para os graficos e relatorios terem o que mostrar.
 * So roda se ainda nao existir nenhum lote, entao reiniciar com um banco em arquivo nao duplica nada.
 */
final class DadosDeDemonstracao {

    private final Composicao.Aplicacao app;
    private final LocalDate hoje;
    private final Random acaso = new Random(2026);
    private int contador = 0;

    private Usuario gestor;
    private Usuario tecnico;
    private Usuario funcionario;

    private DadosDeDemonstracao(Composicao.Aplicacao app, LocalDate hoje) {
        this.app = app;
        this.hoje = hoje;
    }

    /** Cria os dados se o banco estiver sem lotes. Devolve true se criou. */
    static boolean semear(Composicao.Aplicacao app) {
        return semear(app, LocalDate.now());
    }

    static boolean semear(Composicao.Aplicacao app, LocalDate hoje) {
        Usuario gestor = app.usuariosRepositorio().buscarPorLogin("gestor").orElse(null);
        if (gestor == null || !app.lotes().listar(gestor).isEmpty()) {
            return false;
        }
        new DadosDeDemonstracao(app, hoje).criar();
        return true;
    }

    private void criar() {
        gestor = app.usuariosRepositorio().buscarPorLogin("gestor").orElseThrow();
        tecnico = app.usuariosRepositorio().buscarPorLogin("tecnico").orElseThrow();
        funcionario = app.usuariosRepositorio().buscarPorLogin("funcionario").orElseThrow();

        criarUsuarios();
        long[] fornecedores = criarFornecedores();
        Insumos in = criarInsumos(fornecedores);
        criarDespesasERecebimentosFixos();

        Lote a = lote(Especie.FRANGO_CORTE, 2500, 38, 1);
        Lote b = lote(Especie.FRANGO_CORTE, 2000, 20, 1);
        Lote c = lote(Especie.POEDEIRA, 1200, 150, 120);
        Lote d = lote(Especie.POEDEIRA, 800, 90, 126);
        Lote e = lote(Especie.FRANGO_CORTE, 2000, 75, 1);

        frango(a, in, 38, 2500, true);
        frango(b, in, 20, 2000, false);
        poedeira(c, in, 1200, 45);
        poedeira(d, in, 800, 30);
        loteEncerrado(e, in, 75, 2000);

        sanidade(a, b, c, d, in);
        manejo(a, b, c, d);
        ajustarEstoqueBaixo(in);
        chamados();
    }

    // ---------- usuarios ----------

    private void criarUsuarios() {
        app.autenticacao().cadastrar("maria", "maria@granja.local", "Maria Souza", "demo12345", Perfil.TECNICO);
        app.autenticacao().cadastrar("joao", "joao@granja.local", "João Lima", "demo12345", Perfil.FUNCIONARIO);
        Usuario ana = app.autenticacao().cadastrar("ana", "ana@granja.local", "Ana Costa", "demo12345", Perfil.FUNCIONARIO);
        app.autenticacao().cadastrar("carlos", "carlos@granja.local", "Carlos Pereira", "demo12345", Perfil.FUNCIONARIO);
        // uma conta inativa, para testar o login recusado e o botao de reativar
        app.usuarios().alterarStatus(gestor, ana.id(), false, agora(dia(40), 9));
    }

    // ---------- fornecedores e estoque ----------

    private long[] criarFornecedores() {
        LocalDateTime t = agora(dia(120), 9);
        Fornecedor f1 = app.fornecedores().cadastrar(gestor, "Agropecuária Boa Safra", "11.222.333/0001-81",
                "(83) 3222-1010", "vendas@boasafra.example", t);
        Fornecedor f2 = app.fornecedores().cadastrar(gestor, "Nutrição Animal Nordeste", "11.444.777/0001-61",
                "(83) 3333-2020", "contato@nutrinordeste.example", t);
        Fornecedor f3 = app.fornecedores().cadastrar(gestor, "Vet Campo Saúde Animal", "45.723.174/0001-10",
                "(83) 98888-3030", "pedidos@vetcampo.example", t);
        Fornecedor f4 = app.fornecedores().cadastrar(gestor, "José Almeida (maravalha)", "529.982.247-25",
                "(83) 99999-4040", "jose@maravalha.example", t);
        return new long[] {f1.id(), f2.id(), f3.id(), f4.id()};
    }

    private record Insumos(Insumo racaoInicial, Insumo racaoCrescimento, Insumo racaoPostura, Insumo newcastle,
                           Insumo gumboro, Insumo antibiotico, Insumo vitaminas, Insumo maravalha) {
    }

    private Insumos criarInsumos(long[] f) {
        var e = app.estoque();
        Insumo ri = e.cadastrarInsumo(gestor, "Ração inicial", CategoriaInsumo.RACAO, "kg", Quantidade.de(300));
        Insumo rc = e.cadastrarInsumo(gestor, "Ração de crescimento", CategoriaInsumo.RACAO, "kg", Quantidade.de(400));
        Insumo rp = e.cadastrarInsumo(gestor, "Ração de postura", CategoriaInsumo.RACAO, "kg", Quantidade.de(500));
        Insumo nc = e.cadastrarInsumo(gestor, "Vacina Newcastle", CategoriaInsumo.VACINA, "dose", Quantidade.de(1000));
        Insumo gb = e.cadastrarInsumo(gestor, "Vacina Gumboro", CategoriaInsumo.VACINA, "dose", Quantidade.de(800));
        Insumo an = e.cadastrarInsumo(gestor, "Enrofloxacina 10%", CategoriaInsumo.MEDICAMENTO, "frasco", Quantidade.de(10));
        Insumo vi = e.cadastrarInsumo(gestor, "Vitaminas e minerais", CategoriaInsumo.MEDICAMENTO, "kg", Quantidade.de(5));
        Insumo ma = e.cadastrarInsumo(gestor, "Maravalha", CategoriaInsumo.MATERIAL, "fardo", Quantidade.de(20));

        // compras: entram no estoque e viram despesa (datas espalhadas)
        compra(ri, f[1], 4500, 380, 80);
        compra(rc, f[1], 6000, 320, 70);
        compra(rc, f[1], 8000, 330, 40);
        compra(rp, f[0], 6000, 310, 100);
        compra(rp, f[0], 5000, 315, 45);
        compra(nc, f[2], 4000, 45, 90);
        compra(nc, f[2], 3000, 47, 30);
        compra(gb, f[2], 6000, 40, 60);
        compra(an, f[2], 30, 4200, 25);
        compra(vi, f[2], 40, 1850, 25);
        compra(ma, f[3], 40, 1800, 60);
        return new Insumos(ri, rc, rp, nc, gb, an, vi, ma);
    }

    private void compra(Insumo insumo, long fornecedor, int quantidade, long valorUnitarioCentavos, int diasAtras) {
        LocalDate data = dia(diasAtras);
        app.financeiro().registrarCompra(gestor, insumo.id(), fornecedor, quantidade, quantidade * valorUnitarioCentavos,
                data, codigo(), agora(data, 10));
    }

    // ---------- financeiro fixo (6 meses) ----------

    private void criarDespesasERecebimentosFixos() {
        for (int mes = 0; mes < 6; mes++) {
            int diasAtras = 5 + mes * 30;
            LocalDate data = dia(diasAtras);
            LocalDateTime t = agora(data, 15);
            app.financeiro().registrarDespesa(gestor, CategoriaFinanceira.MAO_DE_OBRA, "Folha dos funcionários",
                    4_800_00L + mes * 120_00L, data, null, codigo(), t);
            app.financeiro().registrarDespesa(gestor, CategoriaFinanceira.ENERGIA_AGUA, "Energia e água do mês",
                    1_150_00L + acaso.nextInt(300_00), data, null, codigo(), t);
            if (mes % 2 == 0) {
                app.financeiro().registrarDespesa(gestor, CategoriaFinanceira.MANUTENCAO, "Manutenção de comedouros e ventiladores",
                        650_00L + acaso.nextInt(400_00), data, null, codigo(), t);
            }
            if (mes % 3 == 1) {
                app.financeiro().registrarDespesa(gestor, CategoriaFinanceira.TRANSPORTE, "Frete de insumos",
                        480_00L, data, null, codigo(), t);
            }
        }
        // vendas de ovos de lotes de antes do periodo, para os meses mais antigos do grafico
        for (int mes = 2; mes < 6; mes++) {
            LocalDate data = dia(12 + mes * 30);
            app.financeiro().registrarReceita(gestor, CategoriaFinanceira.VENDA_OVOS, "Venda de ovos (lote anterior)",
                    9_500_00L + acaso.nextInt(2_500_00), data, null, codigo(), agora(data, 17));
        }
    }

    // ---------- lotes ----------

    private Lote lote(Especie especie, int aves, int diasAtras, int idadeInicial) {
        LocalDate chegada = dia(diasAtras);
        return app.lotes().cadastrar(gestor, codigo(), especie, Quantidade.de(aves), chegada, idadeInicial,
                agora(chegada, 7));
    }

    /** Frango de corte: racao diaria, mortes pequenas, pesagem semanal. O lote A tem um dia ruim (alerta). */
    private void frango(Lote lote, Insumos in, int dias, int aves, boolean diaRuimDeAtencao) {
        int vivas = aves;
        for (int d = dias - 1; d >= 0; d--) {
            LocalDate data = dia(d);
            int idade = dias - d;
            int mortes = 2 + acaso.nextInt(Math.max(3, aves / 200));
            if (diaRuimDeAtencao && d == 8) {
                mortes = aves * 36 / 1000; // 3,6% no dia: passa do limite de atencao
            }
            if (!diaRuimDeAtencao && d == 3) {
                mortes = aves * 70 / 1000; // 7% no dia: critico
            }
            mortes = Math.min(mortes, vivas - 1);
            app.lotes().registrarMortalidade(funcionario, lote.id(), mortes,
                    mortes > aves / 40 ? "Calor forte no galpão" : "", codigo(), agora(data, 8));
            vivas -= mortes;

            Insumo racao = idade <= 14 ? in.racaoInicial() : in.racaoCrescimento();
            int kg = Math.max(20, (int) Math.round(vivas * (idade <= 14 ? 0.03 + idade * 0.0035 : 0.09 + (idade - 14) * 0.0016)));
            app.alimentacao().registrar(funcionario, lote.id(), racao.id(), kg, data, codigo(), agora(data, 9));

            if (idade % 7 == 0) {
                int g = (int) (idade * idade * 1.45 + idade * 22 + 40) + acaso.nextInt(40);
                app.producao().registrarPesagem(tecnico, lote.id(), g, data, codigo(), agora(data, 11));
            }
        }
    }

    /** Poedeira: racao a cada dois dias, postura todo dia (taxa entre 78% e 90%), mortes raras. */
    private void poedeira(Lote lote, Insumos in, int aves, int diasComRegistro) {
        int vivas = aves;
        for (int d = diasComRegistro - 1; d >= 0; d--) {
            LocalDate data = dia(d);
            if (acaso.nextInt(4) == 0) {
                int mortes = 1 + acaso.nextInt(3);
                app.lotes().registrarMortalidade(funcionario, lote.id(), mortes, "", codigo(), agora(data, 8));
                vivas -= mortes;
            }
            if (d % 2 == 0) {
                int kg = (int) Math.round(vivas * 0.115 * 2);
                app.alimentacao().registrar(funcionario, lote.id(), in.racaoPostura().id(), kg, data, codigo(), agora(data, 9));
            }
            double taxa = 0.78 + acaso.nextDouble() * 0.12;
            // fim de semana um pouco abaixo, para o grafico nao ficar uma linha reta
            if (data.getDayOfWeek().getValue() >= 6) {
                taxa -= 0.03;
            }
            int ovos = (int) Math.round(vivas * taxa);
            app.producao().registrarPostura(funcionario, lote.id(), ovos, data, codigo(), agora(data, 16));
        }
        // venda semanal de ovos do lote nos ultimos 40 dias
        for (int semana = 0; semana < 6; semana++) {
            LocalDate data = dia(2 + semana * 7);
            app.financeiro().registrarReceita(gestor, CategoriaFinanceira.VENDA_OVOS,
                    "Venda de ovos, semana " + (6 - semana), 3_400_00L + acaso.nextInt(2_000_00), data,
                    lote.id(), codigo(), agora(data, 17));
        }
    }

    /** Lote que ja foi vendido: historico curto, receita da venda e encerramento. */
    private void loteEncerrado(Lote lote, Insumos in, int diasDeLote, int aves) {
        int vivas = aves;
        for (int d = diasDeLote - 1; d >= 12; d -= 7) {
            LocalDate data = dia(d);
            int mortes = 8 + acaso.nextInt(20);
            app.lotes().registrarMortalidade(funcionario, lote.id(), mortes, "", codigo(), agora(data, 8));
            vivas -= mortes;
            int kg = (int) Math.round(vivas * 0.09 * 7);
            app.alimentacao().registrar(funcionario, lote.id(), in.racaoCrescimento().id(), Math.min(kg, 600), data,
                    codigo(), agora(data, 9));
        }
        LocalDate venda = dia(12);
        app.financeiro().registrarReceita(gestor, CategoriaFinanceira.VENDA_AVES, "Venda do lote para o abatedouro",
                41_800_00L, venda, lote.id(), codigo(), agora(venda, 14));
        app.lotes().encerrar(gestor, lote.id(), MotivoEncerramento.VENDA, codigo(), agora(venda, 15));
    }

    // ---------- sanidade e manejo ----------

    private void sanidade(Lote a, Lote b, Lote c, Lote d, Insumos in) {
        var s = app.sanidade();
        vacina(a, in.newcastle(), 2400, 38 - 7);
        vacina(a, in.gumboro(), 2400, 38 - 14);
        vacina(b, in.newcastle(), 1900, 20 - 7);
        vacina(b, in.gumboro(), 1900, 20 - 14);
        vacina(c, in.newcastle(), 1100, 20);
        vacina(d, in.newcastle(), 780, 15);

        // ocorrencia ainda aberta no lote A, com tratamento em andamento (aparece nos alertas)
        LocalDate abriu = dia(6);
        OcorrenciaSanitaria o1 = s.abrirOcorrencia(tecnico, a.id(),
                "Aves apáticas e com diarreia no galpão 2", abriu, agora(abriu, 8));
        s.registrarTratamento(tecnico, o1.id(), in.antibiotico().id(), 3, abriu, "Primeira dose", codigo(), agora(abriu, 9));
        s.registrarTratamento(tecnico, o1.id(), in.antibiotico().id(), 3, dia(5), "Segunda dose", codigo(), agora(dia(5), 9));

        // ocorrencia resolvida na poedeira C
        LocalDate abriuC = dia(14);
        OcorrenciaSanitaria o2 = s.abrirOcorrencia(tecnico, c.id(), "Queda de postura e casca fina", abriuC, agora(abriuC, 8));
        s.registrarTratamento(tecnico, o2.id(), in.vitaminas().id(), 5, abriuC, "Misturado na ração", codigo(), agora(abriuC, 9));
        s.encerrarOcorrencia(tecnico, o2.id(), dia(10), agora(dia(10), 16));
    }

    private void vacina(Lote lote, Insumo vacina, int doses, int diasAtras) {
        LocalDate data = dia(diasAtras);
        app.sanidade().registrarVacinacao(tecnico, lote.id(), vacina.id(), doses, data, data.plusDays(21), "", codigo(),
                agora(data, 8));
    }

    private void manejo(Lote a, Lote b, Lote c, Lote d) {
        var m = app.manejo();
        for (Lote lote : new Lote[] {a, b, c, d}) {
            LocalDate base = lote == a ? dia(38) : lote == b ? dia(20) : lote == c ? dia(60) : dia(40);
            m.registrarHigienizacao(funcionario, lote.id(), TipoHigienizacao.LIMPEZA_GALPAO, base, "", codigo(), agora(base, 8));
            m.registrarHigienizacao(funcionario, lote.id(), TipoHigienizacao.DESINFECCAO, base.plusDays(1), "", codigo(),
                    agora(base.plusDays(1), 8));
        }
        m.registrarHigienizacao(funcionario, a.id(), TipoHigienizacao.TROCA_DE_CAMA, dia(12), "Cama úmida", codigo(), agora(dia(12), 8));
        m.registrarHigienizacao(funcionario, c.id(), TipoHigienizacao.LIMPEZA_BEBEDOUROS_COMEDOUROS, dia(4), "", codigo(), agora(dia(4), 8));
        m.registrarPrograma(tecnico, c.id(), 14, 20, dia(100), "Início da postura", codigo(), agora(dia(100), 9));
        m.registrarPrograma(tecnico, c.id(), 16, 25, dia(30), "Aumento gradual da luz", codigo(), agora(dia(30), 9));
        m.registrarPrograma(tecnico, d.id(), 16, 25, dia(45), "", codigo(), agora(dia(45), 9));
    }

    /** Deixa alguns insumos abaixo do minimo, para o alerta de estoque ter o que mostrar. */
    private void ajustarEstoqueBaixo(Insumos in) {
        reduzirAte(in.maravalha(), 8);
        reduzirAte(in.antibiotico(), 7);
    }

    private void reduzirAte(Insumo insumo, int alvo) {
        int saldo = app.estoque().listar(gestor).stream().filter(i -> i.id().equals(insumo.id())).findFirst()
                .orElseThrow().saldo().valor();
        if (saldo > alvo) {
            app.estoque().movimentar(funcionario, insumo.id(), TipoMovimentoEstoque.SAIDA, saldo - alvo,
                    OrigemMovimento.USO_GERAL, codigo(), agora(dia(1), 10));
        }
    }

    // ---------- suporte ----------

    private void chamados() {
        var c = app.chamados();
        var c1 = c.abrir(funcionario, CategoriaChamado.DUVIDA, "Como registro a baixa de um lote?",
                "Vendi parte das aves do lote e não sei se uso mortalidade ou outra opção.", codigo(), agora(dia(9), 10));
        c.responder(gestor, c1.id(), StatusChamado.RESOLVIDO,
                "Use \"Baixa\" no detalhe do lote e escolha o motivo (venda, descarte ou transferência).", agora(dia(8), 9));
        var c2 = c.abrir(tecnico, CategoriaChamado.PROBLEMA, "Vacinação não aparece no histórico",
                "Registrei a vacina de ontem e ela não apareceu na aba de sanidade do lote.", codigo(), agora(dia(3), 14));
        c.responder(gestor, c2.id(), StatusChamado.EM_ATENDIMENTO, "Estou verificando, volto com uma resposta.", agora(dia(2), 9));
        c.abrir(funcionario, CategoriaChamado.SUGESTAO, "Atalho para registrar ração",
                "Seria bom ter um botão de registrar ração direto na tela inicial do celular.", codigo(), agora(dia(1), 16));
    }

    // ---------- utilidades ----------

    private LocalDate dia(int diasAtras) {
        return hoje.minusDays(diasAtras);
    }

    private static LocalDateTime agora(LocalDate data, int hora) {
        return data.atTime(hora, 0);
    }

    private CodigoLancamento codigo() {
        contador++;
        return new CodigoLancamento("demo-" + contador);
    }
}
