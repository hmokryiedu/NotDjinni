# NotDjinni Project Architecture Documentation

## Table of Contents
1. [Overview](#overview)
2. [Technology Stack](#technology-stack)
3. [Architecture Pattern](#architecture-pattern)
4. [Project Structure](#project-structure)
5. [Layer Responsibilities](#layer-responsibilities)
6. [Naming Conventions](#naming-conventions)
7. [Code Style Guidelines](#code-style-guidelines)
8. [CRUD Operation Patterns](#crud-operation-patterns)
9. [Error Handling](#error-handling)
10. [Authentication & Authorization](#authentication--authorization)
11. [Dependency Injection](#dependency-injection)
12. [Best Practices](#best-practices)

---

## Overview

NotDjinni is a Kotlin-based REST API built with Ktor that implements a job marketplace platform. The application connects job seekers with employers, handling user authentication, profile management, and job-related operations.

**Core Features:**
- User authentication (JWT-based)
- Job seeker profiles with work experience
- Employer profiles with company associations
- Company management
- Role-based access control

---

## Technology Stack

- **Language**: Kotlin 1.9+
- **Framework**: Ktor (async web framework)
- **Database**: PostgreSQL
- **ORM**: Exposed (JetBrains)
- **Dependency Injection**: Koin with KSP annotation processing
- **Serialization**: kotlinx.serialization
- **Authentication**: JWT (RS256 algorithm)
- **Async**: Kotlin Coroutines

---

## Architecture Pattern

NotDjinni follows **Clean Architecture** principles with clear separation of concerns across four main layers:

```
┌─────────────────────────────────────────┐
│      Presentation Layer (HTTP/REST)      │  ← Routes, DTOs, Controllers
├─────────────────────────────────────────┤
│      Domain Layer (Business Logic)       │  ← Interfaces, Exceptions
├─────────────────────────────────────────┤
│    Data Layer (Repository Pattern)       │  ← Implementations, Mappers
├─────────────────────────────────────────┤
│    Database Layer (Persistence)          │  ← DAOs, Entities, Tables
└─────────────────────────────────────────┘
```

**Data Flow:**
```
HTTP Request
  → Request DTO
  → Domain Model
  → Entity
  → Database

Database
  → Entity
  → Domain Model
  → Response DTO
  → HTTP Response
```

---

## Project Structure

```
src/main/kotlin/not/djinni/
├── Application.kt                    # Entry point
│
├── auth/                             # JWT authentication logic
│   ├── TokenProvider.kt
│   ├── DefaultTokenProvider.kt
│   └── model/
│       └── JwtConfiguration.kt
│
├── database/                         # Database layer
│   ├── NotDjinniDatabase.kt         # DB initialization, transaction runner
│   ├── api/                          # DAO interfaces & entities
│   │   ├── user/
│   │   │   ├── UserDao.kt
│   │   │   └── UserEntity.kt
│   │   ├── seeker/
│   │   │   ├── SeekerProfileDao.kt
│   │   │   ├── SeekerProfileEntity.kt
│   │   │   ├── WorkExperienceDao.kt
│   │   │   └── WorkExperienceEntity.kt
│   │   └── employer/
│   │       ├── CompanyDao.kt
│   │       ├── CompanyEntity.kt
│   │       ├── EmployerProfileDao.kt
│   │       └── EmployerProfileEntity.kt
│   └── impl/                         # DAO implementations & tables
│       ├── user/
│       │   ├── UserTable.kt
│       │   └── DefaultUserDao.kt
│       ├── seeker/
│       │   ├── SeekerProfileTable.kt
│       │   ├── DefaultSeekerProfileDao.kt
│       │   ├── WorkExperienceTable.kt
│       │   └── DefaultWorkExperienceDao.kt
│       └── employer/
│           ├── CompanyTable.kt
│           ├── DefaultCompanyDao.kt
│           ├── EmployerProfileTable.kt
│           └── DefaultEmployerProfileDao.kt
│
├── data/                             # Data layer
│   ├── mapper/                       # Entity ↔ Domain conversions
│   │   ├── User.kt
│   │   ├── SeekerProfile.kt
│   │   ├── WorkExperience.kt
│   │   ├── Company.kt
│   │   └── EmployerProfile.kt
│   └── repository/                   # Repository implementations
│       ├── DefaultUserRepository.kt
│       ├── DefaultAuthRepository.kt
│       ├── DefaultSeekerProfileRepository.kt
│       ├── DefaultCompanyRepository.kt
│       └── DefaultEmployerProfileRepository.kt
│
├── di/                               # Dependency injection
│   └── AppModule.kt                  # Koin module with component scan
│
├── domain/                           # Domain layer
│   ├── repository/                   # Repository interfaces
│   │   ├── UserRepository.kt
│   │   ├── AuthRepository.kt
│   │   ├── SeekerProfileRepository.kt
│   │   ├── CompanyRepository.kt
│   │   └── EmployerProfileRepository.kt
│   └── exception/                    # Business exceptions
│       ├── user/
│       │   └── UserException.kt
│       ├── auth/
│       │   └── AuthException.kt
│       ├── seeker/
│       │   └── SeekerProfileException.kt
│       └── employer/
│           ├── CompanyException.kt
│           └── EmployerProfileException.kt
│
├── model/                            # Domain models
│   ├── User.kt
│   ├── role/
│   │   ├── SeekerProfile.kt
│   │   ├── WorkExperience.kt
│   │   ├── Company.kt
│   │   ├── EmployerProfile.kt
│   │   └── EmployerProfileWithCompany.kt
│   └── token/
│       ├── AuthTokens.kt
│       └── RefreshToken.kt
│
└── presentation/                     # Presentation layer
    ├── plugins/                      # Ktor configuration
    │   ├── Authentication.kt
    │   ├── CORS.kt
    │   ├── Json.kt
    │   └── Koin.kt
    └── router/
        ├── Router.kt                 # Router interface
        ├── DefaultRouter.kt          # Main router implementation
        ├── extension/
        │   ├── Result.kt             # Error handling DSL
        │   └── RoutingContext.kt
        └── routes/
            ├── Route.kt              # Route interface
            ├── common/               # Shared utilities
            │   ├── auth/
            │   │   └── JwtAuth.kt
            │   ├── extension/
            │   │   └── Call.kt
            │   ├── mapper/
            │   │   └── ErrorResult.kt
            │   ├── model/
            │   │   └── ErrorResult.kt
            │   └── response/
            │       ├── ErrorResponse.kt
            │       └── common/
            │           └── MessageResponse.kt
            ├── auth/
            │   ├── AuthRoute.kt
            │   ├── resources/
            │   │   └── Auth.kt
            │   ├── request/
            │   │   ├── LoginRequest.kt
            │   │   ├── RegisterRequest.kt
            │   │   └── RefreshTokenRequest.kt
            │   ├── response/
            │   │   └── AuthResponse.kt
            │   └── mapper/
            │       └── AuthException.kt
            ├── user/
            │   ├── UserRoute.kt
            │   ├── resources/
            │   │   └── User.kt
            │   └── mapper/
            │       ├── User.kt
            │       └── UserException.kt
            ├── seeker/
            │   ├── SeekerRoute.kt
            │   ├── resources/
            │   │   └── Seeker.kt
            │   ├── request/
            │   │   ├── CreateProfileRequest.kt
            │   │   ├── UpdateProfileRequest.kt
            │   │   └── WorkExperienceRequest.kt
            │   ├── response/
            │   │   ├── SeekerProfileResponse.kt
            │   │   ├── WorkExperienceResponse.kt
            │   │   └── WorkExperienceIdResponse.kt
            │   └── mapper/
            │       ├── SeekerProfile.kt
            │       └── SeekerProfileException.kt
            ├── company/
            │   ├── CompanyRoute.kt
            │   ├── resources/
            │   │   └── Company.kt
            │   ├── request/
            │   │   ├── CreateCompanyRequest.kt
            │   │   └── UpdateCompanyRequest.kt
            │   ├── response/
            │   │   └── CompanyResponse.kt
            │   └── mapper/
            │       ├── Company.kt
            │       └── CompanyException.kt
            └── employer/
                ├── EmployerRoute.kt
                ├── resources/
                │   └── Employer.kt
                ├── request/
                │   ├── CreateEmployerProfileRequest.kt
                │   └── UpdateEmployerProfileRequest.kt
                ├── response/
                │   └── EmployerProfileResponse.kt
                └── mapper/
                    ├── EmployerProfile.kt
                    └── EmployerProfileException.kt
```

---

## Layer Responsibilities

### 1. Presentation Layer (`presentation/`)

**Purpose**: HTTP interface, request/response handling, routing

**Responsibilities:**
- Define API endpoints using Ktor Resources
- Validate and parse HTTP requests
- Convert Request DTOs → Domain models
- Convert Domain models → Response DTOs
- Handle authentication/authorization
- Map domain exceptions → HTTP status codes
- Return properly formatted HTTP responses

**Key Components:**
- **Routes**: Implement `Route` interface, define endpoint logic
- **Resources**: Type-safe routing with `@Resource` annotations
- **Request DTOs**: `@Serializable` data classes for incoming JSON
- **Response DTOs**: `@Serializable` data classes for outgoing JSON
- **Mappers**: Extension functions for DTO ↔ Domain conversions

**Example:**
```kotlin
@Single
class SeekerRoute(
    private val seekerProfileRepository: SeekerProfileRepository
) : Route {
    override fun install(root: Routing) = with(root) {
        getProfile()
        createProfile()
    }

    private fun Routing.getProfile() {
        authenticate(JwtAuth.NAME) {
            get<Seeker.Profile> {
                val userId = getUserIdFromTokenOrSendError() ?: return@get
                seekerProfileRepository.getProfile(userId)
                    .onSuccess { call.respond(it.toResponse()) }
                    .handleError(call, SeekerProfileException::toStatusCode)
            }
        }
    }
}
```

---

### 2. Domain Layer (`domain/`)

**Purpose**: Business logic interfaces and rules

**Responsibilities:**
- Define repository interfaces (contracts)
- Define business exceptions (sealed classes)
- Declare domain-level validation rules
- No implementation details (interface-only)

**Key Components:**
- **Repository Interfaces**: Define business operations
- **Exceptions**: Sealed classes for domain errors

**Example:**
```kotlin
interface SeekerProfileRepository {
    suspend fun getProfile(userId: Long): Result<SeekerProfile>
    suspend fun createProfile(userId: Long, profile: SeekerProfile): Result<SeekerProfile>
    suspend fun updateProfile(userId: Long, profile: SeekerProfile): Result<Unit>
    suspend fun deleteProfile(userId: Long): Result<Unit>
}

sealed class SeekerProfileException(override val message: String) : Throwable() {
    class ProfileNotFound : SeekerProfileException("Profile not found")
    class ProfileAlreadyExists : SeekerProfileException("Profile already exists")
    class Unauthorized : SeekerProfileException("Unauthorized to access this resource")
}
```

---

### 3. Data Layer (`data/`)

**Purpose**: Repository implementations, data transformation

**Responsibilities:**
- Implement repository interfaces
- Coordinate DAO calls
- Transform Entity ↔ Domain models
- Wrap database operations in `Result<T>`
- Throw domain exceptions on errors
- Business logic validation

**Key Components:**
- **Repository Implementations**: Concrete classes implementing domain interfaces
- **Mappers**: Extension functions for Entity ↔ Domain conversions

**Example:**
```kotlin
@Single(binds = [SeekerProfileRepository::class])
class DefaultSeekerProfileRepository(
    private val seekerProfileDao: SeekerProfileDao,
    private val workExperienceDao: WorkExperienceDao
) : SeekerProfileRepository {

    override suspend fun getProfile(userId: Long) = runCatching {
        seekerProfileDao.getProfileWithWorkExperienceByUserId(userId)
            ?.toDomain()
            ?: throw SeekerProfileException.ProfileNotFound()
    }

    override suspend fun createProfile(userId: Long, profile: SeekerProfile) = runCatching {
        if (seekerProfileDao.profileExists(userId)) {
            throw SeekerProfileException.ProfileAlreadyExists()
        }
        val profileId = seekerProfileDao.createProfile(profile.toEntity(userId))
        // ... create related entities
        seekerProfileDao.getProfileWithWorkExperienceByProfileId(profileId)?.toDomain()
            ?: throw SeekerProfileException.ProfileNotFound()
    }
}
```

---

### 4. Database Layer (`database/`)

**Purpose**: Data persistence, database access

**Responsibilities:**
- Define database schema (Exposed tables)
- Define entity data classes
- Implement CRUD operations
- Execute database queries
- Validate data integrity
- Handle database constraints

**Key Components:**
- **Entities**: Data classes representing database rows
- **Tables**: Exposed table definitions (`object XXXTable : LongIdTable`)
- **Table Entities**: Exposed DAO entities (`class XXXTableEntity : LongEntity`)
- **DAO Interfaces**: Define database operations
- **DAO Implementations**: Execute queries using Exposed

**Example:**
```kotlin
// Table definition
object SeekerProfileTable : LongIdTable("job_seeker_profiles", "id") {
    val userId = reference("user_id", UserTable).uniqueIndex()
    val specialty = varchar("specialty", 255)
    val experienceYears = integer("experience_years")
    val desiredSalary = integer("desired_salary")
    val aboutMe = text("about_me").nullable()
}

// Entity class
class SeekerProfileTableEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<SeekerProfileTableEntity>(SeekerProfileTable)

    var userId by SeekerProfileTable.userId
    var specialty by SeekerProfileTable.specialty
    var experienceYears by SeekerProfileTable.experienceYears
    var desiredSalary by SeekerProfileTable.desiredSalary
    var aboutMe by SeekerProfileTable.aboutMe
    val workExperiences by WorkExperienceTableEntity referrersOn WorkExperienceTable.profileId
}

// DAO implementation
@Single([SeekerProfileDao::class])
class DefaultSeekerProfileDao : SeekerProfileDao {
    override suspend fun createProfile(profile: SeekerProfileEntity): Long = runQuery {
        SeekerProfileTableEntity.new {
            userId = EntityID(profile.userId, UserTable)
            specialty = profile.specialty
            experienceYears = profile.experienceYears
            desiredSalary = profile.desiredSalary
            aboutMe = profile.aboutMe
        }.id.value
    }
}
```

---

## Naming Conventions

### Package Names
- All lowercase, no underscores
- Singular nouns (e.g., `model`, `mapper`, `exception`)
- Feature-based grouping (e.g., `seeker`, `employer`, `company`)

### Class Names
- **PascalCase**
- Descriptive, singular nouns
- Interfaces: `UserRepository`, `CompanyDao`
- Implementations: `DefaultUserRepository`, `DefaultCompanyDao`
- Exceptions: `UserException`, `CompanyException` (sealed classes)
- Entities: `UserEntity`, `CompanyEntity`
- Tables: `UserTable`, `CompanyTable` (objects)
- Table Entities: `UserTableEntity`, `CompanyTableEntity`
- DTOs: `CreateProfileRequest`, `SeekerProfileResponse`

### Function Names
- **camelCase**
- Verb-based for actions: `getProfile`, `createCompany`, `updateProfile`
- Boolean functions: `profileExists`, `isValid`
- Conversion functions: `toDomain`, `toEntity`, `toResponse`

### Property Names
- **camelCase**
- Descriptive nouns: `userId`, `companyName`, `experienceYears`
- Boolean properties: `isActive`, `isCurrent`

### Constants
- **UPPER_SNAKE_CASE**
- Declared in companion objects or top-level
- Example: `MAX_VARCHAR_LENGTH`, `JWT_EXPIRATION_TIME`

### File Names
- Match primary class name
- One public class per file
- Example: `SeekerProfile.kt` contains `SeekerProfile` data class

### JSON Field Names (Serialization)
- **snake_case** using `@SerialName`
- Example: `@SerialName("company_name")`

---

## Code Style Guidelines

### 1. Kotlin Language Features

**Data Classes:**
```kotlin
// Use for DTOs, entities, domain models
data class Company(
    val id: Long,
    val companyName: String,
    val website: String?,
    val description: String
)
```

**Sealed Classes:**
```kotlin
// Use for sum types (e.g., exceptions, states)
sealed class CompanyException(override val message: String) : Throwable() {
    class CompanyNotFound : CompanyException("Company not found")
    class CompanyAlreadyExists : CompanyException("Company already exists")
    data class InvalidCompanyData(override val message: String) : CompanyException(message)
}
```

**Extension Functions:**
```kotlin
// Use for conversions and utility functions
fun CompanyEntity.toDomain() = Company(
    id = id,
    companyName = companyName,
    website = website,
    description = description
)

fun Company.toResponse() = CompanyResponse(
    id = id,
    companyName = companyName,
    website = website,
    description = description
)
```

**Scope Functions:**
```kotlin
// apply: Object configuration
val entity = SeekerProfileTableEntity.new {
    userId = EntityID(profile.userId, UserTable)
    specialty = profile.specialty
}

// let: Transformations and null-safe operations
val profile = dao.getProfile(id)?.toDomain()

// run: Execute block with error handling
entity ?: run { throw NotFoundException() }

// with: Multiple operations on same object
with(root) {
    getProfile()
    createProfile()
}

// also: Side effects
val id = dao.createProfile(profile).also { profileId ->
    dao.createRelatedData(profileId)
}
```

**Nullability:**
```kotlin
// Use nullable types explicitly
val website: String?  // Can be null
val name: String      // Never null

// Safe call operator
val length = website?.length

// Elvis operator with error
val profile = dao.getProfile(id) ?: throw ProfileNotFoundException()
```

**Single-Expression Functions:**
```kotlin
// Use when function body is a single expression
fun Company.toEntity() = CompanyEntity(
    id = id,
    companyName = companyName,
    website = website,
    description = description
)
```

---

### 2. Koin Dependency Injection

**Annotations:**
```kotlin
// Single instance (singleton)
@Single
class SeekerRoute(private val repository: SeekerProfileRepository) : Route

// Bind to interface
@Single(binds = [SeekerProfileRepository::class])
class DefaultSeekerProfileRepository : SeekerProfileRepository

// Module with component scan
@Module
@ComponentScan("not.djinni")
class AppModule
```

---

### 3. Coroutines & Async

**Suspend Functions:**
```kotlin
// All repository and DAO functions are suspend
interface SeekerProfileRepository {
    suspend fun getProfile(userId: Long): Result<SeekerProfile>
}
```

**Database Transactions:**
```kotlin
// Use runQuery wrapper for all database operations
suspend fun <T> runQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
```

---

### 4. Serialization

**Request/Response DTOs:**
```kotlin
@Serializable
data class CreateCompanyRequest(
    @SerialName("company_name")
    val companyName: String,
    @SerialName("website")
    val website: String? = null,
    @SerialName("description")
    val description: String
)

@Serializable
data class CompanyResponse(
    @SerialName("id")
    val id: Long,
    @SerialName("company_name")
    val companyName: String,
    @SerialName("website")
    val website: String?,
    @SerialName("description")
    val description: String
)
```

---

## CRUD Operation Patterns

### Repository Pattern (Result Type)

All repository methods return `Result<T>` from Kotlin stdlib:

```kotlin
override suspend fun createProfile(userId: Long, profile: SeekerProfile) = runCatching {
    // 1. Validation
    if (seekerProfileDao.profileExists(userId)) {
        throw SeekerProfileException.ProfileAlreadyExists()
    }

    // 2. Create main entity
    val profileId = seekerProfileDao.createProfile(profile.toEntity(userId))

    // 3. Create related entities
    val experiences = profile.workExperience.map { it.toEntity(profileId) }
    workExperienceDao.createWorkExperiences(experiences)

    // 4. Return created entity with relations
    seekerProfileDao.getProfileWithWorkExperienceByProfileId(profileId)
        ?.toDomain()
        ?: throw SeekerProfileException.ProfileNotFound()
}
```

### Route Pattern (Error Handling)

Routes use `.onSuccess` and `.handleError`:

```kotlin
private fun Routing.createProfile() {
    authenticate(JwtAuth.NAME) {
        post<Seeker.Profile> {
            // 1. Extract user from JWT
            val userId = getUserIdFromTokenOrSendError() ?: return@post

            // 2. Parse request body
            val request = call.receive<CreateProfileRequest>()

            // 3. Call repository
            seekerProfileRepository.createProfile(userId, request.toDomain())
                // 4. Success: Convert to response and send
                .onSuccess {
                    call.respond(
                        status = HttpStatusCode.Created,
                        message = it.toResponse()
                    )
                }
                // 5. Error: Map exception to status code
                .handleError(
                    call = call,
                    mapToCode = SeekerProfileException::toStatusCode
                )
        }
    }
}
```

### DAO Pattern (Exposed ORM)

DAOs execute queries using `runQuery`:

```kotlin
override suspend fun createProfile(profile: SeekerProfileEntity): Long = runQuery {
    // 1. Validation (optional)
    validateProfile(profile)

    // 2. Create entity using Exposed
    SeekerProfileTableEntity.new {
        userId = EntityID(profile.userId, UserTable)
        specialty = profile.specialty
        experienceYears = profile.experienceYears
        desiredSalary = profile.desiredSalary
        aboutMe = profile.aboutMe
    }.id.value
}

override suspend fun getProfile(id: Long): SeekerProfileEntity? = runQuery {
    SeekerProfileTableEntity.findById(id)?.toEntity()
}

override suspend fun updateProfile(profile: SeekerProfileEntity): Boolean = runQuery {
    SeekerProfileTableEntity.findById(profile.id)?.apply {
        specialty = profile.specialty
        experienceYears = profile.experienceYears
        desiredSalary = profile.desiredSalary
        aboutMe = profile.aboutMe
    } != null
}

override suspend fun deleteProfile(id: Long): Boolean = runQuery {
    SeekerProfileTableEntity.findById(id)?.apply { delete() } != null
}
```

---

## Error Handling

### 1. Domain Exceptions

Define sealed classes in `domain/exception/`:

```kotlin
sealed class SeekerProfileException(override val message: String) : Throwable() {
    class ProfileNotFound : SeekerProfileException("Profile not found")
    class ProfileAlreadyExists : SeekerProfileException("Profile already exists")
    class WorkExperienceNotFound : SeekerProfileException("Work experience not found")
    class Unauthorized : SeekerProfileException("Unauthorized to access this resource")
    data class InvalidProfileData(override val message: String) : SeekerProfileException(message)
}
```

### 2. Exception to HTTP Status Code Mapping

Create mapper in presentation layer:

```kotlin
// presentation/router/routes/seeker/mapper/SeekerProfileException.kt
fun SeekerProfileException.toStatusCode(): HttpStatusCode = when (this) {
    is SeekerProfileException.ProfileNotFound -> HttpStatusCode.NotFound
    is SeekerProfileException.ProfileAlreadyExists -> HttpStatusCode.Conflict
    is SeekerProfileException.WorkExperienceNotFound -> HttpStatusCode.NotFound
    is SeekerProfileException.Unauthorized -> HttpStatusCode.Forbidden
    is SeekerProfileException.InvalidProfileData -> HttpStatusCode.BadRequest
}
```

### 3. Error Response Format

```json
{
  "error": "Profile not found"
}
```

---

## Authentication & Authorization

### JWT Configuration

Located in `presentation/plugins/Authentication.kt`:

```kotlin
fun Application.installAuthentication() {
    val tokenProvider by inject<TokenProvider>()
    install(Authentication) {
        jwt(JwtAuth.NAME) {
            verifier(JWT.require(Algorithm.RSA256(...)))
            validate { credential ->
                if (credential.payload.getClaim(JwtAuth.USER_ID_CLAIM_NAME).asLong() != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ...)
            }
        }
    }
}
```

### Protected Routes

Wrap routes with `authenticate(JwtAuth.NAME)`:

```kotlin
authenticate(JwtAuth.NAME) {
    get<Seeker.Profile> {
        val userId = getUserIdFromTokenOrSendError() ?: return@get
        // ... route logic
    }
}
```

### Extract User ID from Token

```kotlin
fun RoutingContext.getUserIdFromTokenOrSendError(): Long? {
    val principal = call.principal<JWTPrincipal>()
    return principal?.getClaim(JwtAuth.USER_ID_CLAIM_NAME)?.asLong()
}
```

---

## Dependency Injection

### Koin Setup

Single module with component scanning:

```kotlin
@Module
@ComponentScan("not.djinni")
class AppModule
```

Install in `presentation/plugins/Koin.kt`:

```kotlin
fun Application.installKoin() {
    install(Koin) {
        slf4jLogger()
        modules(AppModule().module)  // KSP-generated
    }
}
```

### Component Registration

**Routes:**
```kotlin
@Single
class SeekerRoute(private val repository: SeekerProfileRepository) : Route
```

**Repositories:**
```kotlin
@Single(binds = [SeekerProfileRepository::class])
class DefaultSeekerProfileRepository(...) : SeekerProfileRepository
```

**DAOs:**
```kotlin
@Single([SeekerProfileDao::class])
class DefaultSeekerProfileDao : SeekerProfileDao
```

**Router:**
```kotlin
@Single([Router::class])
class DefaultRouter(
    private val authRoute: AuthRoute,
    private val userRoute: UserRoute,
    private val seekerRoute: SeekerRoute,
    private val companyRoute: CompanyRoute,
    private val employerRoute: EmployerRoute
) : Router
```

---

## Best Practices

### 1. File Organization
- One public class per file
- File name matches class name
- Group related classes in feature packages

### 2. Immutability
- Use `val` over `var` whenever possible
- Data classes are immutable by default
- Use `copy()` for modifications

### 3. Null Safety
- Explicit nullability with `?`
- Use safe calls (`?.`) and elvis operator (`?:`)
- Avoid `!!` (non-null assertion)

### 4. Code Clarity
- Self-documenting code (minimal comments)
- Descriptive variable and function names
- Small, focused functions
- Expression-oriented programming

### 5. Error Handling
- Use `Result<T>` for repository methods
- Use sealed classes for domain exceptions
- Map exceptions to HTTP status codes at presentation layer
- Provide meaningful error messages

### 6. Testing Strategy
- Unit tests for repositories
- Integration tests for DAOs
- API tests for routes
- Test error scenarios

### 7. Database Operations
- All database calls wrapped in `runQuery`
- Use transactions for multi-step operations
- Validate data at DAO layer
- Use foreign key constraints

### 8. Security
- All sensitive endpoints require JWT authentication
- Extract user ID from token (don't trust request body)
- Validate ownership before updates/deletes
- Use parameterized queries (Exposed handles this)

### 9. API Design
- RESTful conventions
- Type-safe routing with Ktor Resources
- Consistent response formats
- Appropriate HTTP status codes
- snake_case for JSON fields

### 10. Performance
- Use indexes on foreign keys and search fields
- Eager load related data when needed
- Use pagination for list endpoints
- Minimize N+1 query problems

---

## Quick Reference

### Create New Feature Checklist

1. **Domain Models** (`model/`)
2. **Database Entities** (`database/api/`)
3. **Database Tables** (`database/impl/`)
4. **DAO Interface** (`database/api/`)
5. **DAO Implementation** (`database/impl/`)
6. **Register Table** (in `NotDjinniDatabase.kt`)
7. **Data Mappers** (`data/mapper/`)
8. **Repository Interface** (`domain/repository/`)
9. **Repository Implementation** (`data/repository/`)
10. **Domain Exceptions** (`domain/exception/`)
11. **Request DTOs** (`presentation/router/routes/.../request/`)
12. **Response DTOs** (`presentation/router/routes/.../response/`)
13. **Presentation Mappers** (`presentation/router/routes/.../mapper/`)
14. **Ktor Resources** (`presentation/router/routes/.../resources/`)
15. **Route Implementation** (`presentation/router/routes/`)
16. **Register Route** (in `DefaultRouter.kt`)

---

## Summary

NotDjinni follows a clean, layered architecture with:
- **Clear separation of concerns** across four layers
- **Type-safe routing** with Ktor Resources
- **Comprehensive error handling** with sealed exceptions
- **Dependency injection** with Koin
- **Async operations** with Kotlin coroutines
- **Consistent code style** and naming conventions
- **Domain-driven design** with rich domain models

By following this architecture, the codebase remains **maintainable**, **testable**, and **scalable**.
