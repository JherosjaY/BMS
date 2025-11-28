# BMS Database Schema Updates - Profile Pictures

## 📋 Overview

This document explains how to update your Neon PostgreSQL database to support the new role-based profile picture system.

## 🎯 What's Being Added

### New Columns (users table)
- `profile_picture_url` (TEXT) - Store image URLs from gallery/camera
- `profile_picture_data` (BYTEA) - Store binary image data
- `has_profile_picture` (BOOLEAN) - Quick flag to check if user has profile picture
- `gender` (VARCHAR) - For officer gender-based emoji icons (👮‍♂️/👮‍♀️)
- `auth_provider` (VARCHAR) - Identify Google vs Email authentication
- `updated_at` (TIMESTAMP) - Track when profile was last updated

### New Table
- `user_images` - Alternative image storage with metadata (image_type, file_name, file_size, mime_type)

### New Functions
- `set_user_profile_picture()` - Save profile picture to database
- `get_user_profile_picture()` - Retrieve profile picture
- `update_user_gender()` - Update user gender
- `detect_user_role()` - Detect role based on username pattern

## 🚀 How to Run the Updates

### Step 1: Open Neon SQL Editor
1. Go to https://console.neon.tech/
2. Select your BMS project
3. Click on "SQL Editor"
4. Create a new query

### Step 2: Copy and Run SQL Script
1. Open the file: `database/schema_updates_profile_pictures.sql`
2. Copy the entire content
3. Paste into Neon SQL Editor
4. Click "Execute" button

### Step 3: Run in Sections (Recommended)

If you prefer to run in sections for better control:

#### Section 1: Alter Users Table
```sql
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS profile_picture_url TEXT,
ADD COLUMN IF NOT EXISTS profile_picture_data BYTEA,
ADD COLUMN IF NOT EXISTS has_profile_picture BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS gender VARCHAR(10) DEFAULT 'male',
ADD COLUMN IF NOT EXISTS auth_provider VARCHAR(20) DEFAULT 'email',
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_auth_provider ON users(auth_provider);
CREATE INDEX IF NOT EXISTS idx_users_gender ON users(gender);
```

#### Section 2: Create user_images Table
```sql
CREATE TABLE IF NOT EXISTS user_images (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    image_type VARCHAR(20) NOT NULL DEFAULT 'profile',
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

CREATE INDEX IF NOT EXISTS idx_user_images_user_id ON user_images(user_id);
CREATE INDEX IF NOT EXISTS idx_user_images_type ON user_images(image_type);
CREATE INDEX IF NOT EXISTS idx_user_images_active ON user_images(is_active);
```

#### Section 3: Update Existing Users
```sql
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
```

#### Section 4: Create Functions
Copy and run the function creation statements from the SQL file.

#### Section 5: Verify Changes
```sql
-- View all users with their profile status
SELECT 
    id,
    username,
    email,
    role,
    gender,
    auth_provider,
    has_profile_picture,
    created_at
FROM users 
ORDER BY role, username;
```

## ✅ Verification

After running the updates, verify everything is working:

### Check Users Table
```sql
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'users' 
ORDER BY ordinal_position;
```

### Check user_images Table
```sql
SELECT * FROM user_images LIMIT 5;
```

### Check Functions
```sql
-- List all functions
SELECT routine_name 
FROM information_schema.routines 
WHERE routine_schema = 'public' 
AND routine_name LIKE '%profile%' OR routine_name LIKE '%gender%';
```

### Test Functions
```sql
-- Test role detection
SELECT username, detect_user_role(username) as detected_role 
FROM users 
LIMIT 5;

-- Test profile picture function
SELECT * FROM set_user_profile_picture('1', 'https://example.com/profile.jpg', NULL);

-- Test gender update
SELECT * FROM update_user_gender('2', 'female');
```

## 📊 Database Schema Diagram

```
users table
├── id (PRIMARY KEY)
├── username (UNIQUE)
├── email (UNIQUE)
├── password
├── first_name
├── last_name
├── role (Admin, Officer, User)
├── gender (male, female, other) ✨ NEW
├── auth_provider (email, google) ✨ NEW
├── profile_picture_url (TEXT) ✨ NEW
├── profile_picture_data (BYTEA) ✨ NEW
├── has_profile_picture (BOOLEAN) ✨ NEW
├── created_at
└── updated_at ✨ NEW

user_images table ✨ NEW
├── id (PRIMARY KEY)
├── user_id (FOREIGN KEY → users.id)
├── image_type (profile, camera, gallery)
├── image_url (TEXT)
├── image_data (BYTEA)
├── file_name (VARCHAR)
├── file_size (INTEGER)
├── mime_type (VARCHAR)
├── is_active (BOOLEAN)
├── created_at
└── updated_at
```

## 🔄 Role Detection Logic

The system now automatically detects user roles based on:

```
Username starts with "off." or "officer." → OFFICER 👮
Username = "admin" or "sentin" → ADMIN ⭐🛡️
Email contains @gmail.com or @google.com → GOOGLE AUTH → USER 📷
Regular signup → USER 📷
```

## 👥 Profile Picture Behavior by Role

| Role | Profile Picture | Behavior | Storage |
|------|-----------------|----------|---------|
| **USER** | 📷 Gallery/Camera | Editable | Local SQLite + Neon DB |
| **ADMIN** | ⭐🛡️ Shield Icon | Fixed (Not editable) | Hardcoded |
| **OFFICER** | 👮 Gender Emoji | Fixed (Based on gender) | PreferencesManager |

## 🧪 Testing Checklist

- [ ] Run ALTER TABLE statements
- [ ] Run CREATE TABLE for user_images
- [ ] Run UPDATE statements for existing users
- [ ] Run function creations
- [ ] Verify columns were added
- [ ] Verify functions were created
- [ ] Test role detection function
- [ ] Test profile picture functions
- [ ] Test gender update function
- [ ] Check all users have proper roles assigned

## ⚠️ Important Notes

1. **Backup First**: Always backup your database before running schema updates
2. **Test Environment**: Test in a development environment first
3. **No Data Loss**: These updates are additive and won't delete existing data
4. **Idempotent**: All statements use `IF NOT EXISTS` so they're safe to run multiple times
5. **Foreign Keys**: user_images table has CASCADE delete for data integrity

## 🆘 Troubleshooting

### Error: "Column already exists"
- This is normal if you've run the script before
- The `IF NOT EXISTS` clause prevents errors
- You can safely run the script again

### Error: "Function already exists"
- Use `CREATE OR REPLACE FUNCTION` to update functions
- The script already includes this

### Error: "Foreign key constraint failed"
- Ensure users table exists before creating user_images table
- Run sections in order

## 📞 Support

If you encounter any issues:
1. Check the error message carefully
2. Verify you're in the correct database
3. Ensure all prerequisites are met
4. Run one section at a time to isolate issues

---

**Last Updated**: 2025-11-28
**Version**: 1.0
**Status**: Ready for Production ✅
