-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),
    google_id VARCHAR(255) UNIQUE,
    registration_method VARCHAR(50) DEFAULT 'normal' CHECK (registration_method IN ('normal', 'google')),
    role VARCHAR(50) DEFAULT 'user' CHECK (role IN ('admin', 'officer', 'user')),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    badge_number VARCHAR(50) UNIQUE,
    department VARCHAR(100),
    phone_number VARCHAR(20),
    profile_picture VARCHAR(500),
    is_active BOOLEAN DEFAULT true,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Cases Table
CREATE TABLE IF NOT EXISTS cases (
    id SERIAL PRIMARY KEY,
    case_number VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'pending' CHECK (status IN ('pending', 'in-progress', 'resolved', 'closed')),
    priority VARCHAR(50) DEFAULT 'medium' CHECK (priority IN ('low', 'medium', 'high')),
    assigned_officer_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    created_by INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    incident_date TIMESTAMP,
    incident_location VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Blotter Reports Table
CREATE TABLE IF NOT EXISTS blotter_reports (
    id SERIAL PRIMARY KEY,
    report_number VARCHAR(50) UNIQUE NOT NULL,
    complainant_name VARCHAR(255) NOT NULL,
    complainant_contact VARCHAR(20),
    respondent_name VARCHAR(255),
    respondent_address TEXT,
    incident_date TIMESTAMP NOT NULL,
    incident_location VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'pending' CHECK (status IN ('pending', 'under-investigation', 'resolved', 'closed')),
    created_by INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assigned_officer_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Case Evidence Table
CREATE TABLE IF NOT EXISTS case_evidence (
    id SERIAL PRIMARY KEY,
    case_id INTEGER NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_type VARCHAR(50),
    file_size INTEGER,
    description TEXT,
    uploaded_by INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    verified BOOLEAN DEFAULT false,
    verified_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    verified_at TIMESTAMP,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Officer Assignments Table
CREATE TABLE IF NOT EXISTS officer_assignments (
    id SERIAL PRIMARY KEY,
    officer_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    case_id INTEGER NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    assigned_by INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(50) DEFAULT 'assigned' CHECK (status IN ('assigned', 'accepted', 'rejected', 'completed')),
    rejection_reason TEXT,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP,
    completed_at TIMESTAMP,
    UNIQUE(officer_id, case_id)
);

-- Officer Performance Table
CREATE TABLE IF NOT EXISTS officer_performance (
    id SERIAL PRIMARY KEY,
    officer_id INTEGER UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    total_cases INTEGER DEFAULT 0,
    completed_cases INTEGER DEFAULT 0,
    avg_resolution_time DECIMAL(10, 2) DEFAULT 0,
    rating DECIMAL(3, 2) DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50),
    is_read BOOLEAN DEFAULT false,
    related_id INTEGER,
    related_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);

-- Activity Logs Table
CREATE TABLE IF NOT EXISTS activity_logs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    description TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for Performance
CREATE INDEX idx_cases_assigned_officer ON cases(assigned_officer_id);
CREATE INDEX idx_cases_created_by ON cases(created_by);
CREATE INDEX idx_cases_status ON cases(status);
CREATE INDEX idx_cases_priority ON cases(priority);
CREATE INDEX idx_blotter_assigned_officer ON blotter_reports(assigned_officer_id);
CREATE INDEX idx_blotter_created_by ON blotter_reports(created_by);
CREATE INDEX idx_blotter_status ON blotter_reports(status);
CREATE INDEX idx_case_evidence_case_id ON case_evidence(case_id);
CREATE INDEX idx_officer_assignments_officer ON officer_assignments(officer_id);
CREATE INDEX idx_officer_assignments_case ON officer_assignments(case_id);
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(is_read);
CREATE INDEX idx_activity_logs_user ON activity_logs(user_id);
CREATE INDEX idx_activity_logs_created ON activity_logs(created_at);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);

-- Views for Analytics
CREATE OR REPLACE VIEW v_officer_workload AS
SELECT 
    u.id,
    u.first_name,
    u.last_name,
    u.badge_number,
    COUNT(DISTINCT c.id) as assigned_cases,
    COUNT(DISTINCT CASE WHEN c.status = 'completed' THEN c.id END) as completed_cases,
    COALESCE(op.rating, 0) as rating
FROM users u
LEFT JOIN cases c ON u.id = c.assigned_officer_id
LEFT JOIN officer_performance op ON u.id = op.officer_id
WHERE u.role = 'officer' AND u.is_active = true
GROUP BY u.id, u.first_name, u.last_name, u.badge_number, op.rating;

CREATE OR REPLACE VIEW v_case_status_summary AS
SELECT 
    status,
    COUNT(*) as total,
    COUNT(CASE WHEN priority = 'high' THEN 1 END) as high_priority,
    COUNT(CASE WHEN priority = 'medium' THEN 1 END) as medium_priority,
    COUNT(CASE WHEN priority = 'low' THEN 1 END) as low_priority
FROM cases
GROUP BY status;
