# Fix SQL Schema and Add RefreshTokens - Implementation Plan

## Overview
This plan fixes a critical SQL syntax error in the Users table and implements a complete JWT refresh token system following Ktor best practices.

## Issues to Fix

### Critical SQL Syntax Error
**File**: `SQL_SCHEME_EXAMPLE.sql:33`
```sql
CREATE TABLE Users
(
    id            SERIAL PRIMARY KEY,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255)        NOT NULL,
    name          VARCHAR(100)        NOT NULL,  -- ❌ TRAILING COMMA
);
```

**Fix**: Remove trailing comma after `name` field

## RefreshTokens Feature Implementation

### Why RefreshTokens are Needed

**Problem with JWT-Only Auth:**
- JWT access tokens are stateless (cannot be revoked)
- Short-lived tokens (15-60 min) → Frequent re-login (bad UX)
- Long-lived tokens → Security risk if stolen
- No "logout" or "logout all devices" functionality

**Solution: Two-Token System**
1. **Access Token**: Short-lived (30 min), self-contained JWT
2. **Refresh Token**: Long-lived (30 days), stored in database

**Benefits:**
- ✅ Token revocation (logout, logout all devices)
- ✅ Better UX (users stay logged in for days)
- ✅ Session management and audit trail
- ✅ Better security (short access token window)

---

## Database Schema

### 1. Fix Users Table

**File**: `SQL_SCHEME_EXAMPLE.sql`

**Change**:
```sql
CREATE TABLE Users
(
    id            SERIAL PRIMARY KEY,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255)        NOT NULL,
    name          VARCHAR(100)        NOT NULL
);  -- Remove trailing comma from line 33
```

### 2. Add RefreshTokens Table

**Location**: After Companies table, before Dictionaries section

```sql
-- Таблиця refresh токенів
CREATE TABLE RefreshTokens
(
    id         SERIAL PRIMARY KEY,
    user_id    INT          NOT NULL,
    token      VARCHAR(512) UNIQUE NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE
);
```

**Schema Notes:**
- `user_id`: Foreign key to Users (CASCADE DELETE - when user deleted, tokens deleted)
- `token`: Unique constraint (no duplicate tokens)
- `expires_at`: Token expiration timestamp
- VARCHAR(512): Enough for UUID or cryptographic hash

### 3. Add Indexes

**Location**: In "Індекси для оптимізації запитів" section (around line 210)

```sql
CREATE INDEX idx_refresh_tokens_user ON RefreshTokens (user_id);
CREATE INDEX idx_refresh_tokens_token ON RefreshTokens (token);
```

**Purpose:**
- `idx_refresh_tokens_user`: Fast lookup of all user's tokens (logout all devices)
- `idx_refresh_tokens_token`: Fast token validation

### 4. Add DROP Statement

**Location**: In DROP TABLE section (around line 7)

```sql
DROP TABLE IF EXISTS RefreshTokens CASCADE;
```

**Order**: Add before `DROP TABLE IF EXISTS Companies CASCADE;`

### 5. Add Comment

**Location**: In "Коментарі до таблиць" section (around line 226)

```sql
COMMENT
ON TABLE RefreshTokens IS 'Refresh токени для JWT автентифікації';
```

---

## Domain Model

### RefreshToken.kt

**Location**: `src/main/kotlin/model/RefreshToken.kt`

```kotlin
package not.djinni.model

import java.time.LocalDateTime

data class RefreshToken(
    val id: Long,
    val userId: Long,
    val token: String,
    val expiresAt: LocalDateTime
)
```

---

## Database Layer

### 1. RefreshTokenEntity.kt

**Location**: `src/main/kotlin/database/api/refreshtoken/RefreshTokenEntity.kt`

```kotlin
package not.djinni.database.api.refreshtoken

import java.time.LocalDateTime

data class RefreshTokenEntity(
    val id: Long = 0,
    val userId: Long,
    val token: String,
    val expiresAt: LocalDateTime
)
```

### 2. RefreshTokenDao.kt

**Location**: `src/main/kotlin/database/api/refreshtoken/RefreshTokenDao.kt`

```kotlin
package not.djinni.database.api.refreshtoken

interface RefreshTokenDao {

    /**
     * Find refresh token by token string
     * @return RefreshTokenEntity or null if not found
     */
    suspend fun findByToken(token: String): RefreshTokenEntity?

    /**
     * Insert new refresh token
     * @return generated token ID
     */
    suspend fun insert(entity: RefreshTokenEntity): Long

    /**
     * Delete refresh token by token string
     * @return true if deleted, false if not found
     */
    suspend fun deleteByToken(token: String): Boolean

    /**
     * Delete all refresh tokens for a user
     * @return number of tokens deleted
     */
    suspend fun deleteAllForUser(userId: Long): Int

    /**
     * Delete expired tokens (cleanup)
     * @return number of tokens deleted
     */
    suspend fun deleteExpired(): Int
}
```

### 3. RefreshTokenTable.kt

**Location**: `src/main/kotlin/database/impl/refreshtoken/RefreshTokenTable.kt`

```kotlin
package not.djinni.database.impl.refreshtoken

import not.djinni.database.api.refreshtoken.RefreshTokenEntity
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

class RefreshTokenTableEntity(id: EntityID<Long>) : LongEntity(id) {
    var userId by RefreshTokenTable.userId
    var token by RefreshTokenTable.token
    var expiresAt by RefreshTokenTable.expiresAt

    companion object : LongEntityClass<RefreshTokenTableEntity>(RefreshTokenTable)
}

object RefreshTokenTable : LongIdTable("refresh_tokens", "id") {
    val userId = long("user_id").index()
    val token = varchar("token", MAX_TOKEN_LENGTH).uniqueIndex()
    val expiresAt = timestamp("expires_at")

    private const val MAX_TOKEN_LENGTH = 512
}

fun RefreshTokenTableEntity.toEntity() = RefreshTokenEntity(
    id = id.value,
    userId = userId,
    token = token,
    expiresAt = expiresAt
)
```

**Notes:**
- Uses Exposed's `timestamp` type for LocalDateTime
- Indexes on `userId` and `token` (via `index()` and `uniqueIndex()`)

### 4. DefaultRefreshTokenDao.kt

**Location**: `src/main/kotlin/database/impl/refreshtoken/DefaultRefreshTokenDao.kt`

```kotlin
package not.djinni.database.impl.refreshtoken

import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.refreshtoken.RefreshTokenDao
import not.djinni.database.api.refreshtoken.RefreshTokenEntity
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.deleteWhere
import org.koin.core.annotation.Single
import java.time.LocalDateTime

@Single([RefreshTokenDao::class])
class DefaultRefreshTokenDao : RefreshTokenDao {

    override suspend fun findByToken(token: String): RefreshTokenEntity? = runQuery {
        RefreshTokenTableEntity
            .find { RefreshTokenTable.token eq token }
            .firstOrNull()
            ?.toEntity()
    }

    override suspend fun insert(entity: RefreshTokenEntity): Long = runQuery {
        RefreshTokenTableEntity.new {
            this.userId = entity.userId
            this.token = entity.token
            this.expiresAt = entity.expiresAt
        }.id.value
    }

    override suspend fun deleteByToken(token: String): Boolean = runQuery {
        val deletedCount = RefreshTokenTable.deleteWhere {
            RefreshTokenTable.token eq token
        }
        deletedCount > 0
    }

    override suspend fun deleteAllForUser(userId: Long): Int = runQuery {
        RefreshTokenTable.deleteWhere {
            RefreshTokenTable.userId eq userId
        }
    }

    override suspend fun deleteExpired(): Int = runQuery {
        RefreshTokenTable.deleteWhere {
            RefreshTokenTable.expiresAt less LocalDateTime.now()
        }
    }
}
```

---

## Data Layer

### 1. Mapper: RefreshToken.kt

**Location**: `src/main/kotlin/data/mapper/RefreshToken.kt`

```kotlin
package not.djinni.data.mapper

import not.djinni.database.api.refreshtoken.RefreshTokenEntity
import not.djinni.model.RefreshToken

fun RefreshTokenEntity.toDomain(): RefreshToken {
    return RefreshToken(
        id = id,
        userId = userId,
        token = token,
        expiresAt = expiresAt
    )
}

fun RefreshToken.toEntity(): RefreshTokenEntity {
    return RefreshTokenEntity(
        id = id,
        userId = userId,
        token = token,
        expiresAt = expiresAt
    )
}
```

### 2. Update AuthRepository Interface

**Location**: `src/main/kotlin/domain/repository/AuthRepository.kt`

**Add methods**:
```kotlin
package not.djinni.domain.repository

import not.djinni.model.User

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String
)

interface AuthRepository {

    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<User>

    // NEW METHODS
    suspend fun generateTokens(userId: Long): Result<AuthTokens>
    suspend fun refreshAccessToken(refreshToken: String): Result<AuthTokens>
    suspend fun logout(refreshToken: String): Result<Unit>
    suspend fun logoutAll(userId: Long): Result<Unit>
}
```

### 3. Update DefaultAuthRepository

**Location**: `src/main/kotlin/data/repository/DefaultAuthRepository.kt`

**Add implementation**:
```kotlin
package not.djinni.data.repository

import not.djinni.auth.TokenProvider
import not.djinni.data.mapper.toDomain
import not.djinni.database.api.refreshtoken.RefreshTokenDao
import not.djinni.database.api.refreshtoken.RefreshTokenEntity
import not.djinni.database.api.user.UserDao
import not.djinni.database.api.user.UserEntity
import not.djinni.domain.exception.auth.AuthException
import not.djinni.domain.repository.AuthRepository
import not.djinni.domain.repository.AuthTokens
import org.koin.core.annotation.Single
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.UUID

@Single([AuthRepository::class])
class DefaultAuthRepository(
    private val userDao: UserDao,
    private val refreshTokenDao: RefreshTokenDao,
    private val tokenProvider: TokenProvider
) : AuthRepository {

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

    override suspend fun generateTokens(userId: Long): Result<AuthTokens> = runCatching {
        // Generate short-lived access token (30 minutes)
        val accessToken = tokenProvider.generate(userId)

        // Generate long-lived refresh token (30 days)
        val refreshToken = UUID.randomUUID().toString()
        val expiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS)

        // Store refresh token in database
        refreshTokenDao.insert(
            RefreshTokenEntity(
                userId = userId,
                token = refreshToken,
                expiresAt = expiresAt
            )
        )

        AuthTokens(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    override suspend fun refreshAccessToken(refreshToken: String): Result<AuthTokens> = runCatching {
        // Find token in database
        val storedToken = refreshTokenDao.findByToken(refreshToken)
            ?: throw AuthException.InvalidRefreshToken()

        // Check if expired
        if (storedToken.expiresAt.isBefore(LocalDateTime.now())) {
            refreshTokenDao.deleteByToken(refreshToken)
            throw AuthException.RefreshTokenExpired()
        }

        // Delete old refresh token (rotation for security)
        refreshTokenDao.deleteByToken(refreshToken)

        // Generate new token pair
        generateTokens(storedToken.userId).getOrThrow()
    }

    override suspend fun logout(refreshToken: String): Result<Unit> = runCatching {
        val deleted = refreshTokenDao.deleteByToken(refreshToken)
        if (!deleted) throw AuthException.InvalidRefreshToken()
    }

    override suspend fun logoutAll(userId: Long): Result<Unit> = runCatching {
        refreshTokenDao.deleteAllForUser(userId)
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
        const val REFRESH_TOKEN_VALIDITY_DAYS = 30L
    }
}
```

**Key Points:**
- `generateTokens()`: Creates both access and refresh tokens
- `refreshAccessToken()`: Validates old token, generates new pair (rotation)
- `logout()`: Deletes single refresh token
- `logoutAll()`: Deletes all user's tokens
- Token rotation: Each refresh invalidates old token

---

## Domain Layer - Exceptions

### Update AuthException.kt

**Location**: `src/main/kotlin/domain/exception/auth/AuthException.kt`

**Add new exceptions**:
```kotlin
data class InvalidRefreshToken(override val message: String = "Invalid refresh token") : AuthException(message)
data class RefreshTokenExpired(override val message: String = "Refresh token expired") : AuthException(message)
```

---

## Presentation Layer

### 1. Update AuthException Mapper

**Location**: `src/main/kotlin/presentation/router/routes/auth/mapper/AuthException.kt`

**Add mappings**:
```kotlin
is AuthException.InvalidRefreshToken -> HttpStatusCode.Unauthorized
is AuthException.RefreshTokenExpired -> HttpStatusCode.Unauthorized
```

### 2. Create Request/Response Models

**Location**: `src/main/kotlin/presentation/router/routes/auth/request/RefreshTokenRequest.kt`

```kotlin
package not.djinni.presentation.router.routes.auth.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token")
    val refreshToken: String
)
```

**Location**: `src/main/kotlin/presentation/router/routes/auth/response/AuthResponse.kt`

**Update to include both tokens**:
```kotlin
package not.djinni.presentation.router.routes.auth.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import not.djinni.presentation.router.common.response.user.UserResponse

@Serializable
data class AuthResponse(
    @SerialName("user")
    val user: UserResponse,
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String
)
```

### 3. Update Auth Resources

**Location**: `src/main/kotlin/presentation/router/routes/auth/resources/Auth.kt`

**Add new routes**:
```kotlin
@Resource("/auth")
class Auth {
    @Resource("/login")
    class Login(val parent: Auth = Auth())

    @Resource("/register")
    class Register(val parent: Auth = Auth())

    @Resource("/refresh")
    class Refresh(val parent: Auth = Auth())

    @Resource("/logout")
    class Logout(val parent: Auth = Auth())

    @Resource("/logout-all")
    class LogoutAll(val parent: Auth = Auth())
}
```

### 4. Update AuthRoute

**Location**: `src/main/kotlin/presentation/router/routes/auth/AuthRoute.kt`

**Update login and register, add new endpoints**:
```kotlin
package not.djinni.presentation.router.routes.auth

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.Routing
import not.djinni.domain.exception.auth.AuthException
import not.djinni.domain.repository.AuthRepository
import not.djinni.presentation.router.routes.Route
import not.djinni.presentation.router.routes.auth.mapper.toStatusCode
import not.djinni.presentation.router.routes.auth.request.LoginRequest
import not.djinni.presentation.router.routes.auth.request.RefreshTokenRequest
import not.djinni.presentation.router.routes.auth.request.RegisterRequest
import not.djinni.presentation.router.routes.auth.resources.Auth
import not.djinni.presentation.router.routes.auth.response.AuthResponse
import not.djinni.presentation.router.routes.common.mapper.toErrorResult
import not.djinni.presentation.router.routes.user.mapper.toResponse
import org.koin.core.annotation.Single

@Single
class AuthRoute(
    private val authRepository: AuthRepository,
) : Route {

    override fun install(root: Routing) = with(root) {
        login()
        register()
        refresh()
        logout()
        logoutAll()
    }

    private fun Routing.login() {
        post<Auth.Login> {
            val loginRequest = call.receive<LoginRequest>()
            authRepository
                .login(loginRequest.email, loginRequest.password)
                .onSuccess { user ->
                    authRepository.generateTokens(user.id)
                        .onSuccess { tokens ->
                            call.respond(
                                HttpStatusCode.OK,
                                AuthResponse(
                                    user = user.toResponse(),
                                    accessToken = tokens.accessToken,
                                    refreshToken = tokens.refreshToken
                                )
                            )
                        }
                        .onFailure { handleError(it) }
                }
                .onFailure { handleError(it) }
        }
    }

    private fun Routing.register() {
        post<Auth.Register> {
            val registerRequest = call.receive<RegisterRequest>()
            authRepository
                .register(registerRequest.name, registerRequest.email, registerRequest.password)
                .onSuccess { user ->
                    authRepository.generateTokens(user.id)
                        .onSuccess { tokens ->
                            call.respond(
                                HttpStatusCode.OK,
                                AuthResponse(
                                    user = user.toResponse(),
                                    accessToken = tokens.accessToken,
                                    refreshToken = tokens.refreshToken
                                )
                            )
                        }
                        .onFailure { handleError(it) }
                }
                .onFailure { handleError(it) }
        }
    }

    private fun Routing.refresh() {
        post<Auth.Refresh> {
            val request = call.receive<RefreshTokenRequest>()
            authRepository
                .refreshAccessToken(request.refreshToken)
                .onSuccess { tokens ->
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf(
                            "access_token" to tokens.accessToken,
                            "refresh_token" to tokens.refreshToken
                        )
                    )
                }
                .onFailure { handleError(it) }
        }
    }

    private fun Routing.logout() {
        post<Auth.Logout> {
            val request = call.receive<RefreshTokenRequest>()
            authRepository
                .logout(request.refreshToken)
                .onSuccess {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Logged out successfully"))
                }
                .onFailure { handleError(it) }
        }
    }

    private fun Routing.logoutAll() {
        authenticate("auth-jwt") {
            post<Auth.LogoutAll> {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asLong()
                    ?: throw AuthException.InvalidCredentials()

                authRepository
                    .logoutAll(userId)
                    .onSuccess {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Logged out from all devices"))
                    }
                    .onFailure { handleError(it) }
            }
        }
    }

    private suspend fun Routing.handleError(error: Throwable) {
        val errorResult = error.toErrorResult(AuthException::toStatusCode)
        call.respond(status = errorResult.code, message = errorResult.model)
    }
}
```

**Key Changes:**
- `login()` and `register()` now return `AuthResponse` with both tokens
- `refresh()`: Takes refresh token, returns new token pair
- `logout()`: Invalidates single refresh token
- `logoutAll()`: Protected route (requires JWT), invalidates all user's tokens

---

## Authentication Flow

### 1. Login/Register
```
Client → POST /auth/login
       ← 200 OK {user, access_token, refresh_token}

Client stores:
- access_token (memory/localStorage)
- refresh_token (secure storage)
```

### 2. API Calls
```
Client → GET /api/vacancies
         Authorization: Bearer <access_token>
       ← 200 OK (data)
```

### 3. Access Token Expired
```
Client → GET /api/vacancies
         Authorization: Bearer <expired_access_token>
       ← 401 Unauthorized

Client → POST /auth/refresh
         {refresh_token: "..."}
       ← 200 OK {access_token, refresh_token}

Client → GET /api/vacancies (retry)
         Authorization: Bearer <new_access_token>
       ← 200 OK (data)
```

### 4. Logout
```
Client → POST /auth/logout
         {refresh_token: "..."}
       ← 200 OK

// Access token still works until expiry (max 30 min)
// But user can't get new tokens
```

### 5. Logout All Devices
```
Client → POST /auth/logout-all
         Authorization: Bearer <access_token>
       ← 200 OK

// All refresh tokens deleted
// All devices must re-login
```

---

## Token Configuration

### Update JwtConfiguration (Optional)

**Location**: `src/main/kotlin/auth/model/JwtConfiguration.kt`

Consider adding:
```kotlin
val accessTokenExpirationMs: Long = 30 * 60 * 1000  // 30 minutes
val refreshTokenExpirationDays: Long = 30           // 30 days
```

---

## File Structure

```
src/main/kotlin/
├── database/
│   ├── api/
│   │   └── refreshtoken/
│   │       ├── RefreshTokenEntity.kt (NEW)
│   │       └── RefreshTokenDao.kt (NEW)
│   └── impl/
│       └── refreshtoken/
│           ├── RefreshTokenTable.kt (NEW)
│           └── DefaultRefreshTokenDao.kt (NEW)
├── data/
│   ├── mapper/
│   │   └── RefreshToken.kt (NEW)
│   └── repository/
│       └── DefaultAuthRepository.kt (MODIFIED - add token methods)
├── domain/
│   ├── repository/
│   │   └── AuthRepository.kt (MODIFIED - add token methods, AuthTokens data class)
│   └── exception/
│       └── auth/
│           └── AuthException.kt (MODIFIED - add token exceptions)
├── model/
│   └── RefreshToken.kt (NEW)
└── presentation/
    └── router/
        └── routes/
            └── auth/
                ├── mapper/
                │   └── AuthException.kt (MODIFIED - map token exceptions)
                ├── request/
                │   └── RefreshTokenRequest.kt (NEW)
                ├── response/
                │   └── AuthResponse.kt (MODIFIED - add tokens)
                ├── resources/
                │   └── Auth.kt (MODIFIED - add routes)
                └── AuthRoute.kt (MODIFIED - add endpoints, update login/register)
```

---

## Implementation Order

### Phase 1: SQL Schema
1. Fix Users table trailing comma
2. Add RefreshTokens table
3. Add indexes
4. Add DROP statement
5. Add comment

### Phase 2: Database Layer
1. Create `RefreshTokenEntity.kt`
2. Create `RefreshTokenDao.kt` interface
3. Create `RefreshTokenTable.kt`
4. Create `DefaultRefreshTokenDao.kt`

### Phase 3: Domain Layer
1. Create `RefreshToken.kt` model
2. Add `AuthTokens` data class to `AuthRepository.kt`
3. Add token methods to `AuthRepository.kt` interface
4. Add token exceptions to `AuthException.kt`

### Phase 4: Data Layer
1. Create `RefreshToken.kt` mapper
2. Update `DefaultAuthRepository.kt` (inject `RefreshTokenDao` and `TokenProvider`)
3. Implement token methods

### Phase 5: Presentation Layer
1. Create `RefreshTokenRequest.kt`
2. Update `AuthResponse.kt`
3. Update `Auth.kt` resources
4. Update `AuthException.kt` mapper
5. Update `AuthRoute.kt`

---

## Testing Checklist

### Database Layer
- [ ] RefreshTokens table created successfully
- [ ] Insert token works
- [ ] Find token by string works
- [ ] Delete token by string works
- [ ] Delete all tokens for user works
- [ ] Delete expired tokens works
- [ ] Indexes exist and are used

### Domain & Data Layer
- [ ] `generateTokens()` creates both tokens
- [ ] Refresh token stored in database
- [ ] Access token is valid JWT
- [ ] `refreshAccessToken()` validates token
- [ ] Expired token throws RefreshTokenExpired
- [ ] Invalid token throws InvalidRefreshToken
- [ ] Token rotation: old token deleted, new token created
- [ ] `logout()` deletes token
- [ ] `logoutAll()` deletes all user tokens

### Presentation Layer
- [ ] Login returns both tokens
- [ ] Register returns both tokens
- [ ] Refresh endpoint works
- [ ] Logout endpoint works
- [ ] LogoutAll endpoint requires auth
- [ ] LogoutAll endpoint deletes all tokens
- [ ] Exception mapping correct (401 for token errors)

### Integration Tests
- [ ] Complete flow: register → access API → refresh → access API
- [ ] Logout prevents refresh
- [ ] LogoutAll invalidates all devices
- [ ] Expired access token rejected
- [ ] Expired refresh token rejected
- [ ] Invalid refresh token rejected

---

## API Documentation

### POST /auth/login
**Request:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123"
}
```

**Response:**
```json
{
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "user@example.com"
  },
  "access_token": "eyJhbGci...",
  "refresh_token": "550e8400-e29b-41d4-a716-446655440000"
}
```

### POST /auth/register
**Request:**
```json
{
  "name": "John Doe",
  "email": "user@example.com",
  "password": "SecurePass123"
}
```

**Response:** (same as login)

### POST /auth/refresh
**Request:**
```json
{
  "refresh_token": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response:**
```json
{
  "access_token": "eyJhbGci...",
  "refresh_token": "7c9e6679-7425-40de-944b-e07fc1f90ae7"
}
```

### POST /auth/logout
**Request:**
```json
{
  "refresh_token": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response:**
```json
{
  "message": "Logged out successfully"
}
```

### POST /auth/logout-all
**Headers:**
```
Authorization: Bearer eyJhbGci...
```

**Response:**
```json
{
  "message": "Logged out from all devices"
}
```

---

## Security Considerations

1. **Token Rotation**: Each refresh generates new token pair, invalidating old ones
2. **Short Access Tokens**: 30-minute window limits damage if stolen
3. **Secure Storage**: Refresh tokens should be stored securely (HttpOnly cookies or secure storage)
4. **HTTPS Only**: All token endpoints must use HTTPS in production
5. **Token Cleanup**: Consider periodic cleanup of expired tokens (cron job)

---

## Future Enhancements

- Add `created_at`, `ip_address`, `user_agent` to RefreshTokens for audit trail
- Add "Active Sessions" page showing all devices
- Add rate limiting on refresh endpoint
- Add token fingerprinting for additional security
- Consider shorter refresh token validity for high-security scenarios
- Add email notification on new device login
