# Fix Kotlin Model Mismatches - Implementation Plan

## Overview
This plan fixes critical mismatches between the Kotlin domain models and the SQL database schema for `SeekerProfile` and `EmployerProfile`.

## Issues Identified

### 1. SeekerProfile Model Issues

**Current State** (`src/main/kotlin/model/role/SeekerProfile.kt`):
```kotlin
package not.djinni.model.role

data class SeekerProfile(
    val id: String,        // ❌ Wrong type (should be Long)
    val aboutMe: String?,
    val speciality: String,
    val desiredSalary: Int,
    val experienceYears: Int,
)                          // ❌ Missing userId field
```

**SQL Schema** (`SQL_SCHEME_EXAMPLE.sql`):
```sql
CREATE TABLE JobSeekerProfiles
(
    id               SERIAL PRIMARY KEY,      -- INT/Long
    user_id          INT UNIQUE NOT NULL,     -- Missing in Kotlin model
    speciality        VARCHAR(255),        
    experience_years INT CHECK (experience_years >= 0),
    desired_salary   DECIMAL(10, 2),
    about_me         TEXT,
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE
);
```

**Problems:**
1. `id` is `String` in Kotlin but `SERIAL` (Integer) in SQL → Type mismatch
2. Missing `userId: Long` field (critical FK to Users table)
3. Typo: `speciality` should be `specialty` to match SQL column name

### 2. EmployerProfile Model Issues

**Current State** (`src/main/kotlin/model/role/EmployerProfile.kt`):
```kotlin
package not.djinni.model.role

data class EmployerProfile(
    val id: Long,
    val companyId: Long
)                          // ❌ Missing userId field
```

**SQL Schema** (`SQL_SCHEME_EXAMPLE.sql`):
```sql
CREATE TABLE EmployerProfiles
(
    id             SERIAL PRIMARY KEY,
    user_id        INT UNIQUE NOT NULL,  -- Missing in Kotlin model
    company_id     INT        NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE,
    FOREIGN KEY (company_id) REFERENCES Companies (id) ON DELETE CASCADE
);
```

**Problems:**
1. Missing `userId: Long` field (critical FK to Users table)

---

## Implementation Plan

### 1. Fix SeekerProfile.kt

**File**: `src/main/kotlin/model/role/SeekerProfile.kt`

**Changes:**
```kotlin
package not.djinni.model.role

data class SeekerProfile(
    val id: Long,              // Changed from String to Long
    val userId: Long,          // NEW FIELD - FK to Users
    val aboutMe: String?,
    val specialty: String,     // Fixed typo: speciality → specialty
    val desiredSalary: Int,
    val experienceYears: Int,
)
```

**Summary of Changes:**
- ✅ Change `id: String` → `id: Long`
- ✅ Add `userId: Long` field
- ✅ Fix typo: `speciality` → `specialty`

**Rationale:**
- `id: Long` matches SQL `SERIAL` (auto-incrementing integer)
- `userId` is required to link profile to user (one-to-one relationship)
- `specialty` matches the SQL column name exactly

### 2. Fix EmployerProfile.kt

**File**: `src/main/kotlin/model/role/EmployerProfile.kt`

**Changes:**
```kotlin
package not.djinni.model.role

data class EmployerProfile(
    val id: Long,
    val userId: Long,      // NEW FIELD - FK to Users
    val companyId: Long
)
```

**Summary of Changes:**
- ✅ Add `userId: Long` field

**Rationale:**
- `userId` is required to link profile to user (one-to-one relationship)
- Each employer profile must be associated with exactly one user account

---

## Field Mapping Reference

### SeekerProfile: Kotlin ↔ SQL

| Kotlin Field        | Type     | SQL Column         | Type            | Notes                              |
|---------------------|----------|--------------------|-----------------|-----------------------------------|
| `id`                | `Long`   | `id`               | `SERIAL`        | Changed from String               |
| `userId`            | `Long`   | `user_id`          | `INT`           | NEW - FK to Users                 |
| `aboutMe`           | `String?`| `about_me`         | `TEXT`          | Nullable                          |
| `specialty`         | `String` | `specialty`        | `VARCHAR(255)`  | Fixed typo from "speciality"      |
| `desiredSalary`     | `Int`    | `desired_salary`   | `DECIMAL(10,2)` | -                                 |
| `experienceYears`   | `Int`    | `experience_years` | `INT`           | -                                 |

### EmployerProfile: Kotlin ↔ SQL

| Kotlin Field | Type   | SQL Column    | Type     | Notes             |
|--------------|--------|---------------|----------|-------------------|
| `id`         | `Long` | `id`          | `SERIAL` | -                 |
| `userId`     | `Long` | `user_id`     | `INT`    | NEW - FK to Users |
| `companyId`  | `Long` | `company_id`  | `INT`    | -                 |

---

## Affected Files

### Files to Modify

```
src/main/kotlin/
└── model/
    └── role/
        ├── SeekerProfile.kt (MODIFY)
        └── EmployerProfile.kt (MODIFY)
```

### Potentially Affected Files (Downstream)

⚠️ **Important**: These changes will affect any code that uses these models. Review and update:

1. **Database Entities**: Ensure entities match the updated models
   - `src/main/kotlin/database/api/jobseeker/JobSeekerProfileEntity.kt` (if exists)
   - `src/main/kotlin/database/api/employer/EmployerProfileEntity.kt` (if exists)

2. **Mappers**: Update entity ↔ domain mapping functions
   - `src/main/kotlin/data/mapper/JobSeekerProfile.kt` (if exists)
   - `src/main/kotlin/data/mapper/EmployerProfile.kt` (if exists)

3. **DAOs**: Update database access methods
   - Any DAO methods that create or query these profiles

4. **Routes/Controllers**: Update API endpoints
   - Profile creation endpoints
   - Profile update endpoints
   - Any endpoints returning profile data

5. **Request/Response Models**: Update DTOs if they mirror domain models
   - `src/main/kotlin/presentation/router/routes/profile/request/*` (if exists)
   - `src/main/kotlin/presentation/router/routes/profile/response/*` (if exists)

---

## Implementation Order

### Step 1: Update Domain Models
1. Fix `SeekerProfile.kt`
2. Fix `EmployerProfile.kt`

### Step 2: Review Database Layer
1. Check if `JobSeekerProfileEntity.kt` exists
2. Check if `EmployerProfileEntity.kt` exists
3. Ensure entities have `userId` field and correct types

### Step 3: Update Mappers
1. Update entity → domain mappers to include `userId`
2. Update domain → entity mappers to include `userId`
3. Fix any `speciality` → `specialty` mappings

### Step 4: Update DAOs
1. Review DAO methods for profile creation
2. Ensure `userId` is set when creating profiles
3. Update any queries that filter by user

### Step 5: Update Presentation Layer
1. Update request DTOs to include `userId` (or derive from authenticated user)
2. Update response DTOs to match new model structure
3. Update routes to pass `userId` correctly

### Step 6: Testing
1. Test profile creation with `userId`
2. Test profile retrieval
3. Test one-to-one relationship (one user = one profile)
4. Test cascade delete (user deleted → profile deleted)

---

## Validation Rules

### SeekerProfile
- `id`: Auto-generated by database (SERIAL)
- `userId`: Required, must reference existing User, unique (one profile per user)
- `specialty`: Required (NOT NULL in SQL)
- `experienceYears`: Required, must be >= 0 (CHECK constraint in SQL)
- `desiredSalary`: Required (NOT NULL in SQL)
- `aboutMe`: Optional (TEXT, can be null)

### EmployerProfile
- `id`: Auto-generated by database (SERIAL)
- `userId`: Required, must reference existing User, unique (one profile per user)
- `companyId`: Required, must reference existing Company

---

## Example Usage

### Creating a SeekerProfile

**Before (Incorrect)**:
```kotlin
val profile = SeekerProfile(
    id = "user-123",           // ❌ Wrong type
    aboutMe = "Software engineer",
    speciality = "Backend",    // ❌ Typo
    desiredSalary = 100000,
    experienceYears = 5
)                              // ❌ Missing userId
```

**After (Correct)**:
```kotlin
val profile = SeekerProfile(
    id = 0L,                   // ✅ Long (0 = not yet persisted)
    userId = 123L,             // ✅ FK to Users table
    aboutMe = "Software engineer",
    specialty = "Backend",     // ✅ Fixed typo
    desiredSalary = 100000,
    experienceYears = 5
)
```

### Creating an EmployerProfile

**Before (Incorrect)**:
```kotlin
val profile = EmployerProfile(
    id = 1L,
    companyId = 10L
)                              // ❌ Missing userId
```

**After (Correct)**:
```kotlin
val profile = EmployerProfile(
    id = 0L,                   // ✅ 0 = not yet persisted
    userId = 123L,             // ✅ FK to Users table
    companyId = 10L
)
```

---

## Database Constraints

### One-to-One Relationship: User ↔ Profile

Both tables have `user_id INT UNIQUE NOT NULL`:
- **UNIQUE**: Ensures one user can have only one profile
- **NOT NULL**: Every profile must have a user
- **CASCADE DELETE**: When user deleted, profile automatically deleted

**Example:**
```sql
-- User with ID 123
INSERT INTO Users (id, email, password_hash, name) VALUES (123, 'john@example.com', 'hash', 'John');

-- Create seeker profile for user 123
INSERT INTO JobSeekerProfiles (user_id, specialty, experience_years, desired_salary)
VALUES (123, 'Backend Developer', 5, 100000);

-- ❌ Cannot create second seeker profile for same user (UNIQUE constraint)
INSERT INTO JobSeekerProfiles (user_id, specialty, experience_years, desired_salary)
VALUES (123, 'Frontend Developer', 3, 90000);
-- ERROR: duplicate key value violates unique constraint

-- ❌ Cannot create employer profile for same user (business logic should prevent)
INSERT INTO EmployerProfiles (user_id, company_id)
VALUES (123, 1);
-- This would succeed in SQL but violates business rule:
-- a user should be EITHER a job seeker OR an employer, not both
```

---

## Business Logic Considerations

### User Role Detection

With `userId` field, you can determine user role:

```kotlin
suspend fun getUserRole(userId: Long): UserRole {
    val hasJobSeekerProfile = jobSeekerProfileDao.findByUserId(userId) != null
    val hasEmployerProfile = employerProfileDao.findByUserId(userId) != null

    return when {
        hasJobSeekerProfile && hasEmployerProfile -> UserRole.BOTH // Should not happen
        hasJobSeekerProfile -> UserRole.JOB_SEEKER
        hasEmployerProfile -> UserRole.EMPLOYER
        else -> UserRole.NO_PROFILE
    }
}
```

### Profile Creation

When creating profiles, always use authenticated user's ID:

```kotlin
// In route handler
authenticate("auth-jwt") {
    post<Profile.CreateSeeker> {
        val userId = call.principal<JWTPrincipal>()
            ?.payload
            ?.getClaim("userId")
            ?.asLong()
            ?: throw AuthException.InvalidCredentials()

        val request = call.receive<CreateSeekerProfileRequest>()

        val profile = SeekerProfile(
            id = 0L,
            userId = userId,  // ✅ From authenticated user
            aboutMe = request.aboutMe,
            specialty = request.specialty,
            desiredSalary = request.desiredSalary,
            experienceYears = request.experienceYears
        )

        // Save profile...
    }
}
```

---

## Migration Notes

### For Existing Data (if applicable)

If there are already profiles in the database without `userId`, you'll need to:

1. **Add userId column**:
   ```sql
   ALTER TABLE JobSeekerProfiles ADD COLUMN user_id INT;
   ALTER TABLE EmployerProfiles ADD COLUMN user_id INT;
   ```

2. **Populate userId** (if you have a way to determine which profile belongs to which user):
   ```sql
   -- Example: if you stored user_id somewhere else
   UPDATE JobSeekerProfiles SET user_id = ...;
   UPDATE EmployerProfiles SET user_id = ...;
   ```

3. **Add constraints**:
   ```sql
   ALTER TABLE JobSeekerProfiles
       ALTER COLUMN user_id SET NOT NULL,
       ADD CONSTRAINT fk_job_seeker_user FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
       ADD CONSTRAINT uq_job_seeker_user_id UNIQUE (user_id);

   ALTER TABLE EmployerProfiles
       ALTER COLUMN user_id SET NOT NULL,
       ADD CONSTRAINT fk_employer_user FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
       ADD CONSTRAINT uq_employer_user_id UNIQUE (user_id);
   ```

**Note**: This assumes you have existing data. If starting fresh, the schema in `SQL_SCHEME_EXAMPLE.sql` already has these constraints.

---

## Testing Checklist

### Unit Tests
- [ ] SeekerProfile can be created with Long id
- [ ] SeekerProfile has userId field
- [ ] SeekerProfile uses "specialty" (not "speciality")
- [ ] EmployerProfile has userId field
- [ ] Both models serialize/deserialize correctly

### Integration Tests
- [ ] Create seeker profile with userId succeeds
- [ ] Create employer profile with userId succeeds
- [ ] Cannot create two seeker profiles for same user (UNIQUE constraint)
- [ ] Cannot create two employer profiles for same user (UNIQUE constraint)
- [ ] Deleting user cascades to profile deletion
- [ ] Profile creation fails if user doesn't exist (FK constraint)

### API Tests
- [ ] Profile creation endpoint requires authentication
- [ ] Profile created with authenticated user's ID
- [ ] Cannot create profile for different user
- [ ] Profile retrieval returns correct userId
- [ ] Specialty field is correctly named in responses

---

## Type Safety Reference

### Why Long for IDs?

PostgreSQL `SERIAL` type is a 4-byte integer (max value: 2,147,483,647). Kotlin's `Long` is 8 bytes but can safely hold all `SERIAL` values.

**Type Mapping:**
- `SERIAL` (PostgreSQL) → `Int` (4 bytes) or `Long` (8 bytes) in Kotlin
- Project uses `Long` for consistency and to support future `BIGSERIAL` migration

**Rationale for Long:**
- ✅ Future-proof (easy migration to BIGSERIAL)
- ✅ Consistent with User.id (already Long)
- ✅ No risk of overflow
- ❌ String would prevent database-level auto-increment
- ❌ String would break SQL joins and foreign keys

---

## Backward Compatibility

### Breaking Changes

⚠️ These changes are **BREAKING** if you have existing code:

1. **SeekerProfile.id type change**:
   - Old code expecting `String` will fail to compile
   - Update all usages to `Long`

2. **New userId field**:
   - Any code creating profiles must now provide `userId`
   - Database queries may need updating

3. **Specialty field rename**:
   - Code accessing `profile.speciality` will fail
   - Update to `profile.specialty`

### Migration Strategy

1. **Update models first** (this plan)
2. **Fix compilation errors** (IDE will highlight them)
3. **Update database layer** (entities, DAOs)
4. **Update API layer** (routes, DTOs)
5. **Test thoroughly** before deploying

---

## Summary

This plan fixes critical type mismatches and missing fields in the profile models:

### SeekerProfile
- ✅ `id: String` → `id: Long` (matches SQL SERIAL)
- ✅ Add `userId: Long` (FK to Users)
- ✅ `speciality` → `specialty` (fix typo)

### EmployerProfile
- ✅ Add `userId: Long` (FK to Users)

All `id` fields are now `Long` type, matching PostgreSQL `SERIAL` (integer) type and enabling proper database relationships.
