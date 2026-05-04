# Project Agent Rules

## Role Mapping

Project-level `AGENTS.md` has priority over global role mapping.

| Role | Agent | Required rules |
| --- | --- | --- |
| `architect` | [@backend_architect](subagent://backend_architect) | `ai/rules/backend-architecture-rules.md` (present) |
| `executor` | [@backend_implementor](subagent://backend_implementor) | `ai/rules/backend-implementation-rules.md` (present) |
| `tester` | [@backend_tester](subagent://backend_tester) | `ai/rules/backend-testing-rules.md` (present), validation phase owner |

Resolution order:

1. Project-level `AGENTS.md`.
2. Global `AGENTS.md`.
3. Unresolved role -> stop and report.

Disabled roles must not fallback to global mapping.

## Disabled Roles

- `ui-designer`
- `plan-coordinator`

## Agent Registration Check

- All mapped agents are registered in `.codex/config.toml` or `.codex/agents/`.

## AI Pipeline V2

Planning artifacts live under:

`ai/specs/<task-id>/`

Required standard-mode artifacts:

- `manifest.md`
- `manifest.yml`
- `architecture-plan.md`
- `architecture-contract.yml`
- `ui-build-sheet.md`, only if UI is active
- `ui-contract.yml`, only if UI is active
- `compatibility-report.md`
- `summary.md`
- `questions.md`
- `user-review-notes.md`
- `changelog.md`
- `validator-report.json`

Main agent rules:

- Keep main context compact.
- Do not paste full research plans into chat.
- Route user questions and answers through `questions.md`.
- Ask approval by artifact versions from `manifest.yml`.
- Do not launch executor while artifacts are stale, incompatible, or unapproved.

Research agent rules:

- Write detailed plans to `ai/specs/<task-id>/`.
- Return compact status and artifact paths in chat.
- Do not edit production/source/config files during research.
- Use `Do Not Infer` sections to block executor guesswork.

Validation phase rules:

- After `executor` finishes implementation for any backend behavior change, main agent dispatches `tester` before claiming task completion.
- `tester` validates affected behavior as an external client through HTTP endpoints.
- Use `validation-plan.md` / `validation-contract.yml` when present.
- Write `validation-result.md` and `validation-result.yml` when validation phase is active or task/profile requires artifacts.
- Create users/entities/test records only through existing endpoints to simulate full real user flow.
- Do not create, update, or delete DB records through SQL, Exposed, repositories, scripts, fixtures, DB consoles, or other direct mutation methods.
- Cover happy paths, negative paths, and meaningful corner cases for affected behavior.
- Return reproducible evidence: request, expected result, actual result, response summary, created test data, and relevant logs.

Coordinator rules:

- `plan-coordinator` is disabled for this backend project unless explicitly added later.
- Main agent handles compatibility/report duties locally when needed.
- Do not fallback to a global coordinator mapping while the role is disabled.

Executor gates:

- `manifest.yml` is approved.
- Current artifact versions match approved versions.
- `validator-report.json` status is `pass`.
- `compatibility-report.md` status is `pass`.
- No stale artifacts.
- No open blocking questions.
- Required artifacts exist.

## Runtime Modes

- `small`: one research domain, no cross-role dependency.
- `standard`: architecture + implementation/test contract dependency, coordinator handled locally unless explicitly enabled.
- `large`: 3+ domains or unknown/high-risk scope; enable a coordinator role first if dedicated coordination is required.

Mode is selected by the main agent and recorded in `manifest.yml`.
Mode may be raised, but must not be lowered within the same task.

## Project Context

- Stack: Kotlin/JVM backend service with Ktor, Koin, Exposed, PostgreSQL driver, and kotlinx.serialization.
- Entry point: `not.djinni.ApplicationKt.module`.
- Main layers: `presentation`, `domain`, `data`, `database`.
- No UI role is active unless explicitly added.
- Project-local rules override global rules.

## Verification

- Use `./gradlew test` for local JVM tests.
- Use `./gradlew build` when the change affects integration, wiring, or packaging.
- For API changes, validate positive and negative request scenarios.

## Core / Rules Update

If project core architecture changes, update relevant `ai/rules/*` in the same task.
Core/architecture changes without matching rule updates are incomplete.
