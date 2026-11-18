# Matriz de rastreabilidade — versão 2 (atualizada em 18/11/2026)

Liga cada regra e caso de uso à classe que o implementa e ao teste que o comprova. As seções 1 a 4 são da v1 (Parcial 01) e mostram o núcleo como estava: o que lá aparece como 🔶 (serviço, persistência, API, eventos) já foi implementado. A **seção 5** traz a situação atual, com todos os 30 casos de uso. Será fechada no pacote final (01/12).

**Situação:** ✅ implementado e testado (domínio) · 🔶 planejado para 24/11 (serviço, persistência, API) · 🎯 Meta · 📄 Declarado.

Os caminhos de classe partem de `backend/src/main/java/br/ufpb/aps/granja/`; os de teste, de `backend/src/test/java/br/ufpb/aps/granja/`.

---

## 1. Regras de negócio → código → testes

| Regra | UC | Classe / método | Testes | Situação |
|---|---|---|---|---|
| **RN07** saldo do lote nunca negativo | UC06, UC04.A | `model/Lote.aplicar`, `model/Quantidade.menos`, `SaldoInsuficienteException` | T01, T02, T13 | ✅ |
| **RN07** saldo do insumo nunca negativo | UC08, UC13 | `model/Insumo.aplicar`, `SaldoInsuficienteException` | T09 | ✅ |
| **RN05** lote encerrado não recebe movimento | UC06, UC13 | `model/Lote.aplicar` (exige lote ativo), `StatusLote`, `LoteEncerradoException` | T03 | ✅ |
| **RN21** encerramento: motivo, baixa do saldo, irreversível | UC04.A | `model/Lote.encerrar`, `MotivoEncerramento.tipoDeBaixa`, `TipoMovimentoLote` | T04, T05, T06 | ✅ |
| **RN06r** saldo = soma algébrica do histórico | UC04, UC04.B, UC06 | `model/Lote.consistenteCom`, `MovimentoLote.efeitoNoSaldo`, `TipoMovimentoLote.sinal` | T07, T08 | ✅ |
| **RN06r** quantidades inteiras e não negativas | todos | `model/Quantidade` (record) | T12, T13 | ✅ |
| **RN19** insumo inativo não recebe movimento | UC08 | `model/Insumo.aplicar`, `model/Insumo.inativar`, `InsumoInativoException` | T10 | ✅ |
| **UC09** alerta de estoque mínimo (limite exato) | UC09 | `model/Insumo.abaixoDoMinimo` | T11 | ✅ |
| **RN22** lançamento com código único | UC06, UC08 | `model/CodigoLancamento` (valor, rejeita vazio) | T14 | ✅ valor do código |
| | | `LoteService`, `EstoqueService` (checagem) e índice `UNIQUE` no repositório SQLite | P01, P02 | 🔶 |
| **RN23** tudo-ou-nada, saldo = histórico | UC04, UC06, UC13 | verificação da invariante: `Lote.consistenteCom` | T07, T08 | ✅ |
| | | `repository/Transacao.executar` e `repository/sqlite` | P03, P04 | 🔶 |
| **RN24** alerta por limite, política substituível (Strategy) | UC06, UC04.B | `policy/PoliticaAlertaMortalidade`, `AlertaMortalidadeDiaria`, `AlertaMortalidadeAcumulada`, `LimitesDeAlerta`, `ResumoMortalidade`, `CriadorPoliticaAlerta`, `CriadorAlertaDiario`, `CriadorAlertaAcumulado` | T19–T25 | ✅ |
| | | `evento/AlertaListener` (Observer) | P05 | 🔶 |
| **RN25** autorização por perfil | UC02, UC04.A, UC06, UC07, UC08, UC09, UC13 | `model/Perfil.permite`, `model/Acao`, `model/Usuario.podeExecutar` | T15, T16, T17, T18 | ✅ |
| | | `service/AutorizacaoService`, `AcessoNegadoException` → HTTP 403 | P06 | 🔶 |
| **RN02** usuário inativo não age | UC02 | `model/Usuario.podeExecutar`, `Usuario.inativar` | T18 | ✅ |
| | | autenticação e hash bcrypt (RNF09) | P07 | 🔶 |
| **RN18** só o Gestor configura limites | UC27 | `model/Acao.CONFIGURAR_LIMITES` em `Perfil.GESTOR`; caso negativo para Técnico e Funcionário | T17 (Gestor), P06 (negativos) | ✅ parcial · 🔶 |
| **RN10** alimentação baixa o estoque de ração | UC13 | `service/AlimentacaoService`, `model/RegistroAlimentacao`, `Insumo.aplicar` | P08 | 🔶 |
| **RN04** código `LOT-AAAA-NNN` | UC04 | gerador de código de lote no serviço | P09 | 🔶 |
| **RN16** histórico mantido 5 anos | UC04.A | por construção: não há operação de exclusão de lote ou movimento | revisão de código | ✅ |
| RN01, RN03, RN08, RN09, RN11, RN12, RN14, RN15, RN17 | UC01–UC03, UC10–UC12, UC16, UC24 | — | — | 🎯 |
| RN13, RN20 | UC18, UC19 | — | — | 📄 |

---

## 2. Casos de uso do núcleo → código → testes

| UC | Ator | Classes principais (hoje) | Classes planejadas (24/11) | Testes | Situação |
|---|---|---|---|---|---|
| UC02 Autenticar | Todos | `Usuario`, `Perfil` | `AutenticacaoService`, `UsuarioRepository` | T15–T18, P07 | ✅ domínio · 🔶 |
| UC04 Cadastrar lote | Técnico, Gestor | `Lote`, `Quantidade`, `Especie`, `MovimentoLote` | `LoteService.cadastrar`, `LoteController` | T07, T12, P03, P09 | ✅ domínio · 🔶 |
| UC04.A Encerrar lote | Técnico, Gestor | `Lote.encerrar`, `MotivoEncerramento` | `LoteService.encerrar` | T04, T05, T06 | ✅ domínio · 🔶 |
| UC04.B Consultar lote | Todos | `Lote.consistenteCom`, `ResumoMortalidade` | `LoteService.consultar` | T07, T08, T19–T22 | ✅ domínio · 🔶 |
| UC06 Registrar mortalidade | Funcionário, Gestor | `Lote.aplicar`, `MovimentoLote`, `PoliticaAlertaMortalidade` | `LoteService.registrarMortalidade`, `BarramentoEventos` | T01–T03, T19–T22, P01, P05 | ✅ domínio · 🔶 |
| UC07 Cadastrar insumo | Gestor | `Insumo`, `CategoriaInsumo` | `EstoqueService.cadastrarInsumo` | T09–T11, P06 | ✅ domínio · 🔶 |
| UC08 Entrada e saída de estoque | Funcionário, Gestor | `Insumo.aplicar`, `MovimentoEstoque`, `TipoMovimentoEstoque`, `OrigemMovimento` | `EstoqueService.movimentar` | T09, T10, P02 | ✅ domínio · 🔶 |
| UC09 Alerta de estoque mínimo | Gestor | `Insumo.abaixoDoMinimo` | `EstoqueService.alertas`, `AlertaListener` | T11 | ✅ domínio · 🔶 |
| UC13 Registrar alimentação | Funcionário, Gestor | `RegistroAlimentacao`, `Insumo.aplicar`, `Lote.aplicar` | `AlimentacaoService.registrar` | P08 | 🔶 |

---

## 3. Testes

### 3.1 Existentes (25, todos passam com `mvn test`)

| ID | Classe de teste | Método |
|---|---|---|
| T01 | `model/LoteTest` | `saidaMaiorQueSaldoELancaSaldoInsuficienteESemAlterarSaldo` |
| T02 | `model/LoteTest` | `saidaIgualAoSaldoZeraSemFicarNegativo` |
| T03 | `model/LoteTest` | `loteEncerradoRejeitaNovosMovimentos` |
| T04 | `model/LoteTest` | `encerrarGeraBaixaDoSaldoRestanteEFechaOLote` |
| T05 | `model/LoteTest` | `encerrarLoteSemSaldoNaoGeraBaixa` |
| T06 | `model/LoteTest` | `encerrarDuasVezesERejeitado` |
| T07 | `model/LoteTest` | `saldoSempreIgualASomaAlgebricaDoHistorico` |
| T08 | `model/LoteTest` | `historicoIncompletoEDetectadoComoInconsistente` |
| T09 | `model/InsumoTest` | `saidaMaiorQueSaldoEBloqueadaComMensagemClara` |
| T10 | `model/InsumoTest` | `insumoInativoNaoRecebeEntradaNemSaida` |
| T11 | `model/InsumoTest` | `alertaDeEstoqueMinimoDisparaExatamenteNoLimite` |
| T12 | `model/QuantidadeTest` | `quantidadeNegativaERejeitadaNaCriacao` |
| T13 | `model/QuantidadeTest` | `subtrairAteZeroPodeMasAlemDeZeroNao` |
| T14 | `model/QuantidadeTest` | `codigoDeLancamentoEmBrancoERejeitado` |
| T15 | `model/PerfilTest` | `funcionarioNaoEncerraLoteNemCadastraInsumo` |
| T16 | `model/PerfilTest` | `tecnicoCadastraEEncerraLoteMasNaoRegistraMortalidade` |
| T17 | `model/PerfilTest` | `gestorTemAcessoIrrestrito` |
| T18 | `model/PerfilTest` | `usuarioInativoNaoExecutaNada` |
| T19 | `policy/PoliticasDeAlertaMortalidadeTest` | `diariaClassificaExatamenteNosLimites` |
| T20 | `policy/PoliticasDeAlertaMortalidadeTest` | `acumuladaIgnoraOQueAconteceuSoNoDia` |
| T21 | `policy/PoliticasDeAlertaMortalidadeTest` | `mesmoResumoPodeTerNiveisDiferentesConformeAPoliticaEscolhida` |
| T22 | `policy/PoliticasDeAlertaMortalidadeTest` | `limitesInvertidosSaoRejeitados` |
| T23 | `policy/CriadorDePoliticaAlertaTest` | `criadorDiarioProduzPoliticaDiaria` |
| T24 | `policy/CriadorDePoliticaAlertaTest` | `criadorAcumuladoProduzPoliticaAcumulada` |
| T25 | `policy/CriadorDePoliticaAlertaTest` | `criadorRejeitaLimitesInvertidos` |

### 3.2 Planejados para a Parcial 02 (24/11)

| ID | Cenário | Regra |
|---|---|---|
| P01 | Registrar mortalidade duas vezes com o mesmo código: saldo e histórico não mudam | RN22 |
| P02 | O `UNIQUE` do banco rejeita código de lançamento duplicado (movimento de lote e de estoque) | RN22 |
| P03 | Falha simulada no meio do cadastro: lote e movimento inicial são desfeitos (rollback) | RN23 |
| P04 | Depois de uma sequência de operações, o saldo lido do banco é igual à soma do histórico lido do banco | RN06r, RN23 |
| P05 | Mortalidade que cruza o limite publica o evento e gera alerta e auditoria | RN24 |
| P06 | O serviço nega ações por perfil e o controller responde 403 | RN25, RN18 |
| P07 | Usuário inativo é recusado no login; a senha é armazenada com bcrypt | RN02, RNF09 |
| P08 | A alimentação baixa o estoque na mesma transação; estoque insuficiente desfaz tudo | RN10, RN23 |
| P09 | O gerador produz `LOT-AAAA-NNN` sequencial dentro do ano | RN04 |

Total: 25 existentes + 9 planejados = 34, contra o mínimo de 6 do enunciado.

---

## 4. Padrões de projeto

| Padrão (categoria GoF) | Onde | Situação |
|---|---|---|
| **Strategy** (comportamento) | `PoliticaAlertaMortalidade` com `AlertaMortalidadeDiaria` e `AlertaMortalidadeAcumulada` | ✅ implementado e testado (T19–T22) |
| **Observer** (comportamento) | `BarramentoEventos`, `OuvinteEvento`, `AuditoriaListener`, `AlertaListener` | 🔶 24/11 |
| **Factory Method** (criação) | `CriadorPoliticaAlerta` com `CriadorAlertaDiario` e `CriadorAlertaAcumulado` | ✅ implementado e testado (T23–T25) |

Observação: o mínimo do PDF pede **duas categorias diferentes**. Strategy e Observer são de comportamento; Factory Method é de criação. Por isso o segundo padrão implementado é o Factory Method, e o Observer fica como terceiro, planejado para 24/11.

---

## 5. Situação atual (18/11/2026): os 30 casos de uso

Serviços em `service/`, persistência em `persistencia/`, API em `api/` e eventos em `evento/`. O backend tem **229 testes** (`mvn test`), todos passando.

| UC | Caso de uso | Serviço | Testes (classe) |
|---|---|---|---|
| UC01 | Cadastrar usuário | `UsuarioService` | `UsuarioServiceTest` (10) |
| UC02 | Autenticar | `AutenticacaoService` | `AutenticacaoServiceTest` (21), `BloqueioDeLoginTest` (7) |
| UC03 | Consultar auditoria | `AuditoriaService`, `AuditoriaListener` | `AuditoriaImutavelTest` (1) |
| UC04, UC04.A, UC04.B, UC06 | Lote, encerramento, consulta e mortalidade | `LoteService` | `LoteServiceTest` (16), `LoteTest` (8), `ConsistenciaBancoTest` (2) |
| UC05 | Idade dos animais e baixas | `LoteService` | `BaixaEIdadeDoLoteTest` (9) |
| UC07, UC08, UC09 | Insumo, estoque e alerta de mínimo | `EstoqueService` | `EstoqueServiceTest` (8), `InsumoTest` (3) |
| UC10, UC27 | Fornecedor e parâmetros do sistema | `FornecedorService`, `ParametrosService`, `ConfiguracaoAlertaService` | `FornecedorEParametrosTest` (5), `ConfiguracaoAlertaServiceTest` (4), `DocumentoTest` (4) |
| UC11, UC12 | Vacinação, ocorrência e tratamento | `SanidadeService` | `SanidadeServiceTest` (8) |
| UC24 | Alertas sanitários | `AlertaSanitarioService` | `AlertaSanitarioServiceTest` (6) |
| UC13 | Alimentação | `AlimentacaoService` | `AlimentacaoServiceTest` (7) |
| UC14, UC15 | Higienização e iluminação | `ManejoService` | `ManejoEProdutividadeTest` (11) |
| UC16 | Produção (postura e pesagem) | `ProducaoService` | `ProducaoServiceTest` (7), `IndicadoresProducaoTest` (5) |
| UC17 | Histórico completo do lote | `HistoricoLoteService` | `HistoricoLoteServiceTest` (1) |
| UC18 | Produtividade por lote | `ProdutividadeService` | `ManejoEProdutividadeTest`, `ConversaoAlimentarTest` (3) |
| UC19, UC20, UC21, UC22 | Compra, despesa, receita e relatório financeiro | `FinanceiroService` | `FinanceiroServiceTest` (8) |
| UC23, UC25, UC26 | Painel, relatório gerencial e gráficos | `GestaoService` | `GestaoServiceTest` (6) |
| UC28, UC29, UC30 | Backup, exportação e chamado de suporte | `BackupService`, `ExportacaoService`, `ChamadoService` | `SuporteDadosTest` (14) |

Transversais: `ApiGranjaTest` (29) e `ApiCasosMetaTest` (8) exercitam as rotas e os códigos HTTP (inclusive o 403 por perfil, RN25); `BancoTest` (4) cobre a persistência; `PerfilTest` (4), `QuantidadeTest` (3), `PoliticasDeAlertaMortalidadeTest` (4) e `CriadorDePoliticaAlertaTest` (3) cobrem o domínio e os padrões Strategy e Factory Method. O Observer está em `BarramentoEventos`, com `AuditoriaListener` e `AlertaListener`.

**Regras que fecharam depois da v1:** RN13 (compra gera entrada de estoque e despesa na mesma transação, `FinanceiroServiceTest`) e RN20 (conversão alimentar, `ConversaoAlimentarTest`).
