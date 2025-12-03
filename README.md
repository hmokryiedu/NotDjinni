# NotDjinni

A REST API backend for a job marketplace platform connecting job seekers with employers. Built with Kotlin and Ktor, NotDjinni provides comprehensive user management, profile creation, and company/employer relationship handling with JWT-based authentication.

---

## 📋 Table of Contents

- [Tech Stack](#-tech-stack)
- [Features & Capabilities](#-features--capabilities)
- [API Endpoints Reference](#-api-endpoints-reference)
- [Project Structure](#-project-structure)
- [Quick Start](#-quick-start)

---

## 🛠 Tech Stack

- **Language**: Kotlin 1.9+
- **Framework**: Ktor (async web framework)
- **Database**: PostgreSQL
- **ORM**: Exposed (JetBrains)
- **Authentication**: JWT with RS256 algorithm
- **Dependency Injection**: Koin with KSP annotation processing
- **Serialization**: kotlinx.serialization
- **Architecture**: Clean Architecture (4-layer separation)
- **Build Tool**: Gradle

---

## ✨ Features & Capabilities

### 🔐 Authentication & Authorization

- **User Registration**: Email/password-based registration
- **JWT Authentication**: Secure token-based authentication using RS256 algorithm
- **Token Refresh**: Refresh token mechanism for maintaining sessions
- **Protected Routes**: Role-based access control for sensitive endpoints

### 👤 User Management

- **User Profiles**: Basic user information management
- **Account Operations**: View and update user details
- **Role Support**: Differentiation between job seekers and employers

### 💼 Job Seeker Profiles

- **Profile Creation**: Job seekers can create detailed profiles with:
  - Specialty/area of expertise
  - Years of experience
  - Desired salary
  - About me section
  - Job category (software dev, data science, DevOps, etc.)
- **Work Experience**: Complete work history management
  - Company name, position, description
  - Start/end dates with current job support
  - Full CRUD operations on individual experiences

### 🏢 Company Management

- **Company Registry**: Public CRUD operations for companies
  - Create, read, update, delete companies
  - Search companies by name
  - List all available companies
- **Company Information**: Name, website, description
- **No Authentication Required**: Open endpoints for easy data seeding and access

### 👔 Employer Profiles

- **Company-Linked Profiles**: Employers associate with existing companies
- **Role Management**: Specify role/position within the company
- **Profile Operations**: Create, view, update, and delete employer profiles
- **Authentication Required**: All operations protected by JWT
- **Immutable Company Association**: Once created, employer cannot switch companies (only role is mutable)

### 📢 Vacancy Management

- **Job Postings**: Employers can create and manage job vacancies
  - Vacancy linked to employer's company
  - Title, description, salary range
  - Employment type (full-time, part-time, contract, etc.)
  - Job category (software dev, data science, DevOps, etc.)
  - Experience requirements
- **Vacancy Lifecycle**: Draft, active, paused, closed, expired status management
- **Advanced Search & Filtering**:
  - Filter by categories, employment types, statuses
  - Salary range filtering
  - Experience level filtering
  - Text search in title and description
  - Sorting by date, salary, experience
- **Public Discovery**: Anyone can search and view active vacancies
- **Authorization**: Only employers from the same company can modify/delete vacancies
- **Employer Dashboard**: View all vacancies for your company

### 📝 Job Applications

- **Application Submission**: Job seekers can apply to vacancies with optional cover letters
- **Application Tracking**: Complete application lifecycle management
  - Status progression: Applied → Reviewing → Interview → Test Task → Offer → Hired
  - Job seekers track all their applications
  - Employers view all applications for their vacancies
- **Duplicate Prevention**: Unique constraint prevents duplicate applications to the same vacancy
- **Authorization Controls**:
  - Job seekers: Full CRUD on their own applications
  - Employers: View applications and update status for their company's vacancies
- **Application Statuses**: APPLIED, REVIEWING, INTERVIEW, TEST_TASK, OFFER, HIRED, REJECTED, WITHDRAWN
- **Cascade Deletion**: Applications automatically deleted when vacancy or seeker profile is removed

---

## 🔌 API Endpoints Reference

### Authentication Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/auth/register` | No | Register a new user |
| `POST` | `/auth/login` | No | Login and receive JWT tokens |
| `POST` | `/auth/refresh` | No | Refresh access token using refresh token |

**Example - Register User:**
```json
POST /auth/register
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "john.doe@example.com"
  }
}
```

---

### User Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/user/me` | JWT | Get current user profile |

---

### Job Seeker Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/seeker/profile` | JWT | Get job seeker profile |
| `POST` | `/seeker/profile` | JWT | Create job seeker profile |
| `PUT` | `/seeker/profile` | JWT | Update job seeker profile |
| `DELETE` | `/seeker/profile` | JWT | Delete job seeker profile |
| `POST` | `/seeker/profile/experience` | JWT | Add work experience |
| `PUT` | `/seeker/profile/experience/{id}` | JWT | Update work experience |
| `DELETE` | `/seeker/profile/experience/{id}` | JWT | Delete work experience |

**Example - Create Job Seeker Profile:**
```json
POST /seeker/profile
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "speciality": "Full Stack Developer",
  "experience_years": 5,
  "desired_salary": 120000,
  "about_me": "Passionate developer with expertise in Kotlin and modern web technologies",
  "job_category": "SOFTWARE_DEV",
  "work_experience": [
    {
      "company_name": "Tech Corp",
      "position": "Senior Developer",
      "description": "Led development of microservices architecture",
      "start_date": "2020-01-15",
      "end_date": "2023-06-30"
    },
    {
      "company_name": "Startup Inc",
      "position": "Full Stack Developer",
      "description": "Building innovative solutions",
      "start_date": "2023-07-01",
      "end_date": null
    }
  ]
}
```

**Response:**
```json
{
  "id": 1,
  "specialty": "Full Stack Developer",
  "experience_years": 5,
  "desired_salary": 120000,
  "about_me": "Passionate developer with expertise in Kotlin and modern web technologies",
  "job_category": "SOFTWARE_DEV",
  "work_experience": [
    {
      "id": 1,
      "company_name": "Tech Corp",
      "position": "Senior Developer",
      "description": "Led development of microservices architecture",
      "start_date": "2020-01-15",
      "end_date": "2023-06-30",
      "is_current": false
    },
    {
      "id": 2,
      "company_name": "Startup Inc",
      "position": "Full Stack Developer",
      "description": "Building innovative solutions",
      "start_date": "2023-07-01",
      "end_date": null,
      "is_current": true
    }
  ]
}
```

---

### Company Endpoints (Public - No Auth)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/company` | No | List all companies |
| `GET` | `/company/{id}` | No | Get company by ID |
| `GET` | `/company/search?name={query}` | No | Search companies by name |
| `POST` | `/company` | No | Create a new company |
| `PUT` | `/company/{id}` | No | Update company details |
| `DELETE` | `/company/{id}` | No | Delete company |

**Example - Create Company:**
```json
POST /company
Content-Type: application/json

{
  "company_name": "Innovative Solutions Ltd",
  "website": "https://innovative-solutions.com",
  "description": "Leading provider of enterprise software solutions specializing in cloud infrastructure and AI-powered analytics."
}
```

**Response:**
```json
{
  "id": 1,
  "company_name": "Innovative Solutions Ltd",
  "website": "https://innovative-solutions.com",
  "description": "Leading provider of enterprise software solutions specializing in cloud infrastructure and AI-powered analytics."
}
```

**Example - Search Companies:**
```json
GET /company/search?name=Innovative

Response:
[
  {
    "id": 1,
    "company_name": "Innovative Solutions Ltd",
    "website": "https://innovative-solutions.com",
    "description": "Leading provider of enterprise software solutions..."
  },
  {
    "id": 5,
    "company_name": "Innovative Tech Corp",
    "website": "https://innovativetech.io",
    "description": "Cutting-edge technology solutions..."
  }
]
```

---

### Employer Profile Endpoints (JWT Auth Required)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/employer/profile` | JWT | Get employer profile |
| `POST` | `/employer/profile` | JWT | Create employer profile |
| `PUT` | `/employer/profile` | JWT | Update employer role (company_id immutable) |
| `DELETE` | `/employer/profile` | JWT | Delete employer profile |

**Example - Create Employer Profile:**
```json
POST /employer/profile
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "company_id": 1,
  "role": "HR Manager"
}
```

**Response:**
```json
{
  "id": 1,
  "role": "HR Manager",
  "company": {
    "id": 1,
    "company_name": "Innovative Solutions Ltd",
    "website": "https://innovative-solutions.com",
    "description": "Leading provider of enterprise software solutions..."
  }
}
```

**Example - Update Employer Profile:**
```json
PUT /employer/profile
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "role": "Senior HR Manager"
}
```

**Note**: The `company_id` is immutable after profile creation. Only the `role` field can be updated.

---

### Vacancy Endpoints

#### Public Endpoints (No Auth)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/vacancy` | No | List/search vacancies with filters |
| `GET` | `/vacancy/{id}` | No | Get vacancy details with company info |
| `GET` | `/vacancy/recent?limit=10` | No | Get recently posted vacancies |
| `GET` | `/company/{id}/vacancies` | No | Get all vacancies for a company |

#### Protected Endpoints (JWT + Employer Profile Required)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/vacancy` | JWT | Create vacancy (auto-linked to employer's company) |
| `PUT` | `/vacancy/{id}` | JWT | Update vacancy (ownership check) |
| `DELETE` | `/vacancy/{id}` | JWT | Delete vacancy (ownership check) |
| `PUT` | `/vacancy/{id}/status` | JWT | Quick status update |
| `GET` | `/employer/vacancies` | JWT | Get employer's company vacancies |

**Example - Create Vacancy:**
```json
POST /vacancy
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "title": "Senior Backend Developer",
  "description": "We are seeking an experienced backend developer...",
  "salary_min": 80000,
  "salary_max": 120000,
  "min_experience_years": 5,
  "employment_type": "FULL_TIME",
  "category": "SOFTWARE_DEV",
  "status": "ACTIVE"
}
```

**Response:**
```json
{
  "id": 1,
  "company_id": 1,
  "title": "Senior Backend Developer",
  "description": "We are seeking an experienced backend developer...",
  "salary_min": 80000,
  "salary_max": 120000,
  "min_experience_years": 5,
  "employment_type": "FULL_TIME",
  "category": "SOFTWARE_DEV",
  "status": "ACTIVE",
  "created_at": "2025-01-15T10:30:00Z",
  "updated_at": "2025-01-15T10:30:00Z"
}
```

**Example - Search Vacancies with Filters:**
```json
GET /vacancy?category=SOFTWARE_DEV&category=DATA_SCIENCE&employment_type=FULL_TIME&salary_min=70000&search=backend&sort_by=salary_max&sort_direction=desc&limit=20&offset=0

Response:
[
  {
    "id": 1,
    "company": {
      "id": 1,
      "company_name": "Tech Corp",
      "website": "https://techcorp.com",
      "description": "Leading technology company"
    },
    "title": "Senior Backend Developer",
    "description": "We are seeking an experienced backend developer...",
    "salary_min": 80000,
    "salary_max": 120000,
    "min_experience_years": 5,
    "employment_type": "FULL_TIME",
    "category": "SOFTWARE_DEV",
    "status": "ACTIVE",
    "created_at": "2025-01-15T10:30:00Z",
    "updated_at": "2025-01-15T10:30:00Z"
  }
]
```

**Available Enum Values:**

Employment Types: `FULL_TIME`, `PART_TIME`, `CONTRACT`, `TEMPORARY`, `INTERNSHIP`, `FREELANCE`

Job Categories: `SOFTWARE_DEV`, `DATA_SCIENCE`, `DEVOPS`, `QA`, `PRODUCT_MGMT`, `DESIGN`, `MARKETING`, `SALES`, `HR`, `FINANCE`, `OPERATIONS`, `SUPPORT`

Vacancy Statuses: `DRAFT`, `ACTIVE`, `PAUSED`, `CLOSED`, `EXPIRED`

**Filter Parameters:**
- `category`: Job category (can be repeated for multiple values)
- `status`: Vacancy status (can be repeated for multiple values)
- `employment_type`: Employment type (can be repeated for multiple values)
- `company_id`: Filter by specific company
- `salary_min`: Minimum salary filter
- `salary_max`: Maximum salary filter
- `min_experience_years`: Minimum experience filter
- `max_experience_years`: Maximum experience filter
- `search`: Text search in title and description
- `sort_by`: Sort field (`created_at`, `updated_at`, `salary_min`, `salary_max`, `title`, `min_experience`)
- `sort_direction`: Sort direction (`asc`, `desc`)
- `limit`: Page size (default: 20)
- `offset`: Page offset (default: 0)

---

### Application Endpoints (JWT Auth Required)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/application` | JWT | Create job application (Job Seeker) |
| `GET` | `/application` | JWT | List my applications (Job Seeker) |
| `GET` | `/application/{id}` | JWT | Get application details (Owner or Employer) |
| `PUT` | `/application/{id}` | JWT | Update application (Job Seeker owner only) |
| `DELETE` | `/application/{id}` | JWT | Delete application (Job Seeker owner only) |
| `PUT` | `/application/{id}/status` | JWT | Update application status (Employer only) |
| `GET` | `/application/vacancy/{vacancyId}` | JWT | List applications for vacancy (Employer only) |
| `GET` | `/application/check/vacancy/{vacancyId}` | JWT | Check if user has applied to vacancy |

**Example - Create Application:**
```json
POST /application
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "vacancy_id": 1,
  "cover_letter": "I am very interested in this position because..."
}
```

**Response:**
```json
{
  "id": 1,
  "vacancy_id": 1,
  "job_seeker_id": 5,
  "status": "APPLIED",
  "cover_letter": "I am very interested in this position because...",
  "created_at": "2025-01-20T14:30:00Z",
  "updated_at": "2025-01-20T14:30:00Z"
}
```

**Example - Get My Applications:**
```json
GET /application?status=APPLIED&sort_by=created_at&sort_direction=desc&limit=20&offset=0
Authorization: Bearer <jwt-token>

Response:
[
  {
    "id": 1,
    "vacancy_id": 1,
    "job_seeker_id": 5,
    "status": "APPLIED",
    "cover_letter": "I am very interested in this position because...",
    "created_at": "2025-01-20T14:30:00Z",
    "updated_at": "2025-01-20T14:30:00Z"
  }
]
```

**Example - Get Application Details:**
```json
GET /application/1
Authorization: Bearer <jwt-token>

Response:
{
  "id": 1,
  "vacancy": {
    "id": 1,
    "company": {
      "id": 1,
      "company_name": "Tech Corp",
      "website": "https://techcorp.com",
      "description": "Leading technology company"
    },
    "title": "Senior Backend Developer",
    "description": "We are seeking an experienced backend developer...",
    "salary_min": 80000,
    "salary_max": 120000,
    "min_experience_years": 5,
    "employment_type": "FULL_TIME",
    "category": "SOFTWARE_DEV",
    "status": "ACTIVE",
    "created_at": "2025-01-15T10:30:00Z",
    "updated_at": "2025-01-15T10:30:00Z"
  },
  "job_seeker": {
    "id": 5,
    "specialty": "Full Stack Developer",
    "experience_years": 5,
    "desired_salary": 120000,
    "about_me": "Passionate developer with expertise in Kotlin...",
    "work_experience": [...]
  },
  "status": "APPLIED",
  "cover_letter": "I am very interested in this position because...",
  "created_at": "2025-01-20T14:30:00Z",
  "updated_at": "2025-01-20T14:30:00Z"
}
```

**Example - Update Application (Job Seeker):**
```json
PUT /application/1
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "cover_letter": "Updated cover letter with more details..."
}
```

**Example - Update Application Status (Employer):**
```json
PUT /application/1/status
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "status": "INTERVIEW"
}
```

**Example - Get Applications for Vacancy (Employer):**
```json
GET /application/vacancy/1?limit=20&offset=0
Authorization: Bearer <jwt-token>

Response:
[
  {
    "id": 1,
    "vacancy": {...},
    "job_seeker": {...},
    "status": "APPLIED",
    "cover_letter": "...",
    "created_at": "2025-01-20T14:30:00Z",
    "updated_at": "2025-01-20T14:30:00Z"
  }
]
```

**Example - Check if Applied to Vacancy (Job Seeker):**
```json
GET /application/check/vacancy/1
Authorization: Bearer <jwt-token>

Response:
{
  "has_applied": true
}
```

**Available Application Statuses:**
- `APPLIED`: Initial application submitted
- `REVIEWING`: Application under review
- `INTERVIEW`: Candidate invited for interview
- `TEST_TASK`: Technical test/task assigned
- `OFFER`: Job offer extended
- `HIRED`: Candidate hired
- `REJECTED`: Application rejected
- `WITHDRAWN`: Application withdrawn by candidate

**Filter Parameters:**
- `status`: Application status (single value)
- `sort_by`: Sort field (`created_at`, `updated_at`)
- `sort_direction`: Sort direction (`ASC`, `DESC`)
- `limit`: Page size (default: 20)
- `offset`: Page offset (default: 0)

**Authorization Rules:**
- **Job Seekers**: Can create, view, update, and delete their own applications
- **Employers**: Can view applications for their company's vacancies and update application status
- **Duplicate Prevention**: Unique constraint prevents applying to the same vacancy twice

**Note**: When a vacancy or job seeker profile is deleted, all related applications are automatically deleted (CASCADE DELETE).

---

## 📁 Project Structure

NotDjinni follows **Clean Architecture** with a 4-layer separation of concerns:

```
src/main/kotlin/not/djinni/
├── presentation/              # HTTP Layer (Routes, DTOs, Controllers)
│   ├── plugins/               # Ktor configuration (Auth, CORS, JSON, Koin)
│   └── router/
│       ├── routes/
│       │   ├── auth/          # Authentication endpoints
│       │   ├── user/          # User management endpoints
│       │   ├── seeker/        # Job seeker endpoints
│       │   ├── company/       # Company CRUD endpoints
│       │   └── employer/      # Employer profile endpoints
│       └── DefaultRouter.kt   # Main router
│
├── domain/                    # Business Logic Layer
│   ├── repository/            # Repository interfaces
│   └── exception/             # Business exceptions (sealed classes)
│
├── data/                      # Data Layer
│   ├── repository/            # Repository implementations
│   └── mapper/                # Entity ↔ Domain model conversions
│
├── database/                  # Database/Persistence Layer
│   ├── api/                   # DAO interfaces & entities
│   ├── impl/                  # DAO implementations & Exposed tables
│   └── NotDjinniDatabase.kt   # Database initialization
│
├── model/                     # Domain models
│   ├── User.kt
│   ├── role/                  # Role-specific models
│   │   ├── SeekerProfile.kt
│   │   ├── WorkExperience.kt
│   │   ├── Company.kt
│   │   ├── EmployerProfile.kt
│   │   └── EmployerProfileWithCompany.kt
│   └── token/
│
├── auth/                      # JWT authentication
│   ├── TokenProvider.kt
│   └── DefaultTokenProvider.kt
│
├── di/                        # Dependency Injection
│   └── AppModule.kt           # Koin module (component scan)
│
└── Application.kt             # Entry point
```

### Layer Responsibilities

1. **Presentation Layer**: HTTP handling, request/response DTOs, routing, authentication checks
2. **Domain Layer**: Business logic interfaces, exceptions, validation rules
3. **Data Layer**: Repository implementations, data transformations, business logic execution
4. **Database Layer**: Data persistence, SQL queries, database schema

### Data Flow

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

## 🚀 Quick Start

### Prerequisites

- **JDK 17+** installed
- **PostgreSQL** database running
- **Gradle** (or use included Gradle wrapper)

### 1. Database Setup

Create a PostgreSQL database and user:

```sql
CREATE DATABASE notdjinni;
CREATE USER notdjinni WITH PASSWORD 'notdjinnipassword';
GRANT ALL PRIVILEGES ON DATABASE notdjinni TO notdjinni;
```

The application will automatically create tables on first run using Exposed schema migrations.

### 2. Configuration

Database configuration is located in:
```
src/main/kotlin/not/djinni/database/NotDjinniDatabase.kt
```

Default connection settings:
- **URL**: `jdbc:postgresql://localhost:5432/notdjinni`
- **User**: `notdjinni`
- **Password**: `notdjinnipassword`

### 3. Build the Project

```bash
./gradlew build
```

### 4. Run the Application

```bash
./gradlew run
```

The server will start on `http://localhost:8080`

### 5. Test the API

**Register a user:**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Login and get JWT token:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Create a company (no auth required):**
```bash
curl -X POST http://localhost:8080/company \
  -H "Content-Type: application/json" \
  -d '{
    "company_name": "Tech Corp",
    "website": "https://techcorp.com",
    "description": "Leading technology company"
  }'
```

**Create employer profile (requires JWT):**
```bash
curl -X POST http://localhost:8080/employer/profile \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "company_id": 1,
    "role": "HR Manager"
  }'
```

---

## 📚 Additional Documentation

- **[Project Architecture](documentation/project-architecture.md)**: Detailed architecture guide covering patterns, conventions, code style, and best practices
- **API Collection**: Import the Postman collection (if available) for easy API testing

---

## 🗃️ Database Schema

The application uses the following main tables:

- **users**: User accounts (email, password)
- **refresh_tokens**: JWT refresh tokens
- **job_seeker_profiles**: Job seeker profile information
- **work_experiences**: Work history for job seekers
- **companies**: Company registry (public)
- **employer_profiles**: Employer profiles linked to companies and users
- **vacancies**: Job postings with employment type, category, and status (enums stored as VARCHAR)
- **applications**: Job applications linking seekers to vacancies with status tracking

All tables use auto-incrementing `BIGSERIAL` IDs and appropriate foreign key constraints with CASCADE DELETE.

**Application Enums**: Application statuses (APPLIED, REVIEWING, INTERVIEW, etc.) are stored as enums in the application code and persisted as VARCHAR in the database.

**Unique Constraints**: The applications table has a unique constraint on `(vacancy_id, job_seeker_id)` to prevent duplicate applications.

**Cascade Deletion**: Applications are automatically deleted when the associated vacancy or job seeker profile is deleted.

---

## 🔑 Key Design Decisions

1. **Clean Architecture**: Strict layer separation ensures maintainability and testability
2. **JWT Authentication**: Stateless authentication using RS256 asymmetric encryption
3. **Type-Safe Routing**: Ktor Resources provide compile-time safety for routes
4. **Immutable Associations**: Employer-company relationships cannot be changed after creation
5. **Public Company API**: Companies are publicly accessible for easy data population and discovery
6. **Repository Pattern**: All business logic isolated in repositories, not in routes
7. **Sealed Class Exceptions**: Type-safe error handling with HTTP status code mapping
8. **Koin DI**: Zero-boilerplate dependency injection with KSP code generation

---

## 🔒 Security

- Passwords are securely hashed (implementation uses appropriate hashing algorithm)
- JWT tokens use RS256 asymmetric encryption
- Protected routes require valid JWT in Authorization header: `Bearer <token>`
- User ID extracted from JWT token (not trusted from request body)
- Foreign key constraints ensure data integrity

---

## 🛣️ Roadmap & Future Enhancements

Potential features for future development:

- **Job Postings**: Employers can create and manage job listings
- **Applications**: Job seekers can apply to positions
- **Matching Algorithm**: Smart job-seeker matching based on skills and requirements
- **Notifications**: Email/push notifications for applications and matches
- **Admin Panel**: Administrative interface for platform management
- **File Uploads**: Support for resumes, company logos, and profile pictures
- **Advanced Search**: Full-text search, filters, and sorting
- **Pagination**: Implement pagination for list endpoints
- **Rate Limiting**: API rate limiting and throttling
- **WebSockets**: Real-time notifications and messaging

**Built with ❤️ using Kotlin and Ktor**
