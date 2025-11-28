# Job Seeker Profile Database Setup Plan

## Overview
This plan outlines the creation of all required classes (entity, DAO, mapper) for the Job Seeker Profile feature and Work Experience feature following the existing architecture patterns in the NotDjinni project.

**Features:**
- Job Seeker Profile: Core profile information (specialty, salary, experience summary)
- Work Experience: Detailed work history (multiple entries per profile)

## Database Schema

### 1. JobSeekerProfiles Table
```sql
CREATE TABLE JobSeekerProfiles
(
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT UNIQUE NOT NULL,
    specialty        VARCHAR(255) NOT NULL,
    experience_years INT NOT NULL CHECK (experience_years >= 0),
    desired_salary   INT NOT NULL,
    about_me         TEXT,
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE
);

CREATE INDEX idx_job_seeker_profiles_user_id ON JobSeekerProfiles(user_id);
```

**Changes from original schema:**
- `id`: BIGSERIAL for auto-incrementing Long IDs (consistent with Users table)
- `desired_salary`: Changed from DECIMAL(10, 2) to INT (stores salary in dollars as integer)
- Added explicit index on `user_id` for query performance

**Notes:**
- All fields except `about_me` are NOT NULL
- `user_id` has UNIQUE constraint (one profile per user)
- CASCADE DELETE: when user is deleted, their profile is automatically deleted
- `experience_years` has CHECK constraint >= 0

### 2. WorkExperience Table
```sql
CREATE TABLE WorkExperience
(
    id           BIGSERIAL PRIMARY KEY,
    profile_id   BIGINT       NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    position     VARCHAR(255) NOT NULL,
    description  TEXT,
    start_date   DATE         NOT NULL,
    end_date     DATE,
    FOREIGN KEY (profile_id) REFERENCES JobSeekerProfiles (id) ON DELETE CASCADE
);

CREATE INDEX idx_work_experience_profile_id ON WorkExperience(profile_id);
```

**Changes from original schema:**
- `id`: Changed from SERIAL (Int) to BIGSERIAL (Long) for consistency
- `profile_id`: Changed from INT to BIGINT to match JobSeekerProfiles.id
- Removed `is_current` field (use null `end_date` to indicate current job)
- Added explicit index on `profile_id` for query performance

**Notes:**
- All fields except `description` and `end_date` are NOT NULL
- `end_date` is NULL when this is the current job
- CASCADE DELETE: when profile is deleted, all work experiences are automatically deleted
- One profile can have multiple work experience entries (one-to-many relationship)

## Domain Models

### 1. SeekerProfile Model

**Location**: `src/main/kotlin/model/role/SeekerProfile.kt` (already exists, needs update)

**Current structure:**
```kotlin
data class SeekerProfile(
    val id: Long,
    val aboutMe: String?,
    val speciality: String,
    val desiredSalary: Int,
    val experienceYears: Int,
)
```

**Updated structure (with work experience):**
```kotlin
data class SeekerProfile(
    val id: Long,
    val aboutMe: String?,
    val speciality: String,
    val desiredSalary: Int,
    val experienceYears: Int,
    val workExperience: List<WorkExperience> = emptyList()
)
```

### 2. WorkExperience Model

**Location**: `src/main/kotlin/model/role/WorkExperience.kt` (new file)

```kotlin
package not.djinni.model.role

import java.time.LocalDate

data class WorkExperience(
    val id: Long,
    val companyName: String,
    val position: String,
    val description: String?,
    val startDate: LocalDate,
    val endDate: LocalDate?
) {
    val isCurrent: Boolean
        get() = endDate == null
}
```

**Notes:**
- Uses `LocalDate` for date fields (standard Java time API)
- `isCurrent` is a computed property (not stored in DB)
- `description` and `endDate` are nullable

## Implementation Plan

### 1. Create SeekerProfileEntity

**File**: `src/main/kotlin/database/api/seeker/SeekerProfileEntity.kt`

```kotlin
package not.djinni.database.api.seeker

data class SeekerProfileEntity(
    val id: Long = 0,
    val userId: Long,
    val specialty: String,
    val experienceYears: Int,
    val desiredSalary: Int,
    val aboutMe: String?
)
```

**Purpose**: Data class representing database row structure (id defaults to 0 for new entities)

### 2. Create SeekerProfileTable

**File**: `src/main/kotlin/database/impl/seeker/SeekerProfileTable.kt`

**Code**:
```kotlin
package not.djinni.database.impl.seeker

import not.djinni.database.api.seeker.SeekerProfileEntity
import not.djinni.database.impl.user.UserTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object SeekerProfileTable : LongIdTable("job_seeker_profiles", "id") {
    val userId = reference("user_id", UserTable).uniqueIndex()
    val specialty = varchar("specialty", 255)
    val experienceYears = integer("experience_years")
    val desiredSalary = integer("desired_salary")
    val aboutMe = text("about_me").nullable()
}

class SeekerProfileTableEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<SeekerProfileTableEntity>(SeekerProfileTable)

    var userId by SeekerProfileTable.userId
    var specialty by SeekerProfileTable.specialty
    var experienceYears by SeekerProfileTable.experienceYears
    var desiredSalary by SeekerProfileTable.desiredSalary
    var aboutMe by SeekerProfileTable.aboutMe
}

fun SeekerProfileTableEntity.toEntity() = SeekerProfileEntity(
    id = id.value,
    userId = userId.value,
    specialty = specialty,
    experienceYears = experienceYears,
    desiredSalary = desiredSalary,
    aboutMe = aboutMe
)
```

**Pattern Reference**: Follows `database/impl/user/UserTable.kt` pattern with LongIdTable

### 3. Create SeekerProfileDao Interface

**File**: `src/main/kotlin/database/api/seeker/SeekerProfileDao.kt`

```kotlin
interface SeekerProfileDao {
    suspend fun createProfile(profile: SeekerProfileEntity): Long
    suspend fun getProfile(id: Long): SeekerProfileEntity?
    suspend fun getProfileByUserId(userId: Long): SeekerProfileEntity?
    suspend fun updateProfile(profile: SeekerProfileEntity): Boolean
    suspend fun profileExists(userId: Long): Boolean
}
```

**Functions**:
- `createProfile`: Creates new profile, returns auto-generated Long ID
- `getProfile`: Retrieves profile by profile ID
- `getProfileByUserId`: Retrieves profile by user ID (most common lookup)
- `updateProfile`: Updates existing profile, returns success status
- `profileExists`: Checks if user already has a profile

### 4. Implement DefaultSeekerProfileDao

**File**: `src/main/kotlin/database/impl/seeker/DefaultSeekerProfileDao.kt`

**Code**:
```kotlin
package not.djinni.database.impl.seeker

import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.seeker.SeekerProfileDao
import not.djinni.database.api.seeker.SeekerProfileEntity
import not.djinni.database.impl.user.UserTable
import org.jetbrains.exposed.dao.id.EntityID
import org.koin.core.annotation.Single

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

    override suspend fun getProfile(id: Long): SeekerProfileEntity? = runQuery {
        SeekerProfileTableEntity.findById(id)?.toEntity()
    }

    override suspend fun getProfileByUserId(userId: Long): SeekerProfileEntity? = runQuery {
        SeekerProfileTableEntity.find { SeekerProfileTable.userId eq EntityID(userId, UserTable) }
            .firstOrNull()
            ?.toEntity()
    }

    override suspend fun updateProfile(profile: SeekerProfileEntity): Boolean = runQuery {
        SeekerProfileTableEntity.findById(profile.id)?.apply {
            specialty = profile.specialty
            experienceYears = profile.experienceYears
            desiredSalary = profile.desiredSalary
            aboutMe = profile.aboutMe
        } != null
    }

    override suspend fun profileExists(userId: Long): Boolean = runQuery {
        !SeekerProfileTableEntity.find { SeekerProfileTable.userId eq EntityID(userId, UserTable) }.empty()
    }
}
```

**Implementation Notes**:
- Uses `@Single([SeekerProfileDao::class])` annotation for Koin DI
- Uses `NotDjinniDatabase.runQuery` for all database operations
- Auto-generates Long ID via `.new { }` (no manual ID generation)
- Validates `experienceYears >= 0` via CHECK constraint in database
- Handles unique constraint violations on `user_id` via database constraint

**Pattern Reference**: Follows `database/impl/user/DefaultUserDao.kt` pattern

### 5. Create WorkExperienceEntity

**File**: `src/main/kotlin/database/api/seeker/WorkExperienceEntity.kt`

```kotlin
package not.djinni.database.api.seeker

import java.time.LocalDate

data class WorkExperienceEntity(
    val id: Long = 0,
    val profileId: Long,
    val companyName: String,
    val position: String,
    val description: String?,
    val startDate: LocalDate,
    val endDate: LocalDate?
)
```

**Purpose**: Data class representing work experience database row structure

### 6. Create WorkExperienceTable

**File**: `src/main/kotlin/database/impl/seeker/WorkExperienceTable.kt`

```kotlin
package not.djinni.database.impl.seeker

import not.djinni.database.api.seeker.WorkExperienceEntity
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.date

object WorkExperienceTable : LongIdTable("work_experience", "id") {
    val profileId = reference("profile_id", SeekerProfileTable)
    val companyName = varchar("company_name", 255)
    val position = varchar("position", 255)
    val description = text("description").nullable()
    val startDate = date("start_date")
    val endDate = date("end_date").nullable()
}

class WorkExperienceTableEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<WorkExperienceTableEntity>(WorkExperienceTable)

    var profileId by WorkExperienceTable.profileId
    var companyName by WorkExperienceTable.companyName
    var position by WorkExperienceTable.position
    var description by WorkExperienceTable.description
    var startDate by WorkExperienceTable.startDate
    var endDate by WorkExperienceTable.endDate
}

fun WorkExperienceTableEntity.toEntity() = WorkExperienceEntity(
    id = id.value,
    profileId = profileId.value,
    companyName = companyName,
    position = position,
    description = description,
    startDate = startDate,
    endDate = endDate
)
```

**Implementation Notes**:
- Uses `date()` from `org.jetbrains.exposed.sql.javatime` for `LocalDate` support
- Foreign key reference to `SeekerProfileTable`
- Follows same pattern as `SeekerProfileTable`

### 7. Create WorkExperienceDao Interface

**File**: `src/main/kotlin/database/api/seeker/WorkExperienceDao.kt`

```kotlin
package not.djinni.database.api.seeker

interface WorkExperienceDao {
    suspend fun createWorkExperience(experience: WorkExperienceEntity): Long
    suspend fun getWorkExperience(id: Long): WorkExperienceEntity?
    suspend fun getWorkExperiencesByProfileId(profileId: Long): List<WorkExperienceEntity>
    suspend fun updateWorkExperience(experience: WorkExperienceEntity): Boolean
    suspend fun deleteWorkExperience(id: Long): Boolean
}
```

**Functions**:
- `createWorkExperience`: Creates new work experience entry, returns auto-generated Long ID
- `getWorkExperience`: Retrieves single work experience by ID
- `getWorkExperiencesByProfileId`: Retrieves all work experiences for a profile (most common)
- `updateWorkExperience`: Updates existing work experience, returns success status
- `deleteWorkExperience`: Deletes work experience entry, returns success status

### 8. Implement DefaultWorkExperienceDao

**File**: `src/main/kotlin/database/impl/seeker/DefaultWorkExperienceDao.kt`

```kotlin
package not.djinni.database.impl.seeker

import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.seeker.WorkExperienceDao
import not.djinni.database.api.seeker.WorkExperienceEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.koin.core.annotation.Single

@Single([WorkExperienceDao::class])
class DefaultWorkExperienceDao : WorkExperienceDao {

    override suspend fun createWorkExperience(experience: WorkExperienceEntity): Long = runQuery {
        WorkExperienceTableEntity.new {
            profileId = EntityID(experience.profileId, SeekerProfileTable)
            companyName = experience.companyName
            position = experience.position
            description = experience.description
            startDate = experience.startDate
            endDate = experience.endDate
        }.id.value
    }

    override suspend fun getWorkExperience(id: Long): WorkExperienceEntity? = runQuery {
        WorkExperienceTableEntity.findById(id)?.toEntity()
    }

    override suspend fun getWorkExperiencesByProfileId(profileId: Long): List<WorkExperienceEntity> = runQuery {
        WorkExperienceTableEntity.find {
            WorkExperienceTable.profileId eq EntityID(profileId, SeekerProfileTable)
        }
            .orderBy(WorkExperienceTable.startDate to org.jetbrains.exposed.sql.SortOrder.DESC)
            .map { it.toEntity() }
    }

    override suspend fun updateWorkExperience(experience: WorkExperienceEntity): Boolean = runQuery {
        WorkExperienceTableEntity.findById(experience.id)?.apply {
            companyName = experience.companyName
            position = experience.position
            description = experience.description
            startDate = experience.startDate
            endDate = experience.endDate
        } != null
    }

    override suspend fun deleteWorkExperience(id: Long): Boolean = runQuery {
        WorkExperienceTableEntity.findById(id)?.let {
            it.delete()
            true
        } ?: false
    }
}
```

**Implementation Notes**:
- Results ordered by `startDate DESC` (most recent first)
- Uses same patterns as `DefaultSeekerProfileDao`
- Profile ID is not updatable (work experience always belongs to same profile)

### 9. Update SeekerProfileDao Interface

**File**: `src/main/kotlin/database/api/seeker/SeekerProfileDao.kt`

**Add new method:**
```kotlin
suspend fun getProfileWithWorkExperience(userId: Long): SeekerProfileEntity?
```

**Purpose**: Fetches profile and its work experiences in a single operation

### 10. Update DefaultSeekerProfileDao

**File**: `src/main/kotlin/database/impl/seeker/DefaultSeekerProfileDao.kt`

**Add injection:**
```kotlin
@Single([SeekerProfileDao::class])
class DefaultSeekerProfileDao(
    private val workExperienceDao: WorkExperienceDao
) : SeekerProfileDao {
    // ... existing methods ...
}
```

**Add new method:**
```kotlin
override suspend fun getProfileWithWorkExperience(userId: Long): SeekerProfileEntity? = runQuery {
    val profile = getProfileByUserId(userId)
    profile
}
```

**Note**: Work experiences will be loaded separately in the mapper layer to maintain separation of concerns

### 11. Create SeekerProfile Mapper

**File**: `src/main/kotlin/data/mapper/SeekerProfile.kt`

```kotlin
package not.djinni.data.mapper

import not.djinni.database.api.seeker.SeekerProfileEntity
import not.djinni.model.role.SeekerProfile
import not.djinni.model.role.WorkExperience

fun SeekerProfileEntity.toDomain(workExperiences: List<WorkExperience> = emptyList()): SeekerProfile {
    return SeekerProfile(
        id = id,
        speciality = specialty,
        experienceYears = experienceYears,
        desiredSalary = desiredSalary,
        aboutMe = aboutMe,
        workExperience = workExperiences
    )
}

fun SeekerProfile.toEntity(userId: Long): SeekerProfileEntity {
    return SeekerProfileEntity(
        id = id,
        userId = userId,
        specialty = speciality,
        experienceYears = experienceYears,
        desiredSalary = desiredSalary,
        aboutMe = aboutMe
    )
}
```

**Notes**:
- Updated `toDomain` to accept optional work experiences list
- The `toEntity` function requires `userId` parameter since domain model doesn't contain it
- Work experiences are not included in `toEntity` (they're managed separately)

### 12. Create WorkExperience Mapper

**File**: `src/main/kotlin/data/mapper/WorkExperience.kt`

```kotlin
package not.djinni.data.mapper

import not.djinni.database.api.seeker.WorkExperienceEntity
import not.djinni.model.role.WorkExperience

fun WorkExperienceEntity.toDomain(): WorkExperience {
    return WorkExperience(
        id = id,
        companyName = companyName,
        position = position,
        description = description,
        startDate = startDate,
        endDate = endDate
    )
}

fun WorkExperience.toEntity(profileId: Long): WorkExperienceEntity {
    return WorkExperienceEntity(
        id = id,
        profileId = profileId,
        companyName = companyName,
        position = position,
        description = description,
        startDate = startDate,
        endDate = endDate
    )
}
```

**Note**: The `toEntity` function requires `profileId` parameter since domain model doesn't contain it

### 13. Register Tables in NotDjinniDatabase

**File**: `src/main/kotlin/database/NotDjinniDatabase.kt`

**Changes**:
1. Import both tables:
   ```kotlin
   import not.djinni.database.impl.seeker.SeekerProfileTable
   import not.djinni.database.impl.seeker.WorkExperienceTable
   ```

2. Update `SchemaUtils.create()` call:
   ```kotlin
   SchemaUtils.create(UserTable, SeekerProfileTable, WorkExperienceTable)
   ```

**Important**: Table order matters! `WorkExperienceTable` must be created AFTER `SeekerProfileTable` due to foreign key constraint.

## File Structure

```
src/main/kotlin/
├── database/
│   ├── api/
│   │   └── seeker/
│   │       ├── SeekerProfileEntity.kt (new)
│   │       ├── SeekerProfileDao.kt (new)
│   │       ├── WorkExperienceEntity.kt (new)
│   │       └── WorkExperienceDao.kt (new)
│   ├── impl/
│   │   └── seeker/
│   │       ├── SeekerProfileTable.kt (new)
│   │       ├── DefaultSeekerProfileDao.kt (new)
│   │       ├── WorkExperienceTable.kt (new)
│   │       └── DefaultWorkExperienceDao.kt (new)
│   └── NotDjinniDatabase.kt (modified - register tables)
├── data/
│   └── mapper/
│       ├── SeekerProfile.kt (new)
│       └── WorkExperience.kt (new)
└── model/
    └── role/
        ├── SeekerProfile.kt (modified - add workExperience field)
        └── WorkExperience.kt (new)
```

## Additional Considerations

### Validation

**SeekerProfile:**
- Validate `experienceYears >= 0` in DAO layer before database operations
- Consider adding max length validation for `specialty` (255 chars)
- Consider adding min/max bounds for `desiredSalary`

**WorkExperience:**
- Validate `startDate <= endDate` when `endDate` is not null
- Validate `startDate` is not in the future
- Consider adding max length validation for `companyName` and `position` (255 chars)
- Consider validation that at most one experience can have null `endDate` (current job)

### Error Handling

**SeekerProfile:**
- Handle unique constraint violation on `user_id` (user already has profile)
- Handle foreign key constraint violation (invalid `user_id`)

**WorkExperience:**
- Handle foreign key constraint violation (invalid `profile_id`)
- Handle cascade delete (profile deletion automatically deletes work experiences)

### Index Performance

**SeekerProfile:**
- The index on `user_id` will optimize `getProfileByUserId()` queries
- PostgreSQL automatically creates index on PRIMARY KEY (`id`)
- PostgreSQL automatically creates index on UNIQUE (`user_id`)

**WorkExperience:**
- The index on `profile_id` will optimize `getWorkExperiencesByProfileId()` queries
- PostgreSQL automatically creates index on PRIMARY KEY (`id`)
- Results ordered by `startDate DESC` in DAO for most recent first

### Future Enhancements

**SeekerProfile:**
- Add pagination for listing profiles
- Add search functionality by specialty or salary range
- Add soft delete if business requirements change
- Add audit fields (created_at, updated_at) if needed for tracking

**WorkExperience:**
- Add pagination for work experience list (if profiles have many experiences)
- Add search/filter by company name or position
- Add validation for overlapping date ranges (detect conflicting work periods)
- Consider adding `employment_type` field (full-time, part-time, contract, etc.)
- Consider adding `location` field (city, country)

## Dependencies

- Exposed ORM (already in project)
  - `exposed-core`
  - `exposed-dao`
  - `exposed-jdbc`
  - `exposed-java-time` (for LocalDate support)
- PostgreSQL JDBC driver (already in project)
- Koin for dependency injection (already in project)
- Kotlin Coroutines (already in project)

## Testing Checklist

### SeekerProfile Tests

- [ ] Create profile with valid data
- [ ] Create profile with duplicate user_id (should fail)
- [ ] Create profile with negative experience_years (should fail)
- [ ] Get profile by ID
- [ ] Get profile by user_id
- [ ] Get profile with work experiences loaded
- [ ] Get non-existent profile (should return null)
- [ ] Update existing profile
- [ ] Update with invalid data (should fail)
- [ ] Check profile exists for user
- [ ] Verify CASCADE DELETE (delete user, profile should be deleted)
- [ ] Verify index is created on user_id

### WorkExperience Tests

- [ ] Create work experience with valid data
- [ ] Create work experience with invalid profile_id (should fail)
- [ ] Create work experience with end_date before start_date (should fail with validation)
- [ ] Get work experience by ID
- [ ] Get all work experiences for a profile (verify ordered by startDate DESC)
- [ ] Get work experiences for non-existent profile (should return empty list)
- [ ] Update existing work experience
- [ ] Update work experience with invalid data (should fail)
- [ ] Delete work experience
- [ ] Delete non-existent work experience (should return false)
- [ ] Verify CASCADE DELETE (delete profile, work experiences should be deleted)
- [ ] Verify index is created on profile_id
- [ ] Test multiple work experiences for same profile
- [ ] Test work experience with null end_date (current job)

### Integration Tests

- [ ] Create profile and add multiple work experiences
- [ ] Update profile and verify work experiences remain intact
- [ ] Fetch complete profile with all work experiences in single operation
- [ ] Delete user and verify both profile and work experiences are cascade deleted
- [ ] Test mapper functions (Entity ↔ Domain conversions)
- [ ] Test repository layer with both DAOs working together

## Implementation Summary

### What's New vs Original Plan

**Original Plan:**
- Only SeekerProfile implementation

**Updated Plan:**
- SeekerProfile implementation (same as original)
- **NEW**: WorkExperience table and complete implementation
- **NEW**: One-to-many relationship (profile → work experiences)
- **UPDATED**: SeekerProfile domain model includes `workExperience: List<WorkExperience>`
- **UPDATED**: Mappers support loading profile with work experiences

### Key Design Decisions

1. **ID Types**: Both tables use `BIGSERIAL` (Long) for consistency
2. **Relationship**: One-to-many (one profile, many work experiences)
3. **Domain Model**: Profile contains work experience list (not separate)
4. **Date Handling**: Use `LocalDate` (Java Time API) via Exposed's `date()` function
5. **Current Job**: Indicated by `end_date = NULL` (no separate `is_current` field)
6. **Ordering**: Work experiences ordered by `startDate DESC` (most recent first)
7. **Cascade Delete**: Both profile → user and work experiences → profile

### Files to Create (Total: 10 new files + 2 modifications)

**New Files (10):**
1. `database/api/seeker/SeekerProfileEntity.kt`
2. `database/api/seeker/SeekerProfileDao.kt`
3. `database/api/seeker/WorkExperienceEntity.kt`
4. `database/api/seeker/WorkExperienceDao.kt`
5. `database/impl/seeker/SeekerProfileTable.kt`
6. `database/impl/seeker/DefaultSeekerProfileDao.kt`
7. `database/impl/seeker/WorkExperienceTable.kt`
8. `database/impl/seeker/DefaultWorkExperienceDao.kt`
9. `data/mapper/SeekerProfile.kt`
10. `data/mapper/WorkExperience.kt`
11. `model/role/WorkExperience.kt`

**Modified Files (2):**
1. `model/role/SeekerProfile.kt` - add `workExperience` field
2. `database/NotDjinniDatabase.kt` - register both tables

### Implementation Order Recommendation

1. Create domain models (SeekerProfile update + WorkExperience)
2. Create entities (SeekerProfileEntity + WorkExperienceEntity)
3. Create tables (SeekerProfileTable + WorkExperienceTable)
4. Create DAOs (SeekerProfileDao + WorkExperienceDao)
5. Create mappers (SeekerProfile + WorkExperience)
6. Register tables in NotDjinniDatabase
7. Write tests

**Why this order?**
- Domain models define the structure (no dependencies)
- Entities mirror domain models
- Tables need entities defined
- DAOs need tables defined
- Mappers need both domain models and entities
- Database registration is final step before testing