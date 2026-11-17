package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.AlertaSanitario;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.OcorrenciaSanitaria;
import br.ufpb.aps.granja.model.ParametrosSistema;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.model.Vacinacao;
import br.ufpb.aps.granja.repository.LoteRepository;
import br.ufpb.aps.granja.repository.SanidadeRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * UC24 e RN17: alertas sanitarios calculados pro dia. A verificacao e por dia: pra mesma data
 * o resultado e o mesmo, a qualquer hora. So entram lotes ativos.
 * <ul>
 *   <li>vacina com reforco ate X dias pra frente: VACINA_PROXIMA;</li>
 *   <li>reforco que ja passou e nao teve outra dose da mesma vacina depois: VACINA_ATRASADA;</li>
 *   <li>ocorrencia aberta ha Y dias ou mais: OCORRENCIA_PROLONGADA.</li>
 * </ul>
 * X e Y vem dos parametros do sistema (UC27).
 */
public final class AlertaSanitarioService {

    private final LoteRepository lotes;
    private final SanidadeRepository sanidade;
    private final ParametrosService parametros;

    public AlertaSanitarioService(LoteRepository lotes, SanidadeRepository sanidade, ParametrosService parametros) {
        this.lotes = lotes;
        this.sanidade = sanidade;
        this.parametros = parametros;
    }

    public List<AlertaSanitario> verificar(Usuario usuario, LocalDate dia) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR_ALERTAS_SANITARIOS);
        ParametrosSistema config = parametros.atuais();
        Map<Long, Lote> ativos = lotes.listar().stream()
                .filter(Lote::estaAtivo)
                .collect(Collectors.toMap(Lote::id, Function.identity()));

        List<AlertaSanitario> alertas = new ArrayList<>();
        List<Vacinacao> vacinacoes = sanidade.listarVacinacoes();
        for (Vacinacao v : vacinacoes) {
            Lote lote = ativos.get(v.loteId());
            if (lote == null || v.proximaDose() == null || v.data().isAfter(dia) || reforcoFeito(v, vacinacoes, dia)) {
                continue;
            }
            if (v.proximaDose().isBefore(dia)) {
                alertas.add(new AlertaSanitario(AlertaSanitario.Tipo.VACINA_ATRASADA, lote.id(), lote.codigo(),
                        "Reforço da vacina (insumo " + v.insumoId() + ") venceu em " + v.proximaDose() + ".",
                        v.proximaDose()));
            } else if (!v.proximaDose().isAfter(dia.plusDays(config.diasAvisoVacina()))) {
                alertas.add(new AlertaSanitario(AlertaSanitario.Tipo.VACINA_PROXIMA, lote.id(), lote.codigo(),
                        "Reforço da vacina (insumo " + v.insumoId() + ") previsto para " + v.proximaDose() + ".",
                        v.proximaDose()));
            }
        }

        for (OcorrenciaSanitaria o : sanidade.listarOcorrencias()) {
            Lote lote = ativos.get(o.loteId());
            if (lote == null || !abertaNoDia(o, dia)) {
                continue;
            }
            long dias = ChronoUnit.DAYS.between(o.dataAbertura(), dia);
            if (dias >= config.diasOcorrenciaAberta()) {
                alertas.add(new AlertaSanitario(AlertaSanitario.Tipo.OCORRENCIA_PROLONGADA, lote.id(), lote.codigo(),
                        "Ocorrência " + o.id() + " aberta há " + dias + " dias: " + o.descricao(), o.dataAbertura()));
            }
        }

        // o mais grave primeiro: atrasada, depois ocorrencia, depois aviso
        alertas.sort(Comparator.comparing((AlertaSanitario a) -> gravidade(a.tipo()))
                .thenComparing(AlertaSanitario::referencia)
                .thenComparing(AlertaSanitario::loteId));
        return alertas;
    }

    /** Teve outra dose da mesma vacina no mesmo lote depois desta, ate o dia consultado? */
    private static boolean reforcoFeito(Vacinacao v, List<Vacinacao> todas, LocalDate dia) {
        return todas.stream().anyMatch(outra -> outra.loteId().equals(v.loteId())
                && outra.insumoId().equals(v.insumoId())
                && outra.data().isAfter(v.data())
                && !outra.data().isAfter(dia));
    }

    /** Consultando um dia passado, ocorrencia encerrada depois daquele dia ainda conta como aberta. */
    private static boolean abertaNoDia(OcorrenciaSanitaria o, LocalDate dia) {
        if (o.dataAbertura().isAfter(dia)) {
            return false;
        }
        return o.aberta() || o.dataEncerramento().isAfter(dia);
    }

    private static int gravidade(AlertaSanitario.Tipo tipo) {
        return switch (tipo) {
            case VACINA_ATRASADA -> 0;
            case OCORRENCIA_PROLONGADA -> 1;
            case VACINA_PROXIMA -> 2;
        };
    }
}
