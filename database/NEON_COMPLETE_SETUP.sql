-- ============================================================================
-- BMS COMPLETE NEON SETUP - COPY & PASTE ALL AT ONCE
-- ============================================================================
-- This file contains ALL database schemas needed for BMS
-- Just copy everything and paste into Neon SQL Editor
-- ============================================================================

-- ============================================================================
-- PART 1: COMPLETE FRESH SETUP (Users & Core Tables)
-- ============================================================================

-- Drop existing tables if they exist (careful in production!)
-- Drop in correct order to avoid foreign key constraint errors
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

-- Disable foreign key constraints temporarily
SET session_replication_role = replica;

-- ============================================================================
-- USERS TABLE
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- USER IMAGES TABLE
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

-- ============================================================================
-- PART 2: HEARINGS SCHEMA
-- ============================================================================

-- HEARINGS TABLE
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

-- HEARING ATTENDEES
CREATE TABLE hearing_attendees (
    id SERIAL PRIMARY KEY,
    hearing_id VARCHAR(255) NOT NULL REFERENCES hearings(hearing_id) ON DELETE CASCADE,
    user_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    attendee_role VARCHAR(50) NOT NULL,
    attendance_status VARCHAR(50) DEFAULT 'invited',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_sync_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- HEARING MINUTES
CREATE TABLE hearing_minutes (
    id SERIAL PRIMARY KEY,
    hearing_id VARCHAR(255) NOT NULL REFERENCES hearings(hearing_id) ON DELETE CASCADE,
    minute_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    added_by VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_sync_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- HEARING NOTIFICATIONS
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

-- ============================================================================
-- PART 3: PERSON HISTORY SCHEMA
-- ============================================================================

-- PERSON PROFILES
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

-- CRIMINAL RECORDS
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

-- CASE INVOLVEMENT
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

-- PERSON RISK LEVELS
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

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================

-- Users indexes
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_auth_provider ON users(auth_provider);
CREATE INDEX idx_users_gender ON users(gender);

-- User images indexes
CREATE INDEX idx_user_images_user_id ON user_images(user_id);
CREATE INDEX idx_user_images_type ON user_images(image_type);
CREATE INDEX idx_user_images_active ON user_images(is_active);

-- Hearings indexes
CREATE INDEX idx_hearings_case_id ON hearings(case_id);
CREATE INDEX idx_hearings_date ON hearings(hearing_date);
CREATE INDEX idx_hearings_status ON hearings(status);
CREATE INDEX idx_hearing_attendees_hearing_id ON hearing_attendees(hearing_id);
CREATE INDEX idx_hearing_attendees_user_id ON hearing_attendees(user_id);
CREATE INDEX idx_hearing_minutes_hearing_id ON hearing_minutes(hearing_id);
CREATE INDEX idx_hearing_notifications_user_id ON hearing_notifications(user_id);
CREATE INDEX idx_hearing_notifications_is_read ON hearing_notifications(is_read);

-- Person history indexes
CREATE INDEX idx_person_profiles_person_id ON person_profiles(person_id);
CREATE INDEX idx_criminal_records_person_id ON criminal_records(person_id);
CREATE INDEX idx_criminal_records_status ON criminal_records(status);
CREATE INDEX idx_case_involvement_person_id ON case_involvement(person_id);
CREATE INDEX idx_case_involvement_case_id ON case_involvement(case_id);
CREATE INDEX idx_person_risk_levels_person_id ON person_risk_levels(person_id);

-- ============================================================================
-- DATABASE FUNCTIONS
-- ============================================================================

-- Function to schedule hearing
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

-- Function to get hearings by date range
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

-- Function to get user hearings
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

-- Function to update hearing status
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

-- Function to add hearing attendee
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

-- Function to add hearing minutes
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

-- Function to create person profile
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

-- Function to add criminal record
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

-- Function to add case involvement
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

-- Function to set user profile picture
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

-- Function to get user profile picture
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

-- Function to detect user role
CREATE OR REPLACE FUNCTION detect_user_role(p_username VARCHAR(255))
RETURNS VARCHAR(50) AS $$
BEGIN
    IF p_username = 'admin' THEN
        RETURN 'Admin';
    ELSIF p_username LIKE 'off.%' THEN
        RETURN 'Officer';
    ELSE
        RETURN 'User';
    END IF;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- RE-ENABLE FOREIGN KEY CONSTRAINTS
-- ============================================================================

SET session_replication_role = DEFAULT;

-- ============================================================================
-- SETUP COMPLETE
-- ============================================================================
-- All tables, indexes, and functions have been created successfully!
-- Your BMS database is now ready for use.
-- ============================================================================
