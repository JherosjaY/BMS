-- ==================== NEON DATABASE SCHEMA ====================
-- Blotter Management System - Complete Database Schema
-- Compatible with PostgreSQL and Neon

-- ==================== USERS TABLE ====================
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE,
    firebase_uid VARCHAR(255) UNIQUE,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    display_name VARCHAR(255),
    photo_url TEXT,
    role VARCHAR(50) DEFAULT 'User', -- Admin, Officer, User
    auth_provider VARCHAR(50), -- google, email, etc.
    phone_number VARCHAR(20),
    barangay VARCHAR(100),
    password_hash VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);

-- ==================== BLOTTER REPORTS TABLE ====================
CREATE TABLE IF NOT EXISTS blotter_reports (
    id SERIAL PRIMARY KEY,
    case_number VARCHAR(100) UNIQUE NOT NULL,
    incident_type VARCHAR(255) NOT NULL,
    complainant_name VARCHAR(255) NOT NULL,
    respondent_name VARCHAR(255) NOT NULL,
    incident_date DATE NOT NULL,
    incident_details TEXT NOT NULL,
    location TEXT,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, ASSIGNED, ONGOING, RESOLVED
    date_filed BIGINT NOT NULL,
    reported_by_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    assigned_officer_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    assigned_officer_ids TEXT, -- For multiple officers (comma-separated)
    resolution_type VARCHAR(255),
    hearing_date DATE,
    hearing_time TIME,
    resolution_details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==================== CASE EVIDENCE/IMAGES TABLE ====================
CREATE TABLE IF NOT EXISTS case_evidence (
    id SERIAL PRIMARY KEY,
    report_id INTEGER REFERENCES blotter_reports(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    file_name VARCHAR(255),
    uploaded_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==================== SMS NOTIFICATIONS LOG ====================
CREATE TABLE IF NOT EXISTS sms_notifications (
    id SERIAL PRIMARY KEY,
    report_id INTEGER REFERENCES blotter_reports(id) ON DELETE CASCADE,
    phone_number VARCHAR(20) NOT NULL,
    message_type VARCHAR(100) NOT NULL, -- HEARING_NOTICE, REMINDER, STATUS_UPDATE, etc.
    message_content TEXT NOT NULL,
    sent_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'SENT' -- SENT, FAILED, PENDING
);

-- ==================== FCM TOKENS FOR PUSH NOTIFICATIONS ====================
CREATE TABLE IF NOT EXISTS fcm_tokens (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    fcm_token TEXT NOT NULL UNIQUE,
    device_type VARCHAR(50), -- android, ios, web
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==================== ACTIVITY LOGS ====================
CREATE TABLE IF NOT EXISTS activity_logs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    report_id INTEGER REFERENCES blotter_reports(id) ON DELETE SET NULL,
    action VARCHAR(255) NOT NULL, -- CREATED, UPDATED, ASSIGNED, RESOLVED, etc.
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==================== INDEXES FOR PERFORMANCE ====================
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_firebase_uid ON users(firebase_uid);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_blotter_reports_case_number ON blotter_reports(case_number);
CREATE INDEX IF NOT EXISTS idx_blotter_reports_status ON blotter_reports(status);
CREATE INDEX IF NOT EXISTS idx_blotter_reports_reported_by ON blotter_reports(reported_by_id);
CREATE INDEX IF NOT EXISTS idx_blotter_reports_assigned_officer ON blotter_reports(assigned_officer_id);
CREATE INDEX IF NOT EXISTS idx_blotter_reports_date_filed ON blotter_reports(date_filed DESC);
CREATE INDEX IF NOT EXISTS idx_case_evidence_report_id ON case_evidence(report_id);
CREATE INDEX IF NOT EXISTS idx_sms_notifications_report_id ON sms_notifications(report_id);
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_user_id ON fcm_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_activity_logs_user_id ON activity_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_activity_logs_report_id ON activity_logs(report_id);

-- ==================== VIEWS FOR COMMON QUERIES ====================
CREATE OR REPLACE VIEW v_active_cases AS
SELECT 
    br.id,
    br.case_number,
    br.incident_type,
    br.complainant_name,
    br.respondent_name,
    br.status,
    u_reporter.display_name as reporter_name,
    u_officer.display_name as officer_name,
    br.date_filed,
    br.updated_at
FROM blotter_reports br
LEFT JOIN users u_reporter ON br.reported_by_id = u_reporter.id
LEFT JOIN users u_officer ON br.assigned_officer_id = u_officer.id
WHERE br.status IN ('PENDING', 'ASSIGNED', 'ONGOING')
ORDER BY br.date_filed DESC;

CREATE OR REPLACE VIEW v_officer_workload AS
SELECT 
    u.id,
    u.display_name,
    u.email,
    COUNT(br.id) as assigned_cases,
    COUNT(CASE WHEN br.status = 'PENDING' THEN 1 END) as pending_cases,
    COUNT(CASE WHEN br.status = 'ONGOING' THEN 1 END) as ongoing_cases
FROM users u
LEFT JOIN blotter_reports br ON u.id = br.assigned_officer_id
WHERE u.role = 'Officer' AND u.is_active = true
GROUP BY u.id, u.display_name, u.email
ORDER BY assigned_cases DESC;

-- ==================== SAMPLE DATA (Optional) ====================
-- Uncomment to insert sample data for testing

-- INSERT INTO users (email, display_name, role, auth_provider, is_active)
-- VALUES 
--     ('admin@blotter.com', 'Admin User', 'Admin', 'email', true),
--     ('officer@blotter.com', 'Officer User', 'Officer', 'email', true),
--     ('user@blotter.com', 'Regular User', 'User', 'email', true);
