# Diagramas UML

Os fontes ficam em [`uml/*.mmd`](.) e as imagens renderizadas em [`../img/`](../img/). O GitHub desenha os blocos Mermaid abaixo diretamente.

## 1. Domínio e políticas (implementado)

Corresponde às classes de `backend/src/main/java/br/ufpb/aps/granja/model` e `.../policy`. Imagem: [`classes-dominio.png`](../img/classes-dominio.png).

```mermaid
classDiagram
direction LR

class Usuario {
  -Long id
  -String login
  -String senhaHash
  -Perfil perfil
  -boolean ativo
  +podeExecutar(Acao) boolean
}
class Perfil {
  <<enumeration>>
  GESTOR
  TECNICO
  FUNCIONARIO
  +permite(Acao) boolean
}

class Lote {
  -Long id
  -String codigo
  -Especie especie
  -Quantidade quantidadeInicial
  -Quantidade saldo
  -StatusLote status
  +aplicar(MovimentoLote) void
  +encerrar(MotivoEncerramento, CodigoLancamento, LocalDateTime) Optional~MovimentoLote~
  +consistenteCom(List~MovimentoLote~) boolean
  +estaAtivo() boolean
}
class MovimentoLote {
  -Long id
  -TipoMovimentoLote tipo
  -Quantidade quantidade
  -CodigoLancamento codigo
  -LocalDateTime dataHora
  -String observacao
  +efeitoNoSaldo() int
}
class TipoMovimentoLote {
  <<enumeration>>
  ENTRADA_INICIAL
  MORTALIDADE
  BAIXA_VENDA
  BAIXA_DESCARTE
  BAIXA_TRANSFERENCIA
  +sinal() int
}
class StatusLote {
  <<enumeration>>
  ATIVO
  ENCERRADO
}
class MotivoEncerramento {
  <<enumeration>>
  VENDA
  DESCARTE
  TRANSFERENCIA
}

class Insumo {
  -Long id
  -String nome
  -CategoriaInsumo categoria
  -Quantidade nivelMinimo
  -Quantidade saldo
  -boolean ativo
  +aplicar(MovimentoEstoque) void
  +abaixoDoMinimo() boolean
}
class MovimentoEstoque {
  -Long id
  -TipoMovimentoEstoque tipo
  -Quantidade quantidade
  -CodigoLancamento codigo
  -OrigemMovimento origem
}
class RegistroAlimentacao {
  -Long id
  -Quantidade quantidadeKg
  -LocalDate data
  -CodigoLancamento codigo
}

class Acao {
  <<enumeration>>
  CADASTRAR_LOTE
  ENCERRAR_LOTE
  REGISTRAR_MORTALIDADE
  MOVIMENTAR_ESTOQUE
  ...
}
class PoliticaAlertaMortalidade {
  <<interface>>
  +avaliar(ResumoMortalidade) NivelAlerta
}
class ResumoMortalidade {
  <<record>>
  -int quantidadeInicial
  -int mortesNoDia
  -int mortesAcumuladas
}
class AlertaMortalidadeDiaria {
  -double limiteAtencao
  -double limiteCritico
}
class AlertaMortalidadeAcumulada {
  -double limiteAtencao
  -double limiteCritico
}
class NivelAlerta {
  <<enumeration>>
  NORMAL
  ATENCAO
  CRITICO
}

Usuario --> Perfil
Perfil ..> Acao
Lote "1" --> "1..*" MovimentoLote : historico
Lote --> StatusLote
Lote ..> MotivoEncerramento
MovimentoLote --> TipoMovimentoLote
Insumo "1" --> "0..*" MovimentoEstoque : extrato
RegistroAlimentacao "0..*" --> "1" Lote
RegistroAlimentacao "0..*" --> "1" Insumo
MovimentoEstoque "0..*" --> "0..1" Lote : consumo
AlertaMortalidadeDiaria ..|> PoliticaAlertaMortalidade
AlertaMortalidadeAcumulada ..|> PoliticaAlertaMortalidade
class CriadorPoliticaAlerta {
  <<abstract>>
  +configurar(double, double) PoliticaAlertaMortalidade
  #criarPolitica(double, double) PoliticaAlertaMortalidade
}
class CriadorAlertaDiario
class CriadorAlertaAcumulado
PoliticaAlertaMortalidade ..> NivelAlerta
PoliticaAlertaMortalidade ..> ResumoMortalidade
CriadorAlertaDiario --|> CriadorPoliticaAlerta
CriadorAlertaAcumulado --|> CriadorPoliticaAlerta
CriadorPoliticaAlerta ..> PoliticaAlertaMortalidade : cria
```

## 2. Aplicação (planejado para 24/11)

Serviços, repositórios, transação e eventos. **Ainda não há código** para estas classes. Imagem: [`classes-aplicacao.png`](../img/classes-aplicacao.png).

```mermaid
classDiagram
direction LR

class LoteController
class EstoqueController
class AlimentacaoController

class LoteService {
  +cadastrar(Usuario, NovoLote) Lote
  +registrarMortalidade(Usuario, ComandoMortalidade) MovimentoLote
  +encerrar(Usuario, Long, MotivoEncerramento) Lote
}
class EstoqueService {
  +cadastrarInsumo(Usuario, NovoInsumo) Insumo
  +movimentar(Usuario, ComandoMovimento) MovimentoEstoque
}
class AlimentacaoService {
  +registrar(Usuario, ComandoAlimentacao) RegistroAlimentacao
}
class AutorizacaoService {
  +exigir(Usuario, Acao) void
}

class Transacao {
  <<interface>>
  +executar(Supplier~T~) T
}
class LoteRepository {
  <<interface>>
}
class MovimentoLoteRepository {
  <<interface>>
  +existePorCodigo(CodigoLancamento) boolean
}
class InsumoRepository {
  <<interface>>
}
class MovimentoEstoqueRepository {
  <<interface>>
  +existePorCodigo(CodigoLancamento) boolean
}
class SqliteRepositories {
  implementacoes JDBC
}

class BarramentoEventos {
  +assinar(OuvinteEvento) void
  +publicar(EventoDominio) void
}
class OuvinteEvento {
  <<interface>>
  +aoOcorrer(EventoDominio) void
}
class AuditoriaListener
class AlertaListener
class PoliticaAlertaMortalidade {
  <<interface>>
}

LoteController --> LoteService
EstoqueController --> EstoqueService
AlimentacaoController --> AlimentacaoService

LoteService --> AutorizacaoService
EstoqueService --> AutorizacaoService
AlimentacaoService --> AutorizacaoService

LoteService --> LoteRepository
LoteService --> MovimentoLoteRepository
LoteService --> Transacao
LoteService --> BarramentoEventos
LoteService --> PoliticaAlertaMortalidade
EstoqueService --> InsumoRepository
EstoqueService --> MovimentoEstoqueRepository
EstoqueService --> Transacao
EstoqueService --> BarramentoEventos
AlimentacaoService --> LoteService
AlimentacaoService --> EstoqueService
AlimentacaoService --> Transacao

SqliteRepositories ..|> LoteRepository
SqliteRepositories ..|> MovimentoLoteRepository
SqliteRepositories ..|> InsumoRepository
SqliteRepositories ..|> MovimentoEstoqueRepository
SqliteRepositories ..|> Transacao

BarramentoEventos o-- OuvinteEvento
AuditoriaListener ..|> OuvinteEvento
AlertaListener ..|> OuvinteEvento
```

## 3. Arquitetura em camadas

Ver [`../arquitetura.md`](../arquitetura.md).
