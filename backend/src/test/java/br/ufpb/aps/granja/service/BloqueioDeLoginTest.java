package br.ufpb.aps.granja.service;

import br.ufpb.aps.granja.Cenario;
import br.ufpb.aps.granja.evento.BarramentoEventos;
import br.ufpb.aps.granja.model.excecao.AutenticacaoException;
import br.ufpb.aps.granja.model.excecao.ContaBloqueadaException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RN03: 5 tentativas erradas seguidas travam o login por 15 minutos. */
class BloqueioDeLoginTest {

    private Cenario c;
    private Relogio relogio;
    private AutenticacaoService auth;

    @BeforeEach
    void preparar() {
        c = new Cenario();
        relogio = new Relogio(Instant.parse("2026-11-17T12:00:00Z"));
        auth = new AutenticacaoService(c.app.usuariosRepositorio(), new BarramentoEventos(), relogio);
    }

    @AfterEach
    void fechar() {
        c.close();
    }

    @Test
    void quintoErroBloqueiaEAteASenhaCertaEhRecusada() {
        errar("gestor", 4);
        ContaBloqueadaException bloqueio = assertThrows(ContaBloqueadaException.class,
                () -> auth.autenticar("gestor", "errada"));
        assertTrue(bloqueio.getMessage().contains("15"));

        assertThrows(ContaBloqueadaException.class, () -> auth.autenticar("gestor", "senha-gestor"));
    }

    @Test
    void depoisDe15MinutosOLoginVoltaAFuncionar() {
        errar("gestor", 5);

        relogio.avancar(Duration.ofMinutes(14));
        assertThrows(ContaBloqueadaException.class, () -> auth.autenticar("gestor", "senha-gestor"));

        relogio.avancar(Duration.ofMinutes(1));
        assertEquals("gestor", auth.autenticar("gestor", "senha-gestor").login());
    }

    @Test
    void acertoAntesDoQuintoErroZeraAContagem() {
        errar("gestor", 4);
        auth.autenticar("gestor", "senha-gestor");

        errar("gestor", 4);
        assertEquals("gestor", auth.autenticar("gestor", "senha-gestor").login());
    }

    @Test
    void bloqueioDeUmLoginNaoAtingeOutro() {
        errar("gestor", 5);

        assertEquals("tecnico", auth.autenticar("tecnico", "senha-tecnico").login());
    }

    @Test
    void loginInexistenteTambemTravaParaNaoRevelarQuemExiste() {
        errar("ninguem", 4);

        assertThrows(ContaBloqueadaException.class, () -> auth.autenticar("ninguem", "x"));
    }

    @Test
    void trocarMaiusculaNaoDriblaOBloqueio() {
        errar("gestor", 5);

        assertThrows(ContaBloqueadaException.class, () -> auth.autenticar("GESTOR", "senha-gestor"));
    }

    @Test
    void bloqueioEntraNoLogDeAuditoria() {
        AutenticacaoService comLog = c.app.autenticacao();
        for (int i = 0; i < 4; i++) {
            assertThrows(AutenticacaoException.class, () -> comLog.autenticar("tecnico", "errada"));
        }
        assertThrows(ContaBloqueadaException.class, () -> comLog.autenticar("tecnico", "errada"));

        assertFalse(c.app.auditoria().listar().stream()
                .filter(e -> e.detalhe().contains("tecnico bloqueado"))
                .toList().isEmpty());
    }

    private void errar(String login, int vezes) {
        for (int i = 0; i < vezes; i++) {
            assertThrows(AutenticacaoException.class, () -> auth.autenticar(login, "errada"));
        }
    }

    private static final class Relogio extends Clock {

        private Instant agora;

        Relogio(Instant agora) {
            this.agora = agora;
        }

        void avancar(Duration tempo) {
            agora = agora.plus(tempo);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zona) {
            return this;
        }

        @Override
        public Instant instant() {
            return agora;
        }
    }
}
