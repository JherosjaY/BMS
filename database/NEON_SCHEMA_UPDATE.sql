-- ==================== NEON SCHEMA UPDATE ====================
-- Run this script to update existing Neon database with missing columns
-- This fixes the "column first_name does not exist" error

-- ⚠️ IMPORTANT: If users table already exists, use these ALTER statements:

-- Step 1: Add missing columns to users table (one by one to avoid conflicts)
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS username VARCHAR(255) UNIQUE;
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS firebase_uid VARCHAR(255) UNIQUE;
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS first_name VARCHAR(100);
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS last_name VARCHAR(100);
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS auth_provider VARCHAR(50);
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS profile_picture_url TEXT;

-- Step 2: Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_firebase_uid ON users(firebase_uid);

-- ==================== VERIFICATION ====================
-- Run this query to verify the schema is correct:
SELECT column_name, data_type FROM information_schema.columns 
WHERE table_name = 'users' ORDER BY ordinal_position;

-- Expected columns:
-- id, username, firebase_uid, email, first_name, last_name, display_name, 
-- photo_url, role, auth_provider, phone_number, barangay, password_hash, 
-- created_at, updated_at, is_active

-- ✅ Schema update complete!
