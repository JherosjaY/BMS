DROP TABLE IF EXISTS case_user_updates CASCADE;
DROP TABLE IF EXISTS case_admin_notes CASCADE;
DROP TABLE IF EXISTS case_investigation_log CASCADE;
DROP TABLE IF EXISTS case_status_history CASCADE;
DROP TABLE IF EXISTS case_timeline CASCADE;
DROP TABLE IF EXISTS user_images CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    role VARCHAR(20) DEFAULT 'User',
    gender VARCHAR(10) DEFAULT 'male',
    auth_provider VARCHAR(20) DEFAULT 'email',
    profile_picture_url TEXT,
    profile_picture_data BYTEA,
    has_profile_picture BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_auth_provider ON users(auth_provider);
CREATE INDEX idx_users_gender ON users(gender);
CREATE INDEX idx_users_username ON users(username);

CREATE TABLE user_images (
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

CREATE INDEX idx_user_images_user_id ON user_images(user_id);
CREATE INDEX idx_user_images_type ON user_images(image_type);
CREATE INDEX idx_user_images_active ON user_images(is_active);

CREATE TABLE case_timeline (
    id SERIAL PRIMARY KEY,
    case_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_title VARCHAR(255) NOT NULL,
    event_description TEXT,
    status VARCHAR(50),
    priority VARCHAR(20) DEFAULT 'medium',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_case_timeline_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_case_timeline_case_id ON case_timeline(case_id);
CREATE INDEX idx_case_timeline_user_id ON case_timeline(user_id);
CREATE INDEX idx_case_timeline_role ON case_timeline(role);
CREATE INDEX idx_case_timeline_event_type ON case_timeline(event_type);
CREATE INDEX idx_case_timeline_timestamp ON case_timeline(timestamp);

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

CREATE INDEX idx_case_status_history_case_id ON case_status_history(case_id);
CREATE INDEX idx_case_status_history_changed_by ON case_status_history(changed_by);
CREATE INDEX idx_case_status_history_timestamp ON case_status_history(timestamp);

CREATE TABLE case_investigation_log (
    id SERIAL PRIMARY KEY,
    case_id VARCHAR(255) NOT NULL,
    officer_id VARCHAR(255) NOT NULL,
    investigation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    investigation_type VARCHAR(50),
    findings TEXT,
    evidence_collected TEXT,
    witnesses_interviewed INT DEFAULT 0,
    suspects_identified INT DEFAULT 0,
    next_steps TEXT,
    status VARCHAR(50) DEFAULT 'ongoing',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_case_investigation_log_officer FOREIGN KEY (officer_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_case_investigation_log_case_id ON case_investigation_log(case_id);
CREATE INDEX idx_case_investigation_log_officer_id ON case_investigation_log(officer_id);
CREATE INDEX idx_case_investigation_log_date ON case_investigation_log(investigation_date);

CREATE TABLE case_admin_notes (
    id SERIAL PRIMARY KEY,
    case_id VARCHAR(255) NOT NULL,
    admin_id VARCHAR(255) NOT NULL,
    note_type VARCHAR(50),
    note_content TEXT NOT NULL,
    priority VARCHAR(20) DEFAULT 'medium',
    status VARCHAR(50) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_case_admin_notes_admin FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_case_admin_notes_case_id ON case_admin_notes(case_id);
CREATE INDEX idx_case_admin_notes_admin_id ON case_admin_notes(admin_id);
CREATE INDEX idx_case_admin_notes_status ON case_admin_notes(status);

CREATE TABLE case_user_updates (
    id SERIAL PRIMARY KEY,
    case_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    update_type VARCHAR(50),
    update_content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_case_user_updates_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_case_user_updates_case_id ON case_user_updates(case_id);
CREATE INDEX idx_case_user_updates_user_id ON case_user_updates(user_id);
CREATE INDEX idx_case_user_updates_is_read ON case_user_updates(is_read);

CREATE OR REPLACE FUNCTION set_user_profile_picture(p_user_id VARCHAR(255), p_image_url TEXT DEFAULT NULL, p_image_data BYTEA DEFAULT NULL) RETURNS JSON AS $$
DECLARE result JSON;
BEGIN
    UPDATE users SET profile_picture_url = COALESCE(p_image_url, profile_picture_url), profile_picture_data = COALESCE(p_image_data, profile_picture_data), has_profile_picture = TRUE, updated_at = CURRENT_TIMESTAMP WHERE id = p_user_id;
    IF FOUND THEN
        result := json_build_object('success', true, 'message', 'Profile picture updated successfully', 'user_id', p_user_id);
    ELSE
        result := json_build_object('success', false, 'message', 'User not found');
    END IF;
    RETURN result;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_user_profile_picture(p_user_id VARCHAR(255)) RETURNS TABLE (profile_picture_url TEXT, profile_picture_data BYTEA, has_profile_picture BOOLEAN, username VARCHAR(255), email VARCHAR(255), role VARCHAR(20), gender VARCHAR(10)) AS $$
BEGIN
    RETURN QUERY SELECT u.profile_picture_url, u.profile_picture_data, u.has_profile_picture, u.username, u.email, u.role, u.gender FROM users u WHERE u.id = p_user_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_user_gender(p_user_id VARCHAR(255), p_gender VARCHAR(10)) RETURNS JSON AS $$
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

CREATE OR REPLACE FUNCTION detect_user_role(p_username VARCHAR(255)) RETURNS VARCHAR(20) AS $$
BEGIN
    IF p_username ILIKE 'off.%' OR p_username ILIKE 'officer.%' THEN RETURN 'Officer'; END IF;
    IF p_username = 'admin' OR p_username = 'sentin' THEN RETURN 'Admin'; END IF;
    RETURN 'User';
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION add_case_timeline_event(p_case_id VARCHAR(255), p_user_id VARCHAR(255), p_role VARCHAR(20), p_event_type VARCHAR(50), p_event_title VARCHAR(255), p_event_description TEXT, p_status VARCHAR(50)) RETURNS JSON AS $$
DECLARE v_timeline_id INT; result JSON;
BEGIN
    INSERT INTO case_timeline (case_id, user_id, role, event_type, event_title, event_description, status) VALUES (p_case_id, p_user_id, p_role, p_event_type, p_event_title, p_event_description, p_status) RETURNING id INTO v_timeline_id;
    result := json_build_object('success', true, 'message', 'Timeline event added successfully', 'timeline_id', v_timeline_id);
    RETURN result;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION get_case_timeline(p_case_id VARCHAR(255)) RETURNS TABLE (id INT, event_type VARCHAR, event_title VARCHAR, event_description TEXT, status VARCHAR, role VARCHAR, username VARCHAR, timestamp TIMESTAMP) AS $$
BEGIN
    RETURN QUERY SELECT ct.id, ct.event_type, ct.event_title, ct.event_description, ct.status, ct.role, u.username, ct.timestamp FROM case_timeline ct JOIN users u ON ct.user_id = u.id WHERE ct.case_id = p_case_id ORDER BY ct.timestamp DESC;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_case_status_with_history(p_case_id VARCHAR(255), p_new_status VARCHAR(50), p_changed_by VARCHAR(255), p_changed_by_role VARCHAR(20), p_reason TEXT) RETURNS JSON AS $$
DECLARE v_old_status VARCHAR(50); result JSON;
BEGIN
    INSERT INTO case_status_history (case_id, old_status, new_status, changed_by, changed_by_role, reason) VALUES (p_case_id, v_old_status, p_new_status, p_changed_by, p_changed_by_role, p_reason);
    INSERT INTO case_timeline (case_id, user_id, role, event_type, event_title, event_description, status) VALUES (p_case_id, p_changed_by, p_changed_by_role, 'status_change', 'Status Changed: ' || v_old_status || ' → ' || p_new_status, p_reason, p_new_status);
    result := json_build_object('success', true, 'message', 'Case status updated successfully', 'old_status', v_old_status, 'new_status', p_new_status);
    RETURN result;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION add_investigation_log(p_case_id VARCHAR(255), p_officer_id VARCHAR(255), p_investigation_type VARCHAR(50), p_findings TEXT, p_evidence_collected TEXT, p_witnesses_interviewed INT, p_suspects_identified INT, p_next_steps TEXT) RETURNS JSON AS $$
DECLARE v_log_id INT; result JSON;
BEGIN
    INSERT INTO case_investigation_log (case_id, officer_id, investigation_type, findings, evidence_collected, witnesses_interviewed, suspects_identified, next_steps) VALUES (p_case_id, p_officer_id, p_investigation_type, p_findings, p_evidence_collected, p_witnesses_interviewed, p_suspects_identified, p_next_steps) RETURNING id INTO v_log_id;
    INSERT INTO case_timeline (case_id, user_id, role, event_type, event_title, event_description, status) VALUES (p_case_id, p_officer_id, 'Officer', 'investigated', 'Investigation: ' || p_investigation_type, p_findings, 'ongoing');
    result := json_build_object('success', true, 'message', 'Investigation log added successfully', 'log_id', v_log_id);
    RETURN result;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION delete_user_account_cascade(p_user_id VARCHAR(255)) RETURNS JSON AS $$
DECLARE v_user_count INT; v_reports_deleted INT; v_assignments_deleted INT; v_investigations_deleted INT; result JSON;
BEGIN
    SELECT COUNT(*) INTO v_user_count FROM users WHERE id = p_user_id;
    IF v_user_count = 0 THEN
        result := json_build_object('success', false, 'message', 'User not found');
        RETURN result;
    END IF;
    DELETE FROM users WHERE id = p_user_id;
    result := json_build_object('success', true, 'message', 'User account and all related data deleted successfully', 'deleted_records', json_build_object('user_account', 1, 'reports', v_reports_deleted, 'case_assignments', v_assignments_deleted, 'investigations', v_investigations_deleted));
    RETURN result;
END;
$$ LANGUAGE plpgsql;

INSERT INTO users (id, username, email, password, first_name, last_name, role, gender, auth_provider) VALUES ('1', 'admin', 'admin@bms.com', '$2a$10$hashedpassword', 'Admin', 'User', 'Admin', 'male', 'email');

SELECT * FROM users;
