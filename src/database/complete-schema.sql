-- ✅ COMPLETE DATABASE SCHEMA FOR BMS
-- Run this script in Neon PostgreSQL

-- User Images Table (for Cloudinary multi-device sync)
CREATE TABLE IF NOT EXISTS user_images (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    context VARCHAR(100), -- 'avatar', 'evidence_*'
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    device_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Admin SMS Logs (for tracking SMS sent by admins)
CREATE TABLE IF NOT EXISTS admin_sms_logs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    user_name VARCHAR(255),
    phone_number VARCHAR(20),
    message TEXT,
    message_type VARCHAR(100), -- 'ANNOUNCEMENT', 'HEARING_NOTICE', etc.
    sent_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50), -- 'SENT', 'FAILED', 'PENDING'
    error_message TEXT
);

-- User Sessions (for multi-device tracking)
CREATE TABLE IF NOT EXISTS user_sessions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    device_id VARCHAR(255) NOT NULL,
    device_model VARCHAR(255),
    app_version VARCHAR(50),
    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, device_id)
);

-- User FCM Tokens (for push notifications to multiple devices)
CREATE TABLE IF NOT EXISTS user_fcm_tokens (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    fcm_token TEXT NOT NULL,
    device_id VARCHAR(255),
    device_type VARCHAR(50), -- 'Android', 'iOS'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, device_id)
);

-- User Activities (for tracking user actions across devices)
CREATE TABLE IF NOT EXISTS user_activities (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    activity_type VARCHAR(100), -- 'LOGIN', 'CREATE_REPORT', 'UPDATE_PROFILE', etc.
    details TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    device_id VARCHAR(255),
    app_version VARCHAR(50)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_user_images_user_id ON user_images(user_id);
CREATE INDEX IF NOT EXISTS idx_user_images_context ON user_images(context);
CREATE INDEX IF NOT EXISTS idx_admin_sms_logs_user_id ON admin_sms_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_admin_sms_logs_sent_by ON admin_sms_logs(sent_by);
CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_fcm_tokens_user_id ON user_fcm_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_user_activities_user_id ON user_activities(user_id);
CREATE INDEX IF NOT EXISTS idx_user_activities_timestamp ON user_activities(timestamp);

-- ✅ Schema complete!
