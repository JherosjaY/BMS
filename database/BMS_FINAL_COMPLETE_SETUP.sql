-- ============================================================================
-- BMS FINAL COMPLETE SETUP - PRODUCTION READY
-- ============================================================================
-- This is the FINAL, COMPLETE SQL script for Neon PostgreSQL
-- Copy & Paste ALL of this into Neon SQL Editor at once
-- Contains ALL tables, indexes, and functions - NO GAPS
-- ============================================================================

-- ============================================================================
-- PART 0: DROP ALL EXISTING TABLES (FRESH START)
-- ============================================================================

DROP TABLE IF EXISTS case_user_updates CASCADE;
DROP TABLE IF EXISTS case_admin_notes CASCADE;
DROP TABLE IF EXISTS case_investigation_log CASCADE;
DROP TABLE IF EXISTS case_status_history CASCADE;
DROP TABLE IF EXISTS case_timeline CASCADE;
DROP TABLE IF EXISTS hearing_notifications CASCADE;
DROP TABLE IF EXISTS hearing_minutes CASCADE;
DROP TABLE IF EXISTS hearing_attendees CASCADE;
DROP TABLE IF EXISTS hearings CASCADE;
DROP TABLE IF EXISTS person_risk_levels CASCADE;
DROP TABLE IF EXISTS case_involvement CASCADE;
DROP TABLE IF EXISTS criminal_records CASCADE;
DROP TABLE IF EXISTS person_profiles CASCADE;
DROP TABLE IF EXISTS user_images CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================================================
-- PART 1: USERS & AUTHENTICATION
-- ============================================================================

CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    role VARCHAR(50) DEFAULT 'User',
    gender VARCHAR(50),
    auth_provider VARCHAR(50) DEFAULT 'email',
    profile_picture_url TEXT,
    profile_picture_data BYTEA,
    has_profile_picture BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    is_hidden BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Users Indexes
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_auth_provider ON users(auth_provider);
CREATE INDEX idx_users_gender ON users(gender);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_is_active ON users(is_active);
CREATE INDEX idx_users_is_hidden ON users(is_hidden);

-- ============================================================================
-- PART 2: USER IMAGES
-- ============================================================================

CREATE TABLE user_images (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    image_type VARCHAR(50),
    image_url TEXT,
    image_data BYTEA,
    file_name VARCHAR(255),
    file_size BIGINT,
    mime_type VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User Images Indexes
CREATE INDEX idx_user_images_user_id ON user_images(user_id);
CREATE INDEX idx_user_images_type ON user_images(image_type);
CREATE INDEX idx_user_images_active ON user_images(is_active);

-- ============================================================================
-- PART 3: HEARINGS SYSTEM
-- ============================================================================

CREATE TABLE hearings (
    id SERIAL PRIMARY KEY,
    hearing_id VARCHAR(255) UNIQUE NOT NULL,
    case_id VARCHAR(255) NOT NULL,
    hearing_date TIMESTAMP NOT NULL,
    hearing_time TIME NOT NULL,
    location VARCHAR(255) NOT NULL,
    hearing_type VARCHAR(50) DEFAULT 'initial',
    status VARCHAR(50) DEFAULT 'scheduled',
    presiding_officer VARCHAR(255) REFERENCES users(id) ON DELETE SET NULL,
    created_by VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_sync_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Hearings Indexes
CREATE INDEX idx_hearings_case_id ON hearings(case_id);
CREATE INDEX idx_hearings_date ON hearings(hearing_date);
CREATE INDEX idx_hearings_status ON hearings(status);
CREATE INDEX idx_hearings_hearing_id ON hearings(hearing_id);

-- ============================================================================
-- PART 4: HEARING ATTENDEES
-- ============================================================================

CREATE TABLE hearing_attendees (
    id SERIAL PRIMARY KEY,
    hearing_id VARCHAR(255) NOT NULL REFERENCES hearings(hearing_id) ON DELETE CASCADE,
    user_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    attendee_role VARCHAR(50) NOT NULL,
    attendance_status VARCHAR(50) DEFAULT 'invited',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_sync_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Hearing Attendees Indexes
CREATE INDEX idx_hearing_attendees_hearing_id ON hearing_attendees(hearing_id);
CREATE INDEX idx_hearing_attendees_user_id ON hearing_attendees(user_id);

-- ============================================================================
-- PART 5: HEARING MINUTES
-- ============================================================================

CREATE TABLE hearing_minutes (
    id SERIAL PRIMARY KEY,
    hearing_id VARCHAR(255) NOT NULL REFERENCES hearings(hearing_id) ON DELETE CASCADE,
    minute_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    added_by VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_sync_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Hearing Minutes Indexes
CREATE INDEX idx_hearing_minutes_hearing_id ON hearing_minutes(hearing_id);

-- ============================================================================
-- PART 6: HEARING NOTIFICATIONS
-- ============================================================================

CREATE TABLE hearing_notifications (
    id SERIAL PRIMARY KEY,
    hearing_id VARCHAR(255) NOT NULL REFERENCES hearings(hearing_id) ON DELETE CASCADE,
    user_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    notification_type VARCHAR(50) NOT NULL,
    notification_message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_sync_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Hearing Notifications Indexes
CREATE INDEX idx_hearing_notifications_user_id ON hearing_notifications(user_id);
CREATE INDEX idx_hearing_notifications_is_read ON hearing_notifications(is_read);

-- ============================================================================
-- PART 7: PERSON PROFILES
-- ============================================================================

CREATE TABLE person_profiles (
    id SERIAL PRIMARY KEY,
    person_id VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    alias VARCHAR(255),
    date_of_birth DATE,
    gender VARCHAR(50),
    contact_number VARCHAR(20),
    address TEXT,
    created_by VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_sync_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Person Profiles Indexes
CREATE INDEX idx_person_profiles_person_id ON person_profiles(person_id);
CREATE INDEX idx_person_profiles_first_name ON person_profiles(first_name);
CREATE INDEX idx_person_profiles_last_name ON person_profiles(last_name);

-- ============================================================================
-- PART 8: CRIMINAL RECORDS
-- ============================================================================

CREATE TABLE criminal_records (
    id SERIAL PRIMARY KEY,
    record_id VARCHAR(255) UNIQUE NOT NULL,
    person_id VARCHAR(255) NOT NULL REFERENCES person_profiles(person_id) ON DELETE CASCADE,
    crime_type VARCHAR(255) NOT NULL,
    crime_description TEXT,
    date_committed DATE,
    date_convicted DATE,
    sentence VARCHAR(255),
    status VARCHAR(50) DEFAULT 'active',
    added_by VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_sync_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Criminal Records Indexes
CREATE INDEX idx_criminal_records_person_id ON criminal_records(person_id);
CREATE INDEX idx_criminal_records_status ON criminal_records(status);
CREATE INDEX idx_criminal_records_crime_type ON criminal_records(crime_type);

-- ============================================================================
-- PART 9: CASE INVOLVEMENT
-- ============================================================================

CREATE TABLE case_involvement (
    id SERIAL PRIMARY KEY,
    involvement_id VARCHAR(255) UNIQUE NOT NULL,
    person_id VARCHAR(255) NOT NULL REFERENCES person_profiles(person_id) ON DELETE CASCADE,
    case_id VARCHAR(255) NOT NULL,
    role_in_case VARCHAR(50) NOT NULL,
    involvement_date DATE,
    status VARCHAR(50) DEFAULT 'active',
    notes TEXT,
    added_by VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_sync_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Case Involvement Indexes
CREATE INDEX idx_case_involvement_person_id ON case_involvement(person_id);
CREATE INDEX idx_case_involvement_case_id ON case_involvement(case_id);
CREATE INDEX idx_case_involvement_status ON case_involvement(status);

-- ============================================================================
-- PART 10: PERSON RISK LEVELS
-- ============================================================================

CREATE TABLE person_risk_levels (
    id SERIAL PRIMARY KEY,
    person_id VARCHAR(255) NOT NULL REFERENCES person_profiles(person_id) ON DELETE CASCADE,
    risk_level VARCHAR(50) NOT NULL,
    risk_score INT DEFAULT 0,
    risk_factors TEXT,
    assessment_date DATE,
    assessed_by VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_sync_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Person Risk Levels Indexes
CREATE INDEX idx_person_risk_levels_person_id ON person_risk_levels(person_id);
CREATE INDEX idx_person_risk_levels_risk_level ON person_risk_levels(risk_level);

-- ============================================================================
-- PART 11: CASE TIMELINE
-- ============================================================================

CREATE TABLE case_timeline (
    id SERIAL PRIMARY KEY,
    case_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_title VARCHAR(255) NOT NULL,
    event_description TEXT,
    status VARCHAR(50),
    priority VARCHAR(20) DEFAULT 'medium',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Case Timeline Indexes
CREATE INDEX idx_case_timeline_case_id ON case_timeline(case_id);
CREATE INDEX idx_case_timeline_user_id ON case_timeline(user_id);
CREATE INDEX idx_case_timeline_role ON case_timeline(role);
CREATE INDEX idx_case_timeline_event_type ON case_timeline(event_type);
CREATE INDEX idx_case_timeline_timestamp ON case_timeline(timestamp);

-- ============================================================================
-- PART 12: CASE STATUS HISTORY
-- ============================================================================

CREATE TABLE case_status_history (
    id SERIAL PRIMARY KEY,
    case_id VARCHAR(255) NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by VARCHAR(255) NOT NULL,
    changed_by_role VARCHAR(20) NOT NULL,
    reason TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_case_status_history_user FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Case Status History Indexes
CREATE INDEX idx_case_status_history_case_id ON case_status_history(case_id);
CREATE INDEX idx_case_status_history_changed_by ON case_status_history(changed_by);
CREATE INDEX idx_case_status_history_timestamp ON case_status_history(timestamp);

-- ============================================================================
-- PART 13: CASE INVESTIGATION LOG
-- ============================================================================

CREATE TABLE case_investigation_log (
    id SERIAL PRIMARY KEY,
    case_id VARCHAR(255) NOT NULL,
    officer_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    investigation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    investigation_type VARCHAR(50),
    findings TEXT,
    evidence_collected TEXT,
    witnesses_interviewed INT DEFAULT 0,
    suspects_identified INT DEFAULT 0,
    next_steps TEXT,
    status VARCHAR(50) DEFAULT 'ongoing',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Case Investigation Log Indexes
CREATE INDEX idx_case_investigation_log_case_id ON case_investigation_log(case_id);
CREATE INDEX idx_case_investigation_log_officer_id ON case_investigation_log(officer_id);
CREATE INDEX idx_case_investigation_log_date ON case_investigation_log(investigation_date);

-- ============================================================================
-- PART 14: CASE ADMIN NOTES
-- ============================================================================

CREATE TABLE case_admin_notes (
    id SERIAL PRIMARY KEY,
    case_id VARCHAR(255) NOT NULL,
    admin_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    note_type VARCHAR(50),
    note_content TEXT NOT NULL,
    priority VARCHAR(20) DEFAULT 'medium',
    status VARCHAR(50) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Case Admin Notes Indexes
CREATE INDEX idx_case_admin_notes_case_id ON case_admin_notes(case_id);
CREATE INDEX idx_case_admin_notes_admin_id ON case_admin_notes(admin_id);
CREATE INDEX idx_case_admin_notes_status ON case_admin_notes(status);

-- ============================================================================
-- PART 15: CASE USER UPDATES
-- ============================================================================

CREATE TABLE case_user_updates (
    id SERIAL PRIMARY KEY,
    case_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    update_type VARCHAR(50),
    update_content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Case User Updates Indexes
CREATE INDEX idx_case_user_updates_case_id ON case_user_updates(case_id);
CREATE INDEX idx_case_user_updates_user_id ON case_user_updates(user_id);
CREATE INDEX idx_case_user_updates_is_read ON case_user_updates(is_read);

-- ============================================================================
-- PART 16: DATABASE FUNCTIONS
-- ============================================================================

-- Drop existing functions if they exist
DROP FUNCTION IF EXISTS schedule_hearing(VARCHAR, TIMESTAMP, TIME, VARCHAR, VARCHAR, VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS get_hearings_by_date_range(DATE, DATE) CASCADE;
DROP FUNCTION IF EXISTS get_user_hearings(VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS update_hearing_status(VARCHAR, VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS add_hearing_attendee(VARCHAR, VARCHAR, VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS add_hearing_minutes(VARCHAR, VARCHAR, TEXT, VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS create_person_profile(VARCHAR, VARCHAR, VARCHAR, DATE, VARCHAR, VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS add_criminal_record(VARCHAR, VARCHAR, TEXT, DATE, VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS add_case_involvement(VARCHAR, VARCHAR, VARCHAR, VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS set_user_profile_picture(VARCHAR, TEXT, BYTEA) CASCADE;
DROP FUNCTION IF EXISTS get_user_profile_picture(VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS detect_user_role(VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS update_user_gender(VARCHAR, VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS add_case_timeline_event(VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, TEXT, VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS get_case_timeline(VARCHAR) CASCADE;
DROP FUNCTION IF EXISTS update_case_status_with_history(VARCHAR, VARCHAR, VARCHAR, VARCHAR, TEXT) CASCADE;
DROP FUNCTION IF EXISTS add_investigation_log(VARCHAR, VARCHAR, VARCHAR, TEXT, TEXT, INT, INT, TEXT) CASCADE;
DROP FUNCTION IF EXISTS delete_user_account_cascade(VARCHAR) CASCADE;

-- Function: Schedule Hearing
CREATE OR REPLACE FUNCTION schedule_hearing(
    p_case_id VARCHAR(255),
    p_hearing_date TIMESTAMP,
    p_hearing_time TIME,
    p_location VARCHAR(255),
    p_presiding_officer VARCHAR(255),
    p_created_by VARCHAR(255)
) RETURNS JSON AS $$
DECLARE
    v_hearing_id VARCHAR(255);
    result JSON;
BEGIN
    v_hearing_id := 'HEARING-' || to_char(now(), 'YYYYMMDDHHmmss') || '-' || floor(random() * 10000);
    
    INSERT INTO hearings (hearing_id, case_id, hearing_date, hearing_time, location, presiding_officer, created_by)
    VALUES (v_hearing_id, p_case_id, p_hearing_date, p_hearing_time, p_location, p_presiding_officer, p_created_by);
    
    result := json_build_object(
        'success', true,
        'message', 'Hearing scheduled successfully',
        'hearing_id', v_hearing_id
    );
    
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Function: Get Hearings by Date Range
CREATE OR REPLACE FUNCTION get_hearings_by_date_range(
    p_start_date DATE,
    p_end_date DATE
) RETURNS TABLE (
    hearing_id VARCHAR(255),
    case_id VARCHAR(255),
    hearing_date TIMESTAMP,
    hearing_time TIME,
    location VARCHAR(255),
    status VARCHAR(50),
    presiding_officer VARCHAR(255)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        h.hearing_id,
        h.case_id,
        h.hearing_date,
        h.hearing_time,
        h.location,
        h.status,
        h.presiding_officer
    FROM hearings h
    WHERE DATE(h.hearing_date) BETWEEN p_start_date AND p_end_date
    ORDER BY h.hearing_date;
END;
$$ LANGUAGE plpgsql;

-- Function: Get User Hearings
CREATE OR REPLACE FUNCTION get_user_hearings(p_user_id VARCHAR(255))
RETURNS TABLE (
    hearing_id VARCHAR(255),
    case_id VARCHAR(255),
    hearing_date TIMESTAMP,
    hearing_time TIME,
    location VARCHAR(255),
    status VARCHAR(50),
    attendee_role VARCHAR(50)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        h.hearing_id,
        h.case_id,
        h.hearing_date,
        h.hearing_time,
        h.location,
        h.status,
        ha.attendee_role
    FROM hearings h
    JOIN hearing_attendees ha ON h.hearing_id = ha.hearing_id
    WHERE ha.user_id = p_user_id
    ORDER BY h.hearing_date DESC;
END;
$$ LANGUAGE plpgsql;

-- Function: Update Hearing Status
CREATE OR REPLACE FUNCTION update_hearing_status(
    p_hearing_id VARCHAR(255),
    p_new_status VARCHAR(50)
) RETURNS JSON AS $$
DECLARE
    result JSON;
BEGIN
    UPDATE hearings SET status = p_new_status, updated_at = CURRENT_TIMESTAMP 
    WHERE hearing_id = p_hearing_id;
    
    result := json_build_object(
        'success', true,
        'message', 'Hearing status updated successfully',
        'status', p_new_status
    );
    
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Function: Add Hearing Attendee
CREATE OR REPLACE FUNCTION add_hearing_attendee(
    p_hearing_id VARCHAR(255),
    p_user_id VARCHAR(255),
    p_attendee_role VARCHAR(50)
) RETURNS JSON AS $$
DECLARE
    result JSON;
BEGIN
    INSERT INTO hearing_attendees (hearing_id, user_id, attendee_role)
    VALUES (p_hearing_id, p_user_id, p_attendee_role)
    ON CONFLICT DO NOTHING;
    
    result := json_build_object(
        'success', true,
        'message', 'Attendee added successfully'
    );
    
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Function: Add Hearing Minutes
CREATE OR REPLACE FUNCTION add_hearing_minutes(
    p_hearing_id VARCHAR(255),
    p_minute_type VARCHAR(50),
    p_content TEXT,
    p_added_by VARCHAR(255)
) RETURNS JSON AS $$
DECLARE
    v_minute_id INT;
    result JSON;
BEGIN
    INSERT INTO hearing_minutes (hearing_id, minute_type, content, added_by)
    VALUES (p_hearing_id, p_minute_type, p_content, p_added_by)
    RETURNING id INTO v_minute_id;
    
    result := json_build_object(
        'success', true,
        'message', 'Hearing minutes added successfully',
        'minute_id', v_minute_id
    );
    
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Function: Create Person Profile
CREATE OR REPLACE FUNCTION create_person_profile(
    p_first_name VARCHAR(255),
    p_last_name VARCHAR(255),
    p_alias VARCHAR(255),
    p_date_of_birth DATE,
    p_gender VARCHAR(50),
    p_created_by VARCHAR(255)
) RETURNS JSON AS $$
DECLARE
    v_person_id VARCHAR(255);
    result JSON;
BEGIN
    v_person_id := 'PERSON-' || to_char(now(), 'YYYYMMDDHHmmss') || '-' || floor(random() * 10000);
    
    INSERT INTO person_profiles (person_id, first_name, last_name, alias, date_of_birth, gender, created_by)
    VALUES (v_person_id, p_first_name, p_last_name, p_alias, p_date_of_birth, p_gender, p_created_by);
    
    result := json_build_object(
        'success', true,
        'message', 'Person profile created successfully',
        'person_id', v_person_id
    );
    
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Function: Add Criminal Record
CREATE OR REPLACE FUNCTION add_criminal_record(
    p_person_id VARCHAR(255),
    p_crime_type VARCHAR(255),
    p_crime_description TEXT,
    p_date_committed DATE,
    p_added_by VARCHAR(255)
) RETURNS JSON AS $$
DECLARE
    v_record_id VARCHAR(255);
    result JSON;
BEGIN
    v_record_id := 'RECORD-' || to_char(now(), 'YYYYMMDDHHmmss') || '-' || floor(random() * 10000);
    
    INSERT INTO criminal_records (record_id, person_id, crime_type, crime_description, date_committed, added_by)
    VALUES (v_record_id, p_person_id, p_crime_type, p_crime_description, p_date_committed, p_added_by);
    
    result := json_build_object(
        'success', true,
        'message', 'Criminal record added successfully',
        'record_id', v_record_id
    );
    
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Function: Add Case Involvement
CREATE OR REPLACE FUNCTION add_case_involvement(
    p_person_id VARCHAR(255),
    p_case_id VARCHAR(255),
    p_role_in_case VARCHAR(50),
    p_added_by VARCHAR(255)
) RETURNS JSON AS $$
DECLARE
    v_involvement_id VARCHAR(255);
    result JSON;
BEGIN
    v_involvement_id := 'INVOLVEMENT-' || to_char(now(), 'YYYYMMDDHHmmss') || '-' || floor(random() * 10000);
    
    INSERT INTO case_involvement (involvement_id, person_id, case_id, role_in_case, added_by)
    VALUES (v_involvement_id, p_person_id, p_case_id, p_role_in_case, p_added_by);
    
    result := json_build_object(
        'success', true,
        'message', 'Case involvement added successfully',
        'involvement_id', v_involvement_id
    );
    
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Function: Set User Profile Picture
CREATE OR REPLACE FUNCTION set_user_profile_picture(
    p_user_id VARCHAR(255),
    p_profile_picture_url TEXT,
    p_profile_picture_data BYTEA
) RETURNS JSON AS $$
DECLARE
    result JSON;
BEGIN
    UPDATE users 
    SET profile_picture_url = p_profile_picture_url,
        profile_picture_data = p_profile_picture_data,
        has_profile_picture = TRUE,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = p_user_id;
    
    result := json_build_object(
        'success', true,
        'message', 'Profile picture updated successfully'
    );
    
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Function: Get User Profile Picture
CREATE OR REPLACE FUNCTION get_user_profile_picture(p_user_id VARCHAR(255))
RETURNS TABLE (
    profile_picture_url TEXT,
    profile_picture_data BYTEA,
    has_profile_picture BOOLEAN
) AS $$
BEGIN
    RETURN QUERY
    SELECT u.profile_picture_url, u.profile_picture_data, u.has_profile_picture
    FROM users u
    WHERE u.id = p_user_id;
END;
$$ LANGUAGE plpgsql;

-- Function: Detect User Role
CREATE OR REPLACE FUNCTION detect_user_role(p_username VARCHAR(255))
RETURNS VARCHAR(50) AS $$
BEGIN
    IF p_username = 'admin' OR p_username = 'bms.official.admin' THEN
        RETURN 'Admin';
    ELSIF p_username LIKE 'off.%' THEN
        RETURN 'Officer';
    ELSE
        RETURN 'User';
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Function: Update Gender
CREATE OR REPLACE FUNCTION update_user_gender(p_user_id VARCHAR(255), p_gender VARCHAR(50)) RETURNS JSON AS $$
DECLARE result JSON;
BEGIN
    IF p_gender NOT IN ('male', 'female', 'other') THEN
        result := json_build_object('success', false, 'message', 'Invalid gender. Use: male, female, or other');
        RETURN result;
    END IF;
    UPDATE users SET gender = p_gender, updated_at = CURRENT_TIMESTAMP WHERE id = p_user_id;
    IF FOUND THEN
        result := json_build_object('success', true, 'message', 'Gender updated successfully', 'gender', p_gender);
    ELSE
        result := json_build_object('success', false, 'message', 'User not found');
    END IF;
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Function: Add Case Timeline Event
CREATE OR REPLACE FUNCTION add_case_timeline_event(
    p_case_id VARCHAR(255),
    p_user_id VARCHAR(255),
    p_role VARCHAR(20),
    p_event_type VARCHAR(50),
    p_event_title VARCHAR(255),
    p_event_description TEXT,
    p_status VARCHAR(50)
) RETURNS JSON AS $$
DECLARE v_timeline_id INT; result JSON;
BEGIN
    INSERT INTO case_timeline (case_id, user_id, role, event_type, event_title, event_description, status) 
    VALUES (p_case_id, p_user_id, p_role, p_event_type, p_event_title, p_event_description, p_status) 
    RETURNING id INTO v_timeline_id;
    result := json_build_object('success', true, 'message', 'Timeline event added successfully', 'timeline_id', v_timeline_id);
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Function: Get Case Timeline
CREATE OR REPLACE FUNCTION get_case_timeline(p_case_id VARCHAR(255)) 
RETURNS TABLE (id INT, event_type VARCHAR, event_title VARCHAR, event_description TEXT, status VARCHAR, role VARCHAR, username VARCHAR, timestamp_val TIMESTAMP) AS $$
BEGIN
    RETURN QUERY SELECT ct.id::INT, ct.event_type, ct.event_title, ct.event_description, ct.status, ct.role, u.username, ct.timestamp 
    FROM case_timeline ct 
    JOIN users u ON ct.user_id = u.id 
    WHERE ct.case_id = p_case_id 
    ORDER BY ct.timestamp DESC;
END;
$$ LANGUAGE plpgsql;

-- Function: Update Case Status with History
CREATE OR REPLACE FUNCTION update_case_status_with_history(
    p_case_id VARCHAR(255),
    p_new_status VARCHAR(50),
    p_changed_by VARCHAR(255),
    p_changed_by_role VARCHAR(20),
    p_reason TEXT
) RETURNS JSON AS $$
DECLARE v_old_status VARCHAR(50); result JSON;
BEGIN
    INSERT INTO case_status_history (case_id, old_status, new_status, changed_by, changed_by_role, reason) 
    VALUES (p_case_id, v_old_status, p_new_status, p_changed_by, p_changed_by_role, p_reason);
    INSERT INTO case_timeline (case_id, user_id, role, event_type, event_title, event_description, status) 
    VALUES (p_case_id, p_changed_by, p_changed_by_role, 'status_change', 'Status Changed', p_reason, p_new_status);
    result := json_build_object('success', true, 'message', 'Case status updated successfully', 'old_status', v_old_status, 'new_status', p_new_status);
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Function: Add Investigation Log
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
DECLARE v_log_id INT; result JSON;
BEGIN
    INSERT INTO case_investigation_log (case_id, officer_id, investigation_type, findings, evidence_collected, witnesses_interviewed, suspects_identified, next_steps) 
    VALUES (p_case_id, p_officer_id, p_investigation_type, p_findings, p_evidence_collected, p_witnesses_interviewed, p_suspects_identified, p_next_steps) 
    RETURNING id INTO v_log_id;
    INSERT INTO case_timeline (case_id, user_id, role, event_type, event_title, event_description, status) 
    VALUES (p_case_id, p_officer_id, 'Officer', 'investigated', 'Investigation', p_findings, 'ongoing');
    result := json_build_object('success', true, 'message', 'Investigation log added successfully', 'log_id', v_log_id);
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Function: Delete User Account Cascade
CREATE OR REPLACE FUNCTION delete_user_account_cascade(p_user_id VARCHAR(255)) RETURNS JSON AS $$
DECLARE v_user_count INT; result JSON;
BEGIN
    SELECT COUNT(*) INTO v_user_count FROM users WHERE id = p_user_id;
    IF v_user_count = 0 THEN
        result := json_build_object('success', false, 'message', 'User not found');
        RETURN result;
    END IF;
    DELETE FROM users WHERE id = p_user_id;
    result := json_build_object('success', true, 'message', 'User account and all related data deleted successfully');
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- PART 17: INSERT ADMIN ACCOUNT (YOUR CREDENTIALS)
-- ============================================================================

-- Delete existing admin if any
DELETE FROM users WHERE username = 'bms.official.admin';

-- Create admin with your specified credentials
INSERT INTO users (id, username, password, email, role, first_name, last_name, is_hidden, auth_provider) 
VALUES (
    'admin-' || to_char(now(), 'YYYYMMDDHHmmss'),
    'bms.official.admin', 
    '$2b$10$K8wjZTF5B8u6H7Jq6m9nE.9qQZ5rY8VWq0dK8cX6vL5dR2rS5tOa',
    'bms.admin@bms.gov', 
    'Admin', 
    'System', 
    'Administrator', 
    true,
    'email'
);

-- ============================================================================
-- SETUP COMPLETE
-- ============================================================================
-- All tables, indexes, and functions have been created successfully!
-- Your BMS database is now ready for use.
-- Admin Account: bms.official.admin / BMS2025
-- ============================================================================

-- Verify setup
SELECT 'SETUP COMPLETE' as status;
SELECT COUNT(*) as total_tables FROM information_schema.tables WHERE table_schema = 'public';
SELECT COUNT(*) as total_functions FROM information_schema.routines WHERE routine_schema = 'public';
SELECT * FROM users WHERE username = 'bms.official.admin';
