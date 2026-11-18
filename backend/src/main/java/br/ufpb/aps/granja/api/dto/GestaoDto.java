package br.ufpb.aps.granja.api.dto;

import br.ufpb.aps.granja.model.GraficosDesempenho;
import br.ufpb.aps.granja.model.PainelGerencial;
import br.ufpb.aps.granja.model.PontoDia;
import br.ufpb.aps.granja.model.RelatorioGerencial;

import java.util.List;

/** Respostas da area do gestor (UC23, UC25 e UC26). Dinheiro em centavos. */
public final class GestaoDto {

    private GestaoDto() {
    }

    public record PainelDto(int lotesAtivos, int avesVivas, int mortesNoMes, int racaoNoMesKg, int ovosNoMes,
                            long receitasDoMesCentavos, long despesasDoMesCentavos, long resultadoDoMesCentavos,
                            int insumosAbaixoDoMinimo) {
        public static PainelDto de(PainelGerencial p) {
            return new PainelDto(p.lotesAtivos(), p.avesVivas(), p.mortesNoMes(), p.racaoNoMesKg(), p.ovosNoMes(),
                    p.receitasDoMesCentavos(), p.despesasDoMesCentavos(), p.resultadoDoMesCentavos(),
                    p.insumosAbaixoDoMinimo());
        }
    }

    public record PontoDto(String data, int valor) {
        static PontoDto de(PontoDia p) {
            return new PontoDto(p.data().toString(), p.valor());
        }
    }

    public record MesDto(String mes, long receitasCentavos, long despesasCentavos) {
    }

    public record GraficosDto(List<MesDto> financeiroPorMes, List<PontoDto> mortesPorDia, List<PontoDto> racaoPorDia,
                              List<PontoDto> ovosPorDia) {
        public static GraficosDto de(GraficosDesempenho g) {
            return new GraficosDto(
                    g.financeiroPorMes().stream()
                            .map(m -> new MesDto(m.mes().toString(), m.receitasCentavos(), m.despesasCentavos())).toList(),
                    g.mortesPorDia().stream().map(PontoDto::de).toList(),
                    g.racaoPorDia().stream().map(PontoDto::de).toList(),
                    g.ovosPorDia().stream().map(PontoDto::de).toList());
        }
    }

    public record LinhaLoteDto(ProdutividadeDto produtividade, String status, long receitasCentavos,
                               long despesasCentavos, long resultadoCentavos) {
    }

    public record InsumoBaixoDto(String nome, String unidade, int saldo, int nivelMinimo) {
    }

    public record RelatorioDto(String de, String ate, long receitasCentavos, long despesasCentavos,
                               long resultadoCentavos, List<LinhaLoteDto> lotes, List<InsumoBaixoDto> insumosAbaixoDoMinimo) {
        public static RelatorioDto de(RelatorioGerencial r) {
            return new RelatorioDto(r.de().toString(), r.ate().toString(), r.receitasCentavos(), r.despesasCentavos(),
                    r.resultadoCentavos(),
                    r.lotes().stream().map(l -> new LinhaLoteDto(ProdutividadeDto.de(l.produtividade()),
                            l.status().name(), l.receitasCentavos(), l.despesasCentavos(), l.resultadoCentavos())).toList(),
                    r.insumosAbaixoDoMinimo().stream()
                            .map(i -> new InsumoBaixoDto(i.nome(), i.unidade(), i.saldo(), i.nivelMinimo())).toList());
        }
    }
}
