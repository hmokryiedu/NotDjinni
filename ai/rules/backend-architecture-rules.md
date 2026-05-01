# Backend Architecture Rules

## Scope

Эти правила обязательны для `@backend_architect`.

## Source Of Truth

- Сначала читать код проекта.
- `build.gradle.kts`, `gradle/libs.versions.toml` и `src/main/resources/application.yaml` использовать для stack/config evidence.
- Диаграммы и документы использовать только как вспомогательный context.
- Если документы конфликтуют с кодом, явно фиксировать конфликт.

## Architecture Boundaries

- `presentation` отвечает за Ktor plugins, router, routes, resources, request/response DTOs и HTTP error mapping.
- `domain` отвечает за repository contracts, use cases, core models и domain exceptions.
- `data` отвечает за repository implementations и mapping между domain/database.
- `database` отвечает за Exposed tables, DAO, entities, filters и storage operations.
- Зависимости должны идти в сторону `presentation -> domain -> data/database` через существующие contracts/DI.
- Koin wiring меняется только если план требует новый binding или замену существующего.

## Decision Rules

- Предпочитать минимальное изменение, которое закрывает задачу.
- Не добавлять новые abstraction layers без прямой причины.
- Не менять public API, DB schema, auth policy или serialization contract без явного решения.
- Для каждого важного решения указывать evidence и confidence.
- Если route contract, DB shape, auth rule или ownership неясны, вернуть вопрос main agent через `agent_status`.

## Required Handoff

Research handoff должен содержать:
- цель и scope;
- затронутые areas/layers;
- route/API contract, если меняется HTTP surface;
- DB/storage impact, если есть;
- DI/wiring impact, если есть;
- error/edge cases;
- validation plan;
- risks and rollback notes, если риск не нулевой.
