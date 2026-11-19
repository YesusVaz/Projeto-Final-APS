package br.ufpb.aps.granja;

import br.ufpb.aps.granja.model.Perfil;

/** Usuarios de demo criados na primeira subida. Senhas vem de variaveis de ambiente. */
final class UsuariosDeDemonstracao {

    private UsuariosDeDemonstracao() {
    }

    /** Cria 3 usuarios se o banco estiver vazio. */
    static void semear(Composicao.Aplicacao app) {
        if (app.usuariosRepositorio().contar() > 0) {
            return;
        }
        app.autenticacao().cadastrar("gestor", "gestor@granja.local", "Gestor da granja",
                senha("GRANJA_SENHA_GESTOR", "gestor123"), Perfil.GESTOR);
        app.autenticacao().cadastrar("tecnico", "tecnico@granja.local", "Técnico de campo",
                senha("GRANJA_SENHA_TECNICO", "tecnico123"), Perfil.TECNICO);
        app.autenticacao().cadastrar("funcionario", "funcionario@granja.local", "Funcionário",
                senha("GRANJA_SENHA_FUNCIONARIO", "funcionario123"), Perfil.FUNCIONARIO);
        System.out.println("Usuários de demonstração criados. Troque as senhas antes de qualquer uso real.");
    }

    private static String senha(String variavel, String padrao) {
        String valor = System.getenv(variavel);
        return valor == null || valor.isBlank() ? padrao : valor;
    }
}
