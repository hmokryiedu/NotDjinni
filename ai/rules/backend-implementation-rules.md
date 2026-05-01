# Backend Implementation Rules

## Scope

Эти правила обязательны для `@backend_implementor`.

## Implementation Principles

- Работать только по approved Research Plan.
- Делать минимальный diff.
- Следовать существующему package layout и naming.
- Не рефакторить соседний код без необходимости.
- Не добавлять speculative flexibility, config или abstractions.
- Не менять dependencies без явного approval.

## Layer Rules

- Route changes держать в `presentation/router/routes/**`.
- Request/response DTO changes держать рядом с соответствующим route.
- Domain contracts/models менять только если это явно часть plan.
- Repository implementations и mappers менять в `data/**`.
- DAO/table/entity changes держать в `database/**`.
- Koin wiring менять в `di/**` только для нужных bindings.

## Behavior And Errors

- Сохранять существующий error mapping style.
- Не скрывать domain exceptions generic HTTP errors, если nearby code maps them specifically.
- Для auth-sensitive endpoints проверять JWT requirements и role/user ownership.
- Для validation добавлять негативные scenarios в verification plan.

## Tests And Checks

- Если меняется behavior, добавить focused tests, если test infra доступна или plan требует ее создать.
- Если test infra отсутствует и добавлять ее не было approved, запустить compile/build и указать residual risk.
- Минимальная verification после code changes: релевантный Gradle compile/test/build task.
- Не заявлять success без свежего command output.
