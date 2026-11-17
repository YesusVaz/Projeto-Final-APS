package br.ufpb.aps.granja.policy;

/** Dados de entrada das politicas. O servico monta o resumo a partir do historico. */
public record ResumoMortalidade(int quantidadeInicial, int mortesNoDia, int mortesAcumuladas) {
    public ResumoMortalidade {
        if (quantidadeInicial <= 0 || mortesNoDia < 0 || mortesAcumuladas < mortesNoDia) {
            throw new IllegalArgumentException("Resumo de mortalidade inconsistente");
        }
    }

    public double percentualNoDia() {
        return mortesNoDia * 100.0 / quantidadeInicial;
    }

    public double percentualAcumulado() {
        return mortesAcumuladas * 100.0 / quantidadeInicial;
    }
}
