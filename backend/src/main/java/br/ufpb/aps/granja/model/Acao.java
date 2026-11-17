package br.ufpb.aps.granja.model;

/** Acoes que passam pela checagem de perfil (RN25). */
public enum Acao {
    CONSULTAR,
    CADASTRAR_LOTE,
    ENCERRAR_LOTE,
    /** Venda, descarte ou transferencia de parte do lote, sem encerrar. */
    REGISTRAR_BAIXA,
    REGISTRAR_MORTALIDADE,
    CADASTRAR_INSUMO,
    MOVIMENTAR_ESTOQUE,
    CONSULTAR_ALERTAS_ESTOQUE,
    REGISTRAR_ALIMENTACAO,
    CONFIGURAR_LIMITES,
    /** So o gestor cadastra usuario escolhendo perfil e status. */
    CADASTRAR_USUARIO,
    /** Listar usuarios e ativar ou inativar (UC01, RN02). */
    GERENCIAR_USUARIOS,
    /** UC03: ler o log de auditoria. */
    CONSULTAR_AUDITORIA,
    /** UC10. */
    CADASTRAR_FORNECEDOR,
    /** UC11. */
    REGISTRAR_VACINACAO,
    /** UC12: abrir ocorrencia, tratar e encerrar. */
    REGISTRAR_OCORRENCIA,
    /** UC24. */
    CONSULTAR_ALERTAS_SANITARIOS,
    /** UC16: postura e pesagem. */
    REGISTRAR_PRODUCAO
}
