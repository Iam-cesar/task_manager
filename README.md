# 1 · API de Gerenciamento de Tarefas

## Nível **Fácil**

## Objetivo

Construir o serviço de backend que sustenta o gerenciamento de tarefas de uma equipe, com foco em estabelecer uma base sólida de aplicação Spring Boot: modelagem de domínio persistente, contratos de API estáveis, validação de entrada, tratamento consistente de erros, consultas com filtro/ordenação/paginação e uma suíte de testes automatizados confiável.

O objetivo técnico principal não é "fazer CRUD funcionar", mas sim decidir **onde cada responsabilidade vive** (entidade, camada de aplicação, contrato HTTP), como o domínio protege suas próprias invariantes e como a API se comporta de forma previsível diante de entrada inválida, recurso inexistente e volume de dados.

## Contexto

A **Órbita**, uma consultoria de 40 pessoas, controla o trabalho dos times em planilhas compartilhadas. O modelo colapsou: duas pessoas editam a mesma linha e uma sobrescreve a outra, ninguém sabe quem fechou o quê, não existe histórico de datas, e a diretoria não consegue responder perguntas simples como "quantas tarefas estão atrasadas por time?".

A decisão foi construir um serviço interno próprio. O time de frontend já tem o app web em andamento e vai consumir a API — o que significa que **o contrato HTTP é um produto**, não um detalhe: precisa de respostas de erro legíveis por máquina, listagens paginadas (a planilha atual já tem 12 mil linhas migradas) e filtros que permitam montar as telas de "minhas tarefas", "atrasadas" e "por projeto" sem que o cliente baixe a base inteira.

Ainda não há autenticação nesta fase — a API vai rodar atrás da VPN da empresa, e o serviço de identidade será construído depois. O usuário responsável é informado explicitamente nas requisições.

## Problema

O sistema precisa registrar tarefas pertencentes a usuários, expor consultas eficientes sobre uma base que cresce, e garantir que o ciclo de vida de uma tarefa seja respeitado — uma tarefa concluída não volta a ser editada por acidente, uma tarefa cancelada não é concluída, e uma tarefa não é atribuída a alguém que não existe mais na empresa.

As principais dificuldades esperadas:

- **Separação entre modelo persistente e contrato de API.** Expor a entidade diretamente na resposta parece funcionar até a primeira mudança de banco ou o primeiro ciclo infinito de serialização. É preciso decidir como representar entrada e saída, e como converter entre elas.
- **Ciclo de vida como regra, não como campo livre.** O status não é uma string qualquer: existem transições legítimas e ilegítimas, e a API deve recusar as ilegítimas com uma resposta clara.
- **Consultas com muitas combinações de filtro.** Filtrar por responsável, status, prioridade, faixa de vencimento e etiqueta — em qualquer combinação — sem cair em um emaranhado de `if`s montando SQL na mão. Spring Data oferece mais de um caminho para isso; escolher é parte do desafio.
- **Erros previsíveis.** Entrada inválida, recurso inexistente, transição proibida e conflito precisam produzir respostas distinguíveis e estáveis, com o mesmo formato de corpo em toda a API.
- **Desempenho de leitura.** Listar tarefas com suas etiquetas e responsável é onde o problema de N+1 consultas aparece pela primeira vez. Detectá-lo exige observar o SQL efetivamente emitido, não apenas confiar que "está funcionando".

## Escopo

O sistema precisa suportar:

- gerenciamento de usuários (cadastro, atualização, desativação, consulta)
- criação, atualização e consulta de tarefas
- atribuição e reatribuição de tarefa a um usuário
- alteração de status de tarefa através de operação explícita
- arquivamento de tarefas (remoção não deve destruir histórico)
- organização de tarefas em projetos
- etiquetas (labels) livres associáveis a tarefas
- listagem de tarefas com filtros combináveis: responsável, projeto, status, prioridade, etiqueta, janela de data de vencimento, atrasadas
- ordenação configurável e paginação em todas as listagens de coleção
- consulta de resumo/contagem por status para alimentar dashboards
- documentação da API acessível

## Entidades e conceitos

```text
User
- identificador
- nome
- e-mail (único)
- situação (ativo / inativo)
- datas de criação e atualização

Project
- identificador
- nome
- descrição
- responsável
- situação

Task
- identificador
- título
- descrição
- projeto ao qual pertence
- usuário responsável
- status
- prioridade
- data de vencimento
- data de conclusão
- indicador de arquivamento
- etiquetas associadas
- datas de criação e atualização

Label
- identificador
- nome (único)
- cor
```

Não está definido aqui se etiqueta é entidade própria ou coleção de valores, se projeto é obrigatório, ou como o status é persistido. Essas decisões de modelagem são suas.

## Regras de negócio

1. Toda tarefa precisa de título; título vazio ou apenas espaços é inválido.
2. Título tem tamanho máximo definido e a violação deve ser rejeitada pela API, não pelo banco.
3. A data de vencimento, quando informada na criação, não pode estar no passado; a validação identifica `due_date` no formato padrão de erro.
4. Uma tarefa só pode ser atribuída a um usuário existente e **ativo**.
5. O status segue um ciclo de vida: uma tarefa nasce pendente, pode ir para em andamento, e de lá para concluída ou cancelada. Qualquer transição fora do que for definido como legítimo deve ser recusada — inclusive reabrir uma tarefa concluída ou concluir uma cancelada. A API aceita a mudança por `PATCH /tasks/{id}/status`.
6. Ao ser concluída, a tarefa registra a data de conclusão automaticamente; essa data nunca é informada pelo cliente.
7. Uma tarefa concluída ou cancelada não aceita alteração de conteúdo nem etiquetas.
8. Uma tarefa arquivada não aparece nas listagens padrão, não pode ser alterada nem excluída. O arquivamento é feito por `POST /tasks/{id}/archive`.
9. A exclusão de usuário com tarefas atribuídas é bloqueada com conflito (`409`); as tarefas permanecem associadas ao usuário.
10. Uma tarefa é considerada atrasada quando sua data/hora de vencimento já passou e não está concluída, cancelada ou arquivada. O campo `overdue` nas respostas indica essa condição; filtre com `GET /tasks?overdue=true`, combinável com `search`, paginação e ordenação.
11. E-mail de usuário é único no sistema, e a tentativa de duplicar deve retornar conflito — não erro genérico de servidor.
12. Nenhuma listagem de coleção pode retornar a base inteira: paginação é obrigatória, com limite máximo de itens por página imposto pelo servidor.

## Requisitos técnicos

- Java (versão LTS atual) e Spring Boot
- REST API
- Spring Web
- Spring Data JPA / Hibernate
- PostgreSQL executando via Docker / Docker Compose
- Migrations versionadas de schema (Flyway ou Liquibase — escolha e justifique)
- Bean Validation nos contratos de entrada
- DTOs distintos para entrada e saída; a entidade não é o contrato
- Tratamento centralizado de exceções com formato de erro único em toda a API
- Paginação e ordenação suportadas pela camada de dados
- Enums persistidos de forma segura a refatoração
- Testes unitários de regra de negócio
- Testes de camada web (requisição/resposta, códigos de status, payloads de erro)
- Testes de repositório/integração com banco real (Testcontainers é o caminho recomendado)
- Documentação da API gerada (OpenAPI/Swagger)
- Perfis de configuração separados para desenvolvimento e teste
- Logging estruturado mínimo

O resumo `GET /tasks/summary/status` retorna contagem por status para tarefas não arquivadas, incluindo status com contagem zero. Os perfis padrão são `dev` e `test`; `application-dev.properties` usa o banco de desenvolvimento configurado em `application.properties`, enquanto `application-test.properties` configura H2 e desativa Liquibase.

### OpenAPI / Swagger UI

Com a aplicação em execução, a especificação OpenAPI está disponível em `/api/v3/api-docs` e a interface interativa Swagger UI em `/api/swagger-ui.html`.

### Formato de erro da API

Todas as respostas de erro usam o mesmo corpo: `status` contém o status HTTP, `message` descreve a falha e `errors` é sempre uma lista. Para erros de domínio ou HTTP sem campos inválidos, `errors` fica vazia:

```json
{
  "status": "404 NOT_FOUND",
  "message": "Task not found",
  "errors": []
}
```

Erros de validação retornam `400 BAD_REQUEST` e identificam cada campo inválido:

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

## Desafios técnicos

- **Modelagem de domínio versus modelagem de tabela**: decidir o que é entidade, o que é valor, e o que é apenas coluna.
- **Ciclo de vida e invariantes**: garantir que uma transição inválida seja impossível, não apenas improvável.
- **Consultas dinâmicas**: escolher entre query methods, `@Query`, Specifications ou Querydsl, entendendo o custo de cada opção.
- **Problema de N+1**: identificar e resolver ao carregar tarefas com relacionamentos.
- **Contrato de erro**: padronizar respostas de falha e mapear exceções de domínio para códigos HTTP adequados.
- **Tempo e fuso**: datas de vencimento e "atrasado" dependem de decisões sobre tipo temporal e fuso horário.
- **Testabilidade**: escrever testes que quebrem quando a regra quebrar, e não quando o código for refatorado.

## Critérios de conclusão

O desafio está concluído quando:

- A aplicação sobe com um único comando de composição (aplicação + banco) e o schema é criado por migrations, sem geração automática de DDL em produção.
- Todas as regras de negócio listadas são verificáveis por chamadas HTTP: cada violação produz o código de status correto e um corpo de erro no formato padrão, incluindo qual campo falhou quando for validação.
- Tentar uma transição de status ilegítima falha de forma explícita e não altera o estado armazenado.
- Listagens retornam dados paginados com metadados de página, respeitam ordenação solicitada, impõem limite máximo de tamanho de página e aceitam filtros combinados.
- A listagem de tarefas com responsável e etiquetas é servida com número de consultas SQL constante relativamente à quantidade de itens retornados — e você consegue demonstrar isso.
- Existe cobertura de testes automatizados que exercita as regras de negócio e os principais fluxos HTTP, e a suíte roda de ponta a ponta sem depender de banco instalado manualmente na máquina.
- A documentação da API está acessível e reflete os contratos reais.

## Extensões opcionais

- Subtarefas e dependência entre tarefas (uma tarefa não conclui antes das dependências).
- Comentários em tarefas, com paginação própria.
- Histórico de alterações da tarefa (quem mudou o quê e quando), usando auditoria da própria stack de persistência.
- Tarefas recorrentes com geração automática da próxima ocorrência.
- Busca textual em título e descrição usando os recursos de busca do PostgreSQL.
- Exportação de listagem filtrada em CSV com streaming, sem carregar tudo em memória.
- Paginação por cursor (keyset) para as listagens grandes, comparando com a paginação por offset.
- Cache de respostas de leitura com validação condicional (ETag / If-None-Match).
- Notificação de tarefas próximas do vencimento via tarefa agendada.

## Documentação

- [Migrações do banco (Liquibase)](docs/migrations.md): como gerar, aplicar e reverter migrações.
