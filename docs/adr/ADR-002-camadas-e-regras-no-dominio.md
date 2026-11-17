# ADR-002 — Arquitetura em camadas, regras no domínio, injeção por construtor e API REST

- **Status:** aceita em 11/11/2026 (a escolha do framework HTTP é provisória, ver "Decisão")
- **Decisor:** Yesus Lucas (Grupo 13)
- **Regras envolvidas:** RN05, RN07, RN21, RN25 · **RNF:** RNF10 (RBAC), RNF12 (camadas)

## Contexto

O Grupo 13 precisa provar que as regras de negócio (saldo, lote encerrado, política de alerta, autorização) estão corretas e testadas, e que o projeto tem uma organização que o autor consegue explicar em uma verificação individual, sem consultar IA. Além disso, o front-end React será feito à parte e precisa de uma API estável, e o escopo completo do AveTrack (30 casos de uso) vai crescer por módulos depois do núcleo.

## Opções consideradas

| Opção | Prós | Contras |
|---|---|---|
| **A. Regras nos controllers/telas, entidades só com dados** | Rápido de escrever no começo | Cada regra precisa ser testada passando por HTTP; regras se duplicam entre telas; a autorização por perfil fica fácil de esquecer |
| **B. Camadas com regras no domínio (escolhida)** | Regras testáveis por teste unitário simples; o controller fica fino; cresce por módulos sem mexer no núcleo | Mais classes e uma camada de service a manter |
| **C. Framework com container de injeção (por exemplo Spring Boot)** | Muita infraestrutura pronta | Muita "mágica" para explicar na defesa; mais tempo de configuração; esconde a composição das dependências |

## Decisão

Adotar a **opção B**, com estas regras:

1. **Camadas:** `controller` → `service` → `model`/`policy`/`repository`/`evento`. As dependências apontam para dentro; o domínio não importa nada de camadas superiores nem de bibliotecas de infraestrutura.
2. **Invariantes nas entidades:** `Lote.aplicar` recusa saldo negativo e lote encerrado; `Insumo.aplicar` recusa saldo negativo e insumo inativo; `Lote.encerrar` gera a baixa do saldo restante. Nenhuma dessas regras vive em controller.
3. **Autorização no service:** `AutorizacaoService.exigir(usuario, acao)` é a primeira chamada de cada caso de uso. O controller só converte `AcessoNegadoException` em HTTP 403.
4. **Injeção de dependências por construtor, sem container:** a classe `Main` é a única que conhece as implementações concretas e monta o grafo. Nas dependências de teste basta passar versões em memória.
5. **API REST em JSON** consumida pelo front-end React, num **monorepo** (`backend/`, `frontend/`, `docs/`). Para a API, a escolha provisória é **Javalin** (leve, sem container de injeção); será confirmada na Parcial 02 e, se mudar, este ADR é atualizado, já que a decisão de camadas não depende do framework.
6. **Mapeamento de exceções:** `AcessoNegadoException` → 403, `LancamentoDuplicadoException` → 409, demais exceções de regra → 422.
7. **Criação de políticas de alerta por Factory Method:** o Gestor escolhe o critério, e um criador concreto (`CriadorAlertaDiario` ou `CriadorAlertaAcumulado`) decide a classe da política (Strategy). O serviço recebe só a interface. Um novo critério é um novo criador, sem alterar o serviço.

## Consequências

**Positivas**
- As regras do núcleo já têm 25 testes unitários sem banco nem HTTP, rodando em segundos com `mvn test`.
- O controller não pode "esquecer" uma regra, porque ela não está lá.
- Novos módulos (sanidade, produção, financeiro) entram como novos services e entidades sem alterar o domínio existente.
- Explicável na defesa: cada regra tem um endereço único no código (ver [matriz](../matriz-rastreabilidade.md)).

**Negativas / custos**
- Mais arquivos e mais código de ligação (comandos, DTOs, conversão) do que a opção A.
- Sem container de injeção, a classe `Main` cresce com o número de services; aceitável para este tamanho.
- O front-end depende do contrato REST; mudanças na API exigem combinar as duas pontas (mesmo autor, baixo risco).

## Evidências

| Evidência | Teste | Situação |
|---|---|---|
| Invariantes do lote e do insumo testadas só no domínio | T01–T11 | ✅ passando |
| Permissões por perfil testadas no domínio | T15–T18 | ✅ passando |
| Política de alerta trocável sem alterar o chamador | T19–T22 | ✅ passando |
| Criação da política pelo criador concreto, sem o chamador conhecer a classe | T23–T25 | ✅ passando |
| Service nega por perfil e controller responde 403 | P06 | 🔶 24/11 |

## Revisão

Reavaliar a escolha do framework HTTP na Parcial 02 e a divisão de pacotes se o número de casos de uso Meta crescer.
