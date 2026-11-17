package br.ufpb.aps.granja.policy;

/** Par de limites em percentual: bateu o de atencao vira ATENCAO, bateu o critico vira CRITICO. */
record LimitesDeAlerta(double atencao, double critico) {
    LimitesDeAlerta {
        if (atencao < 0 || critico < atencao) {
            throw new IllegalArgumentException("Limites inválidos: exige 0 <= atenção <= crítico");
        }
    }

    NivelAlerta classificar(double percentual) {
        if (percentual >= critico) {
            return NivelAlerta.CRITICO;
        }
        if (percentual >= atencao) {
            return NivelAlerta.ATENCAO;
        }
        return NivelAlerta.NORMAL;
    }
}
