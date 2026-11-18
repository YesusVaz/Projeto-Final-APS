package br.ufpb.aps.granja;

import br.ufpb.aps.granja.evento.AlertaListener;
import br.ufpb.aps.granja.evento.AuditoriaListener;
import br.ufpb.aps.granja.evento.BarramentoEventos;
import br.ufpb.aps.granja.evento.EventoAuditavel;
import br.ufpb.aps.granja.evento.MortalidadeRegistrada;
import br.ufpb.aps.granja.persistencia.Banco;
import br.ufpb.aps.granja.persistencia.SqliteAlertaRepository;
import br.ufpb.aps.granja.persistencia.SqliteAlimentacaoRepository;
import br.ufpb.aps.granja.persistencia.SqliteAuditoriaRepository;
import br.ufpb.aps.granja.persistencia.SqliteConfiguracaoAlertaRepository;
import br.ufpb.aps.granja.persistencia.SqliteFinanceiroRepository;
import br.ufpb.aps.granja.persistencia.SqliteGestaoRepository;
import br.ufpb.aps.granja.persistencia.SqliteChamadoRepository;
import br.ufpb.aps.granja.persistencia.SqliteExportacaoRepository;
import br.ufpb.aps.granja.persistencia.SqliteFornecedorRepository;
import br.ufpb.aps.granja.persistencia.SqliteInsumoRepository;
import br.ufpb.aps.granja.persistencia.SqliteLancamentoRepository;
import br.ufpb.aps.granja.persistencia.SqliteLoteRepository;
import br.ufpb.aps.granja.persistencia.SqliteManejoRepository;
import br.ufpb.aps.granja.persistencia.SqliteParametrosRepository;
import br.ufpb.aps.granja.persistencia.SqliteProducaoRepository;
import br.ufpb.aps.granja.persistencia.SqliteSanidadeRepository;
import br.ufpb.aps.granja.persistencia.SqliteUsuarioRepository;
import br.ufpb.aps.granja.repository.AlertaRepository;
import br.ufpb.aps.granja.repository.AlimentacaoRepository;
import br.ufpb.aps.granja.repository.AuditoriaRepository;
import br.ufpb.aps.granja.repository.ConfiguracaoAlertaRepository;
import br.ufpb.aps.granja.repository.FinanceiroRepository;
import br.ufpb.aps.granja.repository.FornecedorRepository;
import br.ufpb.aps.granja.repository.InsumoRepository;
import br.ufpb.aps.granja.repository.LancamentoRepository;
import br.ufpb.aps.granja.repository.LoteRepository;
import br.ufpb.aps.granja.repository.ManejoRepository;
import br.ufpb.aps.granja.repository.ProducaoRepository;
import br.ufpb.aps.granja.repository.SanidadeRepository;
import br.ufpb.aps.granja.repository.UsuarioRepository;
import br.ufpb.aps.granja.service.AlertaSanitarioService;
import br.ufpb.aps.granja.service.AlimentacaoService;
import br.ufpb.aps.granja.service.AuditoriaService;
import br.ufpb.aps.granja.service.AutenticacaoService;
import br.ufpb.aps.granja.service.BackupService;
import br.ufpb.aps.granja.service.ChamadoService;
import br.ufpb.aps.granja.service.ConfiguracaoAlertaService;
import br.ufpb.aps.granja.service.ExportacaoService;
import br.ufpb.aps.granja.service.EstoqueService;
import br.ufpb.aps.granja.service.FinanceiroService;
import br.ufpb.aps.granja.service.FornecedorService;
import br.ufpb.aps.granja.service.GestaoService;
import br.ufpb.aps.granja.service.HistoricoLoteService;
import br.ufpb.aps.granja.service.LoteService;
import br.ufpb.aps.granja.service.ManejoService;
import br.ufpb.aps.granja.service.ParametrosService;
import br.ufpb.aps.granja.service.ProducaoService;
import br.ufpb.aps.granja.service.ProdutividadeService;
import br.ufpb.aps.granja.service.SanidadeService;
import br.ufpb.aps.granja.service.UsuarioService;

import java.nio.file.Path;
import java.util.function.UnaryOperator;

/**
 * Raiz de composicao (ADR-002). Monta repositorios, servicos e ouvintes pelo construtor, sem container.
 * Usada pelo Main e pelos testes. O UnaryOperator deixa embrulhar o repositorio de lotes nos testes de falha.
 */
public final class Composicao {

    public record Aplicacao(Banco banco, LoteService lotes, EstoqueService estoque, AlimentacaoService alimentacao,
                            ConfiguracaoAlertaService configuracao, AutenticacaoService autenticacao,
                            UsuarioService usuarios, AuditoriaService consultaAuditoria,
                            FornecedorService fornecedores, SanidadeService sanidade,
                            AlertaSanitarioService alertasSanitarios, ProducaoService producao,
                            ParametrosService parametros, HistoricoLoteService historico,
                            FinanceiroService financeiro, ManejoService manejo,
                            ProdutividadeService produtividade, GestaoService gestao,
                            ChamadoService chamados, ExportacaoService exportacao, BackupService backups,
                            LoteRepository lotesRepositorio, InsumoRepository insumosRepositorio,
                            UsuarioRepository usuariosRepositorio, AuditoriaRepository auditoria,
                            AlertaRepository alertas) {
    }

    private Composicao() {
    }

    /** Pasta dos backups quando ninguem diz outra: "backups", ao lado de onde o servidor roda. */
    public static final Path PASTA_DE_BACKUPS = Path.of("backups");

    public static Aplicacao criar(Banco banco) {
        return criar(banco, PASTA_DE_BACKUPS);
    }

    public static Aplicacao criar(Banco banco, Path pastaDeBackups) {
        return criar(banco, pastaDeBackups, repositorio -> repositorio);
    }

    public static Aplicacao criar(Banco banco, UnaryOperator<LoteRepository> envolverLotes) {
        return criar(banco, PASTA_DE_BACKUPS, envolverLotes);
    }

    public static Aplicacao criar(Banco banco, Path pastaDeBackups, UnaryOperator<LoteRepository> envolverLotes) {
        LoteRepository lotesRepo = envolverLotes.apply(new SqliteLoteRepository(banco));
        InsumoRepository insumosRepo = new SqliteInsumoRepository(banco);
        UsuarioRepository usuariosRepo = new SqliteUsuarioRepository(banco);
        AlimentacaoRepository alimentacoesRepo = new SqliteAlimentacaoRepository(banco);
        LancamentoRepository lancamentosRepo = new SqliteLancamentoRepository(banco);
        AuditoriaRepository auditoria = new SqliteAuditoriaRepository(banco);
        AlertaRepository alertas = new SqliteAlertaRepository(banco);
        ConfiguracaoAlertaRepository configuracaoRepo = new SqliteConfiguracaoAlertaRepository(banco);
        SanidadeRepository sanidadeRepo = new SqliteSanidadeRepository(banco);
        ProducaoRepository producaoRepo = new SqliteProducaoRepository(banco);
        FornecedorRepository fornecedoresRepo = new SqliteFornecedorRepository(banco);
        ManejoRepository manejoRepo = new SqliteManejoRepository(banco);
        FinanceiroRepository financeiroRepo = new SqliteFinanceiroRepository(banco);
        ProdutividadeService produtividade = new ProdutividadeService(lotesRepo, alimentacoesRepo, producaoRepo);

        // Observer: tudo que e auditavel vai pro log, e a mortalidade ainda pode gerar alerta
        BarramentoEventos eventos = new BarramentoEventos();
        eventos.inscrever(EventoAuditavel.class, new AuditoriaListener(auditoria));
        eventos.inscrever(MortalidadeRegistrada.class, new AlertaListener(alertas));

        ConfiguracaoAlertaService configuracao = new ConfiguracaoAlertaService(configuracaoRepo);
        ParametrosService parametros = new ParametrosService(new SqliteParametrosRepository(banco), eventos);
        return new Aplicacao(
                banco,
                new LoteService(banco, lotesRepo, lancamentosRepo, alertas, configuracao, eventos),
                new EstoqueService(banco, insumosRepo, lancamentosRepo),
                new AlimentacaoService(banco, lotesRepo, insumosRepo, alimentacoesRepo, lancamentosRepo),
                configuracao,
                new AutenticacaoService(usuariosRepo, eventos),
                new UsuarioService(usuariosRepo, eventos),
                new AuditoriaService(auditoria),
                new FornecedorService(banco, fornecedoresRepo, eventos),
                new SanidadeService(banco, lotesRepo, insumosRepo, sanidadeRepo, lancamentosRepo, eventos),
                new AlertaSanitarioService(lotesRepo, sanidadeRepo, parametros),
                new ProducaoService(banco, lotesRepo, producaoRepo, lancamentosRepo, eventos),
                parametros,
                new HistoricoLoteService(lotesRepo, alimentacoesRepo, sanidadeRepo, producaoRepo, insumosRepo, manejoRepo),
                new FinanceiroService(banco, financeiroRepo, insumosRepo, fornecedoresRepo,
                        lotesRepo, lancamentosRepo, eventos),
                new ManejoService(banco, lotesRepo, manejoRepo, lancamentosRepo, eventos),
                produtividade,
                new GestaoService(lotesRepo, insumosRepo, new SqliteGestaoRepository(banco), financeiroRepo, produtividade),
                new ChamadoService(banco, new SqliteChamadoRepository(banco), lancamentosRepo, eventos),
                new ExportacaoService(new SqliteExportacaoRepository(banco), eventos),
                new BackupService(banco, pastaDeBackups, eventos),
                lotesRepo,
                insumosRepo,
                usuariosRepo,
                auditoria,
                alertas);
    }
}
