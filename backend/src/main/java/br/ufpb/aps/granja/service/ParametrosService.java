package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.evento.BarramentoEventos;
import br.ufpb.aps.granja.evento.OperacaoRealizada;
import br.ufpb.aps.granja.model.Acao;
import br.ufpb.aps.granja.model.ParametrosSistema;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.repository.ParametrosRepository;

import java.time.LocalDateTime;

/** UC27: parametros do sistema. So o gestor muda (RN18), qualquer perfil le. */
public final class ParametrosService {

    private final ParametrosRepository repositorio;
    private final BarramentoEventos eventos;

    public ParametrosService(ParametrosRepository repositorio, BarramentoEventos eventos) {
        this.repositorio = repositorio;
        this.eventos = eventos;
    }

    public ParametrosSistema consultar(Usuario usuario) {
        Autorizacao.exigir(usuario, Acao.CONSULTAR);
        return atuais();
    }

    public ParametrosSistema configurar(Usuario usuario, int diasAvisoVacina, int diasOcorrenciaAberta,
                                        LocalDateTime agora) {
        Autorizacao.exigir(usuario, Acao.CONFIGURAR_LIMITES);
        ParametrosSistema novos = new ParametrosSistema(diasAvisoVacina, diasOcorrenciaAberta);
        repositorio.salvar(novos);
        eventos.publicar(new OperacaoRealizada("PARAMETROS", "Aviso de vacina em " + diasAvisoVacina
                + " dias e ocorrência prolongada em " + diasOcorrenciaAberta + " dias, por " + usuario.login(), agora));
        return novos;
    }

    /** Em vigor. Enquanto o gestor nao mexe, vale o padrao. */
    ParametrosSistema atuais() {
        return repositorio.buscar().orElse(ParametrosSistema.PADRAO);
    }
}
