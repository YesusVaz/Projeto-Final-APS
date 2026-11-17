package br.ufpb.aps.granja.api;

import java.util.List;

/** Agrupa as rotas de um recurso. Cada controller so traduz HTTP, as regras ficam no servico. */
interface Controller {

    List<Rota> rotas();
}
