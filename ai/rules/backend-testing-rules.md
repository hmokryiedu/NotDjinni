# Backend Testing Rules

## Scope

Эти правила обязательны для `@backend_tester`.

## Manual QA Goal

Тестировать backend как внешний клиент:
- запустить Ktor server;
- выполнить HTTP requests;
- проверить status code, response body, headers/auth behavior и logs;
- вернуть воспроизводимый report.

## Startup

- Использовать Gradle или generated application script.
- Перед стартом проверить host/port из `src/main/resources/application.yaml`.
- Если порт занят, DB недоступна, config неполный или server не стартует, вернуть `blocked` с command output.
- Не менять файлы для запуска backend.

## Request Coverage

- Проверять endpoint/flow из задачи.
- Для измененного flow покрыть happy path и минимум один negative case.
- Для validation phase проверять полный user flow через public/existing HTTP endpoints, если flow затронут.
- Продумывать corner cases: missing/invalid fields, boundary values, duplicates, invalid auth, repeated requests, ordering/pagination/filtering if touched.
- Для auth flow проверять no-token/invalid-token cases, если endpoint protected.
- Для regression smoke брать только соседние endpoints, реально связанные с задачей.
- Не расширять scope до полного API audit без запроса.

## Test Data

- Test data создавать только через existing HTTP endpoints.
- Симулировать real user/client flow, включая prerequisite records.
- Использовать unique synthetic values per run.
- Cleanup делать только через existing endpoint, если такой endpoint есть.
- Если cleanup невозможен через endpoint, указать created records/identifiers в evidence.

Запрещено:
- создавать, апдейтить или удалять записи напрямую через SQL;
- использовать Exposed DAO/table calls, repositories, scripts, fixtures, DB consoles или любые direct DB mutation methods;
- обходить auth/business rules через internal APIs.

Если prerequisite нельзя создать через endpoint, вернуть `blocked` или зафиксировать validation gap. Не обходить запрет direct DB mutation.

## Evidence

Для каждого проверенного сценария указывать:
- request method and URL;
- request body, если есть;
- expected status/result;
- actual status/result;
- response body summary;
- created test data/identifiers, если есть;
- relevant logs, если есть.

## Bug Report

Каждый баг должен содержать:
- severity: blocker / major / minor;
- steps to reproduce;
- expected result;
- actual result;
- evidence;
- affected endpoint/flow.

Нельзя репортить предположения как баги. Если не воспроизведено, писать `Not reproduced`.
