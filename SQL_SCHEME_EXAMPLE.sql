-- ============================================
-- NotDjinni Database Schema
-- ============================================
-- This schema represents the complete database structure for the NotDjinni job marketplace platform.
-- Tables are organized by domain and listed in dependency order.
--
-- DOMAIN ORGANIZATION:
-- 1. Authentication: Users, RefreshTokens
-- 2. Companies & Employment: Companies, EmployerProfiles
-- 3. Job Seekers: SeekerProfiles, WorkExperiences
-- 4. Job Postings: Vacancies
-- 5. Applications: Applications
--
-- CONSTRAINT BEHAVIOR:
-- - CASCADE: Parent deletion cascades to children (e.g., deleting a user deletes their profile)
-- - RESTRICT: Child cannot be deleted if referenced by parent (e.g., cannot delete company with active profiles)
--
-- ENUM STORAGE:
-- - Enums are stored as VARCHAR in PostgreSQL
-- - Examples: application statuses, vacancy statuses, employment types, job categories
--
-- ============================================

-- ============================================
-- 1. AUTHENTICATION DOMAIN
-- ============================================

CREATE TABLE "user" (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE INDEX idx_user_email ON "user"(email);

-- Refresh tokens for JWT authentication
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    token VARCHAR(512) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

-- ============================================
-- 2. COMPANIES & EMPLOYMENT DOMAIN
-- ============================================

-- Company registry
CREATE TABLE companies (
    id BIGSERIAL PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL UNIQUE,
    website VARCHAR(255),
    description TEXT NOT NULL
);

CREATE INDEX idx_companies_company_name ON companies(company_name);

-- Employer profiles linking users to companies
CREATE TABLE employer_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES "user"(id) ON DELETE CASCADE,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE RESTRICT,
    role VARCHAR(255) NOT NULL
);

CREATE INDEX idx_employer_profiles_user_id ON employer_profiles(user_id);
CREATE INDEX idx_employer_profiles_company_id ON employer_profiles(company_id);

-- ============================================
-- 3. JOB SEEKERS DOMAIN
-- ============================================

-- Job seeker profiles
CREATE TABLE job_seeker_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES "user"(id) ON DELETE CASCADE,
    specialty VARCHAR(255) NOT NULL,
    experience_years INT NOT NULL,
    desired_salary INT NOT NULL,
    about_me TEXT,
    job_category VARCHAR(50) NOT NULL
);

CREATE INDEX idx_job_seeker_profiles_user_id ON job_seeker_profiles(user_id);
CREATE INDEX idx_job_seeker_profiles_job_category ON job_seeker_profiles(job_category);

-- Work experience history for job seekers
CREATE TABLE work_experience (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES job_seeker_profiles(id) ON DELETE CASCADE,
    company_name VARCHAR(255) NOT NULL,
    position VARCHAR(255) NOT NULL,
    description TEXT,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP
);

CREATE INDEX idx_work_experience_profile_id ON work_experience(profile_id);
CREATE INDEX idx_work_experience_start_date ON work_experience(start_date);

-- ============================================
-- 4. JOB POSTINGS DOMAIN
-- ============================================

-- Job vacancies posted by employers
CREATE TABLE vacancies (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    salary_min INT NOT NULL,
    salary_max INT NOT NULL,
    min_experience_years INT,
    employment_type VARCHAR(50),
    category VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_vacancies_company_id ON vacancies(company_id);
CREATE INDEX idx_vacancies_status ON vacancies(status);
CREATE INDEX idx_vacancies_category ON vacancies(category);
CREATE INDEX idx_vacancies_employment_type ON vacancies(employment_type);
CREATE INDEX idx_vacancies_created_at ON vacancies(created_at);
CREATE INDEX idx_vacancies_updated_at ON vacancies(updated_at);

-- ============================================
-- 5. APPLICATIONS DOMAIN
-- ============================================

-- Job applications submitted by seekers to vacancies
CREATE TABLE applications (
    id BIGSERIAL PRIMARY KEY,
    vacancy_id BIGINT NOT NULL REFERENCES vacancies(id) ON DELETE CASCADE,
    job_seeker_id BIGINT NOT NULL REFERENCES job_seeker_profiles(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    cover_letter TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    UNIQUE(vacancy_id, job_seeker_id)
);

CREATE INDEX idx_applications_vacancy_id ON applications(vacancy_id);
CREATE INDEX idx_applications_job_seeker_id ON applications(job_seeker_id);
CREATE INDEX idx_applications_status ON applications(status);
CREATE INDEX idx_applications_created_at ON applications(created_at);
CREATE INDEX idx_applications_updated_at ON applications(updated_at);
CREATE UNIQUE INDEX idx_applications_vacancy_seeker ON applications(vacancy_id, job_seeker_id);

-- ============================================
-- End of Schema
-- ============================================
