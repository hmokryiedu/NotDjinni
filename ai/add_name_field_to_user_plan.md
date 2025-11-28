# Add Name Field to User - Implementation Plan

## Overview
This plan outlines the changes required to add a `name` field to the User system, including database schema, entities, repository, and API endpoints.

## Current State Analysis

**Already Updated (No changes needed)**:
- `model/User.kt` - Domain model already has `name: String`
- `database/api/user/UserEntity.kt` - Entity already has `name: String`
- `data/mapper/User.kt` - Mapper already includes `name` field

**Requires Updates**:
- `database/impl/user/UserTable.kt` - Missing `name` column
- `database/impl/user/DefaultUserDao.kt` - Missing `name` assignment in upsertUser
- `domain/repository/AuthRepository.kt` - Missing `name` parameter in register()
- `data/repository/DefaultAuthRepository.kt` - Missing `name` parameter and validation
- `presentation/router/routes/auth/AuthRoute.kt` - Using LoginRequest for register (needs RegisterRequest)
- Need to create `RegisterRequest.kt` with `name` field

## Updated Database Schema

### SQL Table Definition
```sql
CREATE TABLE Users
(
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE INDEX idx_users_email ON Users(email);
```

**Changes**:
- Added `name VARCHAR(100) NOT NULL` column
- Name is required (NOT NULL)
- Maximum length: 100 characters

## Implementation Plan

### 1. Update UserTable

**File**: `src/main/kotlin/database/impl/user/UserTable.kt`

**Changes**:
```kotlin
package not.djinni.database.impl.user

import not.djinni.database.api.user.UserEntity
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

class UserTableEntity(id: EntityID<Long>) : LongEntity(id) {
    var name by UserTable.name
    var email by UserTable.email
    var password by UserTable.password

    companion object Companion : LongEntityClass<UserTableEntity>(UserTable)
}

object UserTable : LongIdTable("user", "id") {
    val name = varchar("name", MAX_NAME_LENGTH)
    val email = varchar("email", MAX_VARCHAR_LENGTH)
    val password = varchar("password", MAX_VARCHAR_LENGTH)

    private const val MAX_NAME_LENGTH = 100
    private const val MAX_VARCHAR_LENGTH = 255
}

fun UserTableEntity.toEntity() = UserEntity(
    id = id.value,
    name = name,
    email = email,
    password = password,
)
```

**Changes Summary**:
- Add `val name = varchar("name", MAX_NAME_LENGTH)` to UserTable
- Add `var name by UserTable.name` to UserTableEntity
- Add `name = name,` to toEntity() mapping
- Add `MAX_NAME_LENGTH = 100` constant

### 2. Update DefaultUserDao

**File**: `src/main/kotlin/database/impl/user/DefaultUserDao.kt`

**Changes**:
```kotlin
package not.djinni.database.impl.user

import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.user.UserDao
import not.djinni.database.api.user.UserEntity
import org.koin.core.annotation.Single

@Single([UserDao::class])
class DefaultUserDao : UserDao {

    override suspend fun getUser(id: Long): UserEntity? = runQuery {
        UserTableEntity.find { UserTable.id eq id }.firstOrNull()?.toEntity()
    }

    override suspend fun upsertUser(user: UserEntity): Long = runQuery {
        UserTableEntity.new {
            this.name = user.name
            this.email = user.email
            this.password = user.password
        }.id.value
    }

    override suspend fun getUserByEmail(email: String): UserEntity? = runQuery {
        UserTableEntity.find { UserTable.email eq email }.firstOrNull()?.toEntity()
    }
}
```

**Changes Summary**:
- Add `this.name = user.name` to upsertUser() method

### 3. Create RegisterRequest

**File**: `src/main/kotlin/presentation/router/routes/auth/request/RegisterRequest.kt`

```kotlin
package not.djinni.presentation.router.routes.auth.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    @SerialName("name")
    val name: String,
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String
)
```

**Purpose**: Separate request model for registration with name field

### 4. Update AuthRepository Interface

**File**: `src/main/kotlin/domain/repository/AuthRepository.kt`

**Changes**:
```kotlin
package not.djinni.domain.repository

import not.djinni.model.User

interface AuthRepository {

    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<User>
}
```

**Changes Summary**:
- Add `name: String` parameter to register() method

### 5. Update DefaultAuthRepository

**File**: `src/main/kotlin/data/repository/DefaultAuthRepository.kt`

**Changes**:
```kotlin
package not.djinni.data.repository

import not.djinni.data.mapper.toDomain
import not.djinni.database.api.user.UserDao
import not.djinni.database.api.user.UserEntity
import not.djinni.domain.repository.AuthRepository
import not.djinni.domain.exception.auth.AuthException
import org.koin.core.annotation.Single
import java.security.MessageDigest

@Single([AuthRepository::class])
class DefaultAuthRepository(private val userDao: UserDao) : AuthRepository {

    private val passwordRegex by lazy { PASSWORD_REGEX.toRegex() }

    override suspend fun login(email: String, password: String) = runCatching {
        val user = userDao.getUserByEmail(email) ?: throw AuthException.UserNotFound()
        if (user.password != password.hash()) throw AuthException.InvalidCredentials()
        return@runCatching user.toDomain()
    }

    override suspend fun register(name: String, email: String, password: String) = runCatching {
        validateName(name)
        if (userDao.getUserByEmail(email = email) != null) throw AuthException.EmailAlreadyInUse()
        if (!passwordRegex.matches(password)) throw AuthException.WeakPassword()
        return@runCatching UserEntity(name = name, email = email, password = password.hash())
            .also { userDao.upsertUser(it) }
            .toDomain()
    }

    private fun validateName(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) throw AuthException.InvalidName("Name cannot be blank")
        if (trimmedName.length < MIN_NAME_LENGTH) throw AuthException.InvalidName("Name must be at least $MIN_NAME_LENGTH characters")
        if (trimmedName.length > MAX_NAME_LENGTH) throw AuthException.InvalidName("Name must be at most $MAX_NAME_LENGTH characters")
    }

    private fun String.hash(): String = MessageDigest.getInstance("SHA-256").digest(this.toByteArray()).toHexString()

    private companion object {
        const val PASSWORD_REGEX = """^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,60}$"""
        const val MIN_NAME_LENGTH = 2
        const val MAX_NAME_LENGTH = 100
    }
}
```

**Changes Summary**:
- Add `name: String` parameter to register()
- Add `name = name` to UserEntity creation
- Add `validateName(name)` function with validation rules:
  - Non-blank (trim and check)
  - Minimum length: 2 characters
  - Maximum length: 100 characters
- Add constants: MIN_NAME_LENGTH = 2, MAX_NAME_LENGTH = 100
- Call `validateName(name)` before other validations

### 6. Add InvalidName Exception

**File**: `src/main/kotlin/domain/exception/auth/AuthException.kt`

**Add new exception class**:
```kotlin
data class InvalidName(override val message: String) : AuthException(message)
```

**Purpose**: Handle name validation errors

**Pattern Reference**: Check existing AuthException file structure and add this exception following the same pattern as other exceptions (like WeakPassword, EmailAlreadyInUse, etc.)

### 7. Update AuthException Mapper

**File**: `src/main/kotlin/presentation/router/routes/auth/mapper/AuthException.kt`

**Add mapping for InvalidName**:
```kotlin
is AuthException.InvalidName -> HttpStatusCode.BadRequest
```

**Purpose**: Map InvalidName exception to HTTP 400 Bad Request

**Pattern Reference**: Check existing mapper file and add this mapping following the same pattern

### 8. Update AuthRoute

**File**: `src/main/kotlin/presentation/router/routes/auth/AuthRoute.kt`

**Changes**:
```kotlin
package not.djinni.presentation.router.routes.auth

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.Routing
import not.djinni.auth.TokenProvider
import not.djinni.domain.exception.auth.AuthException
import not.djinni.domain.repository.AuthRepository
import not.djinni.presentation.router.routes.Route
import not.djinni.presentation.router.routes.auth.mapper.toStatusCode
import not.djinni.presentation.router.routes.auth.request.LoginRequest
import not.djinni.presentation.router.routes.auth.request.RegisterRequest
import not.djinni.presentation.router.routes.auth.resources.Auth
import not.djinni.presentation.router.routes.auth.response.TokenResponse
import not.djinni.presentation.router.routes.common.mapper.toErrorResult
import org.koin.core.annotation.Single

@Single
class AuthRoute(
    private val tokenProvider: TokenProvider,
    private val authRepository: AuthRepository,
) : Route {

    override fun install(root: Routing) = with(root) {
        login()
        register()
    }

    private fun Routing.login() {
        post<Auth.Login> {
            val loginRequest = call.receive<LoginRequest>()
            authRepository
                .login(loginRequest.email, loginRequest.password)
                .onSuccess { user ->
                    val token = tokenProvider.generate(user.id)
                    call.respond(HttpStatusCode.OK, TokenResponse(token))
                }
                .onFailure {
                    val errorResult = it.toErrorResult(AuthException::toStatusCode)
                    call.respond(status = errorResult.code, message = errorResult.model)
                }
        }
    }

    private fun Routing.register() {
        post<Auth.Register> {
            val registerRequest = call.receive<RegisterRequest>()
            authRepository
                .register(registerRequest.name, registerRequest.email, registerRequest.password)
                .onSuccess { user ->
                    val token = tokenProvider.generate(user.id)
                    call.respond(HttpStatusCode.OK, TokenResponse(token))
                }
                .onFailure {
                    val errorResult = it.toErrorResult(AuthException::toStatusCode)
                    call.respond(status = errorResult.code, message = errorResult.model)
                }
        }
    }
}
```

**Changes Summary**:
- Import `RegisterRequest`
- Change `call.receive<LoginRequest>()` to `call.receive<RegisterRequest>()` in register()
- Change `authRepository.register(registerRequest.email, registerRequest.password)` to `authRepository.register(registerRequest.name, registerRequest.email, registerRequest.password)`

## File Structure

```
src/main/kotlin/
├── database/
│   ├── api/
│   │   └── user/
│   │       └── UserEntity.kt (no changes - already has name)
│   └── impl/
│       └── user/
│           ├── UserTable.kt (MODIFIED - add name column)
│           └── DefaultUserDao.kt (MODIFIED - add name assignment)
├── data/
│   ├── mapper/
│   │   └── User.kt (no changes - already maps name)
│   └── repository/
│       └── DefaultAuthRepository.kt (MODIFIED - add name param & validation)
├── domain/
│   ├── repository/
│   │   └── AuthRepository.kt (MODIFIED - add name parameter)
│   └── exception/
│       └── auth/
│           └── AuthException.kt (MODIFIED - add InvalidName)
├── model/
│   └── User.kt (no changes - already has name)
└── presentation/
    └── router/
        └── routes/
            └── auth/
                ├── mapper/
                │   └── AuthException.kt (MODIFIED - map InvalidName)
                ├── request/
                │   ├── LoginRequest.kt (no changes)
                │   └── RegisterRequest.kt (NEW FILE)
                └── AuthRoute.kt (MODIFIED - use RegisterRequest)
```

## Implementation Order

1. **Step 1**: Update `UserTable.kt` and `DefaultUserDao.kt` (database layer)
2. **Step 2**: Create `RegisterRequest.kt` (presentation layer)
3. **Step 3**: Add `InvalidName` exception to `AuthException.kt` (domain layer)
4. **Step 4**: Update `AuthException.kt` mapper (presentation layer)
5. **Step 5**: Update `AuthRepository.kt` interface (domain layer)
6. **Step 6**: Update `DefaultAuthRepository.kt` implementation (data layer)
7. **Step 7**: Update `AuthRoute.kt` (presentation layer)

**Rationale**: Work bottom-up (database → domain → data → presentation) to ensure dependencies are satisfied

## Validation Rules

### Name Validation
- **Non-blank**: Name must not be empty or only whitespace (use `trim()` before validation)
- **Minimum length**: 2 characters (after trimming)
- **Maximum length**: 100 characters (after trimming)
- **Exception**: Throw `AuthException.InvalidName` with descriptive message

### Validation Order in register()
1. Validate name first
2. Check if email already exists
3. Validate password strength
4. Create user

**Rationale**: Fail fast with client-side errors before database queries

## Error Handling

### New Exception: AuthException.InvalidName
- **HTTP Status**: 400 Bad Request
- **Message**: Descriptive message (e.g., "Name must be at least 2 characters")
- **Thrown by**: `DefaultAuthRepository.validateName()`

### Existing Exceptions (unchanged)
- `AuthException.EmailAlreadyInUse` → 409 Conflict
- `AuthException.WeakPassword` → 400 Bad Request
- `AuthException.UserNotFound` → 404 Not Found
- `AuthException.InvalidCredentials` → 401 Unauthorized

## API Changes

### Register Endpoint

**Before**:
```json
POST /auth/register
{
  "email": "user@example.com",
  "password": "SecurePass123"
}
```

**After**:
```json
POST /auth/register
{
  "name": "John Doe",
  "email": "user@example.com",
  "password": "SecurePass123"
}
```

**Response** (unchanged):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**New Error Response**:
```json
{
  "error": "Name must be at least 2 characters"
}
```
Status: 400 Bad Request

## Testing Checklist

### Database Layer
- [ ] UserTable has `name` column with varchar(100)
- [ ] UserTableEntity has `name` property delegate
- [ ] UserTableEntity.toEntity() includes `name` in mapping
- [ ] DefaultUserDao.upsertUser() sets `name` when creating user
- [ ] User can be created with name and retrieved successfully

### Domain Layer
- [ ] AuthRepository.register() signature includes `name` parameter
- [ ] AuthException.InvalidName exists and is a data class

### Data Layer
- [ ] DefaultAuthRepository.register() accepts `name` parameter
- [ ] Name validation: blank name throws InvalidName
- [ ] Name validation: name with 1 character throws InvalidName
- [ ] Name validation: name with 2 characters passes
- [ ] Name validation: name with 100 characters passes
- [ ] Name validation: name with 101 characters throws InvalidName
- [ ] Name validation: "  " (spaces only) throws InvalidName
- [ ] UserEntity created with trimmed name

### Presentation Layer
- [ ] RegisterRequest data class exists with name, email, password
- [ ] AuthException mapper includes InvalidName → BadRequest
- [ ] AuthRoute.register() uses RegisterRequest
- [ ] AuthRoute.register() passes name to repository

### Integration Tests
- [ ] Register with valid name succeeds and returns token
- [ ] Register with blank name returns 400 Bad Request
- [ ] Register with name too short returns 400 Bad Request
- [ ] Register with name too long returns 400 Bad Request
- [ ] Register with valid name creates user with correct name in database
- [ ] Login still works with existing users (backward compatibility)

## Additional Considerations

### Database Migration (Not Included)
Since the plan excludes migration scripts, ensure the database is recreated with the new schema or manually add the column:
```sql
ALTER TABLE Users ADD COLUMN name VARCHAR(100) NOT NULL DEFAULT 'User';
```

Then remove the DEFAULT constraint:
```sql
ALTER TABLE Users ALTER COLUMN name DROP DEFAULT;
```

**Note**: This is for reference only if manual migration is needed

### Name Trimming
- Names should be trimmed before validation and storage
- Example: "  John Doe  " becomes "John Doe"
- This prevents accidental leading/trailing whitespace

### Future Enhancements
- Add name update endpoint (currently not in scope)
- Add name format validation (e.g., no special characters, no numbers)
- Add name uniqueness constraint (if business requires)
- Add display name vs full name distinction
- Add internationalization support for name validation messages

## Dependencies

All required dependencies already exist in the project:
- Exposed ORM
- PostgreSQL JDBC driver
- Koin for dependency injection
- Kotlin Coroutines
- Ktor for HTTP handling
- Kotlinx Serialization

## Summary

This plan adds the `name` field to the User system with the following key points:

1. **Database**: Add `name VARCHAR(100) NOT NULL` column to Users table
2. **Validation**: Name must be 2-100 characters, non-blank, trimmed
3. **API**: Register endpoint now requires `name` field in request
4. **Error Handling**: New `InvalidName` exception for validation failures
5. **Backward Compatibility**: Login endpoint unchanged
6. **File Changes**: 7 files modified, 1 new file created