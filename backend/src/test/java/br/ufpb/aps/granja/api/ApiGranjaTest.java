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
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private HttpResponse<String> cadastrarLote(String codigoLancamento, int quantidadeInicial) throws Exception {
        return enviar("POST", "/lotes", "tecnico", "senha-tecnico",
                "{\"codigoLancamento\":\"" + codigoLancamento + "\",\"especie\":\"FRANGO_CORTE\",\"quantidadeInicial\":"
                        + quantidadeInicial + "}");
    }

    private HttpResponse<String> enviar(String metodo, String caminho, String usuario, String senha, String corpo)
            throws IOException, InterruptedException {
        HttpRequest.Builder requisicao = HttpRequest.newBuilder(URI.create(base + caminho))
                .header("Content-Type", "application/json");
        if (usuario != null) {
            String credenciais = usuario + ":" + senha;
            requisicao.header("Authorization",
                    "Basic " + Base64.getEncoder().encodeToString(credenciais.getBytes(StandardCharsets.UTF_8)));
        }
        HttpRequest.BodyPublisher publicador = corpo == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(corpo, StandardCharsets.UTF_8);
        requisicao.method(metodo, publicador);
        return http.send(requisicao.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
}
