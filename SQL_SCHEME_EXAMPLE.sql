-- Drop existing tables if needed (in reverse order of dependencies)
DROP TABLE IF EXISTS RefreshTokens CASCADE;
DROP TABLE IF EXISTS Companies CASCADE;
DROP TABLE IF EXISTS Users CASCADE;

-- Users table
CREATE TABLE Users
(
    id            SERIAL PRIMARY KEY,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255)        NOT NULL,
    name          VARCHAR(100)        NOT NULL
);

-- Refresh tokens table for JWT authentication
CREATE TABLE RefreshTokens
(
    id         SERIAL PRIMARY KEY,
    user_id    INT          NOT NULL,
    token      VARCHAR(512) UNIQUE NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE
);

-- Indexes for optimization
CREATE INDEX idx_users_email ON Users (email);
CREATE INDEX idx_refresh_tokens_user ON RefreshTokens (user_id);
CREATE INDEX idx_refresh_tokens_token ON RefreshTokens (token);

-- Table comments
COMMENT ON TABLE Users IS 'User accounts table';
COMMENT ON TABLE RefreshTokens IS 'Refresh B>:5=8 4;O JWT 02B5=B8DV:0FVW';
