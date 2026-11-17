# Frontend

Interface web do Sistema de Gestão para Granjas. É feita separadamente do backend e **fica fora do build Maven**.

**Stack:** React + Vite + TypeScript + Tailwind CSS.

<<<<<<< HEAD
## Como rodar

1. Suba o backend. Rode a classe `br.ufpb.aps.granja.Main` (pela IDE, ou com o classpath do Maven). Ela sobe a API em `http://127.0.0.1:8080/api`. A porta muda pela variável `PORTA`.
2. Na pasta `frontend`:

```bash
=======
## Criar o projeto

Esta pasta está vazia de propósito. Na raiz do repositório:

```bash
npm create vite@latest frontend -- --template react-ts
cd frontend
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
npm install
npm run dev
```

<<<<<<< HEAD
O Vite abre em `http://localhost:5173`. As chamadas para `/api` passam por um proxy para `http://127.0.0.1:8080`. Se a API estiver em outro endereço, use a variável `GRANJA_API`.

### Usuários de demonstração

O `Main` cria três usuários. As senhas padrão são estas, e podem ser trocadas pelas variáveis `GRANJA_SENHA_GESTOR`, `GRANJA_SENHA_TECNICO` e `GRANJA_SENHA_FUNCIONARIO`:

| Login | Senha padrão | Perfil |
|---|---|---|
| `gestor` | `gestor123` | Gestor |
| `tecnico` | `tecnico123` | Técnico |
| `funcionario` | `funcionario123` | Funcionário |

## O que está ligado à API

- **Entrar:** `POST /api/login` confere a senha com bcrypt e devolve um token. O front guarda só o token, em memória, e manda `Authorization: Bearer` nas chamadas seguintes. A senha não fica guardada depois do login. A sessão vale 8 horas. "Sair" chama `POST /api/logout`, que invalida o token.
- **Cadastro de usuário:** a aba é pública. Sem login, ela manda `POST /api/cadastro` com e-mail, usuário, nome e senha. A conta nasce como Funcionário e já fica ativa. Perfil e status nunca vêm do formulário público. Com o gestor logado, o mesmo formulário ganha as opções de perfil e de status e manda `POST /api/usuarios`, que exige token de gestor e responde 403 para os outros perfis. Usuário ou e-mail repetido volta 409.
- **Lotes, mortalidade, alimentação e estoque:** ainda não ligados ao front.

## Como o front fala com a API

Toda chamada passa por `src/api/http.ts`. Essa camada converte o perfil: a API manda `GESTOR`, `TECNICO` e `FUNCIONARIO` (nomes do enum), e o front trabalha em minúsculo.

=======
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
## Regras para a demonstração

O PDF da disciplina pede que funcionalidades não concluídas sejam declaradas e que não se apresentem telas ou dados estáticos como se estivessem implementados. Por isso:

<<<<<<< HEAD
- só ligue à API o que o backend já implementa;
- para o que ainda não existe, a tela deve mostrar claramente **"não implementado"**, sem dados falsos;
- o protótipo visual serve de referência, mas não é a especificação.

## Contrato com o backend

Erros devolvidos pela API no corpo `{"erro": "..."}`: 401 (sem token, token vencido ou credencial errada, com `WWW-Authenticate: Bearer`), 403 (sem permissão), 404, 405, 409 (lançamento, usuário ou e-mail repetido), 422 (regra de negócio) e 500. Ver [`docs/arquitetura.md`](../docs/arquitetura.md).

## Padrões de tela

- Todo controle de formulário tem 3rem de altura (`campo` em `src/index.css`, `Botao` em `src/componentes/Botao.tsx`).
- Rótulo, controle e aviso de `Campo` ficam sempre na mesma altura, pra tela não pular quando aparece uma mensagem.
- Entrar e Cadastro usam a mesma largura de coluna para o formulário.
- A navbar (`src/componentes/Navegacao.tsx`) centraliza as opções. O destaque desliza na troca de aba e pode ser arrastado: ao soltar, encaixa na opção mais próxima. As setas do teclado também trocam de aba.
- Códigos de requisito (RN, RNF) e numeração não aparecem na interface. Ficam nos documentos.
=======
- só ligue à API o que o backend já implementa (ver o [status no README](../README.md#status-por-entrega));
- para o que ainda não existe, a tela deve mostrar claramente **"não implementado"**, sem dados falsos;
- o protótipo do Figma do AveTrack serve de referência visual, mas não é a especificação.

## Contrato com o backend

A API REST será definida na Parcial 02. Mapeamento de erros esperado: 403 (sem permissão), 409 (lançamento duplicado) e 422 (violação de regra de negócio). Ver [`docs/arquitetura.md`](../docs/arquitetura.md).
>>>>>>> a3481d5f595d5a9c67769d30c2b2a373c1db74b1
