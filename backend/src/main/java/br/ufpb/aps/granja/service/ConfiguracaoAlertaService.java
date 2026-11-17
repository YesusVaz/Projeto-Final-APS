package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.policy.ConfiguracaoAlerta;
import br.ufpb.aps.granja.policy.CriadorAlertaAcumulado;
import br.ufpb.aps.granja.policy.CriadorAlertaDiario;
import br.ufpb.aps.granja.policy.CriadorPoliticaAlerta;
import br.ufpb.aps.granja.policy.CriterioAlerta;
import br.ufpb.aps.granja.policy.PoliticaAlertaMortalidade;
import br.ufpb.aps.granja.repository.ConfiguracaoAlertaRepository;

/** RN18 e RN24: o Gestor define criterio e limites, e a politica vem do criador certo. */
public final class ConfiguracaoAlertaService {

    /** Limites padrao enquanto o Gestor nao mexe: 2% de atencao e 5% de critico no dia. */
    static final ConfiguracaoAlerta PADRAO = new ConfiguracaoAlerta(CriterioAlerta.DIARIO, 2.0, 5.0);

    private final ConfiguracaoAlertaRepository repositorio;

    public ConfiguracaoAlertaService(ConfiguracaoAlertaRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** So o Gestor muda os limites (RN18). A politica e montada antes de gravar, o que ja valida. */
    public ConfiguracaoAlerta configurar(Usuario usuario, CriterioAlerta criterio, double atencao, double critico) {
        Autorizacao.exigir(usuario, Acao.CONFIGURAR_LIMITES);
        ConfiguracaoAlerta configuracao = new ConfiguracaoAlerta(criterio, atencao, critico);
        criarPolitica(configuracao);
        repositorio.salvar(configuracao);
        return configuracao;
    }

    public ConfiguracaoAlerta consultar(Usuario usuario) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR);
        return atual();
    }

    /** Politica em vigor. O servico de lote usa ela na hora de avaliar a mortalidade. */
    public PoliticaAlertaMortalidade politicaAtual() {
        return criarPolitica(atual());
    }

    private ConfiguracaoAlerta atual() {
        return repositorio.buscar().orElse(PADRAO);
    }

    /** Factory Method: o criterio escolhe o criador, e quem usa so ve a interface. */
    private static PoliticaAlertaMortalidade criarPolitica(ConfiguracaoAlerta configuracao) {
        CriadorPoliticaAlerta criador = configuracao.criterio() == CriterioAlerta.ACUMULADO
                ? new CriadorAlertaAcumulado()
                : new CriadorAlertaDiario();
        return criador.configurar(configuracao.atencao(), configuracao.critico());
    }
}
