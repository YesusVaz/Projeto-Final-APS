package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.Composicao;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.service.AutenticacaoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /** Recebe a aplicacao montada pela Composicao e registra um controller por recurso. */
    public ApiGranja(Composicao.Aplicacao app) {
        this.autenticacao = app.autenticacao();
        List<Controller> controllers = List.of(
                new AutenticacaoController(app.autenticacao(), app.usuarios()),
                new LoteController(app.lotes(), app.historico()),
                new AlimentacaoController(app.alimentacao()),
                new InsumoController(app.estoque()),
                new ConfiguracaoAlertaController(app.configuracao()),
                new ParametrosController(app.parametros()),
                new AuditoriaController(app.consultaAuditoria()),
                new FornecedorController(app.fornecedores()),
                new SanidadeController(app.sanidade(), app.alertasSanitarios()),
                new ProducaoController(app.producao()),
                new FinanceiroController(app.financeiro()),
                new ManejoController(app.manejo(), app.produtividade()),
                new GestaoController(app.gestao()),
                new ChamadoController(app.chamados()),
                new ExportacaoController(app.exportacao()),
                new BackupController(app.backups()));
        controllers.forEach(c -> rotas.addAll(c.rotas()));
    }

    /** Sobe o servidor. Porta 0 pega uma livre (usado nos testes). */
    public HttpServer iniciar(int porta) throws IOException {
        return iniciar(null, porta);
    }

    /** Igual a {@link #iniciar(int)}, mas escutando em outro endereco (ex.: 0.0.0.0 dentro do Docker). Nulo = so a maquina local. */
    public HttpServer iniciar(String endereco, int porta) throws IOException {
        InetAddress host = endereco == null || endereco.isBlank()
                ? InetAddress.getLoopbackAddress() : InetAddress.getByName(endereco);
        HttpServer servidor = HttpServer.create(new InetSocketAddress(host, porta), 0);
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
        Requisicao requisicao = new Requisicao(usuario, token, parametros, Http.lerCorpo(ex),
                consulta(ex.getRequestURI().getRawQuery()));
        Resposta resposta = rota.handler().tratar(requisicao);
        Http.enviar(ex, resposta.status(), resposta.corpo());
    }

    /** Quebra o ?a=1&b=2 da URL num mapa. Chave repetida fica com o ultimo valor. */
    private static Map<String, String> consulta(String bruta) {
        Map<String, String> valores = new HashMap<>();
        if (bruta == null || bruta.isEmpty()) {
            return valores;
        }
        for (String par : bruta.split("&")) {
            int igual = par.indexOf('=');
            String chave = igual < 0 ? par : par.substring(0, igual);
            String valor = igual < 0 ? "" : par.substring(igual + 1);
            valores.put(URLDecoder.decode(chave, StandardCharsets.UTF_8), URLDecoder.decode(valor, StandardCharsets.UTF_8));
        }
        return valores;
    }
}
