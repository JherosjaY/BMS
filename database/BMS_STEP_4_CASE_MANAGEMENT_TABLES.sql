-- 📋 STEP 4: CASE MANAGEMENT TABLES
-- Manages case timeline, status history, investigation logs, admin notes, and user updates
-- Run this AFTER STEP 3

-- ✅ TABLE 11: case_timeline (Main case information and timeline)
CREATE TABLE case_timeline (
    id SERIAL PRIMARY KEY,
    caseNumber VARCHAR(100) UNIQUE NOT NULL,
    caseTitle VARCHAR(255),
    caseDescription TEXT,
    caseType VARCHAR(100), -- 'criminal', 'civil', 'administrative', etc.
    status VARCHAR(50) DEFAULT 'open', -- 'open', 'under_investigation', 'closed', 'archived'
    severity VARCHAR(50), -- 'low', 'medium', 'high', 'critical'
    reportedDate DATE,
    reportedBy INTEGER REFERENCES users(id) ON DELETE SET NULL,
    assignedTo INTEGER REFERENCES users(id) ON DELETE SET NULL,
    location VARCHAR(255),
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_case_status CHECK (status IN ('open', 'under_investigation', 'closed', 'archived')),
    CONSTRAINT valid_severity CHECK (severity IN ('low', 'medium', 'high', 'critical'))
);

-- Create indexes for case_timeline table
CREATE INDEX idx_case_timeline_caseNumber ON case_timeline(caseNumber);
CREATE INDEX idx_case_timeline_status ON case_timeline(status);
CREATE INDEX idx_case_timeline_severity ON case_timeline(severity);
CREATE INDEX idx_case_timeline_reportedDate ON case_timeline(reportedDate);
CREATE INDEX idx_case_timeline_assignedTo ON case_timeline(assignedTo);

-- ✅ TABLE 12: case_status_history (Track status changes)
CREATE TABLE case_status_history (
    id SERIAL PRIMARY KEY,
    caseId INTEGER NOT NULL REFERENCES case_timeline(id) ON DELETE CASCADE,
    previousStatus VARCHAR(50),
    newStatus VARCHAR(50) NOT NULL,
    changedBy INTEGER REFERENCES users(id) ON DELETE SET NULL,
    changeReason TEXT,
    changedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for case_status_history table
CREATE INDEX idx_case_status_history_caseId ON case_status_history(caseId);
CREATE INDEX idx_case_status_history_changedAt ON case_status_history(changedAt);

-- ✅ TABLE 13: case_investigation_log (Investigation activities)
CREATE TABLE case_investigation_log (
    id SERIAL PRIMARY KEY,
    caseId INTEGER NOT NULL REFERENCES case_timeline(id) ON DELETE CASCADE,
    investigationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    investigator INTEGER REFERENCES users(id) ON DELETE SET NULL,
    activityType VARCHAR(100), -- 'interview', 'evidence_collection', 'site_visit', 'report_filed', etc.
    description TEXT,
    findings TEXT,
    evidenceCollected TEXT,
    nextSteps TEXT,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for case_investigation_log table
CREATE INDEX idx_case_investigation_log_caseId ON case_investigation_log(caseId);
CREATE INDEX idx_case_investigation_log_investigator ON case_investigation_log(investigator);
CREATE INDEX idx_case_investigation_log_investigationDate ON case_investigation_log(investigationDate);

-- ✅ TABLE 14: case_admin_notes (Administrative notes and comments)
CREATE TABLE case_admin_notes (
    id SERIAL PRIMARY KEY,
    caseId INTEGER NOT NULL REFERENCES case_timeline(id) ON DELETE CASCADE,
    noteAuthor INTEGER REFERENCES users(id) ON DELETE SET NULL,
    noteContent TEXT NOT NULL,
    noteType VARCHAR(50), -- 'general', 'urgent', 'follow_up', 'decision', etc.
    isImportant BOOLEAN DEFAULT FALSE,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for case_admin_notes table
CREATE INDEX idx_case_admin_notes_caseId ON case_admin_notes(caseId);
CREATE INDEX idx_case_admin_notes_noteAuthor ON case_admin_notes(noteAuthor);
CREATE INDEX idx_case_admin_notes_isImportant ON case_admin_notes(isImportant);

-- ✅ TABLE 15: case_user_updates (Updates visible to users)
CREATE TABLE case_user_updates (
    id SERIAL PRIMARY KEY,
    caseId INTEGER NOT NULL REFERENCES case_timeline(id) ON DELETE CASCADE,
    updateAuthor INTEGER REFERENCES users(id) ON DELETE SET NULL,
    updateContent TEXT NOT NULL,
    updateType VARCHAR(50), -- 'status_change', 'hearing_scheduled', 'evidence_found', 'verdict', etc.
    isPublic BOOLEAN DEFAULT TRUE,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for case_user_updates table
CREATE INDEX idx_case_user_updates_caseId ON case_user_updates(caseId);
CREATE INDEX idx_case_user_updates_updateAuthor ON case_user_updates(updateAuthor);
CREATE INDEX idx_case_user_updates_isPublic ON case_user_updates(isPublic);

-- ✅ VERIFY SETUP
SELECT 
    'case_timeline' as table_name,
    COUNT(*) as row_count
FROM case_timeline
UNION ALL
SELECT 
    'case_status_history' as table_name,
    COUNT(*) as row_count
FROM case_status_history
UNION ALL
SELECT 
    'case_investigation_log' as table_name,
    COUNT(*) as row_count
FROM case_investigation_log
UNION ALL
SELECT 
    'case_admin_notes' as table_name,
    COUNT(*) as row_count
FROM case_admin_notes
UNION ALL
SELECT 
    'case_user_updates' as table_name,
    COUNT(*) as row_count
FROM case_user_updates;

-- ✅ STEP 4 COMPLETE
-- Case management tables created successfully!
-- All 15 tables are now ready!
-- Next: Run BMS_STEP_5_FUNCTIONS_AND_INDEXES.sql for optimization
