package br.ufpb.aps.granja;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.persistencia.Banco;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Os dados de exemplo passam pelas regras de verdade: se uma regra recusar algum, o teste quebra. */
class DadosDeDemonstracaoTest {

    private Composicao.Aplicacao novaAplicacao() throws IOException {
        Composicao.Aplicacao app = Composicao.criar(Banco.memoria(), Files.createTempDirectory("granja-demo"));
        UsuariosDeDemonstracao.semear(app);
        return app;
    }

    @Test
    void criaOsDadosEResultaEmAlertasERelatorios() throws IOException {
        Composicao.Aplicacao app = novaAplicacao();
        Usuario gestor = app.usuariosRepositorio().buscarPorLogin("gestor").orElseThrow();

        assertTrue(DadosDeDemonstracao.semear(app));

        var lotes = app.lotes().listar(gestor);
        assertEquals(5, lotes.size());
        assertEquals(1, lotes.stream().filter(l -> !l.estaAtivo()).count(), "um lote ja vendido");

        assertTrue(app.estoque().alertasDeEstoque(gestor).size() >= 2, "ha insumos abaixo do minimo");
        assertTrue(app.lotes().alertas(gestor).size() >= 2, "ha alertas de mortalidade");
        assertEquals(3, app.chamados().todos(gestor).size());

        var usuarios = app.usuarios().listar(gestor);
        assertEquals(7, usuarios.size());
        assertEquals(1, usuarios.stream().filter(u -> !u.ativo()).count(), "uma conta inativa");

        LocalDate hoje = LocalDate.now();
        var financeiro = app.financeiro().relatorio(gestor, hoje.minusMonths(6), hoje, null);
        assertTrue(financeiro.receitasCentavos() > 0);
        assertTrue(financeiro.despesasCentavos() > 0);

        var painel = app.gestao().painel(gestor, LocalDate.now());
        assertEquals(4, painel.lotesAtivos());
        assertTrue(app.gestao().graficos(gestor, LocalDate.now()) != null);
    }

    @Test
    void naoDuplicaQuandoJaTemLotes() throws IOException {
        Composicao.Aplicacao app = novaAplicacao();
        assertTrue(DadosDeDemonstracao.semear(app));
        assertFalse(DadosDeDemonstracao.semear(app));
    }
}
