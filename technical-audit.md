# NotDjinni Technical Audit

Generated: 2026-05-13  
Workspace: `/Users/hlibmokryi/Development/JVM/Kotlin/NotDjinni/NotDjinni`  
Scope: repository-level audit of tech stack, architecture, runtime config, data model, API surface, implementation patterns, verification state, risks, and open doubts.  
Method: static inspection of Gradle config, docs, source code, resources, SQL scripts, diagrams, and safe Gradle verification commands. No production code was changed.

## 1. Executive Summary

NotDjinni is a single-module Kotlin backend service for a job marketplace similar to Djinni. It exposes a REST API built on Ktor, persists to PostgreSQL through JetBrains Exposed DAO/SQL APIs, uses Koin annotations plus KSP for dependency injection, and implements JWT authentication with RSA keys.

The repository follows a clean-architecture-inspired shape:

1. `presentation`: Ktor plugins, typed resources, routes, request/response DTOs, error mapping.
2. `domain`: repository interfaces, business exceptions, use case interfaces and one recommendation use case.
3. `data`: repository implementations plus entity/domain mappers.
4. `database`: Exposed table definitions, DAO interfaces/entities, DAO implementations, transaction helper.
5. `model`: domain models and enums.

Current maturity: functional prototype or educational backend, not production-hardened yet. Build/test task succeeds, but there are no committed test sources. Runtime configuration, database credentials, host/port, and JWT key paths are hardcoded or local-machine-specific. There is no migration tool; schema is created via `SchemaUtils.create`. Security-sensitive concerns exist around password hashing, key handling, refresh token design, and public company write endpoints.

## 2. Evidence And Verification

Inspected files and areas:

- Build: `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`, `gradle.properties`, `gradle/wrapper/gradle-wrapper.properties`.
- Runtime resources: `src/main/resources/application.yaml`, `src/main/resources/logback.xml`, `src/main/resources/certificates/jwks.json`.
- Entrypoint and plugins: `Application.kt`, `presentation/plugins/*`.
- Dependency injection: `di/AppModule.kt`, Koin annotations across services.
- Routing: `presentation/router/*`, `presentation/router/routes/**`.
- Domain contracts: `domain/repository/**`, `domain/exception/**`, `domain/usecase/**`.
- Data repositories and mappers: `data/repository/**`, `data/mapper/**`.
- Persistence: `database/NotDjinniDatabase.kt`, `database/api/**`, `database/impl/**`.
- Docs/scripts/diagrams: `README.md`, `documentation/project-architecture.md`, `SQL_SCHEME_EXAMPLE.sql`, `scripts/*.sql`, `diagram/**/*.puml`.

Commands run:

```bash
./gradlew tasks --all
./gradlew test
```

Results:

- `./gradlew tasks --all`: `BUILD SUCCESSFUL`.
- `./gradlew test`: `BUILD SUCCESSFUL`.
- KSP warning seen during test: `[Deprecation] 'defaultModule' generation is deprecated. Use KSP argument arg("KOIN_DEFAULT_MODULE","true") to activate default module generation.`
- `src/test` does not exist, so test success currently means compilation and empty test task success, not behavioral coverage.

Repository state observed before edits:

- Existing untracked `TECHNICAL_AUDIT.md`.
- Existing unrelated deleted files under `examples/**`.
- This audit intentionally creates lowercase `technical-audit.md` and does not touch the existing untracked uppercase file.

## 3. Build And Project Shape

### Gradle Shape

- Root project name: `NotDjinni`.
- Single Gradle project; no subprojects declared in `settings.gradle.kts`.
- Repositories: `mavenCentral()` only.
- Gradle wrapper distribution: `gradle-8.14.3-bin.zip`.
- Group: `not.djinni`.
- Version: `0.0.1`.
- Application main class: `io.ktor.server.netty.EngineMain`.

### Applied Plugins

From `build.gradle.kts`:

- `org.jetbrains.kotlin.jvm`
- `io.ktor.plugin`
- `com.google.devtools.ksp`
- `org.jetbrains.kotlin.plugin.serialization`

### Version Catalog

Effective dependency versions in `gradle/libs.versions.toml`:

- Kotlin: `2.2.20`
- KSP: `2.2.20-2.0.3`
- Ktor: `3.3.0`
- Koin: `4.1.0`
- Koin annotations/compiler: `2.1.0`
- Exposed: `0.47.0`
- PostgreSQL JDBC driver: `42.7.3`
- Logback: `1.5.18`

Important drift:

- `README.md` and `documentation/project-architecture.md` say Kotlin `1.9+`, but build config uses Kotlin `2.2.20`.
- `gradle.properties` contains old-looking or unused values: `exposed_version=0.61.0`, `koin_version=3.5.6`, `logback_version=1.4.14`, etc. The build uses the version catalog, not these properties.

### Dependencies

Ktor bundle:

- `ktor-server-core`
- `ktor-server-netty` (catalog alias says `ktor-server-cio`, but module is Netty)
- `ktor-server-cors`
- `ktor-server-call-logging`
- `ktor-server-auth`
- `ktor-server-auth-jwt`
- `ktor-server-resources`
- `ktor-server-config-yaml`
- `ktor-serialization-kotlinx-json`
- `ktor-server-content-negotiation-jvm`

Koin bundle:

- `koin-core`
- `koin-annotations`
- `koin-ktor`
- `koin-logger-slf4j`
- KSP compiler: `koin-ksp-compiler`

Persistence bundle:

- `exposed-dao`
- `exposed-core`
- `exposed-jdbc`
- `exposed-kotlin-datetime`
- `postgresql`

Logging:

- `logback-classic`

No test dependencies are declared.

## 4. Runtime Configuration

### Ktor Deployment

`application.yaml`:

```yaml
ktor:
  application:
    modules:
      - not.djinni.ApplicationKt.module
  deployment:
    port: 8080
    host: "192.168.0.8"
```

Implications:

- Service is configured for a specific LAN IP, not `0.0.0.0`, `127.0.0.1`, or environment-driven host.
- This may break on machines without `192.168.0.8`.
- Port is fixed to `8080`.

### JWT Runtime Config

`application.yaml`:

```yaml
jwt:
  issuer: "http://192.168.0.8:8080"
  audience: "http://192.168.0.8:8080"
  realm: "NotDjinni API"
```

`DefaultTokenProvider` loads this YAML and reads keys from:

- `keys/private_key.pem`
- `keys/public_key.pem`

JWT details:

- Algorithm: RSA256.
- Claim used for user identity: `userId`.
- Access token expiry: `3_600_000L` milliseconds, i.e. 1 hour.

### Database Runtime Config

`NotDjinniDatabase` connects directly to:

```text
jdbc:postgresql://localhost:5432/notdjinni
user=notdjinni
password=notdjinnipassword
```

Implications:

- DB host, port, database, username, and password are hardcoded.
- No environment variable or config-file override is used.
- App startup requires local PostgreSQL with matching database/user/password.
- There is no connection pool configuration visible.

### Logging

`logback.xml`:

- Console appender.
- Pattern includes timestamp, thread, level, logger, message.
- Root level: `INFO`.
- Netty and Jetty loggers: `INFO`.

Ktor `CallLogging` is installed without custom filters or MDC.

## 5. Application Bootstrap

Entrypoint:

- `main(args)` delegates to `EngineMain.main(args)`.
- Ktor module is `Application.module()`.

Startup sequence in `Application.module()`:

1. `NotDjinniDatabase.init()`
2. `installKoin()`
3. `installLogging()`
4. `installAuthentication()`
5. `install(Resources)`
6. `installSerialization()`
7. `get<Router>().install(this)`

Important detail:

- `installCORS()` exists in `presentation/plugins/CORS.kt`, but `Application.module()` does not call it. Despite Ktor CORS dependency being present, CORS is not active.

## 6. Dependency Injection

DI mechanism:

- Koin runtime integration through `install(Koin)`.
- Koin annotation scanning through `@Module` and `@ComponentScan("not.djinni")`.
- KSP generates module access through `org.koin.ksp.generated.module`.

Module root:

```kotlin
@Module
@ComponentScan("not.djinni")
class AppModule
```

Component annotations used:

- `@Single` for repositories, DAOs, route handlers, router, token provider.
- `@Factory` for `GetRecommendedVacanciesForSeekerUseCase`.

Patterns:

- Implementations bind to interfaces through annotations, e.g. `@Single(binds = [TokenProvider::class])`.
- Some code uses old Koin annotation syntax style: `@Single([Router::class])`; it compiles.
- KSP warning suggests current default module generation setup is deprecated.

## 7. Architecture And Package Map

Top-level package: `not.djinni`.

Observed structure:

```text
src/main/kotlin/not/djinni/
  Application.kt
  auth/
  data/
  database/
  di/
  domain/
  model/
  presentation/
```

### Presentation Layer

Main responsibilities:

- Ktor plugin installation.
- Typed resource definitions with `io.ktor.resources.Resource`.
- HTTP route registration.
- Request DTO deserialization.
- Response DTO serialization.
- HTTP status mapping.
- Extract JWT principal/claims.
- Translate domain exceptions to error responses.

Subpackages:

- `presentation/plugins`
- `presentation/router`
- `presentation/router/routes/auth`
- `presentation/router/routes/user`
- `presentation/router/routes/token`
- `presentation/router/routes/seeker`
- `presentation/router/routes/company`
- `presentation/router/routes/employer`
- `presentation/router/routes/vacancy`
- `presentation/router/routes/application`
- `presentation/router/routes/common`

Route composition:

- `Router` interface defines `fun install(application: Application)`.
- `DefaultRouter` injects all route classes and installs them under one Ktor `routing` block.

### Domain Layer

Main responsibilities:

- Repository contracts.
- Business exception classes.
- Use case interfaces.
- Recommendation use case.

Repository interfaces:

- `AuthRepository`
- `UserRepository`
- `SeekerProfileRepository`
- `CompanyRepository`
- `EmployerProfileRepository`
- `VacancyRepository`
- `ApplicationRepository`

Use cases:

- `UseCase<T>`
- `UseCaseWithParams<T, P>`
- `GetRecommendedVacanciesForSeekerUseCase`

Most business logic currently lives in repository implementations, not in separate use cases.

### Data Layer

Main responsibilities:

- Repository implementations.
- Entity/domain mapping.
- Business rule orchestration involving multiple DAOs.

Repository implementations:

- `DefaultAuthRepository`
- `DefaultUserRepository`
- `DefaultSeekerProfileRepository`
- `DefaultCompanyRepository`
- `DefaultEmployerProfileRepository`
- `DefaultVacancyRepository`
- `DefaultApplicationRepository`

Common pattern:

```kotlin
override suspend fun operation(...) = runCatching {
    // validate / authorize
    // call DAO
    // map entity to domain
}
```

### Database Layer

Main responsibilities:

- Exposed database connection.
- Transaction helper.
- DAO contracts and persistence DTOs.
- Table definitions and DAO implementations.

Core object:

- `NotDjinniDatabase`
  - `Database.connect(...)`
  - `SchemaUtils.create(...)`
  - `runQuery { ... }` using `newSuspendedTransaction(Dispatchers.IO)`

Subpackages:

- `database/api/*`: DAO interfaces, entity data classes, filter objects.
- `database/impl/*`: Exposed `LongIdTable`, `LongEntity`, DAO implementations.

### Model Layer

Main responsibilities:

- Domain models.
- Enums.
- Token models.

Domains:

- User
- Seeker profile and work experience
- Employer profile and company
- Vacancy
- Application
- Tokens

## 8. API Surface

The app uses Ktor typed resources. Main endpoints observed:

### Auth

Resource root: `/auth`

- `POST /auth/login`
- `POST /auth/register`
- `POST /auth/refresh`
- `POST /auth/logout`

Behavior:

- Register validates name and password strength.
- Login validates SHA-256 password hash.
- Register/login return access and refresh tokens.
- Refresh rotates refresh token by deleting old token and generating new pair.
- Logout deletes refresh token.

### Token

- `GET /token/validate`

Behavior:

- Protected by JWT auth.
- Returns "Token is valid" message.

### User

- `GET /user/me` or equivalent resource path defined by `User` resource.

Behavior:

- Protected by JWT.
- Uses `userId` claim.
- Returns current user.

### Company

Resource root: `/company`

- `GET /company`
- `GET /company/{id}`
- `GET /company/search?name=...`
- `POST /company`
- `PUT /company/{id}`
- `DELETE /company/{id}`

Behavior:

- Public read and public write. No JWT protection on create/update/delete.

### Employer

Resource root under `Employer` resource.

Operations:

- Get employer profile.
- Create employer profile.
- Update employer role.
- Delete employer profile.
- Get employer vacancies.

Behavior:

- All protected by JWT.
- Employer profile links user to company.
- Update only changes role; company association is effectively immutable through API.

### Seeker

Resource root under `Seeker` resource.

Operations:

- Get seeker profile.
- Create seeker profile.
- Update seeker profile.
- Delete seeker profile.
- Add work experience.
- Update work experience by id.
- Delete work experience by id.
- Get recommended vacancies.

Behavior:

- All protected by JWT.
- Recommended vacancies use seeker profile salary, experience, category, and optional query.

### Vacancy

Resource root: `/vacancy`

- `GET /vacancy`
- `GET /vacancy/{id}`
- `GET /vacancy/recent?limit=...`
- `GET /company/{companyId}/vacancies`
- `POST /vacancy`
- `PUT /vacancy/{id}`
- `DELETE /vacancy/{id}`
- `PUT /vacancy/{id}/status`

Behavior:

- Public listing/detail/recent/company vacancies.
- Create/update/delete/status update protected by JWT.
- Mutations require employer profile and same company authorization.
- Filtering supports query params:
  - `category`
  - `status`
  - `employment_type`
  - `company_id`
  - `salary_min`
  - `salary_max`
  - `experience_years`
  - `search`
  - `sort_by`
  - `sort_direction`
  - `limit`
  - `offset`

Defaults:

- `limit = 20`
- `offset = 0`
- vacancy sort default: `CREATED_AT DESC`

### Application

Resource root: `/application`

- `POST /application`
- `GET /application`
- `GET /application/{id}`
- `PUT /application/{id}`
- `DELETE /application/{id}`
- `PUT /application/{id}/status`
- `GET /application/vacancy/{vacancyId}`
- `GET /application/check/vacancy/{vacancyId}`

Behavior:

- All protected by JWT.
- Job seekers create/update/delete their own applications.
- Employers for same company can view applications and update statuses.
- Duplicate application prevention at repository level and DB unique index.

## 9. Data Model

### User

Exposed table: `UserTable`, SQL name `"user"`.

Fields:

- `id: Long`
- `name: String`, max 100
- `email: String`, max 255
- `password: String`, max 255

Notes:

- `SQL_SCHEME_EXAMPLE.sql` declares `email UNIQUE`.
- Exposed `UserTable` does not declare `uniqueIndex()` on `email`.
- Repository checks duplicate email before insert, but DB-level uniqueness is not enforced by Exposed schema.

### Refresh Token

Table: `refresh_tokens`.

Fields:

- `id`
- `userId: Long`, indexed
- `token: String`, unique, max 512
- `expiresAt: Instant`

Notes:

- Exposed table uses plain `long("user_id")`, not a foreign-key reference to `UserTable`.
- `SQL_SCHEME_EXAMPLE.sql` declares FK cascade to `"user"(id)`, but Exposed schema does not.
- Expired-token cleanup DAO exists (`deleteExpired`) but no scheduled job or startup cleanup is visible.

### Company

Table: `companies`.

Fields:

- `id`
- `company_name`, unique
- `website`, nullable
- `description`

Notes:

- Public API allows unauthenticated company create/update/delete.

### Employer Profile

Table: `employer_profiles`.

Fields:

- `id`
- `user_id`, unique FK to `UserTable`
- `company_id`, FK to `CompanyTable`
- `role`

Notes:

- One employer profile per user.
- Company association is set at creation.

### Seeker Profile

Table: `job_seeker_profiles`.

Fields:

- `id`
- `user_id`, unique FK to `UserTable`
- `specialty`
- `experience_years`
- `desired_salary`
- `about_me`, nullable
- `job_category`

Notes:

- One seeker profile per user.
- Job category uses Kotlin enum persisted through Exposed `enumeration`.

### Work Experience

Table: `work_experience`.

Fields:

- `id`
- `profile_id`, FK to `SeekerProfileTable`
- `company_name`
- `position`
- `description`, nullable
- `start_date`
- `end_date`, nullable

Notes:

- DAO validates blank company/position, start after end, future start date.
- No DB cascade option is specified in Exposed table definition.

### Vacancy

Table: `vacancies`.

Fields:

- `id`
- `company_id`, FK to `CompanyTable`
- `title`
- `description`
- `salary_min`
- `salary_max`
- `min_experience_years`, nullable
- `employment_type`, nullable enum
- `category`, nullable enum
- `status`, enum
- `created_at`
- `updated_at`

Enums:

- `VacancyStatusCode`: `DRAFT`, `ACTIVE`, `PAUSED`, `CLOSED`, `EXPIRED`
- `EmploymentTypeCode`: present, values referenced by seed comments.
- `JobCategoryCode`: present, values referenced by seed comments.

Notes:

- SQL seed script stores enum values as ordinals (`0`, `1`, etc.).
- Exposed `enumeration<T>` commonly maps enum values through integer ordinals in this style, matching seed script intent.
- There is no validation visible for `salary_min <= salary_max`, nonblank title, or nonnegative salary in route/repository code.

### Application

Table: `applications`.

Fields:

- `id`
- `vacancy_id`, FK to `VacancyTable`, `onDelete = CASCADE`
- `job_seeker_id`, FK to `SeekerProfileTable`, `onDelete = CASCADE`
- `status`
- `cover_letter`, nullable
- `created_at`
- `updated_at`

Enums:

- `ApplicationStatusCode`: `APPLIED`, `REVIEWING`, `INTERVIEW`, `TEST_TASK`, `OFFER`, `HIRED`, `REJECTED`, `WITHDRAWN`

Constraints:

- Unique index on `(vacancy_id, job_seeker_id)`.
- Repository also checks duplicates before insert.

## 10. Persistence Patterns

### Transaction Model

All DAO operations use:

```kotlin
NotDjinniDatabase.runQuery {
    // Exposed call
}
```

`runQuery` wraps `newSuspendedTransaction(Dispatchers.IO)`.

### Schema Creation

On startup:

```kotlin
SchemaUtils.create(
    UserTable,
    RefreshTokenTable,
    SeekerProfileTable,
    WorkExperienceTable,
    CompanyTable,
    EmployerProfileTable,
    VacancyTable,
    ApplicationTable
)
```

Implications:

- App creates missing tables at startup.
- No versioned migrations.
- No explicit schema evolution.
- No rollback/down migrations.
- Startup has side effects against database.

### Query Style

Mixed Exposed styles:

- DAO entity style: `LongEntity`, `LongEntityClass`, `EntityID`.
- SQL DSL style: `selectAll()`, `andWhere`, `innerJoin`, `deleteWhere`.

### Filtering And Sorting

Vacancy filtering is built dynamically from `VacancyFilter`:

- Company id equality.
- Category/status/employment type `inList`.
- Salary min/max comparisons.
- Experience comparison.
- Search against lowercased title or description.
- Sort field mapping to table columns.

Application filtering:

- Optional company join.
- Vacancy id.
- Job seeker id.
- Status.
- Sort by created/updated.

Potential issue:

- Search query is interpolated into `like "%$searchQuery%"` after query param parsing. Exposed parameterization likely protects values at SQL-expression level, but wildcard behavior is not escaped. `%` and `_` in user input will act as LIKE wildcards.

## 11. Authentication And Authorization

### Access Token

- JWT signed with RSA256.
- Contains `userId`.
- Validated by Ktor JWT plugin with issuer and audience.
- Rejected if `userId` claim is missing.

### Refresh Token

- Generated by same `TokenProvider.generate(userId)` method as access token.
- Stored in DB with 30-day application-level expiry.
- On refresh:
  - token string is looked up in DB;
  - DB expiry is checked;
  - old refresh token is deleted;
  - new access and refresh tokens are generated.

Design concern:

- Access token and refresh token are structurally same JWT type; there is no separate token type claim, jti, or dedicated refresh-token signing/expiry semantics.
- Refresh endpoint does not verify JWT signature/issuer/audience directly; it trusts DB lookup by token string and DB expiry.

### Password Handling

Current implementation:

```kotlin
MessageDigest.getInstance("SHA-256").digest(password.toByteArray()).toHexString()
```

Risks:

- No per-user salt.
- No adaptive password hashing.
- SHA-256 is not appropriate for password storage in production.
- Recommended production options: Argon2id, bcrypt, or PBKDF2 with strong parameters and per-user salt.

### Authorization Rules

Observed repository-level authorization:

- Vacancy mutations require an employer profile.
- Vacancy mutation is allowed only if employer profile company matches vacancy company.
- Application read is allowed for owning seeker or employer from vacancy company.
- Application update/delete is seeker-owner only.
- Application status update is employer-company only.
- Employer profile APIs require JWT.
- Seeker profile APIs require JWT.
- Company mutations are public.

There is no role enum or role table. User can potentially have both seeker and employer profiles.

## 12. Serialization And DTO Style

Serialization:

- `kotlinx.serialization`.
- Ktor `ContentNegotiation` with JSON.

JSON config:

```kotlin
Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
    prettyPrint = true
}
```

Implications:

- API tolerates unknown fields.
- Lenient parsing may accept non-strict JSON forms.
- `explicitNulls = false` omits nulls in output.
- `prettyPrint = true` increases response size.

DTO patterns:

- Requests and responses are separate from domain models.
- `@Serializable` is used on request/response DTOs.
- Mappers convert:
  - request DTO to domain;
  - domain to response DTO;
  - entity to domain and back.

Naming style:

- Domain uses camelCase.
- JSON likely uses Kotlin property names unless `@SerialName` is present.
- Some README examples use snake_case, while code needs inspection per DTO for actual serialized names. This is a likely doc/API drift area.

## 13. Error Handling

Pattern:

- Domain-specific exceptions inherit from sealed `Throwable`.
- Presentation mapper converts exception type to `HttpStatusCode`.
- Shared `Result<T>.handleError(...)` converts failures into error responses.

Default:

- Unknown exceptions map to `ErrorResult.Default` through `toErrorResult`.

Strengths:

- Consistent status mapping pattern.
- Business errors stay typed.
- Routes avoid repeated try/catch.

Weaknesses:

- `runCatching` may catch programmer errors and infrastructure failures, then expose raw exception messages through `message?.toErrorResponse()`.
- Some DAOs call `error(...)`, producing generic `IllegalStateException`.
- No global status pages plugin is visible.
- User route uses custom `onFailure` instead of shared `handleError`, minor consistency gap.

## 14. Business Capabilities

### Auth

- Register.
- Login.
- Refresh access token.
- Logout.
- Validate token.

Validation:

- Password regex requires lowercase, uppercase, digit, length 8-60.
- Name length 2-100 and not blank.
- Email uniqueness checked in repository, not DB by Exposed table.

### User

- Get current user.

### Company

- Create company.
- List all companies.
- Search company by name.
- Get by id.
- Update company.
- Delete company.

### Employer

- Create employer profile linked to company.
- Get profile with company.
- Update role.
- Delete profile.
- List employer/company vacancies.

### Seeker

- Create seeker profile.
- Get profile.
- Update profile.
- Delete profile.
- Manage work experience.
- Get recommended vacancies.

### Vacancy

- Create vacancy as employer.
- Public list/search/detail/recent/company vacancy discovery.
- Update vacancy.
- Delete vacancy.
- Update vacancy status.

### Application

- Job seeker applies to vacancy.
- Job seeker lists own applications.
- Job seeker updates cover letter.
- Job seeker deletes application.
- Employer views applications for vacancy.
- Employer updates application status.
- Check if current seeker has applied to a vacancy.

## 15. Recommendation Logic

Only explicit use case found:

`GetRecommendedVacanciesForSeekerUseCase`

Inputs:

- `userId`
- free-text query
- limit
- offset

Logic:

1. Load seeker profile.
2. Build `VacancyFilter`:
   - `searchQuery = params.query`
   - `salaryMin = seekerProfile.desiredSalary`
   - `experienceYears = seekerProfile.experienceYears`
   - `categories = listOf(seekerProfile.jobCategory)`
3. Return vacancy repository results.

Interpretation:

- This is filter-based recommendation, not scoring/ranking/ML.
- It recommends jobs matching seeker category, seeker experience, and desired salary minimum.

Potential semantic issue:

- Vacancy DAO applies `VacancyTable.salaryMin greaterEq salaryMin`, which means vacancy lower bound must be at least seeker desired salary. Depending on product intent, matching by `salaryMax >= desiredSalary` may be more inclusive.

## 16. Documentation And Diagrams

Docs:

- `README.md`: long user-facing API and feature doc.
- `documentation/project-architecture.md`: architecture guide.
- `SQL_SCHEME_EXAMPLE.sql`: SQL schema reference.
- `TECHNICAL_AUDIT.md`: existing untracked file, not modified by this audit.
- `technical-audit.md`: this new audit file.

Diagrams:

- Business flows under `diagram/business`.
- Class diagrams under `diagram/classes`.
- Component diagram under `diagram/components`.
- Sequence diagrams under `diagram/consequences`.
- Cooperation diagrams under `diagram/cooperation`.
- Deployment diagram under `diagram/deployment`.
- State diagrams under `diagram/states`.
- Use-case diagrams under `diagram`.

Diagram format:

- PlantUML (`.puml`).

Docs drift:

- README/docs mention Kotlin `1.9+`; actual build is Kotlin `2.2.20`.
- SQL example contains constraints/indexes that Exposed table definitions do not fully create.
- README endpoint examples need validation against actual DTO `@SerialName` usage before treating as contract.

## 17. Scripts And Seed Data

Scripts:

- `scripts/companies.sql`
- `scripts/seed_vacancies.sql`

Company script:

- Inserts many Ukrainian/global tech companies.

Vacancy script:

- Inserts sample vacancies for company IDs.
- Assumes companies already exist.
- Comments define enum ordinals for employment type, category, status.

Operational note:

- Seed scripts are manual SQL scripts.
- No Gradle task or app startup seeding mechanism is visible.

## 18. Testing State

Observed:

- No `src/test` directory.
- No test dependencies in build file.
- `./gradlew test` succeeds because compilation succeeds and there are no real tests.

Implications:

- No automated coverage for auth flows.
- No automated coverage for repository authorization.
- No automated coverage for DB schema/table mapping.
- No automated coverage for route request/response contracts.
- No automated coverage for edge cases like duplicate applications, invalid enum params, public/company writes, refresh token rotation.

Suggested first tests:

1. Unit tests for `DefaultAuthRepository` password validation, duplicate email, refresh rotation, logout.
2. Unit tests for `DefaultVacancyRepository` same-company authorization.
3. Unit tests for `DefaultApplicationRepository` owner/employer authorization and duplicate prevention.
4. Route tests with Ktor test host for auth, company, vacancy, application flows.
5. DAO integration tests using Testcontainers PostgreSQL or a local controlled database.

No new dependencies were added during this audit.

## 19. Security Findings

### High Priority

1. Password hashing is plain SHA-256.
   - Risk: fast offline cracking if DB leaks.
   - Better: Argon2id/bcrypt/PBKDF2 with salt and work factor.

2. Private key is read from `keys/private_key.pem`.
   - `.gitignore` excludes `/keys`, but files exist locally.
   - Risk depends on whether keys were ever committed or shared.
   - Better: secrets manager, env path, mounted secret, rotation process.

3. Database credentials are hardcoded in source.
   - Risk: local-only config becomes production leak or blocks deployment.
   - Better: environment-driven config and no secrets in code.

4. Company write endpoints are public.
   - Risk: any client can create, modify, or delete companies.
   - Maybe intentional for seeding/prototype, but dangerous in production.

### Medium Priority

5. Refresh token and access token share same generator and shape.
   - Better: token type claim, jti, different expiry semantics, explicit refresh-token validation path.

6. No migration framework.
   - Risk: schema changes are unsafe after real data exists.
   - Better: Flyway or Liquibase.

7. `SchemaUtils.create` runs at startup.
   - Useful for prototypes, risky for production schema ownership.

8. CORS plugin exists but is not installed.
   - If browser clients need API access, current config will not apply.

9. Unknown exceptions can leak messages.
   - `toErrorResult` uses throwable message for response body.

10. No rate limiting or brute-force protection visible.
    - Auth endpoints can be abused.

### Lower Priority / Hardening

11. JSON `prettyPrint = true` increases response payload size.
12. JSON `isLenient = true` may accept inputs stricter APIs would reject.
13. Ktor host/issuer/audience hardcoded to a LAN IP.
14. Refresh token cleanup exists but is not scheduled.
15. LIKE wildcard escaping is not handled for search.

## 20. Reliability And Maintainability Findings

### Strong Parts

- Clear package layering.
- Separate request/response DTOs.
- Repository interfaces isolate domain from persistence.
- Typed routes via Ktor Resources reduce string-path duplication.
- Consistent mapper functions.
- Business exceptions map to HTTP statuses.
- Authorization logic is mostly centralized in repositories.
- DAO code uses suspended Exposed transactions.
- Build is modern: Kotlin 2.2.20, Ktor 3.3.0, Gradle 8.14.3.

### Weak Parts

- No tests.
- No migrations.
- Runtime config is not environment-based.
- Docs/config drift.
- Business validation is partial and distributed.
- Most business behavior is in repository implementations, while use-case layer is barely used.
- No OpenAPI output checked in despite Ktor plugin task existing.
- No Docker Compose or local DB setup file visible.
- No CI config found in inspected file list.

## 21. Code Style And Conventions

Observed conventions:

- Kotlin official style enabled in `gradle.properties`.
- Small route methods per endpoint.
- Extension mappers named `toDomain`, `toResponse`, `toEntity`.
- Repositories named `DefaultXRepository`.
- DAOs named `DefaultXDao`.
- Tables named `XTable`; Exposed entities named `XTableEntity`.
- Domain exceptions as sealed classes.
- `Result<T>` returned from repositories instead of throwing across route boundary.
- Constants in companion objects for query param names/defaults.

Potential cleanup:

- Remove unused imports in `UserRoute` and `TokenRoute`.
- Align Koin annotation syntax consistently.
- Move validation out of DAOs into domain/data layer consistently.
- Normalize error handling to always use `handleError`.

## 22. Deployment And Operations

Supported by Gradle/Ktor tasks:

- `run`
- `runFatJar`
- `buildFatJar`
- `runDocker`
- `buildImage`
- `publishImage`
- `publishImageToLocalRegistry`
- Shadow distribution tasks.
- Jib tasks from Ktor plugin.

Observed gaps:

- No Dockerfile inspected.
- No docker-compose file for PostgreSQL.
- No environment-specific configs.
- No health endpoint visible.
- No readiness/liveness endpoint visible.
- No metrics/tracing visible.
- No connection pool sizing visible.
- No structured audit logging visible.

## 23. Questions And Doubts

These are not blockers for the audit, but they matter before production work:

1. Is public company create/update/delete intentional, or should companies be admin/employer protected?
2. Should one user be allowed to have both seeker and employer profiles?
3. Should recommended vacancies match `salaryMax >= desiredSalary` instead of `salaryMin >= desiredSalary`?
4. Should vacancy creation allow `DRAFT` only by default, or can clients set any initial status?
5. Should applications be allowed only for `ACTIVE` vacancies?
6. Should employers be able to update application status to any status, or should there be a state-transition graph?
7. Are README snake_case JSON examples still accurate for current DTO serialization?
8. Is `192.168.0.8` the intended development host, or a stale local value?
9. Is `TECHNICAL_AUDIT.md` an older user file that should be replaced, kept, or merged later?
10. Should schema be owned by Exposed startup creation, SQL scripts, or a migration tool?

## 24. Recommended Next Steps

### Immediate

1. Externalize DB and JWT config to environment variables or Ktor config.
2. Replace SHA-256 password hashing with adaptive salted hashing.
3. Decide and enforce authorization for company write endpoints.
4. Add real tests for auth, vacancy authorization, application authorization, and duplicate application prevention.
5. Install CORS or remove unused CORS plugin/dependency/config if not needed.

### Near Term

1. Add migration tool and stop relying on `SchemaUtils.create` for persistent environments.
2. Add local dev setup docs for PostgreSQL.
3. Align README/docs with Kotlin 2.2.20 and actual JSON naming.
4. Add OpenAPI generation output or API contract tests.
5. Add CI to run `./gradlew test`.

### Later

1. Introduce health/readiness endpoints.
2. Add rate limiting for auth endpoints.
3. Add structured logging and request correlation.
4. Add token revocation/cleanup job.
5. Move complex business flows from repositories into use cases as the domain grows.

## 25. Audit Conclusion

NotDjinni is a clear, compact Ktor/PostgreSQL backend with strong educational architecture signals: clean package layering, typed routing, repositories, mappers, Koin annotation DI, and Exposed persistence. The main implementation approach is pragmatic repository-centered business logic with DTO/domain/entity mapping at layer boundaries.

Primary gap is not architecture shape; it is production readiness. Hardcoded config, weak password hashing, absent migrations, public write endpoints for companies, no tests, and docs drift should be addressed before treating this service as deployable beyond local/prototype use.
