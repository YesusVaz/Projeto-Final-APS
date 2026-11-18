package br.ufpb.aps.granja.model;

/** Acoes que passam pela checagem de perfil (RN25). */
public enum Acao {
    CONSULTAR,
    CADASTRAR_LOTE,
    ENCERRAR_LOTE,
    /** Venda, descarte ou transferencia de parte do lote, sem encerrar. */
    REGISTRAR_BAIXA,
    /** Acrescentar animais a um lote que ja existe. */
    REGISTRAR_ENTRADA_ANIMAIS,
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
    REGISTRAR_PRODUCAO,
    /** UC19 a UC22: compras, despesas, receitas e relatorio financeiro. So o Gestor. */
    GERENCIAR_FINANCEIRO,
    /** UC14. */
    REGISTRAR_HIGIENIZACAO,
    /** UC15. */
    REGISTRAR_ILUMINACAO,
    /** UC18: produtividade do lote. So o Gestor. */
    CONSULTAR_PRODUTIVIDADE,
    /** UC23, UC25 e UC26: painel, relatorio gerencial e graficos. So o Gestor. */
    CONSULTAR_GESTAO,
    /** UC30: qualquer perfil abre um chamado de suporte. */
    ABRIR_CHAMADO,
    /** UC30: ver todos os chamados e responder. So o Gestor. */
    GERENCIAR_CHAMADOS,
    /** UC29: exportar dados em CSV. So o Gestor. */
    EXPORTAR_DADOS,
    /** UC28: fazer e baixar backup do banco. So o Gestor. */
    GERENCIAR_BACKUP
}
