# Employer Profile and Company Database Setup Plan

## Overview
This plan outlines the creation of all required classes (entity, DAO, mapper) for the Company and Employer Profile features following the existing architecture patterns in the NotDjinni project.

## Database Schema

### SQL Table Definitions

```sql
CREATE TABLE Companies
(
    id           BIGSERIAL PRIMARY KEY,
    company_name VARCHAR(255) UNIQUE NOT NULL,
    website      VARCHAR(255),
    description  TEXT NOT NULL
);

CREATE INDEX idx_companies_company_name ON Companies(company_name);

CREATE TABLE EmployerProfiles
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT UNIQUE NOT NULL,
    company_id BIGINT NOT NULL,
    role       VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE,
    FOREIGN KEY (company_id) REFERENCES Companies (id) ON DELETE CASCADE
);

CREATE INDEX idx_employer_profiles_user_id ON EmployerProfiles(user_id);
CREATE INDEX idx_employer_profiles_company_id ON EmployerProfiles(company_id);
```

**Schema Notes:**
- All IDs are BIGSERIAL for auto-incrementing Long IDs (consistent with Users table)
- `user_id` is BIGINT to match Users table (Long type)
- CASCADE DELETE on both foreign keys
- Indexes on foreign keys for JOIN performance
- `role` field is required (NOT NULL)

## Domain Models

**Location**: `src/main/kotlin/model/role/`

### Company.kt

```kotlin
package not.djinni.model.role

data class Company(
    val id: Long,
    val companyName: String,
    val website: String?,
    val description: String
)
```

### EmployerProfile.kt

```kotlin
package not.djinni.model.role

data class EmployerProfile(
    val id: Long,
    val companyId: Long,
    val role: String
)
```

### EmployerProfileWithCompany.kt

```kotlin
package not.djinni.model.role

data class EmployerProfileWithCompany(
    val id: Long,
    val role: String,
    val company: Company
)
```

## Implementation Plan

### 1. Create CompanyEntity

**File**: `src/main/kotlin/database/api/employer/CompanyEntity.kt`

```kotlin
package not.djinni.database.api.employer

data class CompanyEntity(
    val id: Long = 0,
    val companyName: String,
    val website: String?,
    val description: String
)
```

### 2. Create CompanyTable

**File**: `src/main/kotlin/database/impl/employer/CompanyTable.kt`

```kotlin
package not.djinni.database.impl.employer

import not.djinni.database.api.employer.CompanyEntity
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object CompanyTable : LongIdTable("companies", "id") {
    val companyName = varchar("company_name", MAX_VARCHAR_LENGTH).uniqueIndex()
    val website = varchar("website", MAX_VARCHAR_LENGTH).nullable()
    val description = text("description")

    private const val MAX_VARCHAR_LENGTH = 255
}

class CompanyTableEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<CompanyTableEntity>(CompanyTable)

    var companyName by CompanyTable.companyName
    var website by CompanyTable.website
    var description by CompanyTable.description
}

fun CompanyTableEntity.toEntity() = CompanyEntity(
    id = id.value,
    companyName = companyName,
    website = website,
    description = description
)
```

### 3. Create EmployerProfileEntity

**File**: `src/main/kotlin/database/api/employer/EmployerProfileEntity.kt`

```kotlin
package not.djinni.database.api.employer

data class EmployerProfileEntity(
    val id: Long = 0,
    val userId: Long,
    val companyId: Long,
    val role: String
)
```

### 4. Create EmployerProfileTable

**File**: `src/main/kotlin/database/impl/employer/EmployerProfileTable.kt`

```kotlin
package not.djinni.database.impl.employer

import not.djinni.database.api.employer.EmployerProfileEntity
import not.djinni.database.impl.user.UserTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object EmployerProfileTable : LongIdTable("employer_profiles", "id") {
    val userId = reference("user_id", UserTable).uniqueIndex()
    val companyId = reference("company_id", CompanyTable)
    val role = varchar("role", MAX_VARCHAR_LENGTH)

    private const val MAX_VARCHAR_LENGTH = 255
}

class EmployerProfileTableEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<EmployerProfileTableEntity>(EmployerProfileTable)

    var userId by EmployerProfileTable.userId
    var company by CompanyTableEntity referencedOn EmployerProfileTable.companyId
    var role by EmployerProfileTable.role
}

fun EmployerProfileTableEntity.toEntity() = EmployerProfileEntity(
    id = id.value,
    userId = userId.value,
    companyId = company.id.value,
    role = role
)
```

### 5. Create CompanyDao Interface

**File**: `src/main/kotlin/database/api/employer/CompanyDao.kt`

```kotlin
package not.djinni.database.api.employer

interface CompanyDao {
    suspend fun createCompany(company: CompanyEntity): Long
    suspend fun getCompany(id: Long): CompanyEntity?
    suspend fun getCompanyByName(name: String): CompanyEntity?
    suspend fun updateCompany(company: CompanyEntity): Boolean
    suspend fun deleteCompany(id: Long): Boolean
    suspend fun companyExists(name: String): Boolean
}
```

### 6. Implement DefaultCompanyDao

**File**: `src/main/kotlin/database/impl/employer/DefaultCompanyDao.kt`

```kotlin
package not.djinni.database.impl.employer

import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.employer.CompanyDao
import not.djinni.database.api.employer.CompanyEntity
import org.koin.core.annotation.Single

@Single([CompanyDao::class])
class DefaultCompanyDao : CompanyDao {

    override suspend fun createCompany(company: CompanyEntity): Long = runQuery {
        CompanyTableEntity.new {
            companyName = company.companyName
            website = company.website
            description = company.description
        }.id.value
    }

    override suspend fun getCompany(id: Long): CompanyEntity? = runQuery {
        CompanyTableEntity.findById(id)?.toEntity()
    }

    override suspend fun getCompanyByName(name: String): CompanyEntity? = runQuery {
        CompanyTableEntity.find { CompanyTable.companyName eq name }
            .firstOrNull()
            ?.toEntity()
    }

    override suspend fun updateCompany(company: CompanyEntity): Boolean = runQuery {
        CompanyTableEntity.findById(company.id)?.apply {
            companyName = company.companyName
            website = company.website
            description = company.description
        } != null
    }

    override suspend fun deleteCompany(id: Long): Boolean = runQuery {
        CompanyTableEntity.findById(id)?.apply { delete() } != null
    }

    override suspend fun companyExists(name: String): Boolean = runQuery {
        !CompanyTableEntity.find { CompanyTable.companyName eq name }.empty()
    }
}
```

### 7. Create EmployerProfileDao Interface

**File**: `src/main/kotlin/database/api/employer/EmployerProfileDao.kt`

```kotlin
package not.djinni.database.api.employer

interface EmployerProfileDao {
    suspend fun createProfile(profile: EmployerProfileEntity): Long
    suspend fun getProfile(id: Long): EmployerProfileWithCompany?
    suspend fun getProfileByUserId(userId: Long): EmployerProfileWithCompany?
    suspend fun updateProfile(profile: EmployerProfileEntity): Boolean
    suspend fun profileExists(userId: Long): Boolean
}

data class EmployerProfileWithCompany(
    val profile: EmployerProfileEntity,
    val company: CompanyEntity
)
```

### 8. Implement DefaultEmployerProfileDao

**File**: `src/main/kotlin/database/impl/employer/DefaultEmployerProfileDao.kt`

```kotlin
package not.djinni.database.impl.employer

import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.employer.EmployerProfileDao
import not.djinni.database.api.employer.EmployerProfileEntity
import not.djinni.database.api.employer.EmployerProfileWithCompany
import not.djinni.database.impl.user.UserTable
import org.jetbrains.exposed.dao.id.EntityID
import org.koin.core.annotation.Single

@Single([EmployerProfileDao::class])
class DefaultEmployerProfileDao : EmployerProfileDao {

    override suspend fun createProfile(profile: EmployerProfileEntity): Long = runQuery {
        EmployerProfileTableEntity.new {
            userId = EntityID(profile.userId, UserTable)
            company = CompanyTableEntity.findById(profile.companyId)
                ?: throw IllegalArgumentException("Company not found: ${profile.companyId}")
            role = profile.role
        }.id.value
    }

    override suspend fun getProfile(id: Long): EmployerProfileWithCompany? = runQuery {
        EmployerProfileTableEntity.findById(id)?.let {
            EmployerProfileWithCompany(
                profile = it.toEntity(),
                company = it.company.toEntity()
            )
        }
    }

    override suspend fun getProfileByUserId(userId: Long): EmployerProfileWithCompany? = runQuery {
        EmployerProfileTableEntity.find { EmployerProfileTable.userId eq userId }
            .firstOrNull()
            ?.let {
                EmployerProfileWithCompany(
                    profile = it.toEntity(),
                    company = it.company.toEntity()
                )
            }
    }

    override suspend fun updateProfile(profile: EmployerProfileEntity): Boolean = runQuery {
        EmployerProfileTableEntity.findById(profile.id)?.apply {
            role = profile.role
        } != null
    }

    override suspend fun profileExists(userId: Long): Boolean = runQuery {
        !EmployerProfileTableEntity.find { EmployerProfileTable.userId eq userId }.empty()
    }
}
```

### 9. Create Company Mapper

**File**: `src/main/kotlin/data/mapper/Company.kt`

```kotlin
package not.djinni.data.mapper

import not.djinni.database.api.employer.CompanyEntity
import not.djinni.model.role.Company

fun CompanyEntity.toDomain() = Company(
    id = id,
    companyName = companyName,
    website = website,
    description = description
)

fun Company.toEntity() = CompanyEntity(
    id = id,
    companyName = companyName,
    website = website,
    description = description
)
```

### 10. Create EmployerProfile Mapper

**File**: `src/main/kotlin/data/mapper/EmployerProfile.kt`

```kotlin
package not.djinni.data.mapper

import not.djinni.database.api.employer.EmployerProfileEntity
import not.djinni.database.api.employer.EmployerProfileWithCompany as EmployerProfileWithCompanyEntity
import not.djinni.model.role.EmployerProfile
import not.djinni.model.role.EmployerProfileWithCompany

fun EmployerProfileWithCompanyEntity.toProfileDomain() = EmployerProfile(
    id = profile.id,
    companyId = profile.companyId,
    role = profile.role
)

fun EmployerProfileWithCompanyEntity.toProfileWithCompanyDomain() = EmployerProfileWithCompany(
    id = profile.id,
    role = profile.role,
    company = company.toDomain()
)

fun EmployerProfile.toEntity(userId: Long) = EmployerProfileEntity(
    id = id,
    userId = userId,
    companyId = companyId,
    role = role
)
```

### 11. Register Tables in NotDjinniDatabase

**File**: `src/main/kotlin/database/NotDjinniDatabase.kt`

**Changes**:
1. Import both new tables:
   ```kotlin
   import not.djinni.database.impl.employer.CompanyTable
   import not.djinni.database.impl.employer.EmployerProfileTable
   ```

2. Update `SchemaUtils.create()` call in `init()`:
   ```kotlin
   fun init() {
       transaction(database) {
           SchemaUtils.create(UserTable, CompanyTable, EmployerProfileTable)
           addLogger(StdOutSqlLogger)
       }
   }
   ```

**Order matters**: CompanyTable must be created before EmployerProfileTable due to foreign key dependency.

## File Structure

```
src/main/kotlin/
├── database/
│   ├── api/
│   │   └── employer/
│   │       ├── CompanyEntity.kt
│   │       ├── CompanyDao.kt
│   │       ├── EmployerProfileEntity.kt
│   │       └── EmployerProfileDao.kt
│   ├── impl/
│   │   └── employer/
│   │       ├── CompanyTable.kt
│   │       ├── DefaultCompanyDao.kt
│   │       ├── EmployerProfileTable.kt
│   │       └── DefaultEmployerProfileDao.kt
│   └── NotDjinniDatabase.kt (modified)
├── data/
│   └── mapper/
│       ├── Company.kt
│       └── EmployerProfile.kt
└── model/
    └── role/
        ├── Company.kt (new)
        ├── EmployerProfile.kt (new)
        └── EmployerProfileWithCompany.kt (new)
```

## Validation Rules

### CompanyDao
- `companyName`: not empty, max 255 chars
- `description`: not empty
- `website`: valid URL format if provided (nullable)
- Check `companyExists(name)` before creation

### EmployerProfileDao
- `role`: not empty, max 255 chars
- Validate company exists before creating profile
- Check `profileExists(userId)` before creation
- `companyId` and `userId` are immutable (only `role` can be updated)

## Error Handling

### CompanyDao
- Unique constraint violation on `company_name`
- Foreign key violation on delete (CASCADE)

### EmployerProfileDao
- Unique constraint violation on `user_id`
- Foreign key violation on `user_id`
- Foreign key violation on `company_id` (validated explicitly in createProfile)

## Index Performance

- PostgreSQL auto-indexes PRIMARY KEY columns
- PostgreSQL auto-indexes UNIQUE columns (`company_name`, `user_id`)
- Manual index on `company_id` optimizes JOIN queries
- Manual index on `company_name` optimizes `getCompanyByName()` queries

## Foreign Key Relationships

### EmployerProfiles → Users
- ON DELETE CASCADE: user deletion → profile deletion
- UNIQUE constraint: one profile per user

### EmployerProfiles → Companies
- ON DELETE CASCADE: company deletion → all related profiles deletion
- Multiple profiles can reference same company

## Immutability Constraints

### EmployerProfile
- `id`: immutable (primary key)
- `userId`: immutable (unique constraint)
- `companyId`: immutable (business rule - only role updates allowed)
- `role`: mutable (updated via `updateProfile()`)

## Future Enhancements

### Company
- Soft delete flag
- Logo URL field
- Industry/category field
- Company size field
- Location/headquarters
- Founded date
- Audit fields (created_at, updated_at)
- Contact information

### EmployerProfile
- Department field
- Hire date field
- Permissions/authority level
- Contact information
- isActive/status field
- Pagination for listings
- Search by role/company
- Audit fields

### Additional Queries
- `getProfilesByCompany(companyId)` with pagination
- Search by company name (partial match)
- Filtering by role, company attributes
- Counting functions

## Dependencies

All already in project:
- Exposed ORM
- PostgreSQL JDBC driver
- Koin (dependency injection)
- Kotlin Coroutines
- Java UUID library

## Testing Checklist

### Company Tests
- [ ] Create company with valid data
- [ ] Create with duplicate name (should fail)
- [ ] Create with empty company_name (should fail)
- [ ] Create with empty description (should fail)
- [ ] Create with null website (should succeed)
- [ ] Create with invalid website format (should fail)
- [ ] Get company by ID
- [ ] Get company by name
- [ ] Get non-existent company (returns null)
- [ ] Update existing company
- [ ] Update with duplicate name (should fail)
- [ ] Delete company (CASCADE deletes profiles)
- [ ] Check company exists by name

### EmployerProfile Tests
- [ ] Create profile with valid data
- [ ] Create with empty role (should fail)
- [ ] Create with duplicate user_id (should fail)
- [ ] Create with non-existent user_id (should fail)
- [ ] Create with non-existent company_id (should fail)
- [ ] Get profile by ID (verify JOIN)
- [ ] Get profile by user_id (verify JOIN)
- [ ] Get non-existent profile (returns null)
- [ ] Update profile role
- [ ] Verify companyId immutability
- [ ] Check profile exists for user
- [ ] Verify user CASCADE DELETE
- [ ] Verify company CASCADE DELETE
- [ ] Verify Company JOIN queries

### Mapper Tests
- [ ] CompanyEntity ↔ Company domain conversion
- [ ] EmployerProfileWithCompany (DAO) → EmployerProfile (domain)
- [ ] EmployerProfileWithCompany (DAO) → EmployerProfileWithCompany (domain)
- [ ] EmployerProfile (domain) → EmployerProfileEntity with userId

### Integration Tests
- [ ] Create company → create profile → verify relationship
- [ ] Multiple profiles for same company
- [ ] Delete company → verify CASCADE
- [ ] Delete user → verify CASCADE
- [ ] Update role → verify only role changes
- [ ] Verify indexes created
- [ ] Verify unique constraints
- [ ] Full flow mapper conversions
- [ ] Business logic with companyId

## Key Architectural Decisions

1. **DAO Separation**: CompanyDao and EmployerProfileDao are separate
2. **JOIN Strategy**: Uses Exposed's `referencedOn` for entity navigation
3. **Return Types**: EmployerProfileDao returns `EmployerProfileWithCompany` (entity-level)
4. **Domain Models**: Three models - `EmployerProfile`, `EmployerProfileWithCompany`, `Company`
5. **ID Type**: Long (BIGSERIAL) for auto-incrementing IDs (consistent with Users table)
6. **Immutability**: Only `role` is mutable; `companyId` and `userId` are immutable
7. **Role Field**: Required (String, NOT NULL), mutable
8. **CASCADE DELETE**: Enabled for both foreign keys
9. **Indexes**: Manual index on `company_id` for JOINs
10. **CompanyId in Domain**: Kept for efficient business logic

## Implementation Order

1. Create domain models (Company.kt, EmployerProfile.kt, EmployerProfileWithCompany.kt)
2. Create CompanyEntity.kt
3. Create CompanyTable.kt
4. Create CompanyDao.kt
5. Create DefaultCompanyDao.kt
6. Create Company mapper
7. Create EmployerProfileEntity.kt
8. Create EmployerProfileTable.kt
9. Create EmployerProfileDao.kt
10. Create DefaultEmployerProfileDao.kt
11. Create EmployerProfile mapper
12. Register tables in NotDjinniDatabase
13. Test Company operations
14. Test EmployerProfile operations with JOINs
15. Test CASCADE DELETE behaviors
16. Test mapper conversions
17. Test business logic with companyId

## Idiomatic Kotlin Features Used

### Scope Functions
- `apply`: Object configuration in update operations
- `let`: Transformations and null-safe operations
- `also`: Side effects (UUID generation)

### Language Features
- Extension functions for entity-to-domain mapping
- Data classes with automatic equals/hashCode/toString
- Nullable types with safe call operator (`?.`)
- `!= null` for boolean null checks
- Single-expression functions
- Type inference
- Property delegation in Exposed entities

### Clean Code Practices
- Self-documenting code without comments
- Immutability (val over var)
- Expression-oriented programming
- Concise function bodies