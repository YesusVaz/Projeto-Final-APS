package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.Cenario;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiGranjaTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    private Cenario c;
    private HttpServer servidor;
    private HttpClient http;
    private String base;

    @BeforeEach
    void subir() throws IOException {
        c = new Cenario();
        ApiGranja api = new ApiGranja(c.app.autenticacao(), c.app.lotes(), c.app.estoque(),
                c.app.alimentacao(), c.app.configuracao());
        servidor = api.iniciar(0);
        base = "http://127.0.0.1:" + servidor.getAddress().getPort() + "/api";
        http = HttpClient.newHttpClient();
    }

    @AfterEach
    void parar() {
        servidor.stop(0);
        c.close();
    }

    @Test
    void requisicaoSemCredenciaisRecebe401ComDesafio() throws Exception {
        HttpResponse<String> resposta = enviar("GET", "/lotes", null, null, null);

        assertEquals(401, resposta.statusCode());
        assertTrue(resposta.headers().firstValue("WWW-Authenticate").isPresent());
    }

    @Test
    void senhaErradaRecebe401() throws Exception {
        assertEquals(401, enviar("GET", "/lotes", "tecnico", "errada", null).statusCode());
    }

    @Test
    void funcionarioNaoCadastraLoteRecebe403() throws Exception {
        HttpResponse<String> resposta = enviar("POST", "/lotes", "funcionario", "senha-funcionario",
                "{\"codigoLancamento\":\"L-1\",\"especie\":\"FRANGO_CORTE\",\"quantidadeInicial\":100}");

        assertEquals(403, resposta.statusCode());
    }

    @Test
    void cadastraLoteRespondendoCom201() throws Exception {
        HttpResponse<String> resposta = cadastrarLote("L-1", 100);

        assertEquals(201, resposta.statusCode());
        JsonNode lote = JSON.readTree(resposta.body());
        assertTrue(lote.get("codigo").asText().startsWith("LOT-"));
        assertEquals(100, lote.get("saldo").asInt());
    }

    @Test
    void codigoDeLancamentoRepetidoRecebe409() throws Exception {
        cadastrarLote("L-1", 100);

        assertEquals(409, cadastrarLote("L-1", 50).statusCode());
    }

    @Test
    void loteInexistenteRecebe404() throws Exception {
        assertEquals(404, enviar("GET", "/lotes/999", "gestor", "senha-gestor", null).statusCode());
    }

    @Test
    void metodoNaoPermitidoRecebe405() throws Exception {
        assertEquals(405, enviar("DELETE", "/lotes", "gestor", "senha-gestor", null).statusCode());
    }

    @Test
    void rotaInexistenteRecebe404() throws Exception {
        assertEquals(404, enviar("GET", "/nada", "gestor", "senha-gestor", null).statusCode());
    }

    @Test
    void jsonInvalidoRecebe400() throws Exception {
        assertEquals(400, enviar("POST", "/lotes", "tecnico", "senha-tecnico", "{nao-e-json").statusCode());
    }

    @Test
    void campoObrigatorioAusenteRecebe422() throws Exception {
        HttpResponse<String> resposta = enviar("POST", "/lotes", "tecnico", "senha-tecnico",
                "{\"codigoLancamento\":\"L-9\",\"especie\":\"FRANGO_CORTE\"}");

        assertEquals(422, resposta.statusCode());
        assertTrue(JSON.readTree(resposta.body()).get("erro").asText().contains("quantidadeInicial"));
    }

    @Test
    void mortalidadeGeraAlertaPelaApi() throws Exception {
        long loteId = JSON.readTree(cadastrarLote("L-1", 100).body()).get("id").asLong();

        HttpResponse<String> mortes = enviar("POST", "/lotes/" + loteId + "/mortalidades", "funcionario", "senha-funcionario",
                "{\"codigoLancamento\":\"M-1\",\"quantidade\":3,\"observacao\":\"doença\"}");

        assertEquals(201, mortes.statusCode());
        JsonNode corpo = JSON.readTree(mortes.body());
        assertEquals("ATENCAO", corpo.get("nivelAlerta").asText());
        assertEquals(97, corpo.get("saldo").asInt());

        JsonNode alertas = JSON.readTree(enviar("GET", "/alertas-mortalidade", "gestor", "senha-gestor", null).body());
        assertEquals(1, alertas.size());

        JsonNode consulta = JSON.readTree(enviar("GET", "/lotes/" + loteId, "gestor", "senha-gestor", null).body());
        assertTrue(consulta.get("consistente").asBoolean());
        assertEquals(2, consulta.get("historico").size());
    }

    @Test
    void estoqueEAlimentacaoPelaApi() throws Exception {
        HttpResponse<String> insumo = enviar("POST", "/insumos", "gestor", "senha-gestor",
                "{\"nome\":\"Ração\",\"categoria\":\"RACAO\",\"unidade\":\"kg\",\"nivelMinimo\":10}");
        assertEquals(201, insumo.statusCode());
        long insumoId = JSON.readTree(insumo.body()).get("id").asLong();

        HttpResponse<String> entrada = enviar("POST", "/insumos/" + insumoId + "/movimentos", "gestor", "senha-gestor",
                "{\"codigoLancamento\":\"E-1\",\"tipo\":\"ENTRADA\",\"quantidade\":100,\"origem\":\"COMPRA\"}");
        assertEquals(200, entrada.statusCode());
        assertEquals(100, JSON.readTree(entrada.body()).get("saldo").asInt());

        long loteId = JSON.readTree(cadastrarLote("L-1", 100).body()).get("id").asLong();
        HttpResponse<String> alimentacao = enviar("POST", "/lotes/" + loteId + "/alimentacoes", "funcionario", "senha-funcionario",
                "{\"insumoId\":" + insumoId + ",\"quantidadeKg\":30,\"codigoLancamento\":\"A-1\"}");
        assertEquals(201, alimentacao.statusCode());

        JsonNode lista = JSON.readTree(enviar("GET", "/insumos", "gestor", "senha-gestor", null).body());
        assertEquals(70, lista.get(0).get("saldo").asInt());

        JsonNode alertas = JSON.readTree(enviar("GET", "/insumos/alertas", "gestor", "senha-gestor", null).body());
        assertEquals(0, alertas.size());
    }

    @Test
    void soOGestorAlteraAConfiguracaoDeAlerta() throws Exception {
        String corpo = "{\"criterio\":\"ACUMULADO\",\"atencao\":2.0,\"critico\":5.0}";

        assertEquals(403, enviar("PUT", "/configuracao/alerta", "tecnico", "senha-tecnico", corpo).statusCode());
        assertEquals(200, enviar("PUT", "/configuracao/alerta", "gestor", "senha-gestor", corpo).statusCode());

        JsonNode atual = JSON.readTree(enviar("GET", "/configuracao/alerta", "tecnico", "senha-tecnico", null).body());
        assertEquals("ACUMULADO", atual.get("criterio").asText());
    }

    // sessao: login, token e logout

    @Test
    void loginDevolveTokenENomeEPerfil() throws Exception {
        HttpResponse<String> resposta = login("gestor", "senha-gestor");

        assertEquals(200, resposta.statusCode());
        JsonNode corpo = JSON.readTree(resposta.body());
        assertTrue(corpo.get("token").asText().length() > 30);
        assertEquals("GESTOR", corpo.get("perfil").asText());
        assertEquals("Gestor de teste", corpo.get("nome").asText());
        assertFalse(resposta.body().contains("senha-gestor"));
    }

    @Test
    void loginComSenhaErradaRecebe401ComDesafioBearer() throws Exception {
        HttpResponse<String> resposta = login("gestor", "errada");

        assertEquals(401, resposta.statusCode());
        assertTrue(resposta.headers().firstValue("WWW-Authenticate").orElse("").startsWith("Bearer"));
    }

    @Test
    void tokenInventadoRecebe401() throws Exception {
        assertEquals(401, enviarComToken("GET", "/lotes", "token-inventado", null).statusCode());
    }

    @Test
    void logoutInvalidaOTokenNaHora() throws Exception {
        String token = tokenDe("gestor", "senha-gestor");

        assertEquals(200, enviarComToken("POST", "/logout", token, null).statusCode());
        assertEquals(401, enviarComToken("GET", "/lotes", token, null).statusCode());
    }

    // cadastro de usuario: so gestor logado

    @Test
    void cadastroDeGestorExigeLogin() throws Exception {
        HttpResponse<String> resposta = enviarComToken("POST", "/usuarios", null, corpoUsuario("ana", "FUNCIONARIO", true));

        assertEquals(401, resposta.statusCode());
    }

    // cadastro publico: sem token, e sempre funcionario ativo

    @Test
    void cadastroPublicoNaoPedeTokenECriaFuncionarioAtivo() throws Exception {
        String corpo = JSON.writeValueAsString(Map.of("login", "ana.lima", "email", "Ana.Lima@Granja.local",
                "nome", "Ana Lima", "senha", "senha-ana", "perfil", "GESTOR", "ativo", false));

        HttpResponse<String> resposta = enviarComToken("POST", "/cadastro", null, corpo);

        assertEquals(201, resposta.statusCode());
        JsonNode usuario = JSON.readTree(resposta.body());
        assertEquals("FUNCIONARIO", usuario.get("perfil").asText());
        assertTrue(usuario.get("ativo").asBoolean());
        assertEquals("ana.lima@granja.local", usuario.get("email").asText());
        assertFalse(resposta.body().contains("senha"));
        assertEquals(200, login("ana.lima", "senha-ana").statusCode());
    }

    @Test
    void cadastroPublicoEmailRepetidoRecebe409() throws Exception {
        assertEquals(409, enviarComToken("POST", "/cadastro", null,
                corpoPublico("ana.lima", "GESTOR@granja.local", "senha-ana")).statusCode());
    }

    @Test
    void cadastroPublicoLoginRepetidoRecebe409() throws Exception {
        assertEquals(409, enviarComToken("POST", "/cadastro", null,
                corpoPublico("gestor", "outra@granja.local", "senha-outra")).statusCode());
    }

    @Test
    void cadastroPublicoSemEmailRecebe422() throws Exception {
        String corpo = JSON.writeValueAsString(Map.of("login", "ana.lima", "nome", "Ana Lima", "senha", "senha-ana"));

        HttpResponse<String> resposta = enviarComToken("POST", "/cadastro", null, corpo);

        assertEquals(422, resposta.statusCode());
        assertTrue(JSON.readTree(resposta.body()).get("erro").asText().contains("email"));
    }

    @Test
    void cadastroPublicoSenhaCurtaRecebe422() throws Exception {
        assertEquals(422, enviarComToken("POST", "/cadastro", null,
                corpoPublico("ana.lima", "ana@granja.local", "123")).statusCode());
    }

    @Test
    void naoGestorNaoCadastraUsuarioRecebe403() throws Exception {
        String token = tokenDe("tecnico", "senha-tecnico");

        assertEquals(403, enviarComToken("POST", "/usuarios", token, corpoUsuario("ana", "FUNCIONARIO", true)).statusCode());
    }

    @Test
    void gestorCadastraUsuarioComPerfilEStatusEscolhidos() throws Exception {
        String token = tokenDe("gestor", "senha-gestor");

        HttpResponse<String> criado = enviarComToken("POST", "/usuarios", token, corpoUsuario("ana", "FUNCIONARIO", true));

        assertEquals(201, criado.statusCode());
        JsonNode corpo = JSON.readTree(criado.body());
        assertEquals("FUNCIONARIO", corpo.get("perfil").asText());
        assertTrue(corpo.get("ativo").asBoolean());
        assertFalse(criado.body().contains("senha"));
        assertEquals(200, login("ana", "senha-ana").statusCode());
    }

    @Test
    void usuarioCriadoInativoNaoEntra() throws Exception {
        String token = tokenDe("gestor", "senha-gestor");

        HttpResponse<String> criado = enviarComToken("POST", "/usuarios", token, corpoUsuario("marcos", "TECNICO", false));

        assertEquals(201, criado.statusCode());
        assertFalse(JSON.readTree(criado.body()).get("ativo").asBoolean());
        assertEquals(401, login("marcos", "senha-marcos").statusCode());
    }

    @Test
    void loginRepetidoRecebe409() throws Exception {
        String token = tokenDe("gestor", "senha-gestor");

        assertEquals(409, enviarComToken("POST", "/usuarios", token, corpoUsuario("tecnico", "FUNCIONARIO", true)).statusCode());
    }

    @Test
    void perfilInvalidoRecebe422() throws Exception {
        String token = tokenDe("gestor", "senha-gestor");

        HttpResponse<String> resposta = enviarComToken("POST", "/usuarios", token, corpoUsuario("ana", "ADMIN", true));

        assertEquals(422, resposta.statusCode());
        assertTrue(JSON.readTree(resposta.body()).get("erro").asText().contains("perfil"));
    }

    @Test
    void senhaCurtaNoCadastroRecebe422() throws Exception {
        String token = tokenDe("gestor", "senha-gestor");
        String corpo = JSON.writeValueAsString(Map.of("login", "ana", "email", "ana@granja.local", "nome", "Ana Lima",
                "senha", "123", "perfil", "FUNCIONARIO"));

        assertEquals(422, enviarComToken("POST", "/usuarios", token, corpo).statusCode());
    }

    private HttpResponse<String> cadastrarLote(String codigoLancamento, int quantidadeInicial) throws Exception {
        return enviar("POST", "/lotes", "tecnico", "senha-tecnico",
                "{\"codigoLancamento\":\"" + codigoLancamento + "\",\"especie\":\"FRANGO_CORTE\",\"quantidadeInicial\":"
                        + quantidadeInicial + "}");
    }

    private HttpResponse<String> login(String usuario, String senha) throws Exception {
        return enviarComToken("POST", "/login", null, JSON.writeValueAsString(Map.of("login", usuario, "senha", senha)));
    }

    private String tokenDe(String usuario, String senha) throws Exception {
        return JSON.readTree(login(usuario, senha).body()).get("token").asText();
    }

    private String corpoUsuario(String login, String perfil, boolean ativo) throws Exception {
        return JSON.writeValueAsString(Map.of("login", login, "email", login + "@granja.local",
                "nome", "Usuário " + login, "senha", "senha-" + login, "perfil", perfil, "ativo", ativo));
    }

    private String corpoPublico(String login, String email, String senha) throws Exception {
        return JSON.writeValueAsString(Map.of("login", login, "email", email, "nome", "Usuário " + login, "senha", senha));
    }

    /** Faz login antes de chamar a rota. Se o login falhar, devolve a propria resposta do login. */
    private HttpResponse<String> enviar(String metodo, String caminho, String usuario, String senha, String corpo)
            throws Exception {
        String token = null;
        if (usuario != null) {
            HttpResponse<String> resposta = login(usuario, senha);
            if (resposta.statusCode() != 200) {
                return resposta;
            }
            token = JSON.readTree(resposta.body()).get("token").asText();
        }
        return enviarComToken(metodo, caminho, token, corpo);
    }

    private HttpResponse<String> enviarComToken(String metodo, String caminho, String token, String corpo)
            throws IOException, InterruptedException {
        HttpRequest.Builder requisicao = HttpRequest.newBuilder(URI.create(base + caminho))
                .header("Content-Type", "application/json");
        if (token != null) {
            requisicao.header("Authorization", "Bearer " + token);
        }
        HttpRequest.BodyPublisher publicador = corpo == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(corpo, StandardCharsets.UTF_8);
        requisicao.method(metodo, publicador);
        return http.send(requisicao.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
}
