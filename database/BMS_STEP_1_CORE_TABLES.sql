-- 📋 STEP 1: CORE TABLES (Users & User Images)
-- These are the foundation tables for authentication and user management
-- Run this AFTER cleanup script

-- ✅ TABLE 1: users (Core authentication table)
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    firstName VARCHAR(100),
    lastName VARCHAR(100),
    role VARCHAR(50) DEFAULT 'user', -- 'user', 'Officer', 'Admin'
    gender VARCHAR(20),
    phoneNumber VARCHAR(20),
    profilePhotoUri TEXT,
    profileCompleted BOOLEAN DEFAULT FALSE,
    isActive BOOLEAN DEFAULT TRUE,
    isHidden BOOLEAN DEFAULT FALSE,
    mustChangePassword BOOLEAN DEFAULT FALSE,
    forcePasswordChange BOOLEAN DEFAULT FALSE,
    lastLogin TIMESTAMP,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    googleId VARCHAR(255),
    auth_provider VARCHAR(50) DEFAULT 'email', -- 'email', 'google'
    
    -- Indexes for performance
    CONSTRAINT valid_role CHECK (role IN ('user', 'Officer', 'Admin')),
    CONSTRAINT valid_auth_provider CHECK (auth_provider IN ('email', 'google'))
);

-- Create indexes for users table
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_googleId ON users(googleId);
CREATE INDEX idx_users_isActive ON users(isActive);
CREATE INDEX idx_users_createdAt ON users(createdAt);

-- ✅ TABLE 2: user_images (Profile pictures and attachments)
CREATE TABLE user_images (
    id SERIAL PRIMARY KEY,
    userId INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    imageUrl TEXT NOT NULL,
    imageType VARCHAR(50) DEFAULT 'profile', -- 'profile', 'document', 'attachment'
    uploadedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    isActive BOOLEAN DEFAULT TRUE
);

-- Create indexes for user_images table
CREATE INDEX idx_user_images_userId ON user_images(userId);
CREATE INDEX idx_user_images_imageType ON user_images(imageType);

-- ✅ INSERT ADMIN ACCOUNT (Pre-created for testing)
INSERT INTO users (
    id,
    username,
    password,
    email,
    role,
    firstName,
    lastName,
    isHidden,
    auth_provider,
    isActive
) VALUES (
    1,
    'bms.official.admin',
    '$2b$10$K8wjZTF5B8u6H7Jq6m9nE.9qQZ5rY8VWq0dK8cX6vL5dR2rS5tOa', -- BMS2025 (bcrypt)
    'official.bms.2025@gmail.com',
    'Admin',
    'System',
    'Administrator',
    true,
    'email',
    true
);

-- ✅ VERIFY SETUP
SELECT 
    'users' as table_name,
    COUNT(*) as row_count
FROM users
UNION ALL
SELECT 
    'user_images' as table_name,
    COUNT(*) as row_count
FROM user_images;

-- ✅ STEP 1 COMPLETE
-- Core tables created successfully!
-- Next: Run BMS_STEP_2_HEARINGS_TABLES.sql
