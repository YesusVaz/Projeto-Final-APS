# Declaração e registro de uso de IA

**Projeto:** Sistema de Gestão para Granjas · Grupo 13 · APS 2026.2
**Autor:** Yesus Lucas

## 1. Declaração

Foi usada IA no desenvolvimento deste projeto.

| Ferramenta | Tipo de atividade |
|---|---|
| Claude (Anthropic), em modo de agente com acesso ao repositório e ao terminal | Leitura dos documentos do AveTrack e das orientações da disciplina; proposta de escopo e de regras de negócio; rascunho de documentação (requisitos, arquitetura, ADRs, matriz); modelagem dos diagramas UML em Mermaid; esqueleto do domínio em Java e dos testes unitários; roteiro e slides da Parcial 01 |

**O que a IA não substitui:** a responsabilidade pela correção, pelas decisões e pela compreensão do que foi entregue é do autor. O autor deve conseguir explicar e alterar cada regra e cada classe sem consultar IA na verificação individual.

## 2. Como as sugestões são verificadas

- Os requisitos propostos pela IA foram conferidos contra o texto das orientações (`Orientações Projeto APS 2026.2`) e contra os documentos do AveTrack.
- Os testes unitários foram escritos pela mesma IA que escreveu o domínio, então eles **também precisam ser confrontados com os requisitos** (por exemplo, o limite exato de uma política de alerta, a fronteira de saldo igual à saída). Cada teste está ligado a uma regra na [matriz de rastreabilidade](matriz-rastreabilidade.md).
- Os testes são executados com `mvn test` e os resultados são lidos, não presumidos.

## 3. Interações importantes

### Interação 1 — Definição do escopo e do plano da Parcial 01 (11/11/2026)

- **Pedido:** montar toda a primeira entrega, com base nas orientações e no AveTrack.
- **Resposta da IA:** leu o PDF, não conseguiu abrir os documentos do AveTrack (exigiam login ou só permitiam download) e propôs um plano com um recorte só de lotes e requisitos próprios.
- **Decisão do autor:** pediu para usar o **escopo completo** do AveTrack, e não só lotes, e enviou os documentos do AveTrack baixados.
- **Consequência:** o escopo passou a ter 30 casos de uso em 8 módulos, divididos em Núcleo, Meta e Declarado, para manter a profundidade que a avaliação exige.

### Interação 2 — Correção de erros da IA no domínio e nos documentos (11/11/2026)

- **Erros encontrados:**
  1. A IA contou 6 casos de uso no núcleo; a contagem certa é 7 (UC02, UC04, UC06, UC07, UC08, UC09, UC13).
  2. O AveTrack exige registrar a **causa** da mortalidade (US06), e a primeira versão de `MovimentoLote` não tinha esse campo; foi acrescentado o campo `observacao`.
  3. Um comentário no `pom.xml` com `--release` quebrou o Maven, porque XML não aceita dois hífens seguidos em comentário. O texto foi reescrito.
- **Verificação:** `mvn test` voltou a passar (22 testes) depois das correções.

### Interação 3 — Revisão das lacunas do AveTrack (11/11/2026)

- **Pedido:** revisar a referência e especificar requisitos próprios.
- **Análise da IA:** apontou que a RN06 (saldo = inicial − mortes) ignora vendas, descartes e transferências, que o encerramento de lote não tinha regra para o saldo restante e que nada impedia o lançamento duplicado nem definia atomicidade.
- **Decisão do autor:** aceitou transformar essas lacunas em regras novas (RN06 revisada e RN21–RN25).
- **Evidência:** [`docs/requisitos.md`](requisitos.md#1-origem-e-adaptações) e testes T01–T08, T19–T22.

## 4. Pendências deste registro

Este registro será atualizado nas próximas entregas com as interações relevantes da Parcial 02 e do pacote final (por exemplo, a melhoria concreta da revisão crítica e a implementação do Observer).

**Mudança após a Interação 3:** a pedido do autor, foi adicionado um segundo padrão de categoria diferente: o Factory Method (`CriadorPoliticaAlerta`, com testes T23–T25), que cria a política de alerta conforme o critério escolhido. O Observer segue planejado para 24/11 como terceiro padrão. Os testes do Factory Method também foram conferidos contra a regra RN24 antes de entrar no slide.
