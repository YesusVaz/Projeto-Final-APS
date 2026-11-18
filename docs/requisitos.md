# Requisitos — Sistema de Gestão para Granjas (Grupo 13)

> Parcial 01 · versão de 11/11/2026 · APS 2026.2 · UFPB Campus IV · Prof. Dr. Dorgival Netto
> Autor: Yesus Lucas (equipe individual)

Este documento especifica o sistema usando o **AveTrack** como referência (ver [Origem e adaptações](#1-origem-e-adaptações)), revisando o que não serve para o recorte de APS e acrescentando regras próprias para consistência de saldo, idempotência e alerta por limite.

---

## 1. Origem e adaptações

O AveTrack é um projeto de outro grupo, da disciplina de Modelagem de Dados (cliente fictício: Maria Luciana, dona de granja). Equipe do AveTrack: Anna Beatriz Alves de Vasconcelos, José Italo Oliveira Pontes, Mykael Bruno Guimarães e Randerson Ranniery Soares da Silva. Os documentos usados como fonte são: Documento de Requisitos (30 histórias de usuário, RN01–RN20, 16 RNFs), Casos de Uso Detalhados (UC01–UC30), Backlog, Modelagem de Dados, Entrevista, Validação com o Cliente e Testes de Usabilidade.

**O que foi mantido:** o domínio (granja, lotes, insumos, alimentação, sanidade, produção, financeiro), os três atores (Gestor, Técnico, Funcionário), o catálogo de 30 casos de uso e as regras RN01–RN20.

**O que foi revisto e adaptado** (o AveTrack tem lacunas que o desafio do Grupo 13 pede para fechar):

| Lacuna no AveTrack | Adaptação neste projeto |
|---|---|
| RN06 define o saldo do lote como `inicial − mortes`. Só mortalidade altera o saldo; venda, descarte e transferência não entram. | **RN06 revisada**: saldo = soma algébrica de **todos** os movimentos do histórico, com a entrada inicial como primeiro movimento. |
| Encerrar o lote é só um cenário alternativo do UC04, sem regra para o saldo restante. | **RN21**: encerramento exige motivo, é irreversível e baixa automaticamente o saldo restante. |
| Nada impede que o mesmo lançamento seja registrado duas vezes (duplo clique, reenvio). | **RN22**: código de lançamento único (idempotência). |
| RN08–RN10 e RN13 geram saídas/entradas automáticas, mas não dizem o que ocorre se uma parte falhar. | **RN23**: operação tudo-ou-nada em transação. |
| RN17/RN18 falam de alerta e limite, sem dizer como o critério é calculado. | **RN24**: política de alerta substituível (Strategy), com limites configuráveis. |
| RBAC existe como RNF10, sem dizer onde é verificado. | **RN25**: autorização na camada de serviço. |

---

## 2. Atores e perfis

| Perfil | Quem é | Ações permitidas (`Perfil` / `Acao` no código) |
|---|---|---|
| **GESTOR** | Dono ou administrador da granja | todas as ações |
| **TECNICO** | Responsável técnico pelos lotes | `CONSULTAR`, `CADASTRAR_LOTE`, `ENCERRAR_LOTE`, `REGISTRAR_BAIXA`, `REGISTRAR_ENTRADA_ANIMAIS`, `REGISTRAR_VACINACAO`, `REGISTRAR_OCORRENCIA`, `CONSULTAR_ALERTAS_SANITARIOS`, `REGISTRAR_PRODUCAO`, `REGISTRAR_ILUMINACAO`, `ABRIR_CHAMADO` |
| **FUNCIONARIO** | Operação diária | `CONSULTAR`, `REGISTRAR_MORTALIDADE`, `MOVIMENTAR_ESTOQUE`, `REGISTRAR_ALIMENTACAO`, `REGISTRAR_PRODUCAO`, `REGISTRAR_HIGIENIZACAO`, `ABRIR_CHAMADO` |

Um usuário **inativo** não executa nenhuma ação (RN02). As permissões acompanham os atores de cada caso de uso do AveTrack. A decisão de dar ao Gestor acesso irrestrito é nossa: no AveTrack ele é administrador (pré-condição do UC01) e supervisiona a operação.

| Ação | Gestor | Técnico | Funcionário |
|---|:-:|:-:|:-:|
| `CONSULTAR` | ✔ | ✔ | ✔ |
| `CADASTRAR_LOTE` | ✔ | ✔ | — |
| `ENCERRAR_LOTE` | ✔ | ✔ | — |
| `REGISTRAR_MORTALIDADE` | ✔ | — | ✔ |
| `CADASTRAR_INSUMO` | ✔ | — | — |
| `MOVIMENTAR_ESTOQUE` | ✔ | — | ✔ |
| `CONSULTAR_ALERTAS_ESTOQUE` | ✔ | — | — |
| `REGISTRAR_ALIMENTACAO` | ✔ | — | ✔ |
| `CONFIGURAR_LIMITES` | ✔ | — | — |
| `REGISTRAR_PRODUCAO` | ✔ | ✔ | ✔ |
| `REGISTRAR_HIGIENIZACAO` | ✔ | — | ✔ |
| `REGISTRAR_ILUMINACAO` | ✔ | ✔ | — |
| `CONSULTAR_PRODUTIVIDADE` | ✔ | — | — |
| `GERENCIAR_FINANCEIRO` | ✔ | — | — |
| `CONSULTAR_GESTAO` | ✔ | — | — |
| `EXPORTAR_DADOS` | ✔ | — | — |
| `GERENCIAR_BACKUP` | ✔ | — | — |
| `GERENCIAR_CHAMADOS` | ✔ | — | — |
| `ABRIR_CHAMADO` | ✔ | ✔ | ✔ |

A tabela traz as ações do núcleo e as acrescentadas ao implementar os demais casos de uso. A lista completa está em `model/Acao.java` e `model/Perfil.java`.

---

## 3. Escopo: catálogo dos 30 casos de uso

O projeto usa o escopo **completo** do AveTrack, organizado em 8 módulos. Para manter profundidade onde importa (a avaliação pede poucos casos de uso, bem feitos e testados), cada caso de uso tem um **nível**:

- **Núcleo** — compromisso para a entrega final (01/12), com regras e testes. São 7 casos de uso.
- **Meta** — implementados depois do núcleo, com testes. São 23: os 10 que já eram Meta e os 13 que estavam declarados (UC14, UC15, UC18 a UC23, UC25, UC26 e UC28 a UC30).
- **Declarado** — especificados e modelados, **não implementados**; ficam declarados como tal no README e na apresentação. Hoje nenhum caso de uso está nesta situação: os 30 têm serviço, API e tela.

| Módulo | UC | Nome | Ator(es) no AveTrack | Nível |
|---|---|---|---|---|
| **M1 Acesso e auditoria** | UC01 | Cadastrar usuário | Gestor | Meta |
| | UC02 | Autenticar no sistema | Todos | **Núcleo** |
| | UC03 | Consultar log de auditoria | Gestor | Meta |
| **M2 Lotes e mortalidade** | UC04 | Cadastrar lote de animais (inclui encerramento) | Técnico | **Núcleo** |
| | UC05 | Consultar idade dos animais | Técnico, Gestor | Meta |
| | UC06 | Registrar mortalidade | Funcionário | **Núcleo** |
| | UC17 | Visualizar histórico completo do lote | Gestor, Técnico | Meta |
| **M3 Insumos e estoque** | UC07 | Cadastrar insumo | Gestor | **Núcleo** |
| | UC08 | Registrar entrada e saída de estoque | Funcionário, Gestor | **Núcleo** |
| | UC09 | Monitorar alerta de estoque mínimo | Gestor | **Núcleo** |
| | UC10 | Cadastrar fornecedor | Gestor | Meta |
| **M4 Sanidade** | UC11 | Registrar vacinação | Técnico | Meta |
| | UC12 | Registrar ocorrência sanitária e tratamento | Técnico | Meta |
| | UC24 | Monitorar alertas sanitários automáticos | Técnico, Gestor | Meta |
| **M5 Manejo e produção** | UC13 | Registrar alimentação | Funcionário | **Núcleo** |
| | UC14 | Registrar higienização | Funcionário | Meta |
| | UC15 | Registrar programa de iluminação | Técnico | Meta |
| | UC16 | Registrar produção (ovos / peso) | Funcionário, Técnico | Meta |
| | UC18 | Analisar produtividade por lote | Gestor | Meta |
| **M6 Financeiro** | UC19 | Registrar compra e gerar despesa | Gestor | Meta |
| | UC20 | Registrar despesa operacional | Gestor | Meta |
| | UC21 | Registrar receita de venda | Gestor | Meta |
| | UC22 | Gerar relatório financeiro | Gestor | Meta |
| **M7 Gestão e relatórios** | UC23 | Visualizar painel gerencial (dashboard) | Gestor | Meta |
| | UC25 | Gerar relatório gerencial | Gestor | Meta |
| | UC26 | Visualizar gráficos de desempenho | Gestor | Meta |
| | UC27 | Configurar parâmetros do sistema | Gestor | Meta |
| **M8 Sistema e suporte** | UC28 | Gerenciar backup e segurança dos dados | Gestor | Meta |
| | UC29 | Exportar dados do sistema | Gestor | Meta |
| | UC30 | Abrir chamado de suporte | Todos | Meta |

Contagem: Núcleo 7 + Meta 23 + Declarado 0 = **30**.

Mínimos da equipe individual (Orientações APS 2026.2): 3 casos de uso completos, sendo 2 além de CRUD; 4 regras de negócio; 6 testes; 2 padrões GoF de categorias diferentes; 2 ADRs; 2 perfis com autorização na camada de serviço. O núcleo cobre todos eles com folga (ver [matriz de rastreabilidade](matriz-rastreabilidade.md)).

---

## 4. Casos de uso do núcleo: critérios de aceitação

Estado em 11/11/2026: o **domínio** de todos os itens abaixo está implementado e testado (25 testes). Serviços, persistência e API entram na Parcial 02 (24/11). Ver a coluna "Situação" da matriz.

### UC02 — Autenticar no sistema
- Credenciais válidas devolvem a identidade com o perfil do usuário.
- Usuário inativo é recusado (RN02); seu histórico permanece.
- A senha é guardada como hash bcrypt, nunca em texto (RNF09).
- Meta: bloqueio por 15 min após 5 tentativas erradas (RN03).

### UC04 — Cadastrar lote de animais
- Dados: espécie (frango de corte ou poedeira) e quantidade inicial, inteira e maior que zero.
- O código segue o padrão `LOT-AAAA-NNN`, é gerado pelo sistema e não pode ser alterado (RN04).
- O lote nasce `ATIVO`, com saldo igual à quantidade inicial.
- O lote e o movimento `ENTRADA_INICIAL` são gravados na **mesma transação** (RN23).
- Quantidade zero ou negativa é rejeitada.

### UC04.A — Encerrar lote (cenário alternativo 2 do UC04 no AveTrack)
- O motivo é obrigatório: venda, descarte ou transferência.
- Se sobrar saldo, o sistema gera automaticamente **uma baixa** com todo o saldo restante, do tipo correspondente ao motivo; o saldo vai a zero (RN21).
- Encerrar um lote sem saldo não gera baixa.
- O encerramento é irreversível; encerrar duas vezes é rejeitado.
- Lote encerrado não recebe nenhum movimento (RN05).

### UC04.B — Consultar lote (visão mínima)
- Mostra saldo, status e histórico de movimentos em ordem.
- Mostra o nível de alerta de mortalidade atual (RN24).
- O saldo exibido é igual à soma do histórico (RN06 revisada). A visão completa é o UC17 (Meta).

### UC06 — Registrar mortalidade
- Exige código de lançamento, quantidade maior que zero e causa (campo de observação do movimento).
- O lote precisa estar ativo (RN05) e a quantidade não pode ultrapassar o saldo (RN07).
- O mesmo código de lançamento enviado de novo não altera saldo nem histórico (RN22).
- Depois de gravar, o sistema avalia a política de alerta configurada e publica um evento quando o nível sobe (RN24).

### UC07 — Cadastrar insumo
- Dados: nome, categoria (ração, vacina, medicamento, material), unidade e nível mínimo.
- O saldo inicial é zero. Apenas o Gestor cadastra (RN25).

### UC08 — Registrar entrada e saída de estoque
- Exige código de lançamento e origem (compra, alimentação, vacinação, tratamento, uso geral).
- Saída maior que o saldo é bloqueada com a mensagem "Saldo insuficiente. Disponível: X" (RN07).
- Insumo inativo não recebe entrada nem saída (RN19).
- Saída igual ao saldo é aceita e zera o estoque.

### UC09 — Monitorar alerta de estoque mínimo
- Lista os insumos com saldo **menor ou igual** ao nível mínimo (o limite exato dispara o alerta).
- Apenas o Gestor consulta (`CONSULTAR_ALERTAS_ESTOQUE`).

### UC13 — Registrar alimentação
- Exige lote ativo e insumo ativo da categoria ração, com quantidade maior que zero.
- Gera automaticamente a saída no estoque da ração (RN10), **na mesma transação**: se a saída falhar por saldo insuficiente, nada é gravado (RN23).
- Funcionário e Gestor registram.

### Casos de uso acrescentados depois do núcleo: resumo dos critérios

| UC | Critérios principais | Onde está |
|---|---|---|
| **UC14** Higienização | Lote ativo, tipo (limpeza, desinfecção, troca de cama...), data não futura e código de lançamento (RN22). Funcionário e Gestor. | `ManejoService.registrarHigienizacao`, menu "Manejo" do lote |
| **UC15** Iluminação | Horas de luz de 1 a 24 por dia, intensidade opcional maior que zero e data de início. Técnico e Gestor. | `ManejoService.registrarIluminacao`, menu "Manejo" do lote |
| **UC18** Produtividade | Só o Gestor. Frango: mortalidade, ração, conversão alimentar (RN20) e ganho de peso. Poedeira: ovos, taxa média de postura e ração por dúzia. O que ainda não dá para calcular aparece vazio, nunca zero inventado. | `ProdutividadeService`, `IndicadoresProducao`, seção "Produtividade" do lote |
| **UC19** Compra | Insumo e fornecedor existentes, quantidade e valor maiores que zero. Entrada no estoque e despesa na **mesma transação** (RN13, RN23). Só o Gestor. | `FinanceiroService.registrarCompra`, tela Financeiro |
| **UC20** Despesa | Categoria de despesa, descrição, valor em centavos inteiros, data não futura e lote opcional. | `FinanceiroService.registrarDespesa` |
| **UC21** Receita | Categoria de receita (venda de ovos, de aves, outras), mesmos campos da despesa. | `FinanceiroService.registrarReceita` |
| **UC22** Relatório financeiro | Período e lote opcional; totais de receitas, despesas e resultado, por categoria e a lista dos lançamentos. | `FinanceiroService.relatorio`, tela Financeiro |
| **UC23** Painel gerencial | Só o Gestor. Lotes ativos, aves vivas, mortes, ração e ovos do mês, receitas, despesas, resultado e insumos abaixo do mínimo. | `GestaoService.painel`, bloco "Neste mês" do Início |
| **UC25** Relatório gerencial | Só o Gestor. Financeiro do período, uma linha por lote (mortalidade, ração, conversão ou ovos, receita, despesa e resultado) e insumos abaixo do mínimo. Período invertido é recusado. | `GestaoService.relatorio`, tela Relatórios |
| **UC26** Gráficos de desempenho | Só o Gestor. Receitas e despesas dos últimos 6 meses; mortalidade, ração e postura dos últimos 30 dias, com zero nos dias sem registro. Cada gráfico tem versão em tabela. | `GestaoService.graficos`, tela Relatórios |
| **UC28** Backup | Só o Gestor. Cria uma cópia consistente do banco (`VACUUM INTO`), lista e baixa as cópias. O nome do arquivo é validado contra um padrão fixo, então não se lê nada fora da pasta de backups. Registrado na auditoria. | `BackupService`, tela Dados |
| **UC29** Exportar dados | Só o Gestor. CSV de lotes, estoque, alimentação, mortalidade, produção e financeiro, em UTF-8 com BOM e separador ponto e vírgula (abre direto no Excel). Células que começam com `=`, `+`, `-` ou `@` ganham apóstrofo, contra injeção de fórmula. Registrado na auditoria. | `ExportacaoService`, `Csv`, tela Dados |
| **UC30** Chamado de suporte | Qualquer perfil abre (assunto e descrição com tamanho mínimo, código de lançamento contra duplo clique) e vê os seus. O Gestor vê todos e responde. O andamento só avança: aberto, em atendimento, resolvido; resolver exige resposta. | `ChamadoService`, tela Suporte |

---

## 5. Regras de negócio

**Legenda de situação:** ✅ domínio implementado e testado hoje · 🔶 depende de serviço/persistência (Parcial 02, 24/11) · 🎯 Meta · 📄 Declarado (especificado, não implementado).

> As marcas das tabelas abaixo são da Parcial 01 (11/11). Depois delas entraram serviços, persistência SQLite, API REST, eventos e os demais casos de uso, com testes em `service/`, `persistencia/` e `api/`. A situação atual por caso de uso está na [matriz de rastreabilidade](matriz-rastreabilidade.md), seção 5.

### 5.1 Regras próprias do projeto (RN21–RN25) e a revisão da RN06

| ID | Regra | Situação |
|---|---|---|
| **RN06r** | O saldo do lote é a soma algébrica dos efeitos de todos os movimentos do histórico. `ENTRADA_INICIAL` soma; `MORTALIDADE` e as baixas (`BAIXA_VENDA`, `BAIXA_DESCARTE`, `BAIXA_TRANSFERENCIA`) subtraem. Quantidades são inteiras, e cada movimento tem quantidade maior que zero. | ✅ |
| **RN21** | Encerrar um lote exige motivo (venda, descarte, transferência), é irreversível e gera automaticamente a baixa do saldo restante, deixando o saldo em zero. Sem saldo, não há baixa. | ✅ |
| **RN22** | Todo lançamento (de lote ou de estoque) tem um **código de lançamento** único. Reenviar o mesmo código não altera saldo nem histórico e retorna `LancamentoDuplicadoException`. | ✅ valor do código · 🔶 verificação no serviço e `UNIQUE` no banco |
| **RN23** | Toda operação é tudo-ou-nada: movimento, atualização do saldo e efeitos automáticos (como a baixa de estoque da alimentação) acontecem na mesma transação. Invariante: saldo = soma do histórico. | ✅ verificação da invariante · 🔶 transação |
| **RN24** | A classificação de alerta de mortalidade é uma **política substituível**, por limite: (a) percentual de mortes no dia ou (b) percentual acumulado, cada uma com limites de ATENÇÃO e CRÍTICO. O limite exato já classifica no nível mais alto. Limites invertidos (atenção maior que crítico) são rejeitados. Quem configura os limites é o Gestor. | ✅ políticas e criação (Factory Method) · 🔶 disparo no registro |
| **RN25** | A autorização por perfil é verificada na **camada de serviço**, não só na tela. Ação negada gera `AcessoNegadoException`, traduzida em HTTP 403 pelo controller. | ✅ tabela de permissões · 🔶 serviço |

### 5.2 Regras herdadas do AveTrack (RN01–RN20)

| ID | Resumo | Situação neste projeto |
|---|---|---|
| RN01 | Login não pode ser alterado após o primeiro acesso | 🎯 UC01 |
| RN02 | Usuário inativado não faz login; histórico preservado | ✅ `Usuario.podeExecutar` · 🔶 login |
| RN03 | 5 tentativas erradas bloqueiam a conta por 15 min | 🎯 UC02 |
| RN04 | Código do lote `LOT-AAAA-NNN`, gerado e imutável | 🔶 gerador no serviço |
| RN05 | Lote encerrado não recebe novos registros | ✅ |
| RN06 | Saldo = inicial − mortes | substituída pela **RN06r** |
| RN07 | Saldo nunca negativo; saída que o negativaria é bloqueada | ✅ insumo e lote |
| RN08 | Vacinação gera saída de estoque | 🎯 UC11 |
| RN09 | Tratamento gera saída de estoque | 🎯 UC12 |
| RN10 | Alimentação gera saída de ração | 🔶 UC13 |
| RN11 | Taxa de postura = ovos / vivos × 100 | 🎯 UC16 |
| RN12 | GMD = (peso atual − anterior) / dias | 🎯 UC16 |
| RN13 | Compra gera entrada de estoque e despesa | ✅ UC19 (`FinanceiroServiceTest`) |
| RN14 | CNPJ/CPF do fornecedor é único | 🎯 UC10 |
| RN15 | Log de auditoria é imutável | 🎯 UC03 |
| RN16 | Histórico de lotes encerrados é mantido por 5 anos | ✅ por construção: não existe operação de exclusão de lote ou movimento |
| RN17 | Alertas sanitários são verificados diariamente | 🎯 UC24 |
| RN18 | Limites e metas só o Gestor altera | ✅ `CONFIGURAR_LIMITES` é exclusiva do Gestor · 🔶 serviço |
| RN19 | Insumo inativo não recebe movimentos; histórico preservado | ✅ |
| RN20 | Conversão alimentar = ração consumida / peso ganho | ✅ UC18 (`ConversaoAlimentarTest`, `ManejoEProdutividadeTest`) |

---

## 6. Requisitos não funcionais

**Adotados** (valem para o núcleo):

| RNF | Tema | Como é atendido |
|---|---|---|
| RNF04 | Integridade referencial | chaves estrangeiras no SQLite; sem exclusão de registros com dependências |
| RNF09 | Senhas com hash seguro | bcrypt (fator de custo 10 ou mais), Parcial 02 |
| RNF10 | Controle de acesso por perfil (RBAC) | `Perfil`/`Acao` no domínio, verificação no serviço, 403 no controller |
| RNF12 | Arquitetura em camadas | controller → service → model/policy/repository ([arquitetura](arquitetura.md)) |
| RNF13 | Código versionado em Git | repositório do projeto, commits pequenos |

**Atendidos em parte:** RNF15 (exportação): CSV no UC29; XLSX não. RNF05 (backup): backup manual no UC28; o automático diário não.

**Declarados fora desta entrega:** RNF01–03 (usabilidade medida com usuários e responsividade), RNF05 (backup automático diário), RNF06 (disponibilidade de 99%), RNF07–08 (desempenho com 100 mil registros e dashboard em tempo real), RNF11 (HTTPS), RNF14 (compatibilidade com navegadores), RNF15 (exportação em XLSX) e RNF16 (adequação à LGPD).

---

## 7. Os desafios do Grupo 13 e onde cada um é resolvido

| Desafio do enunciado | Regra / caso de uso |
|---|---|
| Cadastro de lotes | UC04, RN04 |
| Movimentações de quantidade | UC06 (mortalidade), baixas e encerramento (UC04.A), RN06r |
| Encerramento de lote | UC04.A, RN21 |
| Impedir saldo negativo | RN07 |
| Impedir movimentação em lote encerrado | RN05 |
| Impedir repetição de um mesmo lançamento | RN22 |
| Variar uma política de classificação ou alerta por limite | RN24 (Strategy) |
| Consistência entre quantidade do lote e histórico | RN06r e RN23 |
| Revisar a referência e especificar requisitos próprios para granjas | seção 1 e este documento |

---

## 8. Limitações conhecidas

- O backend tem **229 testes automatizados** (`mvn test`): domínio, políticas, serviços, persistência SQLite e API. Os testes que comprovam cada regra estão na [matriz de rastreabilidade](matriz-rastreabilidade.md).
- Baixas parciais de lote (venda, descarte, transferência sem encerrar) entram pelo `LoteService.registrarBaixa`.
- O front-end React (`frontend/`) fala com a API real. Nada é simulado nem estático.
- Os 30 casos de uso têm implementação. Ficam fora o que a seção 6 declara (backup automático diário, XLSX, HTTPS, LGPD, desempenho com 100 mil registros).
- O backup é manual e a **restauração** é feita fora do sistema, parando o servidor e trocando o arquivo `.db`. Uma cópia contém o banco inteiro, inclusive os hashes bcrypt das senhas, por isso só o Gestor baixa.
- Não há testes de concorrência além da garantia do banco (transação e `UNIQUE`).
