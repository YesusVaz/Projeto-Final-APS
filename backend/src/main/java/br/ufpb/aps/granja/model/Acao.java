package br.ufpb.aps.granja.model;

/** Acoes do nucleo que passam pela checagem de perfil (RN25). */
public enum Acao {
    CONSULTAR,
    CADASTRAR_LOTE,
    ENCERRAR_LOTE,
    REGISTRAR_MORTALIDADE,
    CADASTRAR_INSUMO,
    MOVIMENTAR_ESTOQUE,
    CONSULTAR_ALERTAS_ESTOQUE,
    REGISTRAR_ALIMENTACAO,
    CONFIGURAR_LIMITES,
    /** So o gestor cadastra usuario. Nao existe cadastro publico. */
    CADASTRAR_USUARIO
}
