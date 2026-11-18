# Prompt para o Copilot (Claude Sonnet): Lotes 2 e 3 do frontend

Cole o bloco "CONTEXTO" no início do chat. Depois cole o "LOTE 2". Só quando o lote 2 estiver rodando e commitado, abra um chat novo (ou use o mesmo), cole de novo o "CONTEXTO" e o "LOTE 3".

---

## CONTEXTO (colar sempre)

Você vai continuar o frontend do projeto "Sistema de Gestão para Granjas" (APS 2026.2, UFPB Campus IV). O backend Java já está pronto e não deve ser alterado. O lote 1 (base visual e componentes) já existe em `frontend/src/ui`.

### Stack e regras do projeto
- React 19, TypeScript ~7.0.2, Vite 8, Tailwind CSS v4 (tokens em `@theme` no `src/index.css`), oxlint.
- tsconfig com `erasableSyntaxOnly` e `verbatimModuleSyntax`: sem `enum`, sem `namespace`, use `import type` para tipos.
- React 19: `ref` é prop normal, contexto usa `<Contexto value=...>`, não use `forwardRef`.
- Nomes em português (componentes, funções, variáveis, comentários curtos em português informal, explicando o porquê e não o óbvio).
- Sem emoji. Sem biblioteca de UI externa. Ícones só pelo `Icone` de `src/ui`.
- Acessibilidade: elementos reais (`button`, `a`, `label`, `dialog`), foco visível, contraste mínimo 4.5:1, alvos de toque de pelo menos 44px no celular, respeitar `prefers-reduced-motion`.
- Rodar sempre no fim: `npm run lint` e `npx tsc -p tsconfig.app.json --noEmit` (ou `npm run build`). Tem que passar sem erro nem warning.
- Não faça commit. Não mexa em `backend/`. Não apague arquivos antigos que eu não citei.

### O que já existe
- `src/ui/` (barrel `src/ui/index.ts`): Botao (primario/acao/contorno/fantasma/perigo; sm/md/lg/icone), Campo (render-prop), Entrada, AreaTexto, Contador, Selecao, Selo (normal/atencao/critico/info/neutro), Abas/Fichas, Tabela (+Cabeca/Corpo/Linha/Cab/Cel), Painel e Dialogo (`<dialog>` nativo + `useDialogo`), Menu/MenuItem, Aviso, ToastProvider + `useToast`, Progresso, Interruptor, Caixa, NavPilula (navbar desktop estilo Apple), NavInferior (celular), Estrutura (Pagina, CabecalhoPagina, Cartao, TituloSecao, ItemLista, Estatistica, Marca, Avatar), Icone.
- `src/telas/Vitrine.tsx` mostra todos os componentes (abrir em `/#vitrine`). Use como exemplo de uso.
- `src/api/http.ts` (`chamar`, `ErroApi`, `Perfil`, `perfilDaApi`), `src/api/sessao.ts` (`entrar`, `sair`), `src/api/usuarios.ts`. O Vite faz proxy de `/api` para `http://127.0.0.1:8080`.
- `src/App.tsx`, `src/telas/Entrar.tsx`, `src/telas/Cadastro.tsx` e `src/componentes/*` são do código antigo. Serão substituídos neste trabalho (veja o lote 2).
- `src/main.tsx` já envolve tudo em `<ToastProvider>` e tem o atalho `#vitrine`. Mantenha o atalho.

### Referência visual (obrigatória)
As telas desenhadas estão em `docs/design/telas/*.html` (um fragmento HTML por tela, `d-m-*` = celular 390x844) e as classes que elas usam em `docs/design/estilo-base.css`. São só referência de estrutura, textos, hierarquia e dados de exemplo. Não copie o HTML: monte cada tela com os componentes de `src/ui` e utilitários Tailwind. Leia o fragmento da tela antes de implementá-la. Se algo do desenho não existir como componente, crie um componente novo em `src/ui` seguindo o padrão dos outros e exporte no `index.ts`.

Princípios do desenho: poucos cartões, muito espaço, uma ação principal por tela, tipografia clara, cor de destaque só em ação e estado, estados sempre com texto e forma (nunca só cor).

### Animações: biblioteca Motion
Use a biblioteca **Motion** (`motion`, import de `motion/react`) para as animações de interface. Instalar com `npm install motion` (rodar no Windows, na pasta `frontend`). Regras:
- Entrada de página: fade + subida curta (cerca de 8px, 220 a 280ms, ease-out). Listas entram em cascata (stagger de 30 a 40ms, no máximo 10 itens animados).
- Troca de rota com `AnimatePresence` (modo `wait`, saída de 120ms).
- Pílula da navbar: use `layoutId` para o destaque deslizar entre itens. Substitui a implementação atual com medição manual, se ficar mais simples.
- Barras e progresso: animar largura de 0 até o valor (600ms, ease-out). Contadores numéricos com `animate`/`useMotionValue`.
- Toasts, Painel e Dialogo: entrada e saída com Motion, sem biblioteca extra.
- Sempre respeitar `prefers-reduced-motion` (`useReducedMotion` ou `MotionConfig reducedMotion="user"` na raiz).
- Não anime propriedades que causam reflow pesado em lista grande. Prefira `transform` e `opacity`.
- Crie `src/ui/movimento.ts` com as variantes e durações compartilhadas (`entrada`, `lista`, `item`, `tempos`) e use em todo lugar, para a sensação ficar uniforme.

### Backend (contrato da API)
Todas as rotas começam com `/api`. Autenticação por `Authorization: Bearer <token>` (o `chamar` já faz isso se passar `token`). `chamar` hoje só aceita `GET | POST | PUT`, o que basta. Erros vêm com a mensagem em português no corpo e `ErroApi.status` (401 = sessão inválida: limpar sessão e ir para entrar; 403 = sem permissão; 400/409/422 = mostrar a mensagem no formulário).

Perfis (vêm em MAIÚSCULO, o front usa minúsculo via `perfilDaApi`): `gestor` (tudo), `tecnico` (consultar, cadastrar/encerrar lote, baixa, vacinação, ocorrência, alertas sanitários, produção), `funcionario` (consultar, mortalidade, movimentar estoque, alimentação, produção). Esconda no front o que o perfil não pode fazer, mas lembre que o backend é quem decide (trate 403).

Rotas e corpos:
- Público: `POST /login {login, senha}` → `{token, login, nome, perfil}`; `POST /cadastro {login, email, nome, senha}`.
- `POST /logout`.
- Usuários (gestor): `GET /usuarios`; `POST /usuarios {login,email,nome,senha,perfil,ativo}`; `PUT /usuarios/{id}/status {ativo}`. Retorno: `{id, login, email, nome, perfil, ativo}`.
- Lotes: `GET /lotes` → `[{id, codigo, especie, quantidadeInicial, saldo, status}]` (especie: `FRANGO_CORTE | POEDEIRA`; status: `ATIVO | ENCERRADO`). `POST /lotes {codigo, especie, quantidadeInicial, dataAlojamento?, idadeInicialDias?}`. `GET /lotes/{id}` → lote + `consistente` + `historico: [{id, tipo, quantidade, codigo, dataHora, observacao}]` (tipo: `ENTRADA_INICIAL | MORTALIDADE | BAIXA_VENDA | ...`). `POST /lotes/{id}/encerramento`. `GET /lotes/{id}/idade` → `{loteId, codigo, dataAlojamento, idadeInicialDias, data, idadeDias, semanas}`. `GET /lotes/{id}/historico` → `{lote, consistente, itens:[{data, hora, categoria, descricao}]}`.
- Mortalidade e baixa: `POST /lotes/{id}/mortalidades {quantidade, observacao?, codigoLancamento}` → lote + `nivelAlerta`. `POST /lotes/{id}/baixas {quantidade, observacao?, codigoLancamento}`.
- `GET /alertas-mortalidade` → `[{loteId, codigoLote, nivel, dataHora}]` (nivel: `ATENCAO | CRITICO`, confirmar no retorno real).
- Alimentação: `POST /lotes/{id}/alimentacoes {insumoId, quantidadeKg, data?, codigoLancamento}`.
- Insumos/estoque: `GET /insumos` → `[{id, nome, categoria, unidade, nivelMinimo, saldo, ativo, abaixoDoMinimo}]` (categoria: `RACAO | VACINA | MEDICAMENTO | MATERIAL`). `POST /insumos {nome, categoria, unidade, nivelMinimo}`. `GET /insumos/alertas`. `POST /insumos/{id}/movimentos {tipo: ENTRADA|SAIDA, quantidade, origem, codigoLancamento, ...}` (origem: `COMPRA | ALIMENTACAO | VACINACAO | TRATAMENTO | USO_GERAL`). Leia `InsumoController.java` para os campos exatos do movimento (fornecedor, etc.).
- Fornecedores: `GET /fornecedores`; `POST /fornecedores {nome, documento, telefone?, email?}` → `{id, nome, documento, tipoDocumento: CPF|CNPJ, telefone, email}`.
- Sanidade: `POST /lotes/{id}/vacinacoes {insumoId, doses, data?, proximaDose?, observacao?, codigoLancamento}`; `GET /lotes/{id}/vacinacoes`; `POST /lotes/{id}/ocorrencias {descricao, data?}`; `GET /ocorrencias`; `GET /ocorrencias/{id}` (com `tratamentos`); `POST /ocorrencias/{id}/tratamentos {insumoId, quantidade, data?, observacao?, codigoLancamento}`; `POST /ocorrencias/{id}/encerramento {data?}`; `GET /alertas-sanitarios` → `[{tipo, loteId, codigoLote, descricao, referencia}]`.
- Produção: `POST /lotes/{id}/posturas {ovos, data?, codigoLancamento}`; `POST /lotes/{id}/pesagens {pesoMedioGramas, data?, codigoLancamento}`; `GET /lotes/{id}/producao` → `[{id, loteId, tipo: POSTURA|PESAGEM, data, valor, vivos, indicador, unidadeIndicador, codigo}]`.
- Gestor: `GET /auditoria` → `[{id, categoria, detalhe, dataHora}]`; `GET|PUT /configuracao/alerta {criterio, atencao, critico}`; `GET|PUT /configuracao/parametros {diasAvisoVacina, diasOcorrenciaAberta}`.

Importante: `codigoLancamento` é uma chave de idempotência. Gere um por tentativa de envio com `crypto.randomUUID()` (guarde no estado do formulário e só troque depois de sucesso), assim um duplo clique ou reenvio não duplica o lançamento. Datas vão como `YYYY-MM-DD`. Se um campo de um corpo não estiver claro, leia o controller correspondente em `backend/src/main/java/br/ufpb/aps/granja/api/` e o DTO em `.../api/dto/` antes de implementar. Não invente campos.

### Como trabalhar
1. Antes de escrever, liste em poucas linhas o que vai criar e quais arquivos antigos vai substituir.
2. Implemente por etapas pequenas, rodando lint e tsc ao final de cada etapa.
3. Um arquivo por responsabilidade. Nada de arquivo com mais de uns 250 linhas sem motivo.
4. Ao terminar, responda com: lista de arquivos criados/alterados, o que testou, e o que ficou de fora.

---

## LOTE 2: estrutura do app, acesso, início e lotes

Objetivo: o app funcionando de ponta a ponta com login real, navegação, e o fluxo de lotes completo.

### 2.1 Dependências
- `npm install motion react-router` (React Router v7, import de `react-router`). Se preferir não usar router, justifique; o padrão é usar.

### 2.2 Camada de API (`src/api/`)
- Manter `http.ts` e `sessao.ts`. Criar um módulo por área: `lotes.ts`, `estoque.ts`, `fornecedores.ts`, `sanidade.ts`, `producao.ts`, `alertas.ts`, `admin.ts`. Cada função recebe o `token` (ou use um cliente ligado à sessão) e devolve tipos TS fortes. Converter enums da API para tipos união em minúsculo ou manter em maiúsculo de forma consistente e documentada num único lugar (`src/api/tipos.ts`).
- Para este lote, só implemente o que o lote 2 usa (lotes, alertas de mortalidade, insumos para a tela de início, usuários para cadastro). O resto fica para o lote 3.

### 2.3 Sessão
- `src/sessao/SessaoContexto.tsx` + hook `useSessao()`: guarda `{token, login, nome, perfil}`, persiste em `sessionStorage` (com try/catch), expõe `entrar`, `sair`, e `perfil`. Em qualquer `ErroApi` 401, limpar a sessão e redirecionar para `/entrar` com um aviso.
- `src/sessao/RotaProtegida.tsx`: exige sessão; opcionalmente exige perfis (`perfis={['gestor']}`). Sem permissão, mostrar uma tela "Sem acesso" simples (não redirecionar em silêncio).

### 2.4 Rotas e estrutura
- `/entrar`, `/cadastro` (públicas).
- Área logada com `LayoutApp` (navbar `NavPilula` no desktop, `NavInferior` abaixo de 768px, conteúdo com `AnimatePresence`):
  - `/` Início
  - `/lotes`, `/lotes/novo`, `/lotes/:id`
  - demais rotas (mortalidade, alimentação, estoque, alertas, sanidade, produção, fornecedores, usuários, auditoria, configuração) ficam como página "Em breve" por enquanto, já registradas, para o lote 3 só preencher.
- Itens da navbar por perfil (veja `docs/design/telas/d-inicio.html` para os rótulos): gestor vê tudo, com Fornecedores/Usuários/Auditoria/Parâmetros dentro do menu do avatar; técnico e funcionário veem só o que podem usar.
- Remover o uso de `App.tsx` antigo: `main.tsx` passa a renderizar o roteador. Manter `#vitrine`. Apagar `src/componentes/*` e as telas antigas só depois de a nova versão estar funcionando e sem referências.

### 2.5 Telas (leia o fragmento antes de cada uma)
- **Entrar** (`d-entrar.html`, `d-m-entrar.html`): formulário login/senha, mostra/oculta senha, erro da API no próprio formulário, botão com estado de carregando, link para criar conta. Ao entrar com sucesso, ir para `/`.
- **Criar conta** (`d-cadastro.html`): login, email, nome, senha e confirmação, validação no cliente, `POST /cadastro`, depois voltar para entrar com toast de sucesso.
- **Início** (`d-inicio.html`, `d-m-inicio.html`): saudação com o nome, resumo curto (lotes ativos, aves vivas, alertas abertos, insumos abaixo do mínimo) calculado a partir de `GET /lotes`, `GET /alertas-mortalidade` e `GET /insumos`, lista dos alertas mais recentes e atalhos para as ações do perfil (registrar mortalidade, alimentação etc.). Carregar tudo em paralelo (`Promise.all`) com esqueleto de carregamento e tratar falha parcial.
- **Lotes** (`d-lotes.html`, `d-m-lotes.html`): lista com busca por código, filtro ativo/encerrado, saldo com barra de progresso (saldo/quantidade inicial), selo de status; no celular vira lista de linhas.
- **Novo lote** (`d-lote-novo.html`): código, espécie (segmentado), quantidade inicial, data de alojamento e idade inicial opcionais. Só perfis que podem cadastrar (gestor e técnico). Sucesso leva ao detalhe do lote.
- **Detalhe do lote** (`d-lote-detalhe.html`): cabeçalho com código, espécie, status, idade em dias/semanas (`/idade`), saldo; aviso se `consistente` for falso; linha do tempo do histórico (`/historico`, agrupar por data); ações "Registrar mortalidade", "Registrar baixa" (diálogo), "Encerrar lote" (diálogo de confirmação). Para o lote 2 pode abrir mortalidade e baixa em `Dialogo` simples; a tela completa de mortalidade vem no lote 3.

### 2.6 Estados que toda tela deve ter
Carregando (esqueleto, não spinner solto), vazio (texto útil e a ação seguinte), erro (mensagem + "Tentar de novo"), sucesso (toast). Crie `src/ui/Esqueleto.tsx` e `src/ui/EstadoVazio.tsx` e `src/ui/EstadoErro.tsx` se ainda não existirem.

### 2.7 Critério de pronto
- Com o backend rodando (`mvn` na pasta `backend`), consigo: criar conta, entrar, ver início, listar lotes, criar lote, abrir detalhe, registrar mortalidade/baixa em diálogo, encerrar lote, sair.
- Recarregar a página mantém a sessão. Token inválido volta para `/entrar`.
- `npm run lint` e `tsc` sem erros. Funciona em 390px e em 1280px.

---

## LOTE 3: registros do dia, estoque, alertas, sanidade, produção e administração

Objetivo: todas as telas restantes do desenho, no desktop e no celular. Pressupõe o lote 2 pronto e commitado.

### 3.1 API
Completar os módulos de `src/api/` com tudo que falta (estoque/insumos/movimentos, fornecedores, sanidade, produção, auditoria, configuração de alerta e parâmetros, usuários listar/alterar status).

### 3.2 Telas (leia o fragmento de cada uma em `docs/design/telas/`)
Registros do dia (perfis: funcionário, técnico e gestor conforme permissão):
- **Registrar mortalidade** (`d-mortalidade.html`, `d-m-mortalidade.html`): escolher lote ativo, quantidade com `Contador` grande (botões +/−), observação opcional, envio com `codigoLancamento`. Mostrar o `nivelAlerta` retornado (normal/atenção/crítico) com `Aviso` e `Selo`, e o novo saldo.
- **Registrar alimentação** (`d-alimentacao.html`, `d-m-alimentacao.html`): lote, insumo da categoria RACAO (mostrar saldo e aviso se a quantidade passar do saldo), quantidade em kg, data (padrão hoje).

Estoque:
- **Estoque** (`d-estoque.html`): lista de insumos com saldo x nível mínimo (barra + selo "abaixo do mínimo"), filtro por categoria, busca; gestor pode cadastrar insumo novo (Painel lateral).
- **Entrada de estoque** (`d-estoque-entrada.html`): movimento de ENTRADA/SAÍDA com origem, quantidade, fornecedor quando for compra (confirme os campos no `InsumoController`).

Alertas:
- **Alertas** (`d-alertas.html`, `d-m-alertas.html`): abas "Mortalidade", "Sanitários", "Estoque" (`/alertas-mortalidade`, `/alertas-sanitarios`, `/insumos/alertas`), cada item com lote/insumo, nível, quando, e link para o lote ou tela relacionada. Estado vazio positivo quando não há alertas.

Sanidade e produção:
- **Sanidade** (`d-sanidade.html`): ocorrências abertas e encerradas (`/ocorrencias`), detalhe em Painel com tratamentos, registrar tratamento, encerrar ocorrência; abrir ocorrência para um lote; lista de vacinações por lote.
- **Registrar vacinação** (`d-sanidade-registrar.html`): lote, insumo da categoria VACINA, doses, data, próxima dose opcional, observação.
- **Produção** (`d-producao.html`): por lote, lista de `/producao` com indicador (taxa de postura % ou GMD g/dia), formulário de postura (poedeira) ou pesagem (frango de corte) conforme a espécie do lote, e um gráfico simples em SVG feito por você (sem biblioteca) da série do indicador.

Administração (só gestor, `RotaProtegida perfis={['gestor']}`):
- **Fornecedores** (`d-fornecedores.html`): lista e cadastro (documento CPF/CNPJ com máscara e validação básica de tamanho; o backend valida de verdade).
- **Usuários** (`d-usuarios.html`): lista, criar usuário com perfil, ativar/desativar (`PUT /usuarios/{id}/status`) com confirmação.
- **Auditoria** (`d-auditoria.html`): tabela com filtro por categoria e busca no detalhe, paginação no cliente (25 por página).
- **Parâmetros** (`d-configuracao.html`): critério e limites de alerta de mortalidade (`/configuracao/alerta`) e parâmetros sanitários (`/configuracao/parametros`), salvar com toast.

### 3.3 Celular
Versões móveis seguem `d-m-*.html`: navegação inferior, formulários de registro em tela cheia com o botão principal fixo no rodapé, listas em linhas em vez de tabelas. Tabelas largas viram lista de linhas abaixo de 768px.

### 3.4 Acabamento
- Troca de rota animada de forma consistente em todas as telas, usando `movimento.ts`.
- Foco vai para o título da página (ou para o primeiro erro do formulário) ao trocar de rota e ao falhar o envio.
- Revisar contraste, tamanhos de toque e navegação por teclado em todos os formulários.
- Atualizar `frontend/README.md` (resolver conflitos de merge se existirem): como rodar, como apontar para a API (`GRANJA_API`), estrutura de pastas.
- Rodar `npm run lint` e `npm run build` sem erros.

### 3.5 Critério de pronto
- Cada tela do desenho existe, funciona com o backend real e respeita o perfil.
- Nenhum formulário envia duas vezes com duplo clique. Toda ação que falha mostra a mensagem do backend.
- Sem `any`, sem `@ts-ignore`, sem warnings do oxlint.
