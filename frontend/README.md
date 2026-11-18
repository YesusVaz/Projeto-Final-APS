# Frontend

Interface web do Sistema de Gestão para Granjas. É feita separadamente do backend e **fica fora do build Maven**.

**Stack:** React 19, TypeScript, Vite, Tailwind CSS v4, React Router 7 e Motion (animações). Lint com oxlint.

## Como rodar

1. Suba o backend. Rode a classe `br.ufpb.aps.granja.Main` (pela IDE, ou com o classpath do Maven). Ela sobe a API em `http://127.0.0.1:8080/api`. A porta muda pela variável `PORTA`.
2. Na pasta `frontend`:

```bash
npm install
npm run dev
```

O Vite abre em `http://localhost:5173`. As chamadas para `/api` passam por um proxy para `http://127.0.0.1:8080`. Se a API estiver em outro endereço, use a variável `GRANJA_API`.

Antes de entregar, rode `npm run lint`, `npx tsc -p tsconfig.app.json --noEmit` e `npm run build`. Os três têm que passar sem erro nem aviso.

### Usuários de demonstração

O `Main` cria três usuários. As senhas padrão são estas, e podem ser trocadas pelas variáveis `GRANJA_SENHA_GESTOR`, `GRANJA_SENHA_TECNICO` e `GRANJA_SENHA_FUNCIONARIO`:

| Login | Senha padrão | Perfil |
|---|---|---|
| `gestor` | `gestor123` | Gestor |
| `tecnico` | `tecnico123` | Técnico |
| `funcionario` | `funcionario123` | Funcionário |

## O que existe

| Área | Telas | Quem usa |
|---|---|---|
| Acesso | Entrar, Criar conta | público |
| Início | resumo, alertas recentes, atalhos | todos |
| Lotes | lista, detalhe com histórico por abas, novo lote, adicionar aves, mortalidade, alimentação, baixa, encerramento; menu "Manejo" (higienização, iluminação); produtividade do lote | conforme o perfil; produtividade só o gestor |
| Estoque | lista, novo insumo, entrada e saída | funcionário, gestor |
| Sanidade | ocorrências, tratamentos, vacinações | técnico, gestor |
| Produção | postura, pesagem e gráfico do indicador | técnico, funcionário, gestor |
| Alertas | mortalidade, sanidade e estoque | todos (abas conforme o perfil) |
| Fornecedores | lista e cadastro | gestor |
| Financeiro | compras (dão entrada no estoque), despesas, receitas e relatório do período | gestor |
| Neste mês (no Início) | receitas, despesas, lucro, aves perdidas, ração e ovos do mês | gestor |
| Relatórios | gráficos de desempenho e relatório gerencial | gestor |
| Dados | exportação em CSV e cópias de segurança do banco | gestor |
| Suporte | abrir chamado e acompanhar; o gestor vê todos e responde | todos |
| Menu do perfil | Configurações, Suporte, Relatórios, Dados, Usuários, Auditoria | Configurações e Suporte para todos; o resto só o gestor |

Quem decide o que cada perfil pode fazer é o backend (responde 403). O front só esconde o que a pessoa não pode usar, em `src/sessao/permissoes.ts`.

## Estrutura

- `src/api/`: um módulo por área. `http.ts` faz a chamada e converte o perfil (a API manda `GESTOR`, `TECNICO` e `FUNCIONARIO`; o front usa minúsculo).
- `src/sessao/`: sessão (guardada em `sessionStorage`), `RotaProtegida` e permissões.
- `src/layout/`: navbar em pílula (desktop), barra de baixo (celular) e a troca de rota animada.
- `src/telas/`: uma pasta por área. `comum/` guarda o que as áreas dividem.
- `src/ui/`: componentes visuais. `/#vitrine` abre a conferência deles.
- `docs/design/`: desenho das telas, só como referência.

## Padrões de tela

- Registros e cadastros abrem em painel lateral, com o fundo desfocado, sobre a lista da área (`?novo=1`, `?mortalidade=1`, `?alimentacao=1`, `?higienizacao=1`, `?iluminacao=1`, `?entrada=1`, `?vacinar=1`, `?compra=1`, `?despesa=1`, `?receita=1`). Telas inteiras ficam para listas e relatórios.
- Todo envio usa um código de lançamento (idempotência): duplo clique ou reenvio não duplica nada.
- Toda tela tem estado de carregando, vazio, erro e sucesso.
- A navbar destaca a tela atual com um destaque que desliza e pode ser arrastado. As setas do teclado também trocam de tela.
- Códigos de requisito (RN, RNF) não aparecem na interface. Ficam nos documentos.

## Regras para a demonstração

O PDF da disciplina pede que funcionalidades não concluídas sejam declaradas e que não se apresentem telas ou dados estáticos como se estivessem implementados. Por isso, só se liga à API o que o backend já implementa, e o que ainda não existe fica declarado em [`docs/requisitos.md`](../docs/requisitos.md).

## Contrato com o backend

Erros vêm no corpo `{"erro": "..."}`: 401 (sem token, token vencido ou credencial errada), 403 (sem permissão), 404, 405, 409 (lançamento, usuário ou e-mail repetido), 422 (regra de negócio) e 500. Ver [`docs/arquitetura.md`](../docs/arquitetura.md).
