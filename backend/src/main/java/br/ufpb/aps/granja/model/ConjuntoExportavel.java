package br.ufpb.aps.granja.model;

import br.ufpb.aps.granja.model.excecao.NaoEncontradoException;

/** UC29: o que o Gestor pode levar embora em CSV. Os de periodo respeitam o de/ate; os outros sao o retrato de hoje. */
public enum ConjuntoExportavel {
    LOTES("lotes", false),
    ESTOQUE("estoque", false),
    ALIMENTACAO("alimentacao", true),
    MORTALIDADE("mortalidade", true),
    PRODUCAO("producao", true),
    FINANCEIRO("financeiro", true);

    private final String chave;
    private final boolean usaPeriodo;

    ConjuntoExportavel(String chave, boolean usaPeriodo) {
        this.chave = chave;
        this.usaPeriodo = usaPeriodo;
    }

    public String chave() {
        return chave;
    }

    public boolean usaPeriodo() {
        return usaPeriodo;
    }

    public static ConjuntoExportavel daChave(String chave) {
        for (ConjuntoExportavel c : values()) {
            if (c.chave.equals(chave)) {
                return c;
            }
        }
        throw new NaoEncontradoException("Não há exportação chamada \"" + chave + "\".");
    }
}
