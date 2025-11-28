-- ============================================================================
-- CASE TIMELINE SCHEMA FOR ALL 3 ROLES
-- Stores case status updates, investigations, and timeline events
-- Syncs between Local SQLite and Neon PostgreSQL
-- ============================================================================

-- ============================================================================
-- STEP 1: CREATE CASE_TIMELINE TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS case_timeline (
    id SERIAL PRIMARY KEY,
    case_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'Admin', 'Officer', 'User'
    event_type VARCHAR(50) NOT NULL, -- 'created', 'updated', 'assigned', 'investigated', 'resolved', 'closed'
    event_title VARCHAR(255) NOT NULL,
    event_description TEXT,
    status VARCHAR(50), -- 'pending', 'ongoing', 'resolved', 'closed'
    priority VARCHAR(20) DEFAULT 'medium', -- 'low', 'medium', 'high', 'critical'
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_case_timeline_case FOREIGN KEY (case_id) REFERENCES blotter_reports(id) ON DELETE CASCADE,
    CONSTRAINT fk_case_timeline_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for faster queries
CREATE INDEX IF NOT EXISTS idx_case_timeline_case_id ON case_timeline(case_id);
CREATE INDEX IF NOT EXISTS idx_case_timeline_user_id ON case_timeline(user_id);
CREATE INDEX IF NOT EXISTS idx_case_timeline_role ON case_timeline(role);
CREATE INDEX IF NOT EXISTS idx_case_timeline_event_type ON case_timeline(event_type);
CREATE INDEX IF NOT EXISTS idx_case_timeline_status ON case_timeline(status);
CREATE INDEX IF NOT EXISTS idx_case_timeline_timestamp ON case_timeline(timestamp);

-- ============================================================================
-- STEP 2: CREATE CASE_STATUS_HISTORY TABLE
-- Tracks all status changes for audit trail
-- ============================================================================
CREATE TABLE IF NOT EXISTS case_status_history (
    id SERIAL PRIMARY KEY,
    case_id VARCHAR(255) NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by VARCHAR(255) NOT NULL, -- user_id
    changed_by_role VARCHAR(20) NOT NULL, -- 'Admin', 'Officer', 'User'
    reason TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_case_status_history_case FOREIGN KEY (case_id) REFERENCES blotter_reports(id) ON DELETE CASCADE,
    CONSTRAINT fk_case_status_history_user FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_case_status_history_case_id ON case_status_history(case_id);
CREATE INDEX IF NOT EXISTS idx_case_status_history_changed_by ON case_status_history(changed_by);
CREATE INDEX IF NOT EXISTS idx_case_status_history_timestamp ON case_status_history(timestamp);

-- ============================================================================
-- STEP 3: CREATE CASE_INVESTIGATION_LOG TABLE
-- For Officer role - track investigation progress
-- ============================================================================
CREATE TABLE IF NOT EXISTS case_investigation_log (
    id SERIAL PRIMARY KEY,
    case_id VARCHAR(255) NOT NULL,
    officer_id VARCHAR(255) NOT NULL,
    investigation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    investigation_type VARCHAR(50), -- 'field_visit', 'interview', 'evidence_collection', 'analysis'
    findings TEXT,
    evidence_collected TEXT,
    witnesses_interviewed INT DEFAULT 0,
    suspects_identified INT DEFAULT 0,
    next_steps TEXT,
    status VARCHAR(50) DEFAULT 'ongoing', -- 'ongoing', 'completed', 'pending'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_case_investigation_log_case FOREIGN KEY (case_id) REFERENCES blotter_reports(id) ON DELETE CASCADE,
    CONSTRAINT fk_case_investigation_log_officer FOREIGN KEY (officer_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_case_investigation_log_case_id ON case_investigation_log(case_id);
CREATE INDEX IF NOT EXISTS idx_case_investigation_log_officer_id ON case_investigation_log(officer_id);
CREATE INDEX IF NOT EXISTS idx_case_investigation_log_date ON case_investigation_log(investigation_date);

-- ============================================================================
-- STEP 4: CREATE CASE_ADMIN_NOTES TABLE
-- For Admin role - administrative notes and oversight
-- ============================================================================
CREATE TABLE IF NOT EXISTS case_admin_notes (
    id SERIAL PRIMARY KEY,
    case_id VARCHAR(255) NOT NULL,
    admin_id VARCHAR(255) NOT NULL,
    note_type VARCHAR(50), -- 'oversight', 'review', 'approval', 'rejection', 'escalation'
    note_content TEXT NOT NULL,
    priority VARCHAR(20) DEFAULT 'medium',
    status VARCHAR(50) DEFAULT 'active', -- 'active', 'resolved', 'archived'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_case_admin_notes_case FOREIGN KEY (case_id) REFERENCES blotter_reports(id) ON DELETE CASCADE,
    CONSTRAINT fk_case_admin_notes_admin FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_case_admin_notes_case_id ON case_admin_notes(case_id);
CREATE INDEX IF NOT EXISTS idx_case_admin_notes_admin_id ON case_admin_notes(admin_id);
CREATE INDEX IF NOT EXISTS idx_case_admin_notes_status ON case_admin_notes(status);

-- ============================================================================
-- STEP 5: CREATE CASE_USER_UPDATES TABLE
-- For User role - case updates and notifications
-- ============================================================================
CREATE TABLE IF NOT EXISTS case_user_updates (
    id SERIAL PRIMARY KEY,
    case_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    update_type VARCHAR(50), -- 'filed', 'updated', 'status_change', 'notification'
    update_content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_case_user_updates_case FOREIGN KEY (case_id) REFERENCES blotter_reports(id) ON DELETE CASCADE,
    CONSTRAINT fk_case_user_updates_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_case_user_updates_case_id ON case_user_updates(case_id);
CREATE INDEX IF NOT EXISTS idx_case_user_updates_user_id ON case_user_updates(user_id);
CREATE INDEX IF NOT EXISTS idx_case_user_updates_is_read ON case_user_updates(is_read);

-- ============================================================================
-- STEP 6: CREATE FUNCTIONS FOR CASE TIMELINE MANAGEMENT
-- ============================================================================

-- Function to add timeline event
CREATE OR REPLACE FUNCTION add_case_timeline_event(
    p_case_id VARCHAR(255),
    p_user_id VARCHAR(255),
    p_role VARCHAR(20),
    p_event_type VARCHAR(50),
    p_event_title VARCHAR(255),
    p_event_description TEXT,
    p_status VARCHAR(50)
) RETURNS JSON AS $$
DECLARE
    v_timeline_id INT;
    result JSON;
BEGIN
    INSERT INTO case_timeline (case_id, user_id, role, event_type, event_title, event_description, status)
    VALUES (p_case_id, p_user_id, p_role, p_event_type, p_event_title, p_event_description, p_status)
    RETURNING id INTO v_timeline_id;
    
    result := json_build_object(
        'success', true,
        'message', 'Timeline event added successfully',
        'timeline_id', v_timeline_id
    );
    
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Function to get case timeline
CREATE OR REPLACE FUNCTION get_case_timeline(p_case_id VARCHAR(255))
RETURNS TABLE (
    id INT,
    event_type VARCHAR,
    event_title VARCHAR,
    event_description TEXT,
    status VARCHAR,
    role VARCHAR,
    username VARCHAR,
    timestamp TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        ct.id,
        ct.event_type,
        ct.event_title,
        ct.event_description,
        ct.status,
        ct.role,
        u.username,
        ct.timestamp
    FROM case_timeline ct
    JOIN users u ON ct.user_id = u.id
    WHERE ct.case_id = p_case_id
    ORDER BY ct.timestamp DESC;
END;
$$ LANGUAGE plpgsql;

-- Function to update case status and log history
CREATE OR REPLACE FUNCTION update_case_status_with_history(
    p_case_id VARCHAR(255),
    p_new_status VARCHAR(50),
    p_changed_by VARCHAR(255),
    p_changed_by_role VARCHAR(20),
    p_reason TEXT
) RETURNS JSON AS $$
DECLARE
    v_old_status VARCHAR(50);
    result JSON;
BEGIN
    -- Get current status
    SELECT status INTO v_old_status FROM blotter_reports WHERE id = p_case_id;
    
    -- Update case status
    UPDATE blotter_reports SET status = p_new_status WHERE id = p_case_id;
    
    -- Log status change
    INSERT INTO case_status_history (case_id, old_status, new_status, changed_by, changed_by_role, reason)
    VALUES (p_case_id, v_old_status, p_new_status, p_changed_by, p_changed_by_role, p_reason);
    
    -- Add timeline event
    INSERT INTO case_timeline (case_id, user_id, role, event_type, event_title, event_description, status)
    VALUES (p_case_id, p_changed_by, p_changed_by_role, 'status_change', 
            'Status Changed: ' || v_old_status || ' → ' || p_new_status, p_reason, p_new_status);
    
    result := json_build_object(
        'success', true,
        'message', 'Case status updated successfully',
        'old_status', v_old_status,
        'new_status', p_new_status
    );
    
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Function to add investigation log (Officer)
CREATE OR REPLACE FUNCTION add_investigation_log(
    p_case_id VARCHAR(255),
    p_officer_id VARCHAR(255),
    p_investigation_type VARCHAR(50),
    p_findings TEXT,
    p_evidence_collected TEXT,
    p_witnesses_interviewed INT,
    p_suspects_identified INT,
    p_next_steps TEXT
) RETURNS JSON AS $$
DECLARE
    v_log_id INT;
    result JSON;
BEGIN
    INSERT INTO case_investigation_log (
        case_id, officer_id, investigation_type, findings, 
        evidence_collected, witnesses_interviewed, suspects_identified, next_steps
    ) VALUES (
        p_case_id, p_officer_id, p_investigation_type, p_findings,
        p_evidence_collected, p_witnesses_interviewed, p_suspects_identified, p_next_steps
    )
    RETURNING id INTO v_log_id;
    
    -- Add timeline event
    INSERT INTO case_timeline (case_id, user_id, role, event_type, event_title, event_description, status)
    VALUES (p_case_id, p_officer_id, 'Officer', 'investigated', 
            'Investigation: ' || p_investigation_type, p_findings, 'ongoing');
    
    result := json_build_object(
        'success', true,
        'message', 'Investigation log added successfully',
        'log_id', v_log_id
    );
    
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- STEP 7: ADD CASCADE DELETE TO BLOTTER_REPORTS
-- ============================================================================
ALTER TABLE IF EXISTS blotter_reports
DROP CONSTRAINT IF EXISTS fk_blotter_reports_user_id CASCADE;

ALTER TABLE IF EXISTS blotter_reports
ADD CONSTRAINT fk_blotter_reports_user_id 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- ============================================================================
-- STEP 8: VERIFICATION QUERIES
-- ============================================================================

-- View all case timeline tables
SELECT table_name 
FROM information_schema.tables 
WHERE table_name LIKE 'case_%' AND table_schema = 'public'
ORDER BY table_name;

-- View all case timeline functions
SELECT routine_name 
FROM information_schema.routines 
WHERE routine_schema = 'public' 
AND (routine_name LIKE '%timeline%' OR routine_name LIKE '%investigation%' OR routine_name LIKE '%status%')
ORDER BY routine_name;

-- ============================================================================
-- SUMMARY OF CASE TIMELINE SCHEMA
-- ============================================================================
-- ✅ case_timeline - Main timeline events for all roles
-- ✅ case_status_history - Audit trail of status changes
-- ✅ case_investigation_log - Officer investigation tracking
-- ✅ case_admin_notes - Admin oversight and notes
-- ✅ case_user_updates - User notifications and updates
-- ✅ Functions for timeline management
-- ✅ CASCADE DELETE for data integrity
-- ✅ Indexes for performance optimization
-- ============================================================================
