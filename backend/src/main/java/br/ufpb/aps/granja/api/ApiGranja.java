package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.service.AlimentacaoService;
import br.ufpb.aps.granja.service.AutenticacaoService;
import br.ufpb.aps.granja.service.ConfiguracaoAlertaService;
import br.ufpb.aps.granja.service.EstoqueService;
import br.ufpb.aps.granja.service.LoteService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * API REST com o HttpServer do JDK, sem framework. POST /api/login confere a senha com bcrypt (RNF09)
 * e devolve um token. As outras rotas mandam esse token no cabecalho Authorization: Bearer.
 * Aqui ficam so o servidor e o despacho das rotas. Cada recurso tem seu controller, o erro vira
 * status no {@link TratadorDeErros} e as regras ficam nos servicos.
 */
public final class ApiGranja {

    private final AutenticacaoService autenticacao;
    private final List<Rota> rotas = new ArrayList<>();

    public ApiGranja(AutenticacaoService autenticacao, LoteService lotes, EstoqueService estoque,
                     AlimentacaoService alimentacao, ConfiguracaoAlertaService configuracao) {
        this.autenticacao = autenticacao;
        List<Controller> controllers = List.of(
                new AutenticacaoController(autenticacao),
                new LoteController(lotes),
                new AlimentacaoController(alimentacao),
                new InsumoController(estoque),
                new ConfiguracaoAlertaController(configuracao));
        controllers.forEach(c -> rotas.addAll(c.rotas()));
    }

    /** Sobe o servidor. Porta 0 pega uma livre (usado nos testes). */
    public HttpServer iniciar(int porta) throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), porta), 0);
        servidor.createContext("/api/", this::tratar);
        servidor.start();
        return servidor;
    }

    private void tratar(HttpExchange ex) throws IOException {
        try {
            despachar(ex);
        } catch (JsonProcessingException | RuntimeException e) {
            TratadorDeErros.responder(ex, e);
        }
    }

    private void despachar(HttpExchange ex) throws IOException {
        String caminho = ex.getRequestURI().getPath();
        String metodo = ex.getRequestMethod();

        for (Rota rota : rotas) {
            Matcher m = rota.caminho().matcher(caminho);
            if (rota.publica() && m.matches() && rota.metodo().equals(metodo)) {
                executar(ex, rota, m, null, null);
                return;
            }
        }

        String token = Http.tokenDoCabecalho(ex);
        Usuario usuario = autenticacao.usuarioDoToken(token);

        boolean caminhoExiste = false;
        for (Rota rota : rotas) {
            Matcher m = rota.caminho().matcher(caminho);
            if (rota.publica() || !m.matches()) {
                continue;
            }
            caminhoExiste = true;
            if (rota.metodo().equals(metodo)) {
                executar(ex, rota, m, usuario, token);
                return;
            }
        }
        Http.enviar(ex, caminhoExiste ? 405 : 404, TratadorDeErros.erro(
                caminhoExiste ? "Método não permitido para esta rota." : "Rota não encontrada."));
    }

    private static void executar(HttpExchange ex, Rota rota, Matcher m, Usuario usuario, String token)
            throws IOException {
        List<String> parametros = new ArrayList<>();
        for (int i = 1; i <= m.groupCount(); i++) {
            parametros.add(m.group(i));
        }
        Resposta resposta = rota.handler().tratar(new Requisicao(usuario, token, parametros, Http.lerCorpo(ex)));
        Http.enviar(ex, resposta.status(), resposta.corpo());
    }
}
