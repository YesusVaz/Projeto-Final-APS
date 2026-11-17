package br.ufpb.aps.granja.model;

/** Motivo da movimentacao do estoque (RN08, RN09, RN10, RN13 geram entrada ou saida sozinhas). */
public enum OrigemMovimento { COMPRA, ALIMENTACAO, VACINACAO, TRATAMENTO, USO_GERAL }
