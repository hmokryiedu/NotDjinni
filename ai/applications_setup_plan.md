# Applications and Application Statuses Database Setup Plan

## Overview
This plan outlines the creation of all required classes (entity, DAO, mapper) for Applications and ApplicationStatuses following the existing architecture patterns in the NotDjinni project.

## Database Schema

### SQL Table Definitions

```sql
CREATE TABLE application_statuses
(
    id          BIGSERIAL PRIMARY KEY,
    status_code VARCHAR(20) UNIQUE NOT NULL,
    sort_order  INT NOT NULL
);

CREATE INDEX idx_application_statuses_status_code ON application_statuses(status_code);

CREATE TABLE applications
(
    id            BIGSERIAL PRIMARY KEY,
    vacancy_id    BIGINT NOT NULL,
    job_seeker_id BIGINT NOT NULL,
    status_id     BIGINT NOT NULL DEFAULT 1,
    cover_letter  TEXT,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vacancy_id) REFERENCES vacancies (id) ON DELETE CASCADE,
    FOREIGN KEY (job_seeker_id) REFERENCES job_seeker_profiles (id) ON DELETE CASCADE,
    FOREIGN KEY (status_id) REFERENCES application_statuses (id) ON DELETE RESTRICT,
    UNIQUE (vacancy_id, job_seeker_id)
);

CREATE INDEX idx_applications_vacancy_id ON applications(vacancy_id);
CREATE INDEX idx_applications_job_seeker_id ON applications(job_seeker_id);
CREATE INDEX idx_applications_status_id ON applications(status_id);
CREATE INDEX idx_applications_created_at ON applications(created_at);
```

**Schema Notes:**
- All IDs are BIGSERIAL for auto-incrementing Long IDs (consistent with other tables)
- `status_id` defaults to 1 (which corresponds to the 'APPLIED' status - first in enum order)
- Timestamps (created_at, updated_at) added for sorting and lifecycle tracking
- CASCADE DELETE on vacancy_id and job_seeker_id: deleting a vacancy or profile deletes related applications
- RESTRICT on status_id: prevents deletion of statuses that are in use by applications
- Unique constraint on (vacancy_id, job_seeker_id) prevents duplicate applications
- `sort_order` is NOT NULL: defines the display order of statuses in UI
- Removed `status_name` column: display names handled client-side for i18n

## Domain Models

**Location**: `src/main/kotlin/model/application/`

### ApplicationStatusCode.kt

```kotlin
package not.djinni.model.application

enum class ApplicationStatusCode {
    APPLIED,
    REVIEWING,
    INTERVIEW,
    TEST_TASK,
    OFFER,
    HIRED,
    REJECTED,
    WITHDRAWN
}
```

**Purpose**: Type-safe enum for application statuses. Enum names (e.g., "APPLIED") are stored in the database. Client handles display names and i18n.

### Application.kt

```kotlin
package not.djinni.model.application

import kotlinx.datetime.Instant

data class Application(
    val id: Long,
    val vacancyId: Long,
    val jobSeekerId: Long,
    val statusId: Long,
    val coverLetter: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### ApplicationWithDetails.kt

```kotlin
package not.djinni.model.application

import kotlinx.datetime.Instant
import not.djinni.model.role.SeekerProfile
import not.djinni.model.vacancy.Vacancy
import not.djinni.model.vacancy.VacancyWithDetails

data class ApplicationWithDetails(
    val id: Long,
    val vacancy: VacancyWithDetails, // Or Vacancy if lightweight is preferred
    val jobSeeker: SeekerProfile,
    val status: ApplicationStatus,
    val coverLetter: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### ApplicationStatus.kt

```kotlin
package not.djinni.model.application

data class ApplicationStatus(
    val id: Long,
    val name: String,
    val code: String,
    val sortOrder: Int?
)
```

## Implementation Plan

### 1. Create ApplicationStatusEntity

**File**: `src/main/kotlin/database/api/application/ApplicationStatusEntity.kt`

```kotlin
package not.djinni.database.api.application

data class ApplicationStatusEntity(
    val id: Long = 0,
    val statusCode: String,
    val sortOrder: Int
)
```

### 2. Create ApplicationStatusTable

**File**: `src/main/kotlin/database/impl/application/ApplicationStatusTable.kt`

```kotlin
package not.djinni.database.impl.application

import not.djinni.database.api.application.ApplicationStatusEntity
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object ApplicationStatusTable : LongIdTable("application_statuses", "id") {
    val statusCode = varchar("status_code", 20).uniqueIndex()
    val sortOrder = integer("sort_order")
}

class ApplicationStatusTableEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ApplicationStatusTableEntity>(ApplicationStatusTable)

    var statusCode by ApplicationStatusTable.statusCode
    var sortOrder by ApplicationStatusTable.sortOrder
}

fun ApplicationStatusTableEntity.toEntity() = ApplicationStatusEntity(
    id = id.value,
    statusCode = statusCode,
    sortOrder = sortOrder
)
```

### 3. Create ApplicationEntity

**File**: `src/main/kotlin/database/api/application/ApplicationEntity.kt`

```kotlin
package not.djinni.database.api.application

import kotlinx.datetime.Instant

data class ApplicationEntity(
    val id: Long = 0,
    val vacancyId: Long,
    val jobSeekerId: Long,
    val statusId: Long,
    val coverLetter: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### 4. Create ApplicationTable

**File**: `src/main/kotlin/database/impl/application/ApplicationTable.kt`

```kotlin
package not.djinni.database.impl.application

import not.djinni.database.api.application.ApplicationEntity
import not.djinni.database.impl.seeker.SeekerProfileTable
import not.djinni.database.impl.vacancy.VacancyTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ApplicationTable : LongIdTable("applications", "id") {
    val vacancyId = reference("vacancy_id", VacancyTable, onDelete = ReferenceOption.CASCADE)
    val jobSeekerId = reference("job_seeker_id", SeekerProfileTable, onDelete = ReferenceOption.CASCADE)
    val statusId = reference("status_id", ApplicationStatusTable, onDelete = ReferenceOption.RESTRICT)
    val coverLetter = text("cover_letter").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    init {
        uniqueIndex(vacancyId, jobSeekerId)
    }
}

class ApplicationTableEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ApplicationTableEntity>(ApplicationTable)

    var vacancyId by ApplicationTable.vacancyId
    var jobSeekerId by ApplicationTable.jobSeekerId
    var statusId by ApplicationTable.statusId
    var coverLetter by ApplicationTable.coverLetter
    var createdAt by ApplicationTable.createdAt
    var updatedAt by ApplicationTable.updatedAt
}

fun ApplicationTableEntity.toEntity() = ApplicationEntity(
    id = id.value,
    vacancyId = vacancyId.value,
    jobSeekerId = jobSeekerId.value,
    statusId = statusId.value,
    coverLetter = coverLetter,
    createdAt = createdAt,
    updatedAt = updatedAt
)
```

### 5. Create Shared SortDirection Enum

**File**: `src/main/kotlin/database/api/common/SortDirection.kt`

```kotlin
package not.djinni.database.api.common

enum class SortDirection {
    ASC,
    DESC
}
```

**Purpose**: Shared enum used by both ApplicationFilter and VacancyFilter to avoid duplication.

### 6. Create ApplicationFilter

**File**: `src/main/kotlin/database/api/application/ApplicationFilter.kt`

```kotlin
package not.djinni.database.api.application

import not.djinni.database.api.common.SortDirection

enum class ApplicationSortField {
    CREATED_AT,
    UPDATED_AT
}

data class ApplicationFilter(
    val vacancyId: Long? = null,
    val jobSeekerId: Long? = null,
    val companyId: Long? = null, // To find all applications for a company's vacancies
    val statusId: Long? = null,
    val sortBy: ApplicationSortField = ApplicationSortField.CREATED_AT,
    val sortDirection: SortDirection = SortDirection.DESC
)
```

### 7. Create ApplicationStatusDao Interface

**File**: `src/main/kotlin/database/api/application/ApplicationStatusDao.kt`

```kotlin
package not.djinni.database.api.application

interface ApplicationStatusDao {
    suspend fun createStatus(status: ApplicationStatusEntity): Long
    suspend fun getStatus(id: Long): ApplicationStatusEntity?
    suspend fun getStatusByCode(code: String): ApplicationStatusEntity?
    suspend fun getAllStatuses(): List<ApplicationStatusEntity>
    suspend fun updateStatus(status: ApplicationStatusEntity): Boolean
    suspend fun deleteStatus(id: Long): Boolean
    suspend fun statusExists(code: String): Boolean
}
```

### 8. Implement DefaultApplicationStatusDao

**File**: `src/main/kotlin/database/impl/application/DefaultApplicationStatusDao.kt`

```kotlin
package not.djinni.database.impl.application

import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.application.ApplicationStatusDao
import not.djinni.database.api.application.ApplicationStatusEntity
import org.koin.core.annotation.Single

@Single([ApplicationStatusDao::class])
class DefaultApplicationStatusDao : ApplicationStatusDao {

    override suspend fun createStatus(status: ApplicationStatusEntity): Long = runQuery {
        ApplicationStatusTableEntity.new {
            statusCode = status.statusCode
            sortOrder = status.sortOrder
        }.id.value
    }

    override suspend fun getStatus(id: Long): ApplicationStatusEntity? = runQuery {
        ApplicationStatusTableEntity.findById(id)?.toEntity()
    }

    override suspend fun getStatusByCode(code: String): ApplicationStatusEntity? = runQuery {
        ApplicationStatusTableEntity.find { ApplicationStatusTable.statusCode eq code }
            .firstOrNull()
            ?.toEntity()
    }

    override suspend fun getAllStatuses(): List<ApplicationStatusEntity> = runQuery {
        ApplicationStatusTableEntity.all().map { it.toEntity() }
    }

    override suspend fun updateStatus(status: ApplicationStatusEntity): Boolean = runQuery {
        ApplicationStatusTableEntity.findById(status.id)?.apply {
            statusCode = status.statusCode
            sortOrder = status.sortOrder
        } != null
    }

    override suspend fun deleteStatus(id: Long): Boolean = runQuery {
        ApplicationStatusTableEntity.findById(id)?.apply { delete() } != null
    }

    override suspend fun statusExists(code: String): Boolean = runQuery {
        !ApplicationStatusTableEntity.find { ApplicationStatusTable.statusCode eq code }.empty()
    }
}
```

### 9. Create ApplicationDao Interface

**File**: `src/main/kotlin/database/api/application/ApplicationDao.kt`

```kotlin
package not.djinni.database.api.application

import not.djinni.database.api.seeker.SeekerProfileEntity
import not.djinni.database.api.vacancy.VacancyWithDetailsEntity

interface ApplicationDao {
    suspend fun createApplication(application: ApplicationEntity): Long
    suspend fun getApplication(id: Long): ApplicationWithDetailsEntity?
    suspend fun updateApplication(application: ApplicationEntity): Boolean
    suspend fun updateApplicationStatus(id: Long, statusId: Long): Boolean
    suspend fun deleteApplication(id: Long): Boolean

    suspend fun getApplications(
        filter: ApplicationFilter,
        limit: Int = 20,
        offset: Int = 0
    ): List<ApplicationEntity>
    
    suspend fun countApplications(filter: ApplicationFilter): Int
    
    suspend fun hasApplied(vacancyId: Long, jobSeekerId: Long): Boolean
}

data class ApplicationWithDetailsEntity(
    val application: ApplicationEntity,
    val vacancy: VacancyWithDetailsEntity,
    val jobSeeker: SeekerProfileEntity,
    val status: ApplicationStatusEntity
)
```

### 10. Implement DefaultApplicationDao

**File**: `src/main/kotlin/database/impl/application/DefaultApplicationDao.kt`

```kotlin
package not.djinni.database.impl.application

import kotlinx.datetime.Clock
import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.application.*
import not.djinni.database.api.employer.CompanyEntity
import not.djinni.database.api.vacancy.VacancyWithDetailsEntity
import not.djinni.database.impl.employer.CompanyTableEntity
import not.djinni.database.impl.seeker.SeekerProfileTable
import not.djinni.database.impl.seeker.SeekerProfileTableEntity
import not.djinni.database.impl.vacancy.EmploymentTypeTableEntity
import not.djinni.database.impl.vacancy.JobCategoryTableEntity
import not.djinni.database.impl.vacancy.VacancyStatusTableEntity
import not.djinni.database.impl.vacancy.VacancyTable
import not.djinni.database.impl.vacancy.VacancyTableEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.koin.core.annotation.Single

@Single([ApplicationDao::class])
class DefaultApplicationDao : ApplicationDao {

    override suspend fun createApplication(application: ApplicationEntity): Long = runQuery {
        ApplicationTableEntity.new {
            vacancyId = EntityID(application.vacancyId, VacancyTable)
            jobSeekerId = EntityID(application.jobSeekerId, SeekerProfileTable)
            statusId = EntityID(application.statusId, ApplicationStatusTable)
            coverLetter = application.coverLetter
            createdAt = application.createdAt
            updatedAt = application.updatedAt
        }.id.value
    }

    override suspend fun getApplication(id: Long): ApplicationWithDetailsEntity? = runQuery {
        val app = ApplicationTableEntity.findById(id) ?: return@runQuery null
        
        // Fetch related entities
        val vacancy = VacancyTableEntity.findById(app.vacancyId.value) ?: return@runQuery null
        val seeker = SeekerProfileTableEntity.findById(app.jobSeekerId.value) ?: return@runQuery null
        val status = ApplicationStatusTableEntity.findById(app.statusId.value) ?: return@runQuery null
        
        // Fetch vacancy details
        val company = CompanyTableEntity.findById(vacancy.companyId.value) ?: return@runQuery null
        
        ApplicationWithDetailsEntity(
            application = app.toEntity(),
            jobSeeker = seeker.toEntity(),
            status = status.toEntity(),
            vacancy = VacancyWithDetailsEntity(
                vacancy = vacancy.toEntity(),
                company = CompanyEntity(
                    id = company.id.value,
                    companyName = company.companyName,
                    website = company.website,
                    description = company.description
                ),
                employmentType = vacancy.employmentTypeId?.value?.let {
                    EmploymentTypeTableEntity.findById(it)?.toEntity()
                },
                category = vacancy.categoryId?.value?.let {
                    JobCategoryTableEntity.findById(it)?.toEntity()
                },
                status = VacancyStatusTableEntity.findById(vacancy.statusId.value)!!.toEntity()
            )
        )
    }

    override suspend fun updateApplication(application: ApplicationEntity): Boolean = runQuery {
        ApplicationTableEntity.findById(application.id)?.apply {
            statusId = EntityID(application.statusId, ApplicationStatusTable)
            coverLetter = application.coverLetter
            updatedAt = Clock.System.now()
        } != null
    }

    override suspend fun updateApplicationStatus(id: Long, statusId: Long): Boolean = runQuery {
        ApplicationTableEntity.findById(id)?.apply {
            this.statusId = EntityID(statusId, ApplicationStatusTable)
            updatedAt = Clock.System.now()
        } != null
    }

    override suspend fun deleteApplication(id: Long): Boolean = runQuery {
        ApplicationTableEntity.findById(id)?.apply { delete() } != null
    }

    override suspend fun getApplications(
        filter: ApplicationFilter,
        limit: Int,
        offset: Int
    ): List<ApplicationEntity> = runQuery {
        buildApplicationQuery(filter)
            .limit(limit, offset.toLong())
            .map { ApplicationTableEntity.wrapRow(it).toEntity() }
    }

    override suspend fun countApplications(filter: ApplicationFilter): Int = runQuery {
        buildApplicationQuery(filter).count().toInt()
    }

    override suspend fun hasApplied(vacancyId: Long, jobSeekerId: Long): Boolean = runQuery {
        !ApplicationTableEntity.find {
            (ApplicationTable.vacancyId eq vacancyId) and (ApplicationTable.jobSeekerId eq jobSeekerId)
        }.empty()
    }

    private fun buildApplicationQuery(filter: ApplicationFilter): Query {
        val source = if (filter.companyId != null) {
            ApplicationTable.innerJoin(VacancyTable)
        } else {
            ApplicationTable
        }

        var query = source.selectAll()

        filter.companyId?.let {
            query = query.andWhere { VacancyTable.companyId eq it }
        }

        filter.vacancyId?.let {
            query = query.andWhere { ApplicationTable.vacancyId eq it }
        }

        filter.jobSeekerId?.let {
            query = query.andWhere { ApplicationTable.jobSeekerId eq it }
        }

        filter.statusId?.let {
            query = query.andWhere { ApplicationTable.statusId eq it }
        }

        val sortOrder = when (filter.sortDirection) {
            SortDirection.ASC -> SortOrder.ASC
            SortDirection.DESC -> SortOrder.DESC
        }

        val sortColumn = when (filter.sortBy) {
            ApplicationSortField.CREATED_AT -> ApplicationTable.createdAt
            ApplicationSortField.UPDATED_AT -> ApplicationTable.updatedAt
        }

        return query.orderBy(sortColumn to sortOrder)
    }
}
```

### 11. Create Mappers

**File**: `src/main/kotlin/data/mapper/ApplicationStatus.kt`

```kotlin
package not.djinni.data.mapper

import not.djinni.database.api.application.ApplicationStatusEntity
import not.djinni.model.application.ApplicationStatus

fun ApplicationStatusEntity.toDomain() = ApplicationStatus(
    id = id,
    name = statusCode, // Use code as name (client handles display)
    code = statusCode,
    sortOrder = sortOrder
)

fun ApplicationStatus.toEntity() = ApplicationStatusEntity(
    id = id,
    statusCode = code,
    sortOrder = sortOrder ?: 0
)
```

**File**: `src/main/kotlin/data/mapper/Application.kt`

```kotlin
package not.djinni.data.mapper

import not.djinni.database.api.application.ApplicationEntity
import not.djinni.database.api.application.ApplicationWithDetailsEntity
import not.djinni.model.application.Application
import not.djinni.model.application.ApplicationWithDetails

fun ApplicationEntity.toDomain() = Application(
    id = id,
    vacancyId = vacancyId,
    jobSeekerId = jobSeekerId,
    statusId = statusId,
    coverLetter = coverLetter,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Application.toEntity() = ApplicationEntity(
    id = id,
    vacancyId = vacancyId,
    jobSeekerId = jobSeekerId,
    statusId = statusId,
    coverLetter = coverLetter,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ApplicationWithDetailsEntity.toDomain() = ApplicationWithDetails(
    id = application.id,
    vacancy = vacancy.toDomain(),
    jobSeeker = jobSeeker.toDomain(),
    status = status.toDomain(),
    coverLetter = application.coverLetter,
    createdAt = application.createdAt,
    updatedAt = application.updatedAt
)
```

### 12. Register Tables and Seed Data in NotDjinniDatabase

**File**: `src/main/kotlin/database/NotDjinniDatabase.kt`

**Complete implementation**:
```kotlin
import kotlinx.coroutines.runBlocking
import not.djinni.database.impl.application.ApplicationStatusTable
import not.djinni.database.impl.application.ApplicationTable
import not.djinni.database.seed.ApplicationStatusSeedData
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object NotDjinniDatabase : KoinComponent {

    private val database: Database by lazy {
        Database.connect(
            "jdbc:postgresql://localhost:5432/notdjinni",
            driver = "org.postgresql.Driver",
            user = "notdjinni",
            password = "notdjinnipassword"
        )
    }

    fun init() {
        transaction(database) {
            SchemaUtils.create(
                UserTable,
                CompanyTable,
                EmployerProfileTable,
                SeekerProfileTable,
                VacancyTable,
                ApplicationStatusTable,  // Must be before ApplicationTable
                ApplicationTable
            )
            addLogger(StdOutSqlLogger)
        }

        // Seed lookup tables
        runBlocking {
            get<ApplicationStatusSeedData>().seedData()
        }
    }

    suspend fun <T> runQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
```

**Order matters**:
- `ApplicationStatusTable` must be created before `ApplicationTable`
- Seed data is called after all tables are created

### 13. Seed Initial Data

**File**: `src/main/kotlin/database/seed/ApplicationStatusSeedData.kt`

```kotlin
package not.djinni.database.seed

import not.djinni.database.api.application.ApplicationStatusDao
import not.djinni.database.api.application.ApplicationStatusEntity
import not.djinni.model.application.ApplicationStatusCode
import org.koin.core.annotation.Single

@Single
class ApplicationStatusSeedData(
    private val applicationStatusDao: ApplicationStatusDao
) {
    suspend fun seedData() {
        ApplicationStatusCode.entries.forEachIndexed { index, statusCode ->
            if (!applicationStatusDao.statusExists(statusCode.name)) {
                applicationStatusDao.createStatus(
                    ApplicationStatusEntity(
                        statusCode = statusCode.name,
                        sortOrder = index + 1
                    )
                )
            }
        }
    }
}
```

## File Structure

```
src/main/kotlin/
├── database/
│   ├── api/
│   │   ├── common/
│   │   │   └── SortDirection.kt (NEW - shared enum)
│   │   └── application/
│   │       ├── ApplicationStatusEntity.kt
│   │       ├── ApplicationStatusDao.kt
│   │       ├── ApplicationEntity.kt
│   │       ├── ApplicationDao.kt
│   │       └── ApplicationFilter.kt (imports SortDirection)
│   ├── impl/
│   │   └── application/
│   │       ├── ApplicationStatusTable.kt
│   │       ├── DefaultApplicationStatusDao.kt
│   │       ├── ApplicationTable.kt (RESTRICT constraint)
│   │       └── DefaultApplicationDao.kt
│   ├── seed/
│   │   └── ApplicationStatusSeedData.kt (uses enum)
│   └── NotDjinniDatabase.kt (modified)
├── data/
│   └── mapper/
│       ├── ApplicationStatus.kt
│       └── Application.kt
└── model/
    └── application/
        ├── ApplicationStatusCode.kt (NEW - enum)
        ├── ApplicationStatus.kt
        ├── Application.kt
        └── ApplicationWithDetails.kt
```

## Validation Rules

### ApplicationDao
- `vacancyId` must exist
- `jobSeekerId` must exist
- `statusId` must exist
- Unique constraint: User cannot apply to the same vacancy twice
- `coverLetter` is optional but recommended

## Error Handling

- Foreign key violations (vacancy, seeker, status)
- Unique constraint violation (duplicate application)
- RESTRICT constraint: attempts to delete a status that's in use will fail (returns false from deleteStatus)

## Index Performance

- Index on `vacancy_id` for fast retrieval of applications per vacancy
- Index on `job_seeker_id` for fast retrieval of a user's applications
- Index on `status_id` for filtering by status
- Index on `created_at` for sorting
- Unique index on `(vacancy_id, job_seeker_id)`

## Foreign Key Relationships

### Applications → Vacancies
- ON DELETE CASCADE: If vacancy is deleted, all applications are deleted.

### Applications → JobSeekerProfiles
- ON DELETE CASCADE: If job seeker profile is deleted, all their applications are deleted.

### Applications → ApplicationStatuses
- ON DELETE RESTRICT: Prevents deletion of statuses that are in use. Status can only be deleted if no applications reference it.

## Filtering & Sorting

- Filter by `vacancyId` (Company view)
- Filter by `jobSeekerId` (Seeker view)
- Filter by `statusId`
- Sort by Date (Newest first default)

## Testing Checklist

### ApplicationStatus Tests
- [ ] Create status
- [ ] Get status by code
- [ ] Get all statuses
- [ ] Check existence

### Application Tests
- [ ] Create application
- [ ] Verify unique constraint (apply twice fails)
- [ ] Get application with details
- [ ] Update status
- [ ] Delete application
- [ ] Filter by vacancy
- [ ] Filter by job seeker
- [ ] Check `hasApplied`
- [ ] Verify CASCADE delete from Vacancy
- [ ] Verify CASCADE delete from Seeker

## Summary

This plan adds the core logic for managing job applications. It connects Vacancies and Job Seekers via a many-to-many relationship table `Applications` with an additional status field managed by a lookup table `ApplicationStatuses`.
