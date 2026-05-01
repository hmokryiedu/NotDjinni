# Правила агентов проекта

## Маппинг ролей

Проектный `AGENTS.md` имеет приоритет над глобальным маппингом ролей.

| Роль | Агент | Обязательные правила |
| --- | --- | --- |
| `architect` | [@backend_architect](subagent://backend_architect) | `ai/rules/backend-architecture-rules.md` |
| `executor` | [@backend_implementor](subagent://backend_implementor) | `ai/rules/backend-implementation-rules.md` |
| `tester` | [@backend_tester](subagent://backend_tester) | `ai/rules/backend-testing-rules.md` |

- Если проектный `AGENTS.md` задаёт `role -> agent`, использовать его вместо глобального маппинга.
- Глобальный маппинг используется только как резервный вариант, если проектный маппинг отсутствует.
- В этом backend-проекте нет роли `ui-designer` в project mapping.

## Правила агентов

- `@backend_architect` обязан использовать `ai/rules/backend-architecture-rules.md`.
- `@backend_implementor` обязан использовать `ai/rules/backend-implementation-rules.md`.
- `@backend_tester` обязан использовать `ai/rules/backend-testing-rules.md`.
- Если задача меняет структуру backend `core`/архитектуры, в той же задаче обязательно обновить релевантные `ai/rules/*`.
- Изменения backend `core`/архитектуры без обновления релевантных `ai/rules/*` считаются незавершёнными.

## Backend Project Context

- Stack: Kotlin/JVM, Ktor, Koin, Exposed, PostgreSQL driver, kotlinx.serialization.
- Entry point: `not.djinni.ApplicationKt.module`.
- Main layers:
  - `presentation`: Ktor plugins, router, routes, request/response DTOs.
  - `domain`: repository contracts, use cases, models, domain exceptions.
  - `data`: repository implementations and mappers.
  - `database`: DAO/table/entity storage layer.

## Тесты и проверки

- Для backend manual QA использовать `@backend_tester`.
- Для локальных JVM unit tests использовать project style и existing Gradle conventions.
- Не использовать Android-specific testing rules для этого проекта.
