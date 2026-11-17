# Frontend

Interface web do Sistema de Gestão para Granjas. É feita separadamente do backend e **fica fora do build Maven**.

**Stack:** React + Vite + TypeScript + Tailwind CSS.

## Criar o projeto

Esta pasta está vazia de propósito. Na raiz do repositório:

```bash
npm create vite@latest frontend -- --template react-ts
cd frontend
npm install
npm run dev
```

## Regras para a demonstração

O PDF da disciplina pede que funcionalidades não concluídas sejam declaradas e que não se apresentem telas ou dados estáticos como se estivessem implementados. Por isso:

- só ligue à API o que o backend já implementa (ver o [status no README](../README.md#status-por-entrega));
- para o que ainda não existe, a tela deve mostrar claramente **"não implementado"**, sem dados falsos;
- o protótipo do Figma do AveTrack serve de referência visual, mas não é a especificação.

## Contrato com o backend

A API REST será definida na Parcial 02. Mapeamento de erros esperado: 403 (sem permissão), 409 (lançamento duplicado) e 422 (violação de regra de negócio). Ver [`docs/arquitetura.md`](../docs/arquitetura.md).
