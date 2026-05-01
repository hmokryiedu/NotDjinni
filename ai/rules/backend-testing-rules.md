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
- Для auth flow проверять no-token/invalid-token cases, если endpoint protected.
- Для regression smoke брать только соседние endpoints, реально связанные с задачей.
- Не расширять scope до полного API audit без запроса.

## Evidence

Для каждого проверенного сценария указывать:
- request method and URL;
- request body, если есть;
- expected status/result;
- actual status/result;
- response body summary;
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
