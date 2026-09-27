# Migrações do banco (Liquibase)

O schema do banco é versionado com Liquibase. A configuração do plugin Maven fica em `liquibase.properties` e os changelogs em `src/main/resources/db/changelog/`.

```
src/main/resources/db/changelog/
├── db.changelog-master.xml   # lista todos os changelogs, em ordem
└── changes/
    └── 001-init-schema.xml
```

Como `spring.jpa.hibernate.ddl-auto=validate`, a aplicação não sobe se o banco não bater com os models. Sempre gere e aplique uma migração depois de alterar uma entidade.

## Pré-requisito

Todos os comandos precisam do banco rodando:

```bash
docker compose up -d
```

## Aplicar migrações pendentes

```bash
./mvnw liquibase:update
```

Subir a aplicação também aplica as migrações pendentes automaticamente (`spring.liquibase.enabled=true`).

## Gerar uma nova migração a partir dos models

1. Garanta que o banco já está com todas as migrações anteriores aplicadas.
2. Gere o diff entre as entidades JPA e o banco:

   ```bash
   ./mvnw compile liquibase:diff
   ```

   O `compile` é obrigatório, pois o plugin lê as classes compiladas dos models. O resultado é gravado em `src/main/resources/db/changelog/changes/new-changelog.xml` (definido em `diffChangeLogFile` no `liquibase.properties`). Para escolher outro nome na hora:

   ```bash
   ./mvnw compile liquibase:diff -Dliquibase.diffChangeLogFile=src/main/resources/db/changelog/changes/002-descricao.xml
   ```

3. Revise o XML gerado. O diff pode sugerir drops ou alterações indesejadas.
4. Renomeie o arquivo seguindo a sequência (ex.: `002-add-coluna-x.xml`). Se o `new-changelog.xml` continuar existindo, o próximo diff é somado a ele.
5. Registre o arquivo no `db.changelog-master.xml`:

   ```xml
   <include file="db/changelog/changes/002-add-coluna-x.xml"/>
   ```

6. Aplique com `./mvnw liquibase:update` (ou subindo a aplicação).

## Outros comandos

| Comando | O que faz |
|---|---|
| `./mvnw liquibase:status` | Lista as migrações ainda não aplicadas |
| `./mvnw liquibase:updateSQL` | Mostra o SQL que seria executado, sem executar |
| `./mvnw liquibase:rollback -Dliquibase.rollbackCount=1` | Desfaz a última migração |
| `./mvnw liquibase:clearCheckSums` | Limpa checksums (necessário após editar um changeset já aplicado) |
