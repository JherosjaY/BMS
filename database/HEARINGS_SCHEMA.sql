-- ============================================================================
-- HEARINGS SCHEMA FOR LOCAL + NEON SYNC
-- ============================================================================

DROP TABLE IF EXISTS hearing_notifications CASCADE;
DROP TABLE IF EXISTS hearing_minutes CASCADE;
DROP TABLE IF EXISTS hearing_attendees CASCADE;
DROP TABLE IF EXISTS hearings CASCADE;

-- 1. HEARINGS TABLE
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

-- 2. HEARING ATTENDEES
CREATE TABLE hearing_attendees (
    id SERIAL PRIMARY KEY,
    hearing_id VARCHAR(255) NOT NULL REFERENCES hearings(hearing_id) ON DELETE CASCADE,
    user_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    attendee_role VARCHAR(50) NOT NULL,
    attendance_status VARCHAR(50) DEFAULT 'invited',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_sync_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. HEARING MINUTES
CREATE TABLE hearing_minutes (
    id SERIAL PRIMARY KEY,
    hearing_id VARCHAR(255) NOT NULL REFERENCES hearings(hearing_id) ON DELETE CASCADE,
    minute_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    added_by VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_sync_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. HEARING NOTIFICATIONS
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
-- INDEXES FOR PERFORMANCE
-- ============================================================================

CREATE INDEX idx_hearings_case_id ON hearings(case_id);
CREATE INDEX idx_hearings_date ON hearings(hearing_date);
CREATE INDEX idx_hearings_status ON hearings(status);
CREATE INDEX idx_hearing_attendees_hearing_id ON hearing_attendees(hearing_id);
CREATE INDEX idx_hearing_attendees_user_id ON hearing_attendees(user_id);
CREATE INDEX idx_hearing_minutes_hearing_id ON hearing_minutes(hearing_id);
CREATE INDEX idx_hearing_notifications_user_id ON hearing_notifications(user_id);
CREATE INDEX idx_hearing_notifications_is_read ON hearing_notifications(is_read);

-- ============================================================================
-- HEARINGS MANAGEMENT FUNCTIONS
-- ============================================================================

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
