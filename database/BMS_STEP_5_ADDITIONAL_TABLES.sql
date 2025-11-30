-- 📋 STEP 5: ADDITIONAL TABLES (Part 1)
-- Manages activity logs, SMS, reports, evidence, and notifications
-- Run this AFTER STEP 4

-- ✅ TABLE 16: activity_logs (System activity tracking)
CREATE TABLE activity_logs (
    id SERIAL PRIMARY KEY,
    userId INTEGER REFERENCES users(id) ON DELETE SET NULL,
    activityType VARCHAR(100), -- 'login', 'logout', 'create', 'update', 'delete', 'view', etc.
    entityType VARCHAR(100), -- 'case', 'hearing', 'person', 'user', etc.
    entityId INTEGER,
    description TEXT,
    ipAddress VARCHAR(45),
    userAgent TEXT,
    activityDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for activity_logs table
CREATE INDEX idx_activity_logs_userId ON activity_logs(userId);
CREATE INDEX idx_activity_logs_activityType ON activity_logs(activityType);
CREATE INDEX idx_activity_logs_activityDate ON activity_logs(activityDate);
CREATE INDEX idx_activity_logs_entityType ON activity_logs(entityType);

-- ✅ TABLE 17: admin_sms_logs (SMS sent by admins)
CREATE TABLE admin_sms_logs (
    id SERIAL PRIMARY KEY,
    adminId INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recipientNumber VARCHAR(20) NOT NULL,
    recipientName VARCHAR(255),
    messageContent TEXT NOT NULL,
    messageType VARCHAR(50), -- 'notification', 'alert', 'reminder', 'update', etc.
    status VARCHAR(50) DEFAULT 'sent', -- 'sent', 'failed', 'pending', 'delivered'
    sentAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deliveredAt TIMESTAMP,
    failureReason TEXT,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for admin_sms_logs table
CREATE INDEX idx_admin_sms_logs_adminId ON admin_sms_logs(adminId);
CREATE INDEX idx_admin_sms_logs_status ON admin_sms_logs(status);
CREATE INDEX idx_admin_sms_logs_sentAt ON admin_sms_logs(sentAt);

-- ✅ TABLE 18: blotter_reports (Police blotter reports)
CREATE TABLE blotter_reports (
    id SERIAL PRIMARY KEY,
    reportNumber VARCHAR(100) UNIQUE NOT NULL,
    reportDate DATE NOT NULL,
    reportTime TIME,
    location VARCHAR(255),
    incidentType VARCHAR(100), -- 'theft', 'assault', 'accident', 'lost_property', etc.
    description TEXT,
    reportedBy INTEGER REFERENCES users(id) ON DELETE SET NULL,
    status VARCHAR(50) DEFAULT 'filed', -- 'filed', 'under_investigation', 'resolved', 'archived'
    severity VARCHAR(50), -- 'low', 'medium', 'high', 'critical'
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_blotter_status CHECK (status IN ('filed', 'under_investigation', 'resolved', 'archived'))
);

-- Create indexes for blotter_reports table
CREATE INDEX idx_blotter_reports_reportNumber ON blotter_reports(reportNumber);
CREATE INDEX idx_blotter_reports_reportDate ON blotter_reports(reportDate);
CREATE INDEX idx_blotter_reports_status ON blotter_reports(status);
CREATE INDEX idx_blotter_reports_incidentType ON blotter_reports(incidentType);

-- ✅ TABLE 19: case_evidence (Evidence for cases)
CREATE TABLE case_evidence (
    id SERIAL PRIMARY KEY,
    caseId INTEGER NOT NULL REFERENCES case_timeline(id) ON DELETE CASCADE,
    evidenceType VARCHAR(100), -- 'physical', 'digital', 'document', 'photo', 'video', etc.
    evidenceDescription TEXT,
    evidenceLocation VARCHAR(255),
    collectedDate DATE,
    collectedBy INTEGER REFERENCES users(id) ON DELETE SET NULL,
    chainOfCustodyNotes TEXT,
    status VARCHAR(50) DEFAULT 'stored', -- 'collected', 'stored', 'analyzed', 'destroyed', 'released'
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for case_evidence table
CREATE INDEX idx_case_evidence_caseId ON case_evidence(caseId);
CREATE INDEX idx_case_evidence_evidenceType ON case_evidence(evidenceType);
CREATE INDEX idx_case_evidence_status ON case_evidence(status);

-- ✅ TABLE 20: fcm_tokens (Firebase Cloud Messaging tokens)
CREATE TABLE fcm_tokens (
    id SERIAL PRIMARY KEY,
    token VARCHAR(500) UNIQUE NOT NULL,
    deviceType VARCHAR(50), -- 'android', 'ios', 'web'
    deviceName VARCHAR(255),
    isActive BOOLEAN DEFAULT TRUE,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    lastUsedAt TIMESTAMP,
    expiresAt TIMESTAMP
);

-- Create indexes for fcm_tokens table
CREATE INDEX idx_fcm_tokens_token ON fcm_tokens(token);
CREATE INDEX idx_fcm_tokens_isActive ON fcm_tokens(isActive);

-- ✅ TABLE 21: sms_notifications (SMS notifications sent)
CREATE TABLE sms_notifications (
    id SERIAL PRIMARY KEY,
    recipientNumber VARCHAR(20) NOT NULL,
    recipientName VARCHAR(255),
    messageContent TEXT NOT NULL,
    notificationType VARCHAR(50), -- 'case_update', 'hearing_reminder', 'alert', etc.
    relatedCaseId INTEGER REFERENCES case_timeline(id) ON DELETE SET NULL,
    status VARCHAR(50) DEFAULT 'sent', -- 'sent', 'failed', 'pending', 'delivered'
    sentAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deliveredAt TIMESTAMP,
    failureReason TEXT,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for sms_notifications table
CREATE INDEX idx_sms_notifications_recipientNumber ON sms_notifications(recipientNumber);
CREATE INDEX idx_sms_notifications_status ON sms_notifications(status);
CREATE INDEX idx_sms_notifications_sentAt ON sms_notifications(sentAt);

-- ✅ VERIFY SETUP
SELECT 
    'activity_logs' as table_name,
    COUNT(*) as row_count
FROM activity_logs
UNION ALL
SELECT 
    'admin_sms_logs' as table_name,
    COUNT(*) as row_count
FROM admin_sms_logs
UNION ALL
SELECT 
    'blotter_reports' as table_name,
    COUNT(*) as row_count
FROM blotter_reports
UNION ALL
SELECT 
    'case_evidence' as table_name,
    COUNT(*) as row_count
FROM case_evidence
UNION ALL
SELECT 
    'fcm_tokens' as table_name,
    COUNT(*) as row_count
FROM fcm_tokens
UNION ALL
SELECT 
    'sms_notifications' as table_name,
    COUNT(*) as row_count
FROM sms_notifications;

-- ✅ STEP 5 COMPLETE
-- 6 additional tables created successfully!
-- Total tables so far: 21
-- Next: Run BMS_STEP_6_FINAL_TABLES.sql
