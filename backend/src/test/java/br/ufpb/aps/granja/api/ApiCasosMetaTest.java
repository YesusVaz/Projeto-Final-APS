package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.Cenario;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Rotas dos casos de uso Meta pela API, com token de verdade. */
class ApiCasosMetaTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    private Cenario c;
    private HttpServer servidor;
    private HttpClient http;
    private String base;
    private String gestor;
    private String tecnico;
    private String funcionario;

    @BeforeEach
    void subir() throws Exception {
        c = new Cenario();
        servidor = new ApiGranja(c.app).iniciar(0);
        base = "http://127.0.0.1:" + servidor.getAddress().getPort() + "/api";
        http = HttpClient.newHttpClient();
        gestor = token("gestor", "senha-gestor");
        tecnico = token("tecnico", "senha-tecnico");
        funcionario = token("funcionario", "senha-funcionario");
    }

    @AfterEach
    void parar() {
        servidor.stop(0);
        c.close();
    }

    @Test
    void gestorListaEInativaUsuarioEOLoginNaoMuda() throws Exception {
        assertEquals(403, chamar("GET", "/usuarios", tecnico, null).statusCode());
        JsonNode lista = corpo(chamar("GET", "/usuarios", gestor, null));
        assertEquals(3, lista.size());

        long idTecnico = c.tecnico.id();
        HttpResponse<String> inativo = chamar("PUT", "/usuarios/" + idTecnico + "/status", gestor,
                "{\"ativo\":false,\"login\":\"outro-login\"}");
        assertEquals(200, inativo.statusCode());
        assertEquals("tecnico", corpo(inativo).get("login").asText());
        assertEquals(401, chamar("GET", "/lotes", tecnico, null).statusCode());
        assertEquals(422, chamar("PUT", "/usuarios/" + idTecnico + "/status", gestor, "{}").statusCode());
    }

    @Test
    void cincoSenhasErradasBloqueiamOLoginPelaApi() throws Exception {
        for (int i = 0; i < 4; i++) {
            assertEquals(401, login("funcionario", "errada").statusCode());
        }
        HttpResponse<String> quinta = login("funcionario", "errada");
        assertEquals(401, quinta.statusCode());
        assertTrue(corpo(quinta).get("erro").asText().contains("15"));
        assertEquals(401, login("funcionario", "senha-funcionario").statusCode());
    }

    @Test
    void auditoriaSoOGestorLe() throws Exception {
        chamar("POST", "/lotes", tecnico, "{\"codigoLancamento\":\"L-1\",\"especie\":\"POEDEIRA\",\"quantidadeInicial\":10}");

        assertEquals(403, chamar("GET", "/auditoria", tecnico, null).statusCode());
        JsonNode log = corpo(chamar("GET", "/auditoria", gestor, null));
        assertTrue(log.size() >= 1);
        assertEquals("LOTE", log.get(log.size() - 1).get("categoria").asText());
    }

    @Test
    void fornecedorCadastraERepetidoDa409() throws Exception {
        String corpo = "{\"nome\":\"Agro Sul\",\"documento\":\"11.222.333/0001-81\"}";

        HttpResponse<String> criado = chamar("POST", "/fornecedores", gestor, corpo);
        assertEquals(201, criado.statusCode());
        assertEquals("CNPJ", corpo(criado).get("tipoDocumento").asText());
        assertEquals(409, chamar("POST", "/fornecedores", gestor, corpo).statusCode());
        assertEquals(403, chamar("POST", "/fornecedores", tecnico, corpo).statusCode());
        assertEquals(422, chamar("POST", "/fornecedores", gestor,
                "{\"nome\":\"X\",\"documento\":\"123\"}").statusCode());
    }

    @Test
    void loteComAlojamentoMostraIdadeBaixaEHistorico() throws Exception {
        long lote = cadastrarLote("FRANGO_CORTE", 100, "\"dataAlojamento\":\"2026-11-01\",\"idadeInicialDias\":1,");

        JsonNode idade = corpo(chamar("GET", "/lotes/" + lote + "/idade?data=2026-11-08", funcionario, null));
        assertEquals(8, idade.get("idadeDias").asInt());
        assertEquals(1, idade.get("semanas").asInt());
        assertEquals(422, chamar("GET", "/lotes/" + lote + "/idade?data=ontem", funcionario, null).statusCode());

        HttpResponse<String> baixa = chamar("POST", "/lotes/" + lote + "/baixas", tecnico,
                "{\"codigoLancamento\":\"B-1\",\"motivo\":\"VENDA\",\"quantidade\":40}");
        assertEquals(201, baixa.statusCode());
        assertEquals(60, corpo(baixa).get("saldo").asInt());
        assertEquals(403, chamar("POST", "/lotes/" + lote + "/baixas", funcionario,
                "{\"codigoLancamento\":\"B-2\",\"motivo\":\"VENDA\",\"quantidade\":1}").statusCode());

        JsonNode historico = corpo(chamar("GET", "/lotes/" + lote + "/historico", funcionario, null));
        assertTrue(historico.get("consistente").asBoolean());
        assertEquals(2, historico.get("itens").size());
    }

    @Test
    void vacinaOcorrenciaTratamentoEAlertaSanitarioPelaApi() throws Exception {
        long lote = cadastrarLote("FRANGO_CORTE", 100, "\"dataAlojamento\":\"2026-11-01\",");
        long vacina = insumoComEstoque("Gumboro", "VACINA", 200);
        long remedio = insumoComEstoque("Amoxicilina", "MEDICAMENTO", 10);

        HttpResponse<String> vacinacao = chamar("POST", "/lotes/" + lote + "/vacinacoes", tecnico,
                "{\"codigoLancamento\":\"V-1\",\"insumoId\":" + vacina + ",\"doses\":100,"
                        + "\"data\":\"2026-11-10\",\"proximaDose\":\"2026-11-20\"}");
        assertEquals(201, vacinacao.statusCode());
        assertEquals(403, chamar("POST", "/lotes/" + lote + "/vacinacoes", funcionario,
                "{\"codigoLancamento\":\"V-2\",\"insumoId\":" + vacina + ",\"doses\":1}").statusCode());

        JsonNode alertas = corpo(chamar("GET", "/alertas-sanitarios?data=2026-11-21", tecnico, null));
        assertEquals("VACINA_ATRASADA", alertas.get(0).get("tipo").asText());
        assertEquals(403, chamar("GET", "/alertas-sanitarios", funcionario, null).statusCode());

        long ocorrencia = corpo(chamar("POST", "/lotes/" + lote + "/ocorrencias", tecnico,
                "{\"descricao\":\"Tosse\",\"data\":\"2026-11-12\"}")).get("id").asLong();
        assertEquals(422, chamar("POST", "/ocorrencias/" + ocorrencia + "/tratamentos", tecnico,
                "{\"codigoLancamento\":\"T-1\",\"insumoId\":" + remedio + ",\"quantidade\":0}").statusCode());
        assertEquals(201, chamar("POST", "/ocorrencias/" + ocorrencia + "/tratamentos", tecnico,
                "{\"codigoLancamento\":\"T-1\",\"insumoId\":" + remedio + ",\"quantidade\":4}").statusCode());
        assertEquals(200, chamar("POST", "/ocorrencias/" + ocorrencia + "/encerramento", tecnico, null).statusCode());
        assertEquals(409, chamar("POST", "/ocorrencias/" + ocorrencia + "/encerramento", tecnico, null).statusCode());

        JsonNode detalhe = corpo(chamar("GET", "/ocorrencias/" + ocorrencia, funcionario, null));
        assertEquals("ENCERRADA", detalhe.get("status").asText());
        assertEquals(1, detalhe.get("tratamentos").size());
        assertEquals(404, chamar("GET", "/ocorrencias/999", funcionario, null).statusCode());
    }

    @Test
    void posturaEPesagemPelaApi() throws Exception {
        long poedeiras = cadastrarLote("POEDEIRA", 1000, "\"dataAlojamento\":\"2026-10-01\",\"idadeInicialDias\":120,");
        String hoje = LocalDate.now().toString();

        HttpResponse<String> postura = chamar("POST", "/lotes/" + poedeiras + "/posturas", funcionario,
                "{\"codigoLancamento\":\"P-1\",\"ovos\":900,\"data\":\"" + hoje + "\"}");
        assertEquals(201, postura.statusCode());
        assertEquals(90.0, corpo(postura).get("indicador").asDouble());
        assertEquals("%", corpo(postura).get("unidadeIndicador").asText());
        assertEquals(409, chamar("POST", "/lotes/" + poedeiras + "/posturas", funcionario,
                "{\"codigoLancamento\":\"P-2\",\"ovos\":800,\"data\":\"" + hoje + "\"}").statusCode());
        assertEquals(422, chamar("POST", "/lotes/" + poedeiras + "/pesagens", funcionario,
                "{\"codigoLancamento\":\"P-3\",\"pesoMedioGramas\":1500}").statusCode());

        assertEquals(1, corpo(chamar("GET", "/lotes/" + poedeiras + "/producao", gestor, null)).size());
    }

    @Test
    void parametrosSoOGestorAltera() throws Exception {
        assertEquals(7, corpo(chamar("GET", "/configuracao/parametros", funcionario, null)).get("diasAvisoVacina").asInt());

        String corpo = "{\"diasAvisoVacina\":3,\"diasOcorrenciaAberta\":10}";
        assertEquals(403, chamar("PUT", "/configuracao/parametros", tecnico, corpo).statusCode());
        assertEquals(200, chamar("PUT", "/configuracao/parametros", gestor, corpo).statusCode());
        assertEquals(10, corpo(chamar("GET", "/configuracao/parametros", tecnico, null)).get("diasOcorrenciaAberta").asInt());
        assertEquals(422, chamar("PUT", "/configuracao/parametros", gestor,
                "{\"diasAvisoVacina\":-1,\"diasOcorrenciaAberta\":10}").statusCode());
    }

    private long cadastrarLote(String especie, int quantidade, String extra) throws Exception {
        HttpResponse<String> resposta = chamar("POST", "/lotes", tecnico, "{" + extra + "\"codigoLancamento\":\"L-"
                + especie + "\",\"especie\":\"" + especie + "\",\"quantidadeInicial\":" + quantidade + "}");
        assertEquals(201, resposta.statusCode(), resposta.body());
        return corpo(resposta).get("id").asLong();
    }

    private long insumoComEstoque(String nome, String categoria, int quantidade) throws Exception {
        long id = corpo(chamar("POST", "/insumos", gestor, "{\"nome\":\"" + nome + "\",\"categoria\":\"" + categoria
                + "\",\"unidade\":\"un\",\"nivelMinimo\":1}")).get("id").asLong();
        chamar("POST", "/insumos/" + id + "/movimentos", gestor, "{\"codigoLancamento\":\"E-" + nome
                + "\",\"tipo\":\"ENTRADA\",\"quantidade\":" + quantidade + ",\"origem\":\"COMPRA\"}");
        return id;
    }

    private String token(String login, String senha) throws Exception {
        return corpo(login(login, senha)).get("token").asText();
    }

    private HttpResponse<String> login(String login, String senha) throws Exception {
        return chamar("POST", "/login", null, "{\"login\":\"" + login + "\",\"senha\":\"" + senha + "\"}");
    }

    private static JsonNode corpo(HttpResponse<String> resposta) throws Exception {
        return JSON.readTree(resposta.body());
    }

    private HttpResponse<String> chamar(String metodo, String caminho, String token, String corpo) throws Exception {
        HttpRequest.Builder requisicao = HttpRequest.newBuilder(URI.create(base + caminho))
                .header("Content-Type", "application/json");
        if (token != null) {
            requisicao.header("Authorization", "Bearer " + token);
        }
        requisicao.method(metodo, corpo == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(corpo, StandardCharsets.UTF_8));
        return http.send(requisicao.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
}
