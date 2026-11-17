package br.ufpb.aps.granja.api;

import br.ufpb.aps.granja.api.dto.SessaoDto;
import br.ufpb.aps.granja.api.dto.UsuarioDto;
import br.ufpb.aps.granja.model.Perfil;
import br.ufpb.aps.granja.model.Usuario;
import br.ufpb.aps.granja.service.AutenticacaoService;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.List;

import static br.ufpb.aps.granja.api.CorpoJson.booleanoOpcional;
import static br.ufpb.aps.granja.api.CorpoJson.enumeracao;
import static br.ufpb.aps.granja.api.CorpoJson.senha;
import static br.ufpb.aps.granja.api.CorpoJson.texto;

/** Login, logout e cadastro de usuarios. Login e cadastro publico sao as unicas rotas sem token. */
final class AutenticacaoController implements Controller {

    private final AutenticacaoService autenticacao;

    AutenticacaoController(AutenticacaoService autenticacao) {
        this.autenticacao = autenticacao;
    }

    @Override
    public List<Rota> rotas() {
        return List.of(
                Rota.postPublica("/api/login", this::login),
                Rota.postPublica("/api/cadastro", this::cadastroPublico),
                Rota.post("/api/logout", this::logout),
                Rota.post("/api/usuarios", this::cadastrarUsuario));
    }

    private Resposta login(Requisicao r) {
        JsonNode c = r.corpo();
        return Resposta.ok(SessaoDto.de(autenticacao.entrar(texto(c, "login"), senha(c))));
    }

    private Resposta logout(Requisicao r) {
        autenticacao.sair(r.token());
        return Resposta.ok(Collections.singletonMap("mensagem", "Sessão encerrada."));
    }

    /** Cadastro publico. Perfil e ativo, se vierem no corpo, sao ignorados: quem se cadastra sozinho nao escolhe isso. */
    private Resposta cadastroPublico(Requisicao r) {
        JsonNode c = r.corpo();
        Usuario novo = autenticacao.cadastrarPublico(texto(c, "login"), texto(c, "email"), texto(c, "nome"),
                senha(c));
        return Resposta.criado(UsuarioDto.de(novo));
    }

    /** Cadastro feito pelo gestor logado. Aqui perfil e status sao escolhidos. */
    private Resposta cadastrarUsuario(Requisicao r) {
        JsonNode c = r.corpo();
        Usuario novo = autenticacao.cadastrarUsuario(r.usuario(), texto(c, "login"), texto(c, "email"),
                texto(c, "nome"), senha(c), enumeracao(Perfil.class, c, "perfil"),
                booleanoOpcional(c, "ativo", true));
        return Resposta.criado(UsuarioDto.de(novo));
    }
}
