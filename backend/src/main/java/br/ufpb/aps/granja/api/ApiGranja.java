package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.model.CategoriaInsumo;
import br.ufpb.aps.granja.model.CodigoLancamento;
import br.ufpb.aps.granja.model.Especie;
import br.ufpb.aps.granja.model.Insumo;
import br.ufpb.aps.granja.model.Lote;
import br.ufpb.aps.granja.model.MotivoEncerramento;
import br.ufpb.aps.granja.model.MovimentoLote;
import br.ufpb.aps.granja.model.OrigemMovimento;
import br.ufpb.aps.granja.model.Quantidade;
import br.ufpb.aps.granja.model.RegistroAlimentacao;
import br.ufpb.aps.granja.model.TipoMovimentoEstoque;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.model.excecao.AcessoNegadoException;
import br.ufpb.aps.granja.model.excecao.AutenticacaoException;
import br.ufpb.aps.granja.model.excecao.LancamentoDuplicadoException;
import br.ufpb.aps.granja.model.excecao.LoteEncerradoException;
import br.ufpb.aps.granja.model.excecao.NaoEncontradoException;
import br.ufpb.aps.granja.model.excecao.RegraDeNegocioException;
import br.ufpb.aps.granja.policy.AlertaMortalidade;
import br.ufpb.aps.granja.policy.ConfiguracaoAlerta;
import br.ufpb.aps.granja.policy.CriterioAlerta;
import br.ufpb.aps.granja.service.AlimentacaoService;
import br.ufpb.aps.granja.service.AutenticacaoService;
import br.ufpb.aps.granja.service.ConfiguracaoAlertaService;
import br.ufpb.aps.granja.service.EstoqueService;
import br.ufpb.aps.granja.service.LoteService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * API REST com o HttpServer do JDK, sem framework. Cada requisicao manda HTTP Basic e a senha
 * e conferida com bcrypt (RNF09). O controller so traduz HTTP, as regras ficam nos servicos e cada
 * excecao de regra vira um status (401, 403, 404, 409, 422, 500).
 */
public final class ApiGranja {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final AutenticacaoService autenticacao;
    private final LoteService lotes;
    private final EstoqueService estoque;
    private final AlimentacaoService alimentacao;
    private final ConfiguracaoAlertaService configuracao;
    private final List<Rota> rotas;

    public ApiGranja(AutenticacaoService autenticacao, LoteService lotes, EstoqueService estoque,
                     AlimentacaoService alimentacao, ConfiguracaoAlertaService configuracao) {
        this.autenticacao = autenticacao;
        this.lotes = lotes;
        this.estoque = estoque;
        this.alimentacao = alimentacao;
        this.configuracao = configuracao;
        this.rotas = List.of(
                new Rota("GET", "/api/lotes", this::listarLotes),
                new Rota("POST", "/api/lotes", this::cadastrarLote),
                new Rota("GET", "/api/lotes/(\\d+)", this::consultarLote),
                new Rota("POST", "/api/lotes/(\\d+)/encerramento", this::encerrarLote),
                new Rota("POST", "/api/lotes/(\\d+)/mortalidades", this::registrarMortalidade),
                new Rota("POST", "/api/lotes/(\\d+)/alimentacoes", this::registrarAlimentacao),
                new Rota("GET", "/api/alertas-mortalidade", this::listarAlertas),
                new Rota("GET", "/api/insumos", this::listarInsumos),
                new Rota("POST", "/api/insumos", this::cadastrarInsumo),
                new Rota("GET", "/api/insumos/alertas", this::alertasDeEstoque),
                new Rota("POST", "/api/insumos/(\\d+)/movimentos", this::movimentarInsumo),
                new Rota("GET", "/api/configuracao/alerta", this::consultarConfiguracao),
                new Rota("PUT", "/api/configuracao/alerta", this::configurarAlerta));
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
            Usuario usuario = autenticar(ex);
            String caminho = ex.getRequestURI().getPath();
            boolean caminhoExiste = false;
            for (Rota rota : rotas) {
                Matcher m = rota.caminho().matcher(caminho);
                if (!m.matches()) {
                    continue;
                }
                caminhoExiste = true;
                if (!rota.metodo().equals(ex.getRequestMethod())) {
                    continue;
                }
                List<String> parametros = new ArrayList<>();
                for (int i = 1; i <= m.groupCount(); i++) {
                    parametros.add(m.group(i));
                }
                Resposta resposta = rota.handler().tratar(usuario, parametros, lerCorpo(ex));
                enviar(ex, resposta.status(), resposta.corpo());
                return;
            }
            enviar(ex, caminhoExiste ? 405 : 404,
                    erro(caminhoExiste ? "Método não permitido para esta rota." : "Rota não encontrada."));
        } catch (AutenticacaoException e) {
            ex.getResponseHeaders().add("WWW-Authenticate", "Basic realm=\"granja\"");
            enviar(ex, 401, erro(e.getMessage()));
        } catch (AcessoNegadoException e) {
            enviar(ex, 403, erro(e.getMessage()));
        } catch (NaoEncontradoException e) {
            enviar(ex, 404, erro(e.getMessage()));
        } catch (LoteEncerradoException | LancamentoDuplicadoException e) {
            enviar(ex, 409, erro(e.getMessage()));
        } catch (JsonProcessingException e) {
            enviar(ex, 400, erro("Corpo JSON inválido."));
        } catch (RegraDeNegocioException | IllegalArgumentException e) {
            enviar(ex, 422, erro(e.getMessage()));
        } catch (RuntimeException e) {
            System.err.println("Erro interno: " + e);
            e.printStackTrace(System.err);
            enviar(ex, 500, erro("Erro interno no servidor."));
        }
    }

    private Usuario autenticar(HttpExchange ex) {
        String cabecalho = ex.getRequestHeaders().getFirst("Authorization");
        if (cabecalho == null || !cabecalho.startsWith("Basic ")) {
            throw new AutenticacaoException();
        }
        String credenciais;
        try {
            credenciais = new String(Base64.getDecoder().decode(cabecalho.substring(6).trim()), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new AutenticacaoException();
        }
        int separador = credenciais.indexOf(':');
        if (separador < 0) {
            throw new AutenticacaoException();
        }
        return autenticacao.autenticar(credenciais.substring(0, separador), credenciais.substring(separador + 1));
    }

    // lotes

    private Resposta listarLotes(Usuario u, List<String> p, JsonNode c) {
        return new Resposta(200, lotes.listar(u).stream().map(ApiGranja::comoLote).toList());
    }

    private Resposta cadastrarLote(Usuario u, List<String> p, JsonNode c) {
        Lote lote = lotes.cadastrar(u, codigo(c), enumeracao(Especie.class, c, "especie"),
                Quantidade.de(inteiro(c, "quantidadeInicial")), agora());
        return new Resposta(201, comoLote(lote));
    }

    private Resposta consultarLote(Usuario u, List<String> p, JsonNode c) {
        LoteService.ConsultaLote consulta = lotes.consultar(u, id(p));
        Map<String, Object> corpo = comoLote(consulta.lote());
        corpo.put("consistente", consulta.consistente());
        corpo.put("historico", consulta.historico().stream().map(ApiGranja::comoMovimento).toList());
        return new Resposta(200, corpo);
    }

    private Resposta encerrarLote(Usuario u, List<String> p, JsonNode c) {
        Lote lote = lotes.encerrar(u, id(p), enumeracao(MotivoEncerramento.class, c, "motivo"), codigo(c), agora());
        return new Resposta(200, comoLote(lote));
    }

    private Resposta registrarMortalidade(Usuario u, List<String> p, JsonNode c) {
        LoteService.RegistroMortalidade registro = lotes.registrarMortalidade(u, id(p), inteiro(c, "quantidade"),
                textoOpcional(c, "observacao"), codigo(c), agora());
        Map<String, Object> corpo = comoLote(registro.lote());
        corpo.put("nivelAlerta", registro.nivel().name());
        return new Resposta(201, corpo);
    }

    private Resposta registrarAlimentacao(Usuario u, List<String> p, JsonNode c) {
        RegistroAlimentacao registro = alimentacao.registrar(u, id(p), longo(c, "insumoId"),
                inteiro(c, "quantidadeKg"), dataOpcional(c, "data"), codigo(c), agora());
        return new Resposta(201, comoRegistro(registro));
    }

    private Resposta listarAlertas(Usuario u, List<String> p, JsonNode c) {
        return new Resposta(200, lotes.alertas(u).stream().map(ApiGranja::comoAlerta).toList());
    }

    // estoque

    private Resposta listarInsumos(Usuario u, List<String> p, JsonNode c) {
        return new Resposta(200, estoque.listar(u).stream().map(ApiGranja::comoInsumo).toList());
    }

    private Resposta cadastrarInsumo(Usuario u, List<String> p, JsonNode c) {
        Insumo insumo = estoque.cadastrarInsumo(u, texto(c, "nome"), enumeracao(CategoriaInsumo.class, c, "categoria"),
                texto(c, "unidade"), Quantidade.de(inteiro(c, "nivelMinimo")));
        return new Resposta(201, comoInsumo(insumo));
    }

    private Resposta alertasDeEstoque(Usuario u, List<String> p, JsonNode c) {
        return new Resposta(200, estoque.alertasDeEstoque(u).stream().map(ApiGranja::comoInsumo).toList());
    }

    private Resposta movimentarInsumo(Usuario u, List<String> p, JsonNode c) {
        Insumo insumo = estoque.movimentar(u, id(p), enumeracao(TipoMovimentoEstoque.class, c, "tipo"),
                inteiro(c, "quantidade"), enumeracao(OrigemMovimento.class, c, "origem"), codigo(c), agora());
        return new Resposta(200, comoInsumo(insumo));
    }

    // config de alerta

    private Resposta consultarConfiguracao(Usuario u, List<String> p, JsonNode c) {
        return new Resposta(200, comoConfiguracao(configuracao.consultar(u)));
    }

    private Resposta configurarAlerta(Usuario u, List<String> p, JsonNode c) {
        ConfiguracaoAlerta atual = configuracao.configurar(u, enumeracao(CriterioAlerta.class, c, "criterio"),
                real(c, "atencao"), real(c, "critico"));
        return new Resposta(200, comoConfiguracao(atual));
    }

    // corpo e resposta

    private static JsonNode lerCorpo(HttpExchange ex) throws IOException {
        byte[] bytes = ex.getRequestBody().readAllBytes();
        return bytes.length == 0 ? null : JSON.readTree(bytes);
    }

    private static void enviar(HttpExchange ex, int status, Object corpo) throws IOException {
        byte[] bytes = JSON.writeValueAsBytes(corpo);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream saida = ex.getResponseBody()) {
            saida.write(bytes);
        }
    }

    private static Map<String, Object> erro(String mensagem) {
        return Collections.singletonMap("erro", mensagem);
    }

    // campos do JSON

    private static JsonNode campoObrigatorio(JsonNode corpo, String campo) {
        if (corpo == null || !corpo.hasNonNull(campo)) {
            throw new IllegalArgumentException("Campo obrigatório ausente: " + campo + ".");
        }
        return corpo.get(campo);
    }

    private static String texto(JsonNode corpo, String campo) {
        JsonNode valor = campoObrigatorio(corpo, campo);
        if (!valor.isTextual() || valor.asText().isBlank()) {
            throw new IllegalArgumentException("Campo " + campo + " deve ser um texto não vazio.");
        }
        return valor.asText().trim();
    }

    private static String textoOpcional(JsonNode corpo, String campo) {
        return corpo == null || !corpo.hasNonNull(campo) ? null : corpo.get(campo).asText();
    }

    private static int inteiro(JsonNode corpo, String campo) {
        JsonNode valor = campoObrigatorio(corpo, campo);
        if (!valor.isIntegralNumber() || !valor.canConvertToInt()) {
            throw new IllegalArgumentException("Campo " + campo + " deve ser um número inteiro.");
        }
        return valor.asInt();
    }

    private static long longo(JsonNode corpo, String campo) {
        JsonNode valor = campoObrigatorio(corpo, campo);
        if (!valor.isIntegralNumber()) {
            throw new IllegalArgumentException("Campo " + campo + " deve ser um número inteiro.");
        }
        return valor.asLong();
    }

    private static double real(JsonNode corpo, String campo) {
        JsonNode valor = campoObrigatorio(corpo, campo);
        if (!valor.isNumber()) {
            throw new IllegalArgumentException("Campo " + campo + " deve ser um número.");
        }
        return valor.asDouble();
    }

    private static <E extends Enum<E>> E enumeracao(Class<E> tipo, JsonNode corpo, String campo) {
        String valor = texto(corpo, campo);
        try {
            return Enum.valueOf(tipo, valor);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Valor inválido para " + campo + ": " + valor + ".");
        }
    }

    private static CodigoLancamento codigo(JsonNode corpo) {
        return new CodigoLancamento(texto(corpo, "codigoLancamento"));
    }

    private static LocalDate dataOpcional(JsonNode corpo, String campo) {
        String valor = textoOpcional(corpo, campo);
        if (valor == null) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(valor);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Campo " + campo + " deve ser uma data no formato AAAA-MM-DD.");
        }
    }

    private static long id(List<String> parametros) {
        return Long.parseLong(parametros.get(0));
    }

    private static LocalDateTime agora() {
        return LocalDateTime.now();
    }

    // conversao pra JSON

    private static Map<String, Object> comoLote(Lote lote) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", lote.id());
        m.put("codigo", lote.codigo());
        m.put("especie", lote.especie().name());
        m.put("quantidadeInicial", lote.quantidadeInicial().valor());
        m.put("saldo", lote.saldo().valor());
        m.put("status", lote.status().name());
        return m;
    }

    private static Map<String, Object> comoMovimento(MovimentoLote movimento) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", movimento.id());
        m.put("tipo", movimento.tipo().name());
        m.put("quantidade", movimento.quantidade().valor());
        m.put("codigo", movimento.codigo().valor());
        m.put("dataHora", movimento.dataHora().toString());
        m.put("observacao", movimento.observacao());
        return m;
    }

    private static Map<String, Object> comoInsumo(Insumo insumo) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", insumo.id());
        m.put("nome", insumo.nome());
        m.put("categoria", insumo.categoria().name());
        m.put("unidade", insumo.unidade());
        m.put("nivelMinimo", insumo.nivelMinimo().valor());
        m.put("saldo", insumo.saldo().valor());
        m.put("ativo", insumo.ativo());
        m.put("abaixoDoMinimo", insumo.abaixoDoMinimo());
        return m;
    }

    private static Map<String, Object> comoRegistro(RegistroAlimentacao registro) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", registro.id());
        m.put("loteId", registro.loteId());
        m.put("insumoId", registro.insumoId());
        m.put("quantidadeKg", registro.quantidadeKg().valor());
        m.put("data", registro.data().toString());
        m.put("codigo", registro.codigo().valor());
        return m;
    }

    private static Map<String, Object> comoAlerta(AlertaMortalidade alerta) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("loteId", alerta.loteId());
        m.put("codigoLote", alerta.codigoLote());
        m.put("nivel", alerta.nivel().name());
        m.put("dataHora", alerta.dataHora().toString());
        return m;
    }

    private static Map<String, Object> comoConfiguracao(ConfiguracaoAlerta configuracao) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("criterio", configuracao.criterio().name());
        m.put("atencao", configuracao.atencao());
        m.put("critico", configuracao.critico());
        return m;
    }

    // tipos internos

    @FunctionalInterface
    private interface Handler {
        Resposta tratar(Usuario usuario, List<String> parametros, JsonNode corpo);
    }

    private record Rota(String metodo, Pattern caminho, Handler handler) {
        Rota(String metodo, String regex, Handler handler) {
            this(metodo, Pattern.compile(regex), handler);
        }
    }

    private record Resposta(int status, Object corpo) {
    }
}
