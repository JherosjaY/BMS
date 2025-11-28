-- ============================================================================
-- BMS DATABASE SCHEMA UPDATES FOR PROFILE PICTURES
-- Run these SQL queries in order in your Neon SQL Editor
-- ============================================================================

-- ============================================================================
-- STEP 1: ALTER USERS TABLE - Add profile picture columns
-- ============================================================================
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS profile_picture_url TEXT,
ADD COLUMN IF NOT EXISTS profile_picture_data BYTEA,
ADD COLUMN IF NOT EXISTS has_profile_picture BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS gender VARCHAR(10) DEFAULT 'male',
ADD COLUMN IF NOT EXISTS auth_provider VARCHAR(20) DEFAULT 'email',
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Create indexes for faster queries
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_auth_provider ON users(auth_provider);
CREATE INDEX IF NOT EXISTS idx_users_gender ON users(gender);

-- ============================================================================
-- STEP 2: CREATE USER_IMAGES TABLE (Alternative approach for image storage)
-- ============================================================================
CREATE TABLE IF NOT EXISTS user_images (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    image_type VARCHAR(20) NOT NULL DEFAULT 'profile', -- 'profile', 'camera', 'gallery'
    image_url TEXT,
    image_data BYTEA,
    file_name VARCHAR(255),
    file_size INTEGER,
    mime_type VARCHAR(50) DEFAULT 'image/jpeg',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user_images_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for user_images table
CREATE INDEX IF NOT EXISTS idx_user_images_user_id ON user_images(user_id);
CREATE INDEX IF NOT EXISTS idx_user_images_type ON user_images(image_type);
CREATE INDEX IF NOT EXISTS idx_user_images_active ON user_images(is_active);

-- ============================================================================
-- STEP 3: UPDATE EXISTING USERS WITH PROPER ROLES AND GENDER
-- ============================================================================
UPDATE users 
SET 
    role = CASE 
        WHEN username = 'sentin' OR email = 'sentin@us.com' THEN 'Admin'
        WHEN username LIKE 'off.%' OR username LIKE 'officer.%' THEN 'Officer'
        ELSE 'User'
    END,
    auth_provider = CASE
        WHEN email LIKE '%@gmail.com' OR email LIKE '%@google.com' THEN 'google'
        ELSE 'email'
    END,
    gender = CASE 
        WHEN username = 'sentin' THEN 'male'
        WHEN username LIKE '%maria%' OR username LIKE '%female%' THEN 'female'
        ELSE 'male'
    END,
    has_profile_picture = FALSE
WHERE role IS NULL OR role = '';

-- ============================================================================
-- STEP 4: CREATE PROFILE PICTURE MANAGEMENT FUNCTIONS
-- ============================================================================

-- Function to set user profile picture
CREATE OR REPLACE FUNCTION set_user_profile_picture(
    p_user_id VARCHAR(255),
    p_image_url TEXT DEFAULT NULL,
    p_image_data BYTEA DEFAULT NULL
) RETURNS JSON AS $$
DECLARE
    result JSON;
BEGIN
    UPDATE users 
    SET 
        profile_picture_url = COALESCE(p_image_url, profile_picture_url),
        profile_picture_data = COALESCE(p_image_data, profile_picture_data),
        has_profile_picture = TRUE,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = p_user_id;
    
    IF FOUND THEN
        result := json_build_object(
            'success', true,
            'message', 'Profile picture updated successfully',
            'user_id', p_user_id
        );
    ELSE
        result := json_build_object(
            'success', false,
            'message', 'User not found'
        );
    END IF;
    
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Function to get user profile picture
CREATE OR REPLACE FUNCTION get_user_profile_picture(p_user_id VARCHAR(255))
RETURNS TABLE (
    profile_picture_url TEXT,
    profile_picture_data BYTEA,
    has_profile_picture BOOLEAN,
    username VARCHAR(255),
    email VARCHAR(255),
    role VARCHAR(20),
    gender VARCHAR(10)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        u.profile_picture_url,
        u.profile_picture_data,
        u.has_profile_picture,
        u.username,
        u.email,
        u.role,
        u.gender
    FROM users u
    WHERE u.id = p_user_id;
END;
$$ LANGUAGE plpgsql;

-- Function to update user gender
CREATE OR REPLACE FUNCTION update_user_gender(
    p_user_id VARCHAR(255),
    p_gender VARCHAR(10)
) RETURNS JSON AS $$
DECLARE
    result JSON;
BEGIN
    -- Validate gender
    IF p_gender NOT IN ('male', 'female', 'other') THEN
        result := json_build_object(
            'success', false,
            'message', 'Invalid gender. Use: male, female, or other'
        );
        RETURN result;
    END IF;
    
    UPDATE users 
    SET 
        gender = p_gender,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = p_user_id;
    
    IF FOUND THEN
        result := json_build_object(
            'success', true,
            'message', 'Gender updated successfully',
            'gender', p_gender
        );
    ELSE
        result := json_build_object(
            'success', false,
            'message', 'User not found'
        );
    END IF;
    
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Function to get user role based on username pattern
CREATE OR REPLACE FUNCTION detect_user_role(p_username VARCHAR(255))
RETURNS VARCHAR(20) AS $$
BEGIN
    -- Rule 1: Username starts with "off." = OFFICER
    IF p_username ILIKE 'off.%' OR p_username ILIKE 'officer.%' THEN
        RETURN 'Officer';
    END IF;
    
    -- Rule 2: Built-in admin accounts
    IF p_username = 'admin' OR p_username = 'sentin' THEN
        RETURN 'Admin';
    END IF;
    
    -- Rule 3: Default to USER
    RETURN 'User';
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- STEP 5: INSERT TEST DATA (Optional - for testing)
-- ============================================================================

-- Insert test officer accounts
INSERT INTO users (username, email, password, first_name, last_name, role, gender, auth_provider) 
VALUES 
    ('off.juan', 'off.juan@bms.com', '$2a$10$hashedpassword1', 'Juan', 'Dela Cruz', 'Officer', 'male', 'email'),
    ('off.maria', 'off.maria@bms.com', '$2a$10$hashedpassword2', 'Maria', 'Santos', 'Officer', 'female', 'email'),
    ('officer.pedro', 'officer.pedro@bms.com', '$2a$10$hashedpassword3', 'Pedro', 'Reyes', 'Officer', 'male', 'email')
ON CONFLICT (username) DO NOTHING;

-- Insert test user accounts (Regular signup)
INSERT INTO users (username, email, password, first_name, last_name, role, gender, auth_provider) 
VALUES 
    ('testuser1', 'testuser1@example.com', '$2a$10$hashedpassword4', 'Test', 'User One', 'User', 'male', 'email'),
    ('testuser2', 'testuser2@example.com', '$2a$10$hashedpassword5', 'Test', 'User Two', 'User', 'female', 'email')
ON CONFLICT (username) DO NOTHING;

-- ============================================================================
-- STEP 6: VERIFICATION QUERIES
-- ============================================================================

-- View all users with their roles and profile status
SELECT 
    id,
    username,
    email,
    role,
    gender,
    auth_provider,
    has_profile_picture,
    created_at,
    updated_at
FROM users 
ORDER BY 
    CASE role 
        WHEN 'Admin' THEN 1
        WHEN 'Officer' THEN 2
        WHEN 'User' THEN 3
        ELSE 4
    END,
    username;

-- View profile picture status
SELECT 
    u.id,
    u.username,
    u.email,
    u.role,
    u.gender,
    u.auth_provider,
    u.has_profile_picture,
    CASE 
        WHEN u.role = 'Admin' THEN '⭐🛡️ Shield Icon (Fixed)'
        WHEN u.role = 'Officer' THEN 
            CASE 
                WHEN u.gender = 'female' THEN '👮‍♀️ Female Officer'
                ELSE '👮‍♂️ Male Officer'
            END
        WHEN u.role = 'User' THEN '📷 Editable PFP (Gallery/Camera)'
        ELSE '❓ Unknown'
    END as profile_type,
    u.created_at
FROM users u
ORDER BY 
    CASE u.role 
        WHEN 'Admin' THEN 1
        WHEN 'Officer' THEN 2
        WHEN 'User' THEN 3
        ELSE 4
    END,
    u.username;

-- ============================================================================
-- STEP 7: TEST THE FUNCTIONS (Optional)
-- ============================================================================

-- Test setting a profile picture
-- SELECT * FROM set_user_profile_picture('1', 'https://example.com/profiles/user1.jpg', NULL);

-- Test getting a profile picture
-- SELECT * FROM get_user_profile_picture('1');

-- Test gender update
-- SELECT * FROM update_user_gender('2', 'female');

-- Test role detection
-- SELECT username, detect_user_role(username) as detected_role FROM users;

-- ============================================================================
-- SUMMARY OF CHANGES
-- ============================================================================
-- ✅ Added profile_picture_url column (TEXT) - for storing image URLs
-- ✅ Added profile_picture_data column (BYTEA) - for storing binary image data
-- ✅ Added has_profile_picture column (BOOLEAN) - flag for quick checks
-- ✅ Added gender column (VARCHAR) - for officer gender-based icons
-- ✅ Added auth_provider column (VARCHAR) - to identify Google vs Email auth
-- ✅ Added updated_at column (TIMESTAMP) - for tracking updates
-- ✅ Created user_images table - alternative image storage with metadata
-- ✅ Created indexes - for faster queries
-- ✅ Created functions - for profile picture management
-- ✅ Updated existing users - with proper roles and gender
-- ============================================================================
