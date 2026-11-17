# ADR-001 — Persistência em SQLite, com transação e `UNIQUE` no código de lançamento

- **Status:** aceita em 11/11/2026
- **Decisor:** Yesus Lucas (Grupo 13)
- **Regras envolvidas:** RN22 (lançamento não se repete), RN23 (tudo-ou-nada, saldo = histórico), RN07 (saldo nunca negativo)

## Contexto

O sistema precisa guardar lotes, movimentos, insumos e usuários. Duas exigências do desafio dependem diretamente da persistência:

1. **Não repetir o mesmo lançamento.** Se o usuário clicar duas vezes ou a requisição for reenviada, o saldo não pode mudar duas vezes. A checagem em código ("já existe esse código?") sofre corrida: duas requisições simultâneas podem passar pela checagem antes de qualquer uma gravar.
2. **Saldo igual à soma do histórico.** O movimento e o novo saldo precisam ser gravados juntos; se um falhar, o outro não pode ficar.

Restrições: projeto individual, prazo curto (entrega final em 01/12), execução em outro computador sem instalar servidor, e o professor poder rodar o projeto a partir do repositório.

## Opções consideradas

| Opção | Prós | Contras |
|---|---|---|
| **Arquivos JSON ou CSV** | Nenhuma dependência; fácil de ler | Sem transação nem restrição de unicidade; gravar movimento e saldo em arquivos diferentes pode deixar os dois inconsistentes; unicidade e concorrência teriam de ser reinventadas em código |
| **SQLite (escolhida)** | Um arquivo local, sem servidor; transações ACID; `UNIQUE`, chaves estrangeiras e `CHECK` garantidos pelo banco; driver JDBC é uma dependência Maven | Escrita concorrente limitada (aceitável para uma granja e para a demonstração); SQL escrito à mão |
| **PostgreSQL** | Robusto, concorrência real | Exige instalar e configurar um servidor, o que dificulta a execução "em outro ambiente" pedida na entrega; excessivo para o escopo |

## Decisão

Usar **SQLite**, acessado por **JDBC puro** (sem ORM), com:

- uma classe `Transacao` com `executar(Supplier<T>)` que abre a transação, executa o bloco e faz commit, ou rollback em qualquer exceção;
- índice **`UNIQUE` em `codigo_lancamento`** nas tabelas `movimento_lote` e `movimento_estoque`;
- `CHECK (saldo >= 0)` e `CHECK (quantidade > 0)` como segunda barreira, além das regras do domínio;
- chaves estrangeiras ativas (`PRAGMA foreign_keys = ON`);
- repositórios definidos como **interfaces** no pacote `repository`; a implementação SQLite fica em `repository.sqlite`, e os testes de serviço podem usar uma versão em memória.

## Consequências

**Positivas**
- A unicidade do lançamento passa a ser garantida pelo banco, valendo também sob concorrência; o serviço só adiciona a mensagem de erro clara (409).
- Movimento e saldo gravados na mesma transação mantêm a invariante da RN23.
- O projeto roda em qualquer máquina com Java e Maven: o arquivo `.db` é criado na primeira execução e pode ser versionado como dado de demonstração.
- A troca de banco no futuro afeta apenas `repository.sqlite`, graças às interfaces.

**Negativas / custos**
- SQL e mapeamento objeto-relacional ficam por nossa conta (mais código que com um ORM).
- SQLite serializa escritas: não serve para uma operação com muitos usuários simultâneos. Isso fica registrado como limitação conhecida.
- As regras continuam no domínio; o `CHECK` do banco é só uma rede de segurança, não a regra principal.

## Evidências

| Evidência | Teste | Situação |
|---|---|---|
| A invariante saldo = soma do histórico já é verificada no domínio | T07, T08 | ✅ passando |
| `UNIQUE` rejeita código duplicado no banco | P02 | 🔶 24/11 |
| Rollback desfaz lote e movimento inicial quando algo falha | P03 | 🔶 24/11 |
| Saldo lido do banco = soma do histórico lido do banco | P04 | 🔶 24/11 |

## Revisão

Reavaliar se a demonstração exigir concorrência real entre vários usuários ou se o volume de dados crescer de forma que o SQLite deixe de atender.
