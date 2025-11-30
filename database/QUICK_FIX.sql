-- ==================== QUICK FIX FOR EXISTING NEON DATABASE ====================
-- Run this to add ALL missing columns at once

-- ✅ USERS TABLE - Add missing columns
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS username VARCHAR(255) UNIQUE;
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS firebase_uid VARCHAR(255) UNIQUE;
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS google_id VARCHAR(255) UNIQUE;
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS first_name VARCHAR(100);
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS last_name VARCHAR(100);
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS auth_provider VARCHAR(50);
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);
ALTER TABLE IF EXISTS users ADD COLUMN IF NOT EXISTS profile_picture_url TEXT;

-- ✅ BLOTTER_REPORTS TABLE - Add missing columns
ALTER TABLE IF EXISTS blotter_reports ADD COLUMN IF NOT EXISTS reported_by_id INTEGER REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE IF EXISTS blotter_reports ADD COLUMN IF NOT EXISTS assigned_officer_id INTEGER REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE IF EXISTS blotter_reports ADD COLUMN IF NOT EXISTS assigned_officer_ids TEXT;

-- ✅ CASE_EVIDENCE TABLE - Add missing columns
ALTER TABLE IF EXISTS case_evidence ADD COLUMN IF NOT EXISTS report_id INTEGER REFERENCES blotter_reports(id) ON DELETE CASCADE;
ALTER TABLE IF EXISTS case_evidence ADD COLUMN IF NOT EXISTS uploaded_by INTEGER REFERENCES users(id) ON DELETE SET NULL;

-- ✅ SMS_NOTIFICATIONS TABLE - Add missing columns
ALTER TABLE IF EXISTS sms_notifications ADD COLUMN IF NOT EXISTS report_id INTEGER REFERENCES blotter_reports(id) ON DELETE CASCADE;
ALTER TABLE IF EXISTS sms_notifications ADD COLUMN IF NOT EXISTS sent_by INTEGER REFERENCES users(id) ON DELETE SET NULL;

-- ✅ FCM_TOKENS TABLE - Add missing columns
ALTER TABLE IF EXISTS fcm_tokens ADD COLUMN IF NOT EXISTS user_id INTEGER REFERENCES users(id) ON DELETE CASCADE;

-- ✅ ACTIVITY_LOGS TABLE - Add missing columns
ALTER TABLE IF EXISTS activity_logs ADD COLUMN IF NOT EXISTS user_id INTEGER REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE IF EXISTS activity_logs ADD COLUMN IF NOT EXISTS report_id INTEGER REFERENCES blotter_reports(id) ON DELETE SET NULL;

-- ✅ Create all indexes
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_firebase_uid ON users(firebase_uid);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_blotter_reports_case_number ON blotter_reports(case_number);
CREATE INDEX IF NOT EXISTS idx_blotter_reports_status ON blotter_reports(status);
CREATE INDEX IF NOT EXISTS idx_blotter_reports_reported_by ON blotter_reports(reported_by_id);
CREATE INDEX IF NOT EXISTS idx_blotter_reports_assigned_officer ON blotter_reports(assigned_officer_id);
CREATE INDEX IF NOT EXISTS idx_case_evidence_report_id ON case_evidence(report_id);
CREATE INDEX IF NOT EXISTS idx_sms_notifications_report_id ON sms_notifications(report_id);
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_user_id ON fcm_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_activity_logs_user_id ON activity_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_activity_logs_report_id ON activity_logs(report_id);

-- ✅ DONE! All columns and indexes added
