# Task Manager API

API REST para organizar usuários, projetos, tarefas e etiquetas. Este projeto nasceu como meu primeiro contato prático com **Java** e **Spring Boot**: construí uma aplicação completa para aprender a linguagem e o ecossistema enquanto resolvia problemas comuns de backend, como persistência, validação, regras de negócio, paginação e testes.

É um projeto de estudo e portfólio, não um produto pronto para produção. Desenvolvi os recursos gradualmente, investigando erros e usando os testes para consolidar o que aprendi.

## O que a API oferece

- Cadastro e consulta paginada de usuários, projetos, tarefas e etiquetas.
- Associação de tarefas a projetos, responsáveis e etiquetas.
- Pesquisa textual em entidades compatíveis com busca no PostgreSQL.
- Paginação, ordenação e limite de 100 itens por página.
- Ciclo de vida explícito das tarefas: `PENDING` → `RUNNING` → `COMPLETED` ou `CANCELED`.
- Arquivamento de tarefas, sem exibi-las nas listagens padrão.
- Consulta de tarefas atrasadas e resumo de tarefas por status.
- Validação de entrada e respostas de erro padronizadas.
- Documentação interativa da API com OpenAPI e Swagger UI.

Uma tarefa concluída ou cancelada não pode ser editada; uma tarefa arquivada não pode ser alterada nem excluída. A exclusão de um usuário com tarefas atribuídas é bloqueada para evitar tarefas órfãs.

## Tecnologias e conceitos

- Java 17 e Spring Boot
- Spring MVC, Spring Data JPA e Hibernate
- PostgreSQL e Docker Compose
- Liquibase para versionar e aplicar migrações
- Bean Validation para validar dados de entrada
- DTOs para separar os contratos HTTP das entidades persistidas
- Testes unitários e web com JUnit, Mockito e MockMvc
- Testes de integração com H2 e PostgreSQL via Testcontainers
- springdoc OpenAPI / Swagger UI

## Como executar

### Pré-requisitos

- JDK 17
- Docker com Docker Compose

O Maven Wrapper está incluído no repositório; não é necessário instalar Maven separadamente. Na primeira execução, o Docker pode precisar baixar as imagens e o Maven pode baixar as dependências.

### Iniciar a aplicação e o banco

Na raiz do projeto:

```bash
docker compose up --build
```

O Compose inicia o PostgreSQL, espera que esteja pronto e então inicia a aplicação. O Liquibase aplica as migrações; o Hibernate valida o schema, sem gerá-lo automaticamente.

- API: `http://localhost:8081/api`
- Swagger UI: `http://localhost:8081/api/swagger-ui.html`
- Especificação OpenAPI: `http://localhost:8081/api/v3/api-docs`
- Health check: `http://localhost:8081/api/health-check`

Para encerrar, use `Ctrl+C` e, se necessário, execute `docker compose down`. Os dados do banco ficam no volume `pgdata` e são preservados entre execuções.

As credenciais definidas no Compose são apenas para desenvolvimento local; não devem ser usadas em um ambiente publicado.

### Executar os testes

Em outro terminal, na raiz do projeto:

```bash
./mvnw test
```

A suíte cobre regras de domínio, serviços, controllers e integração com persistência. Os testes PostgreSQL usam Testcontainers e precisam de Docker; os testes de aplicação em perfil `test` usam H2. O perfil `dev` é o padrão da aplicação e usa PostgreSQL com Liquibase.

## Rotas principais

Todas as rotas usam o prefixo `/api`.

| Recurso | Rotas |
| --- | --- |
| Health check | `GET /health-check` |
| Usuários | `/users` — `GET`, `POST`; `GET`, `PATCH`, `DELETE /users/{id}`; `POST /users/{id}/status` |
| Projetos | `/projects` — `GET`, `POST`; operações de consulta, atualização, status e membros em `/projects/{id}` |
| Tarefas | `/tasks` — `GET`, `POST`; `GET`, `PATCH`, `DELETE /tasks/{id}` |
| Ciclo de vida | `PATCH /tasks/{id}/status`; `POST /tasks/{id}/archive` |
| Resumo e atraso | `GET /tasks/summary/status`; `GET /tasks?overdue=true` |
| Etiquetas | `/labels` — `GET`, `POST`; `GET`, `PATCH`, `DELETE /labels/{id}` |

Os parâmetros de paginação e ordenação seguem o padrão Spring Data, por exemplo `?page=0&size=20&sort=title,asc`. O servidor limita páginas a 100 itens.

### Contrato de erro

Erros usam um corpo consistente com `status`, `message` e `errors`. Erros de validação identificam o campo inválido:

```json
{
  "status": "400 BAD_REQUEST",
  "message": "Validation failed",
  "errors": [
    {
      "field": "title",
      "message": "Title should not be empty"
    }
  ]
}
```

## Desafios técnicos que enfrentei

Este projeto foi também uma forma de aprender a investigar problemas reais, em vez de apenas fazer os endpoints responderem:

- **Tipos retornados por consultas nativas:** encontrei um `ClassCastException` ao tratar IDs vindos do PostgreSQL como `Integer`, quando podiam ser `Long`. Passei a receber os IDs como `Number` e fazer a conversão explícita com `Math.toIntExact`.
- **Paginação e problema de N+1:** carregar relações diretamente em uma consulta paginada podia multiplicar linhas ou gerar consultas por tarefa. A listagem passou a buscar primeiro uma página de IDs e, em seguida, carregar as relações dos itens da página em uma consulta. Um teste com estatísticas do Hibernate compara páginas de tamanhos diferentes e verifica que a quantidade de consultas permanece constante.
- **Regras do ciclo de vida:** protegi as transições de status e as alterações de tarefas concluídas, canceladas ou arquivadas no domínio, evitando depender apenas do comportamento dos controllers.
- **Respostas de erro:** reuni erros de validação, ausência de recursos e conflitos em um formato único, com códigos HTTP adequados e nomes de campo quando aplicável.
- **Integridade das relações:** a exclusão de usuários com tarefas atribuídas é recusada para evitar referências órfãs.
- **Datas e atrasos:** implementei a rejeição de vencimento passado na criação e a regra de atraso que exclui tarefas concluídas, canceladas ou arquivadas.
- **Testes e contexto Spring:** aprendi que testes de repositório com recorte JPA não carregam automaticamente todos os serviços da aplicação. Ajustei as dependências necessárias no teste PostgreSQL e corrigi um fixture de teste que tentava excluir uma etiqueta sem configurar seu ID.
- **Configuração por ambiente:** separei os perfis de desenvolvimento e teste, usando PostgreSQL e Liquibase em desenvolvimento e H2 nos testes de aplicação.

Essas dificuldades me ajudaram a praticar leitura de stack traces, depuração de integração entre camadas e criação de testes que verificam comportamento, não apenas implementação.

## Decisões e limites conhecidos

- A API ainda não implementa autenticação ou autorização; o foco desta etapa foi aprender a estrutura de uma API Spring e suas regras de domínio.
- O vencimento é representado como instante (`Instant`), e a aplicação usa UTC para persistência.
- O PostgreSQL é necessário para as consultas de busca textual específicas do banco e para executar os testes Testcontainers.

## Documentação adicional

- [Migrações do banco com Liquibase](docs/migrations.md)
- [Swagger UI](http://localhost:8081/api/swagger-ui.html), após iniciar a aplicação
- [Especificação OpenAPI](http://localhost:8081/api/v3/api-docs), após iniciar a aplicação
