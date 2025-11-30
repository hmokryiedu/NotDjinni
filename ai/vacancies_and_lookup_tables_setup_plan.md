# Vacancies Database Setup Plan

## Overview
This plan outlines the creation of all required classes (entity, DAO, mapper) for Vacancies using enum-based lookup values (EmploymentTypeCode, JobCategoryCode, VacancyStatusCode) following the existing architecture patterns in the NotDjinni project.

**Key Decision**: No separate lookup tables for EmploymentType, JobCategory, or VacancyStatus. These are stored as enums and persisted as VARCHAR in the database using Exposed's `enumeration<T>()` function.

## Database Schema

### SQL Table Definitions

```sql
CREATE TABLE vacancies
(
    id                   BIGSERIAL PRIMARY KEY,
    company_id           BIGINT NOT NULL,
    title                VARCHAR(255) NOT NULL,
    description          TEXT NOT NULL,
    salary_min           INT NOT NULL,
    salary_max           INT NOT NULL,
    min_experience_years INT CHECK (min_experience_years >= 0),
    employment_type      VARCHAR(255),
    category             VARCHAR(255),
    status               VARCHAR(255) NOT NULL DEFAULT 'DRAFT',
    created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE
);

CREATE INDEX idx_vacancies_company_id ON vacancies(company_id);
CREATE INDEX idx_vacancies_category ON vacancies(category);
CREATE INDEX idx_vacancies_status ON vacancies(status);
CREATE INDEX idx_vacancies_employment_type ON vacancies(employment_type);
CREATE INDEX idx_vacancies_created_at ON vacancies(created_at);
```

**Schema Notes:**
- All IDs are BIGSERIAL for auto-incrementing Long IDs (consistent with other tables)
- salary_min and salary_max are INT (stores salary in dollars, NOT NULL)
- Timestamps (created_at, updated_at) for sorting and display
- CASCADE DELETE on company_id: deleting company deletes its vacancies
- **No lookup tables**: employment_type, category, and status are VARCHAR storing enum names (e.g., "FULL_TIME", "SOFTWARE_DEV", "ACTIVE")
- Exposed's `enumeration<T>()` automatically handles enum ↔ VARCHAR conversion
- Indexes on enum columns for efficient filtering
- CHECK constraint on min_experience_years >= 0
- Default status is 'DRAFT' for newly created vacancies

## Domain Models

**Location**: `src/main/kotlin/model/vacancy/`

### EmploymentTypeCode.kt

```kotlin
package not.djinni.model.vacancy

enum class EmploymentTypeCode {
    FULL_TIME,
    PART_TIME,
    CONTRACT,
    TEMPORARY,
    INTERNSHIP,
    FREELANCE
}
```

**Purpose**: Type-safe enum for employment types. Enum names (e.g., "FULL_TIME") are stored directly in the database.

### JobCategoryCode.kt

```kotlin
package not.djinni.model.vacancy

enum class JobCategoryCode {
    SOFTWARE_DEV,
    DATA_SCIENCE,
    DEVOPS,
    QA,
    PRODUCT_MGMT,
    DESIGN,
    MARKETING,
    SALES,
    HR,
    FINANCE,
    OPERATIONS,
    SUPPORT
}
```

**Purpose**: Type-safe enum for job categories. Client handles display names and i18n.

### VacancyStatusCode.kt

```kotlin
package not.djinni.model.vacancy

enum class VacancyStatusCode {
    ACTIVE,
    CLOSED,
    DRAFT,
    PAUSED,
    EXPIRED
}
```

**Purpose**: Type-safe enum for vacancy statuses. Represents the lifecycle state of a vacancy.

### Vacancy.kt

```kotlin
package not.djinni.model.vacancy

import kotlinx.datetime.Instant

data class Vacancy(
    val id: Long,
    val companyId: Long,
    val title: String,
    val description: String,
    val salary: Salary,
    val minExperienceYears: Int?,
    val employmentType: EmploymentTypeCode?,
    val category: JobCategoryCode?,
    val status: VacancyStatusCode,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class Salary(
    val min: Int,
    val max: Int
)
```

### VacancyWithDetails.kt

```kotlin
package not.djinni.model.vacancy

import kotlinx.datetime.Instant
import not.djinni.model.role.Company

data class VacancyWithDetails(
    val id: Long,
    val company: Company,
    val title: String,
    val description: String,
    val salary: Salary,
    val minExperienceYears: Int?,
    val employmentType: EmploymentTypeCode?,
    val category: JobCategoryCode?,
    val status: VacancyStatusCode,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

**Note**: VacancyWithDetails uses enum codes directly. Client handles display names for employment types, categories, and statuses.

## Implementation Plan

### 1. Create EmploymentTypeEntity

**File**: `src/main/kotlin/database/api/vacancy/EmploymentTypeEntity.kt`

```kotlin
package not.djinni.database.api.vacancy

data class EmploymentTypeEntity(
    val id: Long = 0,
    val typeName: String,
    val typeCode: String
)
```

### 2. Create EmploymentTypeTable

**File**: `src/main/kotlin/database/impl/vacancy/EmploymentTypeTable.kt`

```kotlin
package not.djinni.database.impl.vacancy

import not.djinni.database.api.vacancy.EmploymentTypeEntity
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object EmploymentTypeTable : LongIdTable("employment_types", "id") {
    val typeName = varchar("type_name", 50).uniqueIndex()
    val typeCode = varchar("type_code", 20).uniqueIndex()
}

class EmploymentTypeTableEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<EmploymentTypeTableEntity>(EmploymentTypeTable)

    var typeName by EmploymentTypeTable.typeName
    var typeCode by EmploymentTypeTable.typeCode
}

fun EmploymentTypeTableEntity.toEntity() = EmploymentTypeEntity(
    id = id.value,
    typeName = typeName,
    typeCode = typeCode
)
```

### 3. Create JobCategoryEntity

**File**: `src/main/kotlin/database/api/vacancy/JobCategoryEntity.kt`

```kotlin
package not.djinni.database.api.vacancy

data class JobCategoryEntity(
    val id: Long = 0,
    val categoryName: String,
    val categoryCode: String
)
```

### 4. Create JobCategoryTable

**File**: `src/main/kotlin/database/impl/vacancy/JobCategoryTable.kt`

```kotlin
package not.djinni.database.impl.vacancy

import not.djinni.database.api.vacancy.JobCategoryEntity
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object JobCategoryTable : LongIdTable("job_categories", "id") {
    val categoryName = varchar("category_name", 100).uniqueIndex()
    val categoryCode = varchar("category_code", 50).uniqueIndex()
}

class JobCategoryTableEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<JobCategoryTableEntity>(JobCategoryTable)

    var categoryName by JobCategoryTable.categoryName
    var categoryCode by JobCategoryTable.categoryCode
}

fun JobCategoryTableEntity.toEntity() = JobCategoryEntity(
    id = id.value,
    categoryName = categoryName,
    categoryCode = categoryCode
)
```

### 5. Create VacancyStatusEntity

**File**: `src/main/kotlin/database/api/vacancy/VacancyStatusEntity.kt`

```kotlin
package not.djinni.database.api.vacancy

data class VacancyStatusEntity(
    val id: Long = 0,
    val statusName: String,
    val statusCode: String
)
```

### 6. Create VacancyStatusTable

**File**: `src/main/kotlin/database/impl/vacancy/VacancyStatusTable.kt`

```kotlin
package not.djinni.database.impl.vacancy

import not.djinni.database.api.vacancy.VacancyStatusEntity
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object VacancyStatusTable : LongIdTable("vacancy_statuses", "id") {
    val statusName = varchar("status_name", 50).uniqueIndex()
    val statusCode = varchar("status_code", 20).uniqueIndex()
}

class VacancyStatusTableEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<VacancyStatusTableEntity>(VacancyStatusTable)

    var statusName by VacancyStatusTable.statusName
    var statusCode by VacancyStatusTable.statusCode
}

fun VacancyStatusTableEntity.toEntity() = VacancyStatusEntity(
    id = id.value,
    statusName = statusName,
    statusCode = statusCode
)
```

### 7. Create VacancyEntity

**File**: `src/main/kotlin/database/api/vacancy/VacancyEntity.kt`

```kotlin
package not.djinni.database.api.vacancy

import kotlinx.datetime.Instant

data class VacancyEntity(
    val id: Long = 0,
    val companyId: Long,
    val title: String,
    val description: String,
    val salaryMin: Int,
    val salaryMax: Int,
    val minExperienceYears: Int?,
    val employmentTypeId: Long?,
    val categoryId: Long?,
    val statusId: Long,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### 8. Create VacancyTable

**File**: `src/main/kotlin/database/impl/vacancy/VacancyTable.kt`

```kotlin
package not.djinni.database.impl.vacancy

import not.djinni.database.api.vacancy.VacancyEntity
import not.djinni.database.impl.employer.CompanyTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object VacancyTable : LongIdTable("vacancies", "id") {
    val companyId = reference("company_id", CompanyTable)
    val title = varchar("title", 255)
    val description = text("description")
    val salaryMin = integer("salary_min")
    val salaryMax = integer("salary_max")
    val minExperienceYears = integer("min_experience_years").nullable()
    val employmentTypeId = reference("employment_type_id", EmploymentTypeTable).nullable()
    val categoryId = reference("category_id", JobCategoryTable).nullable()
    val statusId = reference("status_id", VacancyStatusTable)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

class VacancyTableEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<VacancyTableEntity>(VacancyTable)

    var companyId by VacancyTable.companyId
    var title by VacancyTable.title
    var description by VacancyTable.description
    var salaryMin by VacancyTable.salaryMin
    var salaryMax by VacancyTable.salaryMax
    var minExperienceYears by VacancyTable.minExperienceYears
    var employmentTypeId by VacancyTable.employmentTypeId
    var categoryId by VacancyTable.categoryId
    var statusId by VacancyTable.statusId
    var createdAt by VacancyTable.createdAt
    var updatedAt by VacancyTable.updatedAt
}

fun VacancyTableEntity.toEntity() = VacancyEntity(
    id = id.value,
    companyId = companyId.value,
    title = title,
    description = description,
    salaryMin = salaryMin,
    salaryMax = salaryMax,
    minExperienceYears = minExperienceYears,
    employmentTypeId = employmentTypeId?.value,
    categoryId = categoryId?.value,
    statusId = statusId.value,
    createdAt = createdAt,
    updatedAt = updatedAt
)
```

### 9. Create VacancyFilter

**File**: `src/main/kotlin/database/api/vacancy/VacancyFilter.kt`

```kotlin
package not.djinni.database.api.vacancy

enum class VacancySortField {
    CREATED_AT,
    UPDATED_AT,
    SALARY_MIN,
    SALARY_MAX,
    TITLE,
    MIN_EXPERIENCE
}

enum class SortDirection {
    ASC,
    DESC
}

data class VacancyFilter(
    val companyId: Long? = null,
    val categoryId: Long? = null,
    val statusId: Long? = null,
    val employmentTypeId: Long? = null,
    val salaryMin: Int? = null,
    val salaryMax: Int? = null,
    val minExperienceYears: Int? = null,
    val maxExperienceYears: Int? = null,
    val searchQuery: String? = null,
    val sortBy: VacancySortField = VacancySortField.CREATED_AT,
    val sortDirection: SortDirection = SortDirection.DESC
)
```

### 10. Create EmploymentTypeDao Interface

**File**: `src/main/kotlin/database/api/vacancy/EmploymentTypeDao.kt`

```kotlin
package not.djinni.database.api.vacancy

interface EmploymentTypeDao {
    suspend fun createEmploymentType(type: EmploymentTypeEntity): Long
    suspend fun getEmploymentType(id: Long): EmploymentTypeEntity?
    suspend fun getEmploymentTypeByCode(code: String): EmploymentTypeEntity?
    suspend fun getAllEmploymentTypes(): List<EmploymentTypeEntity>
    suspend fun updateEmploymentType(type: EmploymentTypeEntity): Boolean
    suspend fun deleteEmploymentType(id: Long): Boolean
    suspend fun employmentTypeExists(code: String): Boolean
}
```

### 11. Implement DefaultEmploymentTypeDao

**File**: `src/main/kotlin/database/impl/vacancy/DefaultEmploymentTypeDao.kt`

```kotlin
package not.djinni.database.impl.vacancy

import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.vacancy.EmploymentTypeDao
import not.djinni.database.api.vacancy.EmploymentTypeEntity
import org.koin.core.annotation.Single

@Single([EmploymentTypeDao::class])
class DefaultEmploymentTypeDao : EmploymentTypeDao {

    override suspend fun createEmploymentType(type: EmploymentTypeEntity): Long = runQuery {
        EmploymentTypeTableEntity.new {
            typeName = type.typeName
            typeCode = type.typeCode
        }.id.value
    }

    override suspend fun getEmploymentType(id: Long): EmploymentTypeEntity? = runQuery {
        EmploymentTypeTableEntity.findById(id)?.toEntity()
    }

    override suspend fun getEmploymentTypeByCode(code: String): EmploymentTypeEntity? = runQuery {
        EmploymentTypeTableEntity.find { EmploymentTypeTable.typeCode eq code }
            .firstOrNull()
            ?.toEntity()
    }

    override suspend fun getAllEmploymentTypes(): List<EmploymentTypeEntity> = runQuery {
        EmploymentTypeTableEntity.all().map { it.toEntity() }
    }

    override suspend fun updateEmploymentType(type: EmploymentTypeEntity): Boolean = runQuery {
        EmploymentTypeTableEntity.findById(type.id)?.apply {
            typeName = type.typeName
            typeCode = type.typeCode
        } != null
    }

    override suspend fun deleteEmploymentType(id: Long): Boolean = runQuery {
        EmploymentTypeTableEntity.findById(id)?.apply { delete() } != null
    }

    override suspend fun employmentTypeExists(code: String): Boolean = runQuery {
        !EmploymentTypeTableEntity.find { EmploymentTypeTable.typeCode eq code }.empty()
    }
}
```

### 12. Create JobCategoryDao Interface

**File**: `src/main/kotlin/database/api/vacancy/JobCategoryDao.kt`

```kotlin
package not.djinni.database.api.vacancy

interface JobCategoryDao {
    suspend fun createJobCategory(category: JobCategoryEntity): Long
    suspend fun getJobCategory(id: Long): JobCategoryEntity?
    suspend fun getJobCategoryByCode(code: String): JobCategoryEntity?
    suspend fun getAllJobCategories(): List<JobCategoryEntity>
    suspend fun updateJobCategory(category: JobCategoryEntity): Boolean
    suspend fun deleteJobCategory(id: Long): Boolean
    suspend fun categoryExists(code: String): Boolean
}
```

### 13. Implement DefaultJobCategoryDao

**File**: `src/main/kotlin/database/impl/vacancy/DefaultJobCategoryDao.kt`

```kotlin
package not.djinni.database.impl.vacancy

import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.vacancy.JobCategoryDao
import not.djinni.database.api.vacancy.JobCategoryEntity
import org.koin.core.annotation.Single

@Single([JobCategoryDao::class])
class DefaultJobCategoryDao : JobCategoryDao {

    override suspend fun createJobCategory(category: JobCategoryEntity): Long = runQuery {
        JobCategoryTableEntity.new {
            categoryName = category.categoryName
            categoryCode = category.categoryCode
        }.id.value
    }

    override suspend fun getJobCategory(id: Long): JobCategoryEntity? = runQuery {
        JobCategoryTableEntity.findById(id)?.toEntity()
    }

    override suspend fun getJobCategoryByCode(code: String): JobCategoryEntity? = runQuery {
        JobCategoryTableEntity.find { JobCategoryTable.categoryCode eq code }
            .firstOrNull()
            ?.toEntity()
    }

    override suspend fun getAllJobCategories(): List<JobCategoryEntity> = runQuery {
        JobCategoryTableEntity.all().map { it.toEntity() }
    }

    override suspend fun updateJobCategory(category: JobCategoryEntity): Boolean = runQuery {
        JobCategoryTableEntity.findById(category.id)?.apply {
            categoryName = category.categoryName
            categoryCode = category.categoryCode
        } != null
    }

    override suspend fun deleteJobCategory(id: Long): Boolean = runQuery {
        JobCategoryTableEntity.findById(id)?.apply { delete() } != null
    }

    override suspend fun categoryExists(code: String): Boolean = runQuery {
        !JobCategoryTableEntity.find { JobCategoryTable.categoryCode eq code }.empty()
    }
}
```

### 14. Create VacancyStatusDao Interface

**File**: `src/main/kotlin/database/api/vacancy/VacancyStatusDao.kt`

```kotlin
package not.djinni.database.api.vacancy

interface VacancyStatusDao {
    suspend fun createVacancyStatus(status: VacancyStatusEntity): Long
    suspend fun getVacancyStatus(id: Long): VacancyStatusEntity?
    suspend fun getVacancyStatusByCode(code: String): VacancyStatusEntity?
    suspend fun getAllVacancyStatuses(): List<VacancyStatusEntity>
    suspend fun updateVacancyStatus(status: VacancyStatusEntity): Boolean
    suspend fun deleteVacancyStatus(id: Long): Boolean
    suspend fun statusExists(code: String): Boolean
}
```

### 15. Implement DefaultVacancyStatusDao

**File**: `src/main/kotlin/database/impl/vacancy/DefaultVacancyStatusDao.kt`

```kotlin
package not.djinni.database.impl.vacancy

import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.vacancy.VacancyStatusDao
import not.djinni.database.api.vacancy.VacancyStatusEntity
import org.koin.core.annotation.Single

@Single([VacancyStatusDao::class])
class DefaultVacancyStatusDao : VacancyStatusDao {

    override suspend fun createVacancyStatus(status: VacancyStatusEntity): Long = runQuery {
        VacancyStatusTableEntity.new {
            statusName = status.statusName
            statusCode = status.statusCode
        }.id.value
    }

    override suspend fun getVacancyStatus(id: Long): VacancyStatusEntity? = runQuery {
        VacancyStatusTableEntity.findById(id)?.toEntity()
    }

    override suspend fun getVacancyStatusByCode(code: String): VacancyStatusEntity? = runQuery {
        VacancyStatusTableEntity.find { VacancyStatusTable.statusCode eq code }
            .firstOrNull()
            ?.toEntity()
    }

    override suspend fun getAllVacancyStatuses(): List<VacancyStatusEntity> = runQuery {
        VacancyStatusTableEntity.all().map { it.toEntity() }
    }

    override suspend fun updateVacancyStatus(status: VacancyStatusEntity): Boolean = runQuery {
        VacancyStatusTableEntity.findById(status.id)?.apply {
            statusName = status.statusName
            statusCode = status.statusCode
        } != null
    }

    override suspend fun deleteVacancyStatus(id: Long): Boolean = runQuery {
        VacancyStatusTableEntity.findById(id)?.apply { delete() } != null
    }

    override suspend fun statusExists(code: String): Boolean = runQuery {
        !VacancyStatusTableEntity.find { VacancyStatusTable.statusCode eq code }.empty()
    }
}
```

### 16. Create VacancyDao Interface

**File**: `src/main/kotlin/database/api/vacancy/VacancyDao.kt`

```kotlin
package not.djinni.database.api.vacancy

import not.djinni.database.api.employer.CompanyEntity

interface VacancyDao {
    suspend fun createVacancy(vacancy: VacancyEntity): Long
    suspend fun getVacancy(id: Long): VacancyWithDetailsEntity?
    suspend fun updateVacancy(vacancy: VacancyEntity): Boolean
    suspend fun deleteVacancy(id: Long): Boolean

    suspend fun getVacancies(
        filter: VacancyFilter,
        limit: Int = 20,
        offset: Int = 0
    ): List<VacancyEntity>

    suspend fun countVacancies(filter: VacancyFilter): Int

    suspend fun getVacanciesByCompany(
        companyId: Long,
        limit: Int = 20,
        offset: Int = 0
    ): List<VacancyEntity>

    suspend fun getRecentVacancies(limit: Int = 10): List<VacancyEntity>

    suspend fun updateVacancyStatus(id: Long, statusId: Long): Boolean

    suspend fun vacancyExists(id: Long): Boolean
    suspend fun countVacanciesByCompany(companyId: Long): Int
}

data class VacancyWithDetailsEntity(
    val vacancy: VacancyEntity,
    val company: CompanyEntity,
    val employmentType: EmploymentTypeEntity?,
    val category: JobCategoryEntity?,
    val status: VacancyStatusEntity
)
```

### 17. Implement DefaultVacancyDao

**File**: `src/main/kotlin/database/impl/vacancy/DefaultVacancyDao.kt`

```kotlin
package not.djinni.database.impl.vacancy

import kotlinx.datetime.Clock
import not.djinni.database.NotDjinniDatabase.runQuery
import not.djinni.database.api.employer.CompanyEntity
import not.djinni.database.api.vacancy.*
import not.djinni.database.impl.employer.CompanyTable
import not.djinni.database.impl.employer.CompanyTableEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.koin.core.annotation.Single

@Single([VacancyDao::class])
class DefaultVacancyDao : VacancyDao {

    override suspend fun createVacancy(vacancy: VacancyEntity): Long = runQuery {
        VacancyTableEntity.new {
            companyId = EntityID(vacancy.companyId, CompanyTable)
            title = vacancy.title
            description = vacancy.description
            salaryMin = vacancy.salaryMin
            salaryMax = vacancy.salaryMax
            minExperienceYears = vacancy.minExperienceYears
            employmentTypeId = vacancy.employmentTypeId?.let { EntityID(it, EmploymentTypeTable) }
            categoryId = vacancy.categoryId?.let { EntityID(it, JobCategoryTable) }
            statusId = EntityID(vacancy.statusId, VacancyStatusTable)
            createdAt = vacancy.createdAt
            updatedAt = vacancy.updatedAt
        }.id.value
    }

    override suspend fun getVacancy(id: Long): VacancyWithDetailsEntity? = runQuery {
        VacancyTableEntity.findById(id)?.let { vacancy ->
            val company = CompanyTableEntity.findById(vacancy.companyId.value)
                ?: return@runQuery null

            VacancyWithDetailsEntity(
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
                status = VacancyStatusTableEntity.findById(vacancy.statusId.value)?.toEntity()
                    ?: return@runQuery null
            )
        }
    }

    override suspend fun updateVacancy(vacancy: VacancyEntity): Boolean = runQuery {
        VacancyTableEntity.findById(vacancy.id)?.apply {
            title = vacancy.title
            description = vacancy.description
            salaryMin = vacancy.salaryMin
            salaryMax = vacancy.salaryMax
            minExperienceYears = vacancy.minExperienceYears
            employmentTypeId = vacancy.employmentTypeId?.let { EntityID(it, EmploymentTypeTable) }
            categoryId = vacancy.categoryId?.let { EntityID(it, JobCategoryTable) }
            statusId = EntityID(vacancy.statusId, VacancyStatusTable)
            updatedAt = Clock.System.now()
        } != null
    }

    override suspend fun deleteVacancy(id: Long): Boolean = runQuery {
        VacancyTableEntity.findById(id)?.apply { delete() } != null
    }

    override suspend fun getVacancies(
        filter: VacancyFilter,
        limit: Int,
        offset: Int
    ): List<VacancyEntity> = runQuery {
        val query = buildVacancyQuery(filter)

        query
            .limit(limit, offset.toLong())
            .map { VacancyTableEntity.wrapRow(it).toEntity() }
    }

    override suspend fun countVacancies(filter: VacancyFilter): Int = runQuery {
        val query = buildVacancyQuery(filter)
        query.count().toInt()
    }

    override suspend fun getVacanciesByCompany(
        companyId: Long,
        limit: Int,
        offset: Int
    ): List<VacancyEntity> = runQuery {
        VacancyTableEntity.find { VacancyTable.companyId eq EntityID(companyId, CompanyTable) }
            .orderBy(VacancyTable.createdAt to SortOrder.DESC)
            .limit(limit, offset.toLong())
            .map { it.toEntity() }
    }

    override suspend fun getRecentVacancies(limit: Int): List<VacancyEntity> = runQuery {
        VacancyTableEntity.all()
            .orderBy(VacancyTable.createdAt to SortOrder.DESC)
            .limit(limit)
            .map { it.toEntity() }
    }

    override suspend fun updateVacancyStatus(id: Long, statusId: Long): Boolean = runQuery {
        VacancyTableEntity.findById(id)?.apply {
            this.statusId = EntityID(statusId, VacancyStatusTable)
            updatedAt = Clock.System.now()
        } != null
    }

    override suspend fun vacancyExists(id: Long): Boolean = runQuery {
        VacancyTableEntity.findById(id) != null
    }

    override suspend fun countVacanciesByCompany(companyId: Long): Int = runQuery {
        VacancyTableEntity.find { VacancyTable.companyId eq EntityID(companyId, CompanyTable) }
            .count()
            .toInt()
    }

    private fun buildVacancyQuery(filter: VacancyFilter): Query {
        var query: Query = VacancyTable.selectAll()

        filter.companyId?.let {
            query = query.andWhere { VacancyTable.companyId eq EntityID(it, CompanyTable) }
        }

        filter.categoryId?.let {
            query = query.andWhere { VacancyTable.categoryId eq EntityID(it, JobCategoryTable) }
        }

        filter.statusId?.let {
            query = query.andWhere { VacancyTable.statusId eq EntityID(it, VacancyStatusTable) }
        }

        filter.employmentTypeId?.let {
            query = query.andWhere { VacancyTable.employmentTypeId eq EntityID(it, EmploymentTypeTable) }
        }

        filter.salaryMin?.let {
            query = query.andWhere { VacancyTable.salaryMax greaterEq it }
        }

        filter.salaryMax?.let {
            query = query.andWhere { VacancyTable.salaryMin lessEq it }
        }

        filter.minExperienceYears?.let {
            query = query.andWhere { VacancyTable.minExperienceYears greaterEq it }
        }

        filter.maxExperienceYears?.let {
            query = query.andWhere { VacancyTable.minExperienceYears lessEq it }
        }

        filter.searchQuery?.let { searchQuery ->
            query = query.andWhere {
                (VacancyTable.title like "%$searchQuery%") or
                (VacancyTable.description like "%$searchQuery%")
            }
        }

        val sortOrder = when (filter.sortDirection) {
            SortDirection.ASC -> SortOrder.ASC
            SortDirection.DESC -> SortOrder.DESC
        }

        val sortColumn = when (filter.sortBy) {
            VacancySortField.CREATED_AT -> VacancyTable.createdAt
            VacancySortField.UPDATED_AT -> VacancyTable.updatedAt
            VacancySortField.SALARY_MIN -> VacancyTable.salaryMin
            VacancySortField.SALARY_MAX -> VacancyTable.salaryMax
            VacancySortField.TITLE -> VacancyTable.title
            VacancySortField.MIN_EXPERIENCE -> VacancyTable.minExperienceYears
        }

        return query.orderBy(sortColumn to sortOrder)
    }
}
```

### 18. Create Mappers

**File**: `src/main/kotlin/data/mapper/EmploymentType.kt`

```kotlin
package not.djinni.data.mapper

import not.djinni.database.api.vacancy.EmploymentTypeEntity
import not.djinni.model.vacancy.EmploymentType

fun EmploymentTypeEntity.toDomain() = EmploymentType(
    id = id,
    name = typeName,
    code = typeCode
)

fun EmploymentType.toEntity() = EmploymentTypeEntity(
    id = id,
    typeName = name,
    typeCode = code
)
```

**File**: `src/main/kotlin/data/mapper/JobCategory.kt`

```kotlin
package not.djinni.data.mapper

import not.djinni.database.api.vacancy.JobCategoryEntity
import not.djinni.model.vacancy.JobCategory

fun JobCategoryEntity.toDomain() = JobCategory(
    id = id,
    name = categoryName,
    code = categoryCode
)

fun JobCategory.toEntity() = JobCategoryEntity(
    id = id,
    categoryName = name,
    categoryCode = code
)
```

**File**: `src/main/kotlin/data/mapper/VacancyStatus.kt`

```kotlin
package not.djinni.data.mapper

import not.djinni.database.api.vacancy.VacancyStatusEntity
import not.djinni.model.vacancy.VacancyStatus

fun VacancyStatusEntity.toDomain() = VacancyStatus(
    id = id,
    name = statusName,
    code = statusCode
)

fun VacancyStatus.toEntity() = VacancyStatusEntity(
    id = id,
    statusName = name,
    statusCode = code
)
```

**File**: `src/main/kotlin/data/mapper/Vacancy.kt`

```kotlin
package not.djinni.data.mapper

import not.djinni.database.api.vacancy.VacancyEntity
import not.djinni.database.api.vacancy.VacancyWithDetailsEntity
import not.djinni.model.vacancy.Salary
import not.djinni.model.vacancy.Vacancy
import not.djinni.model.vacancy.VacancyWithDetails

fun VacancyEntity.toDomain() = Vacancy(
    id = id,
    companyId = companyId,
    title = title,
    description = description,
    salary = Salary(min = salaryMin, max = salaryMax),
    minExperienceYears = minExperienceYears,
    employmentTypeId = employmentTypeId,
    categoryId = categoryId,
    statusId = statusId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Vacancy.toEntity() = VacancyEntity(
    id = id,
    companyId = companyId,
    title = title,
    description = description,
    salaryMin = salary.min,
    salaryMax = salary.max,
    minExperienceYears = minExperienceYears,
    employmentTypeId = employmentTypeId,
    categoryId = categoryId,
    statusId = statusId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun VacancyWithDetailsEntity.toDomain() = VacancyWithDetails(
    id = vacancy.id,
    company = company.toDomain(),
    title = vacancy.title,
    description = vacancy.description,
    salary = Salary(min = vacancy.salaryMin, max = vacancy.salaryMax),
    minExperienceYears = vacancy.minExperienceYears,
    employmentType = employmentType?.toDomain(),
    category = category?.toDomain(),
    status = status.toDomain(),
    createdAt = vacancy.createdAt,
    updatedAt = vacancy.updatedAt
)
```

### 19. Register Tables in NotDjinniDatabase

**File**: `src/main/kotlin/database/NotDjinniDatabase.kt`

**Changes**:
1. Import all new tables:
   ```kotlin
   import not.djinni.database.impl.vacancy.EmploymentTypeTable
   import not.djinni.database.impl.vacancy.JobCategoryTable
   import not.djinni.database.impl.vacancy.VacancyStatusTable
   import not.djinni.database.impl.vacancy.VacancyTable
   ```

2. Update `SchemaUtils.create()` call in `init()`:
   ```kotlin
   fun init() {
       transaction(database) {
           SchemaUtils.create(
               UserTable,
               CompanyTable,
               EmployerProfileTable,
               SeekerProfileTable,
               EmploymentTypeTable,
               JobCategoryTable,
               VacancyStatusTable,
               VacancyTable
           )
           addLogger(StdOutSqlLogger)
       }
   }
   ```

**Order matters**:
- EmploymentTypeTable, JobCategoryTable, VacancyStatusTable must be created before VacancyTable
- CompanyTable must be created before VacancyTable

### 20. Seed Initial Data

**File**: `src/main/kotlin/database/seed/VacancySeedData.kt`

```kotlin
package not.djinni.database.seed

import not.djinni.database.api.vacancy.*
import org.koin.core.annotation.Single

@Single
class VacancySeedData(
    private val employmentTypeDao: EmploymentTypeDao,
    private val jobCategoryDao: JobCategoryDao,
    private val vacancyStatusDao: VacancyStatusDao
) {
    suspend fun seedData() {
        seedEmploymentTypes()
        seedJobCategories()
        seedVacancyStatuses()
    }

    private suspend fun seedEmploymentTypes() {
        val types = listOf(
            EmploymentTypeEntity(typeName = "Full-time", typeCode = "FULL_TIME"),
            EmploymentTypeEntity(typeName = "Part-time", typeCode = "PART_TIME"),
            EmploymentTypeEntity(typeName = "Contract", typeCode = "CONTRACT"),
            EmploymentTypeEntity(typeName = "Temporary", typeCode = "TEMPORARY"),
            EmploymentTypeEntity(typeName = "Internship", typeCode = "INTERNSHIP"),
            EmploymentTypeEntity(typeName = "Freelance", typeCode = "FREELANCE")
        )

        types.forEach { type ->
            if (!employmentTypeDao.employmentTypeExists(type.typeCode)) {
                employmentTypeDao.createEmploymentType(type)
            }
        }
    }

    private suspend fun seedJobCategories() {
        val categories = listOf(
            JobCategoryEntity(categoryName = "Software Development", categoryCode = "SOFTWARE_DEV"),
            JobCategoryEntity(categoryName = "Data Science", categoryCode = "DATA_SCIENCE"),
            JobCategoryEntity(categoryName = "DevOps", categoryCode = "DEVOPS"),
            JobCategoryEntity(categoryName = "Quality Assurance", categoryCode = "QA"),
            JobCategoryEntity(categoryName = "Product Management", categoryCode = "PRODUCT_MGMT"),
            JobCategoryEntity(categoryName = "Design", categoryCode = "DESIGN"),
            JobCategoryEntity(categoryName = "Marketing", categoryCode = "MARKETING"),
            JobCategoryEntity(categoryName = "Sales", categoryCode = "SALES"),
            JobCategoryEntity(categoryName = "Human Resources", categoryCode = "HR"),
            JobCategoryEntity(categoryName = "Finance", categoryCode = "FINANCE"),
            JobCategoryEntity(categoryName = "Operations", categoryCode = "OPERATIONS"),
            JobCategoryEntity(categoryName = "Customer Support", categoryCode = "SUPPORT")
        )

        categories.forEach { category ->
            if (!jobCategoryDao.categoryExists(category.categoryCode)) {
                jobCategoryDao.createJobCategory(category)
            }
        }
    }

    private suspend fun seedVacancyStatuses() {
        val statuses = listOf(
            VacancyStatusEntity(statusName = "Active", statusCode = "ACTIVE"),
            VacancyStatusEntity(statusName = "Closed", statusCode = "CLOSED"),
            VacancyStatusEntity(statusName = "Draft", statusCode = "DRAFT"),
            VacancyStatusEntity(statusName = "Paused", statusCode = "PAUSED"),
            VacancyStatusEntity(statusName = "Expired", statusCode = "EXPIRED")
        )

        statuses.forEach { status ->
            if (!vacancyStatusDao.statusExists(status.statusCode)) {
                vacancyStatusDao.createVacancyStatus(status)
            }
        }
    }
}
```

**Usage**: Call `seedData()` after database initialization in `NotDjinniDatabase.init()` or application startup.

## File Structure

```
src/main/kotlin/
├── database/
│   ├── api/
│   │   └── vacancy/
│   │       ├── EmploymentTypeEntity.kt
│   │       ├── EmploymentTypeDao.kt
│   │       ├── JobCategoryEntity.kt
│   │       ├── JobCategoryDao.kt
│   │       ├── VacancyStatusEntity.kt
│   │       ├── VacancyStatusDao.kt
│   │       ├── VacancyEntity.kt
│   │       ├── VacancyDao.kt
│   │       └── VacancyFilter.kt
│   ├── impl/
│   │   └── vacancy/
│   │       ├── EmploymentTypeTable.kt
│   │       ├── DefaultEmploymentTypeDao.kt
│   │       ├── JobCategoryTable.kt
│   │       ├── DefaultJobCategoryDao.kt
│   │       ├── VacancyStatusTable.kt
│   │       ├── DefaultVacancyStatusDao.kt
│   │       ├── VacancyTable.kt
│   │       └── DefaultVacancyDao.kt
│   ├── seed/
│   │   └── VacancySeedData.kt
│   └── NotDjinniDatabase.kt (modified)
├── data/
│   └── mapper/
│       ├── EmploymentType.kt
│       ├── JobCategory.kt
│       ├── VacancyStatus.kt
│       └── Vacancy.kt
└── model/
    └── vacancy/
        ├── EmploymentType.kt
        ├── JobCategory.kt
        ├── VacancyStatus.kt
        ├── Vacancy.kt
        └── VacancyWithDetails.kt
```

## Validation Rules

### VacancyDao
- `salary.min` <= `salary.max`
- `minExperienceYears` >= 0 (if provided)
- `title`: not empty, max 255 chars
- `description`: not empty
- Company must exist (foreign key)
- Status must exist (foreign key)
- Category must exist if provided (foreign key)
- EmploymentType must exist if provided (foreign key)

### Reference Table DAOs
- `code`: unique, not empty
- `name`: unique, not empty
- Max lengths enforced by schema

## Error Handling

### VacancyDao
- Foreign key violation on `company_id` (company doesn't exist)
- Foreign key violation on `status_id` (status doesn't exist)
- Foreign key violation on `category_id` (category doesn't exist)
- Foreign key violation on `employment_type_id` (type doesn't exist)

### Reference Table DAOs
- Unique constraint violation on `code`
- Unique constraint violation on `name`

## Index Performance

- PostgreSQL auto-indexes PRIMARY KEY columns
- PostgreSQL auto-indexes UNIQUE columns (code, name fields)
- Manual indexes on foreign keys optimize JOINs
- Index on `created_at` optimizes sorting by date
- Indexes on `category_id`, `status_id`, `employment_type_id` optimize filtering

## Foreign Key Relationships

### Vacancies → Companies
- ON DELETE CASCADE: company deletion → vacancies deletion
- Many vacancies per company

### Vacancies → EmploymentTypes
- ON DELETE SET NULL: type deletion → vacancy.employment_type_id = NULL
- Optional relationship

### Vacancies → JobCategories
- ON DELETE SET NULL: category deletion → vacancy.category_id = NULL
- Optional relationship

### Vacancies → VacancyStatuses
- ON DELETE SET NULL: status deletion → vacancy.status_id = NULL
- Required relationship but SET NULL for safety

## Filtering & Sorting

### VacancyFilter Options

**Filters**:
- `companyId`: Filter by specific company
- `categoryId`: Filter by job category
- `statusId`: Filter by vacancy status (Active, Closed, etc.)
- `employmentTypeId`: Filter by employment type
- `salaryMin`: Filter vacancies with max salary >= this value
- `salaryMax`: Filter vacancies with min salary <= this value
- `minExperienceYears`: Filter by minimum experience required
- `maxExperienceYears`: Filter by maximum experience required
- `searchQuery`: Text search in title and description (LIKE query)

**Sorting**:
- `sortBy`: Field to sort by (CREATED_AT, UPDATED_AT, SALARY_MIN, SALARY_MAX, TITLE, MIN_EXPERIENCE)
- `sortDirection`: ASC or DESC
- Default: `CREATED_AT DESC` (newest first)

### Query Examples

**Homepage - Latest jobs**:
```kotlin
getVacancies(
    filter = VacancyFilter(
        statusId = activeStatusId,
        sortBy = VacancySortField.CREATED_AT,
        sortDirection = SortDirection.DESC
    ),
    limit = 20
)
```

**Search - High paying software jobs**:
```kotlin
getVacancies(
    filter = VacancyFilter(
        categoryId = softwareDevCategoryId,
        salaryMin = 100000,
        statusId = activeStatusId,
        sortBy = VacancySortField.SALARY_MAX,
        sortDirection = SortDirection.DESC
    )
)
```

**Entry-level jobs**:
```kotlin
getVacancies(
    filter = VacancyFilter(
        maxExperienceYears = 2,
        statusId = activeStatusId,
        sortBy = VacancySortField.MIN_EXPERIENCE,
        sortDirection = SortDirection.ASC
    )
)
```

## Seed Data

### EmploymentTypes
- Full-time (FULL_TIME)
- Part-time (PART_TIME)
- Contract (CONTRACT)
- Temporary (TEMPORARY)
- Internship (INTERNSHIP)
- Freelance (FREELANCE)

### JobCategories
- Software Development (SOFTWARE_DEV)
- Data Science (DATA_SCIENCE)
- DevOps (DEVOPS)
- Quality Assurance (QA)
- Product Management (PRODUCT_MGMT)
- Design (DESIGN)
- Marketing (MARKETING)
- Sales (SALES)
- Human Resources (HR)
- Finance (FINANCE)
- Operations (OPERATIONS)
- Customer Support (SUPPORT)

### VacancyStatuses
- Active (ACTIVE) - Accepting applications
- Closed (CLOSED) - No longer accepting applications
- Draft (DRAFT) - Not yet published
- Paused (PAUSED) - Temporarily not accepting applications
- Expired (EXPIRED) - Automatically closed after time period

## Additional DAO Functions

### VacancyDao
- `getRecentVacancies(limit)`: Get newest vacancies (for homepage)
- `updateVacancyStatus(id, statusId)`: Quick status change without full update
- `countVacanciesByCompany(companyId)`: Count for company dashboard
- `getVacanciesByCompany(companyId)`: List company's vacancies with pagination

### Lookup DAOs
- Full CRUD operations (create, read, update, delete)
- `getByCode(code)`: Lookup by machine-readable code
- `getAll()`: Get all entries (small lists, no pagination needed)
- `exists(code)`: Check existence before creation

## Future Enhancements

### Vacancies
- Location field (city, country, remote)
- Application deadline
- Number of positions available
- Benefits/perks list
- Skills/requirements tags
- Application count
- View count (analytics)
- Bookmark/favorite functionality
- Email notifications for new matches
- Salary currency field (multi-currency support)

### Search & Filtering
- Full-text search with relevance ranking
- Location-based search with radius
- Salary range presets (e.g., $50k-$80k, $80k-$120k)
- Multiple category selection
- Skills/tags filtering
- Date range filters (posted in last 7/30/90 days)

### Analytics
- View count per vacancy
- Application count per vacancy
- Most popular categories
- Average salary by category
- Time to fill metrics

### Permissions
- Only company employees can edit their company's vacancies
- Admin can moderate all vacancies
- Public can only view Active status vacancies

## Dependencies

All required dependencies already exist in the project:
- Exposed ORM (with datetime support for timestamps)
- PostgreSQL JDBC driver
- Koin for dependency injection
- Kotlin Coroutines
- kotlinx-datetime for Instant type

## Testing Checklist

### EmploymentType Tests
- [ ] Create employment type with valid data
- [ ] Create with duplicate code (should fail)
- [ ] Create with duplicate name (should fail)
- [ ] Get employment type by ID
- [ ] Get employment type by code
- [ ] Get all employment types
- [ ] Update employment type
- [ ] Delete employment type (SET NULL on vacancies)
- [ ] Check exists by code

### JobCategory Tests
- [ ] Create job category with valid data
- [ ] Create with duplicate code (should fail)
- [ ] Create with duplicate name (should fail)
- [ ] Get category by ID
- [ ] Get category by code
- [ ] Get all categories
- [ ] Update category
- [ ] Delete category (SET NULL on vacancies)
- [ ] Check exists by code

### VacancyStatus Tests
- [ ] Create vacancy status with valid data
- [ ] Create with duplicate code (should fail)
- [ ] Create with duplicate name (should fail)
- [ ] Get status by ID
- [ ] Get status by code
- [ ] Get all statuses
- [ ] Update status
- [ ] Delete status (SET NULL on vacancies)
- [ ] Check exists by code

### Vacancy Tests
- [ ] Create vacancy with valid data
- [ ] Create with salary_min > salary_max (should validate)
- [ ] Create with negative experience (should fail CHECK constraint)
- [ ] Create with non-existent company (should fail FK)
- [ ] Create with non-existent status (should fail FK)
- [ ] Get vacancy by ID (verify JOINs)
- [ ] Update vacancy
- [ ] Update vacancy status
- [ ] Delete vacancy
- [ ] Verify CASCADE DELETE from company
- [ ] Verify SET NULL from category deletion
- [ ] Check vacancy exists

### Filtering Tests
- [ ] Filter by company ID
- [ ] Filter by category ID
- [ ] Filter by status ID
- [ ] Filter by employment type ID
- [ ] Filter by salary range
- [ ] Filter by experience range
- [ ] Text search in title
- [ ] Text search in description
- [ ] Combined filters
- [ ] Empty filter (all vacancies)

### Sorting Tests
- [ ] Sort by created_at ASC
- [ ] Sort by created_at DESC (default)
- [ ] Sort by salary_min ASC
- [ ] Sort by salary_max DESC
- [ ] Sort by title ASC
- [ ] Sort by min_experience ASC
- [ ] Pagination with consistent sorting

### Pagination Tests
- [ ] Get first page (offset 0)
- [ ] Get second page (offset 20)
- [ ] Count vacancies matches filter
- [ ] Pagination maintains sort order

### Seed Data Tests
- [ ] Seed employment types (idempotent)
- [ ] Seed job categories (idempotent)
- [ ] Seed vacancy statuses (idempotent)
- [ ] Verify all seed data created
- [ ] Run seed multiple times (no duplicates)

### Integration Tests
- [ ] Create company → create vacancy → verify relationship
- [ ] Seed data → create vacancy → filter by category
- [ ] Create multiple vacancies → test sorting
- [ ] Search with multiple filters → verify results
- [ ] Delete company → verify CASCADE on vacancies
- [ ] Delete category → verify SET NULL on vacancies
- [ ] Full flow with VacancyWithDetails

## Implementation Order

1. **Step 1**: Create all domain models (EmploymentType, JobCategory, VacancyStatus, Vacancy, VacancyWithDetails, Salary)
2. **Step 2**: Create all entity classes (EmploymentTypeEntity, JobCategoryEntity, VacancyStatusEntity, VacancyEntity)
3. **Step 3**: Create all table objects (EmploymentTypeTable, JobCategoryTable, VacancyStatusTable, VacancyTable)
4. **Step 4**: Create VacancyFilter with enums
5. **Step 5**: Create DAO interfaces (EmploymentTypeDao, JobCategoryDao, VacancyStatusDao, VacancyDao)
6. **Step 6**: Implement lookup table DAOs (DefaultEmploymentTypeDao, DefaultJobCategoryDao, DefaultVacancyStatusDao)
7. **Step 7**: Implement DefaultVacancyDao with filtering and sorting
8. **Step 8**: Create all mappers
9. **Step 9**: Register tables in NotDjinniDatabase
10. **Step 10**: Create VacancySeedData
11. **Step 11**: Test lookup tables
12. **Step 12**: Test vacancy CRUD
13. **Step 13**: Test filtering and sorting
14. **Step 14**: Test pagination
15. **Step 15**: Test CASCADE DELETE and SET NULL
16. **Step 16**: Test seed data
17. **Step 17**: Integration tests

**Rationale**: Build from bottom-up (lookup tables first, then vacancies), test incrementally

## Summary

This plan adds Vacancies and supporting lookup tables with the following key points:

1. **Tables**: EmploymentTypes, JobCategories, VacancyStatuses (lookup), Vacancies (main)
2. **IDs**: All BIGSERIAL for auto-incrementing Long IDs (consistent with Users table)
3. **Salary**: INT (whole dollars), NOT NULL, min and max as separate fields
4. **Timestamps**: created_at and updated_at for sorting and display
5. **Filtering**: VacancyFilter object with 9 filter options + sorting
6. **Sorting**: 6 sort fields (created_at, updated_at, salary_min, salary_max, title, experience)
7. **Default Sort**: created_at DESC (newest first)
8. **Pagination**: All list methods support limit/offset
9. **Relationships**: CASCADE DELETE from company, SET NULL from lookup tables
10. **Seed Data**: 6 employment types, 12 categories, 5 statuses
11. **Domain Models**: Vacancy (base with IDs), VacancyWithDetails (rich with objects)
12. **Full CRUD**: All DAOs support create, read, update, delete
13. **Special Queries**: getRecent, getByCompany, updateStatus, count
14. **File Count**: 25 new files (5 domain models, 4 entities, 4 tables, 4 DAOs, 4 DAO impls, 4 mappers, 1 filter, 1 seed)
15. **Performance**: Long IDs provide 2x better performance vs UUID (smaller indexes, faster JOINs)
