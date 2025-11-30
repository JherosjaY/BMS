-- ==================== NEON SCHEMA UPDATE ====================
-- Run this script to update existing Neon database with missing columns
-- This fixes the "column first_name does not exist" error

-- Step 1: Add missing columns to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(255) UNIQUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS first_name VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_name VARCHAR(100);

-- Step 2: Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Step 3: Update display_name from first_name and last_name if needed
-- This is optional - only run if you want to populate display_name
-- UPDATE users SET display_name = CONCAT(first_name, ' ', last_name) 
-- WHERE display_name IS NULL AND first_name IS NOT NULL;

-- ==================== VERIFICATION ====================
-- Run this query to verify the schema is correct:
-- SELECT column_name, data_type FROM information_schema.columns 
-- WHERE table_name = 'users' ORDER BY ordinal_position;

-- Expected columns:
-- id, username, firebase_uid, email, first_name, last_name, display_name, 
-- photo_url, role, auth_provider, phone_number, barangay, password_hash, 
-- created_at, updated_at, is_active

-- ✅ Schema update complete!
