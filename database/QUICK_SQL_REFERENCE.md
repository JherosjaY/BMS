# 🚀 QUICK SQL REFERENCE - Copy & Paste Ready

**⏱️ Time to complete: ~5 minutes**

---

## 📋 STEP-BY-STEP INSTRUCTIONS

### **STEP 1: Open Neon SQL Editor**
1. Go to https://console.neon.tech/
2. Select your BMS project
3. Click "SQL Editor"
4. Create a new query

---

## ✅ STEP 2: RUN THESE SQL COMMANDS IN ORDER

### **COMMAND 1: Add columns to users table**
Copy and paste this entire block:

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

**Click Execute** ✅

---

### **COMMAND 2: Create user_images table**
Copy and paste this entire block:

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

**Click Execute** ✅

---

### **COMMAND 3: Update existing users with roles and gender**
Copy and paste this entire block:

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

**Click Execute** ✅

---

### **COMMAND 4: Create set_user_profile_picture function**
Copy and paste this entire block:

```sql
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
```

**Click Execute** ✅

---

### **COMMAND 5: Create get_user_profile_picture function**
Copy and paste this entire block:

```sql
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
```

**Click Execute** ✅

---

### **COMMAND 6: Create update_user_gender function**
Copy and paste this entire block:

```sql
CREATE OR REPLACE FUNCTION update_user_gender(
    p_user_id VARCHAR(255),
    p_gender VARCHAR(10)
) RETURNS JSON AS $$
DECLARE
    result JSON;
BEGIN
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
```

**Click Execute** ✅

---

### **COMMAND 7: Create detect_user_role function**
Copy and paste this entire block:

```sql
CREATE OR REPLACE FUNCTION detect_user_role(p_username VARCHAR(255))
RETURNS VARCHAR(20) AS $$
BEGIN
    IF p_username ILIKE 'off.%' OR p_username ILIKE 'officer.%' THEN
        RETURN 'Officer';
    END IF;
    
    IF p_username = 'admin' OR p_username = 'sentin' THEN
        RETURN 'Admin';
    END IF;
    
    RETURN 'User';
END;
$$ LANGUAGE plpgsql;
```

**Click Execute** ✅

---

## 🔍 VERIFICATION - RUN THESE TO VERIFY EVERYTHING WORKS

### **VERIFY 1: Check all users with their roles**
```sql
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

**Expected Output**: You should see all your users with proper roles assigned ✅

---

### **VERIFY 2: Check profile picture status**
```sql
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
```

**Expected Output**: You should see all users with their profile types ✅

---

### **VERIFY 3: Check if functions were created**
```sql
SELECT routine_name 
FROM information_schema.routines 
WHERE routine_schema = 'public' 
AND (routine_name LIKE '%profile%' OR routine_name LIKE '%gender%' OR routine_name LIKE '%detect%');
```

**Expected Output**: Should show 4 functions:
- detect_user_role
- get_user_profile_picture
- set_user_profile_picture
- update_user_gender

✅

---

### **VERIFY 4: Check user_images table**
```sql
SELECT * FROM information_schema.tables 
WHERE table_name = 'user_images';
```

**Expected Output**: Should show user_images table exists ✅

---

## 🧪 OPTIONAL: TEST THE FUNCTIONS

### **Test 1: Test role detection**
```sql
SELECT username, detect_user_role(username) as detected_role 
FROM users 
LIMIT 5;
```

---

### **Test 2: Test profile picture function**
```sql
SELECT * FROM set_user_profile_picture('1', 'https://example.com/profile.jpg', NULL);
```

---

### **Test 3: Test gender update**
```sql
SELECT * FROM update_user_gender('2', 'female');
```

---

### **Test 4: Test get profile picture**
```sql
SELECT * FROM get_user_profile_picture('1');
```

---

## ✅ COMPLETION CHECKLIST

After running all commands, verify:

- [ ] COMMAND 1 executed successfully (ALTER TABLE)
- [ ] COMMAND 2 executed successfully (CREATE TABLE user_images)
- [ ] COMMAND 3 executed successfully (UPDATE users)
- [ ] COMMAND 4 executed successfully (set_user_profile_picture function)
- [ ] COMMAND 5 executed successfully (get_user_profile_picture function)
- [ ] COMMAND 6 executed successfully (update_user_gender function)
- [ ] COMMAND 7 executed successfully (detect_user_role function)
- [ ] VERIFY 1 shows all users with roles
- [ ] VERIFY 2 shows profile types
- [ ] VERIFY 3 shows 4 functions created
- [ ] VERIFY 4 shows user_images table exists

---

## 🎉 YOU'RE DONE!

Once all commands execute successfully, your database is ready for the BMS role-based profile system! 🚀

**Next Steps:**
1. Pull latest code: `git pull origin main`
2. Rebuild Android app: Build → Rebuild Project
3. Test all 3 roles in the app
4. Deploy to phone for production testing

---

**Time Completed**: ~5 minutes ⏱️  
**Status**: ✅ READY FOR TESTING
