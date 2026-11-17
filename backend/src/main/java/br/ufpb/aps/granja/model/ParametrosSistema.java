package br.ufpb.aps.granja.model;

/**
 * UC27: parametros que o gestor ajusta (RN18). Por enquanto sao os prazos dos alertas sanitarios:
 * quantos dias antes do reforco a vacina comeca a avisar, e depois de quantos dias aberta
 * uma ocorrencia vira alerta.
 */
public record ParametrosSistema(int diasAvisoVacina, int diasOcorrenciaAberta) {

    public static final ParametrosSistema PADRAO = new ParametrosSistema(7, 7);

    public ParametrosSistema {
        if (diasAvisoVacina < 0 || diasAvisoVacina > 90) {
            throw new IllegalArgumentException("O aviso de vacina deve ficar entre 0 e 90 dias.");
        }
        if (diasOcorrenciaAberta < 1 || diasOcorrenciaAberta > 365) {
            throw new IllegalArgumentException("O prazo de ocorrência aberta deve ficar entre 1 e 365 dias.");
        }
    }
}
