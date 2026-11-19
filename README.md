# Sistema de Gestão para Granjas

Projeto do **Grupo 13** da disciplina de Análise e Projeto de Sistemas (APS 2026.2), UFPB Campus IV, Prof. Dr. Dorgival Netto. Autor: Yesus Lucas (equipe individual).

O sistema controla uma granja de frango de corte e de poedeiras: lotes de aves, mortalidade, estoque de insumos, alimentação, sanidade, produção, financeiro e relatórios para o gestor. O domínio e os 30 casos de uso vêm do AveTrack, projeto de outro grupo, revisto neste trabalho (ver [`docs/requisitos.md`](docs/requisitos.md)).

## O que tem

- **Backend** em Java 17 e Maven, com a API REST sobre o `HttpServer` do JDK (sem framework) e banco SQLite. Camadas: controller, service, model, policy e repository.
- **Frontend** em React 19, TypeScript, Vite e Tailwind v4, na pasta [`frontend/`](frontend/).
- **Três perfis** (Gestor, Técnico e Funcionário), com a autorização checada no serviço e não só na tela. A API responde 403 quando a ação não é permitida.
- **30 casos de uso** implementados, em 8 módulos: acesso e auditoria, lotes, estoque, sanidade, manejo e produção, financeiro, gestão e relatórios, sistema e suporte.
- **229 testes automatizados** no backend.

### Os desafios do enunciado

| Desafio | Como é resolvido |
|---|---|
| Saldo do lote nunca negativo | RN07, no domínio e no banco (`CHECK`) |
| Nada em lote encerrado | RN05 |
| Encerrar baixa o saldo restante | RN21 |
| Saldo igual à soma do histórico | RN06r e RN23, tudo na mesma transação |
| Não repetir o mesmo lançamento | RN22, código de lançamento único |
| Alerta por limite que pode variar | RN24, padrão Strategy, com limites configuráveis |

Padrões de projeto: **Strategy** (política de alerta), **Factory Method** (criação da política) e **Observer** (barramento de eventos, auditoria e alertas).

## Como rodar

Precisa de JDK 17 ou mais novo, Maven e Node.js.

```bash
# testes do backend (da raiz)
mvn test

# API em http://127.0.0.1:8080/api (banco só em memória, some ao encerrar)
mvn -q -pl backend compile
mvn -q -pl backend dependency:build-classpath -Dmdep.outputFile=cp.txt
java -cp "backend/target/classes:$(cat backend/cp.txt)" br.ufpb.aps.granja.Main
```

Para guardar os dados, passe o caminho de um arquivo como argumento (`... br.ufpb.aps.granja.Main granja.db`). Variáveis: `PORTA` (padrão 8080) e `BACKUPS` (pasta das cópias de segurança, padrão `./backups`).

Para testar com dados de exemplo (5 lotes, estoque com alertas, 6 meses de financeiro, chamados e usuários extras), acrescente `--demo` (ou defina `GRANJA_DEMO=1`): `... br.ufpb.aps.granja.Main --demo`. Os dados só são criados se ainda não existir nenhum lote; usuários extras: `maria` (técnico), `joao`, `carlos` (funcionários) e `ana` (inativa), todos com senha `demo12345`.

### Com Docker (banco persistente)

Precisa só do Docker. Na raiz do projeto:

```bash
docker compose up --build            # sobe backend + frontend
GRANJA_DEMO=1 docker compose up --build   # na primeira vez, já com dados de exemplo
```

Abra http://localhost:3000 (login `gestor` / `gestor123`). O banco (`granja.db`) e as cópias de segurança ficam no volume `granja-dados`, então os dados continuam depois de `docker compose down` e de subir de novo. Só `docker compose down -v` apaga o volume (e os dados). O backend não é exposto fora do Docker: o nginx do frontend repassa `/api` para ele. Fora do Docker, o mesmo efeito vem de `GRANJA_DB=caminho/granja.db` (ou do argumento com o caminho do arquivo).

```bash
# interface, em outro terminal
cd frontend
npm install
npm run dev     # http://localhost:5173
```

Detalhes do frontend em [`frontend/README.md`](frontend/README.md).

### Usuários de demonstração

| Login | Senha | Perfil |
|---|---|---|
| `gestor` | `gestor123` | Gestor |
| `tecnico` | `tecnico123` | Técnico |
| `funcionario` | `funcionario123` | Funcionário |

Troque as senhas antes de qualquer uso real.

## O que fica fora

Backup automático diário, exportação em XLSX (a exportação é em CSV), HTTPS, adequação à LGPD e a medição de desempenho com 100 mil registros estão declarados como fora desta entrega em [`docs/requisitos.md`](docs/requisitos.md), seção 6. A restauração de um backup é feita fora do sistema, trocando o arquivo `.db`.

## Documentos

- [`docs/requisitos.md`](docs/requisitos.md): escopo, perfis, regras de negócio e limitações.
- [`docs/matriz-rastreabilidade.md`](docs/matriz-rastreabilidade.md): cada caso de uso, a classe que o implementa e os testes que o comprovam.
- [`docs/arquitetura.md`](docs/arquitetura.md) e [`docs/adr/`](docs/adr/): arquitetura em camadas e decisões.
- [`docs/uml/`](docs/uml/): diagramas.
- [`docs/uso-de-ia.md`](docs/uso-de-ia.md): como a IA foi usada no trabalho.
