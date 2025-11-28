-- ============================================================================
-- PERSON HISTORY SCHEMA FOR OFFICER "VIEW PERSON HISTORY" FEATURE
-- Supports LOCAL SQLite + NEON PostgreSQL sync across multi-devices
-- ============================================================================

DROP TABLE IF EXISTS person_images CASCADE;
DROP TABLE IF EXISTS person_case_involvement CASCADE;
DROP TABLE IF EXISTS person_criminal_records CASCADE;
DROP TABLE IF EXISTS person_profiles CASCADE;

-- 1. PERSON PROFILE TABLE
CREATE TABLE person_profiles (
    id SERIAL PRIMARY KEY,
    person_id VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    alias VARCHAR(255),
    date_of_birth DATE,
    gender VARCHAR(10),
    address TEXT,
    contact_info TEXT,
    identification_mark TEXT,
    risk_level VARCHAR(20) DEFAULT 'low',
    created_by VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_sync_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. PERSON CRIMINAL RECORDS
CREATE TABLE person_criminal_records (
    id SERIAL PRIMARY KEY,
    person_id VARCHAR(255) NOT NULL REFERENCES person_profiles(person_id) ON DELETE CASCADE,
    case_number VARCHAR(255),
    crime_type VARCHAR(100) NOT NULL,
    crime_description TEXT,
    date_committed DATE,
    date_arrested DATE,
    sentencing TEXT,
    status VARCHAR(50) DEFAULT 'closed',
    officer_in_charge VARCHAR(255) REFERENCES users(id) ON DELETE SET NULL,
    court_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_sync_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. PERSON CASE INVOLVEMENT
CREATE TABLE person_case_involvement (
    id SERIAL PRIMARY KEY,
    person_id VARCHAR(255) NOT NULL REFERENCES person_profiles(person_id) ON DELETE CASCADE,
    case_id VARCHAR(255) NOT NULL,
    involvement_type VARCHAR(50) NOT NULL,
    involvement_details TEXT,
    status VARCHAR(50),
    officer_notes TEXT,
    created_by VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_sync_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. PERSON IMAGES
CREATE TABLE person_images (
    id SERIAL PRIMARY KEY,
    person_id VARCHAR(255) NOT NULL REFERENCES person_profiles(person_id) ON DELETE CASCADE,
    image_type VARCHAR(50) NOT NULL,
    image_url TEXT,
    image_data BYTEA,
    description TEXT,
    taken_by VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    taken_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_sync_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================================

CREATE INDEX idx_person_profiles_person_id ON person_profiles(person_id);
CREATE INDEX idx_person_profiles_name ON person_profiles(first_name, last_name);
CREATE INDEX idx_person_profiles_risk_level ON person_profiles(risk_level);
CREATE INDEX idx_criminal_records_person_id ON person_criminal_records(person_id);
CREATE INDEX idx_case_involvement_person_id ON person_case_involvement(person_id);
CREATE INDEX idx_person_images_person_id ON person_images(person_id);

-- ============================================================================
-- SYNC MANAGEMENT FUNCTIONS
-- ============================================================================

-- Function to get unsynced person data
CREATE OR REPLACE FUNCTION get_unsynced_person_data(p_last_sync TIMESTAMP)
RETURNS TABLE (
    table_name TEXT,
    record_id INTEGER,
    person_id VARCHAR(255),
    sync_data JSONB
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        'person_profiles' as table_name,
        pp.id as record_id,
        pp.person_id,
        jsonb_build_object(
            'person_id', pp.person_id,
            'first_name', pp.first_name,
            'last_name', pp.last_name,
            'alias', pp.alias,
            'risk_level', pp.risk_level,
            'created_by', pp.created_by
        ) as sync_data
    FROM person_profiles pp
    WHERE pp.last_sync_at < p_last_sync OR pp.last_sync_at IS NULL;
END;
$$ LANGUAGE plpgsql;

-- Function to mark records as synced
CREATE OR REPLACE FUNCTION mark_person_data_synced(p_person_id VARCHAR(255))
RETURNS VOID AS $$
BEGIN
    UPDATE person_profiles SET last_sync_at = CURRENT_TIMESTAMP WHERE person_id = p_person_id;
    UPDATE person_criminal_records SET last_sync_at = CURRENT_TIMESTAMP WHERE person_id = p_person_id;
    UPDATE person_case_involvement SET last_sync_at = CURRENT_TIMESTAMP WHERE person_id = p_person_id;
    UPDATE person_images SET last_sync_at = CURRENT_TIMESTAMP WHERE person_id = p_person_id;
END;
$$ LANGUAGE plpgsql;

-- Function to search person by name
CREATE OR REPLACE FUNCTION search_person_by_name(p_search_term VARCHAR(255))
RETURNS TABLE (
    id INT,
    person_id VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    alias VARCHAR(255),
    risk_level VARCHAR(20),
    created_at TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        pp.id,
        pp.person_id,
        pp.first_name,
        pp.last_name,
        pp.alias,
        pp.risk_level,
        pp.created_at
    FROM person_profiles pp
    WHERE pp.first_name ILIKE '%' || p_search_term || '%'
       OR pp.last_name ILIKE '%' || p_search_term || '%'
       OR pp.alias ILIKE '%' || p_search_term || '%'
    ORDER BY pp.created_at DESC;
END;
$$ LANGUAGE plpgsql;

-- Function to get complete person history
CREATE OR REPLACE FUNCTION get_complete_person_history(p_person_id VARCHAR(255))
RETURNS TABLE (
    profile_data JSONB,
    criminal_records JSONB,
    case_involvements JSONB,
    images JSONB
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        (SELECT jsonb_build_object(
            'id', pp.id,
            'person_id', pp.person_id,
            'first_name', pp.first_name,
            'last_name', pp.last_name,
            'alias', pp.alias,
            'date_of_birth', pp.date_of_birth,
            'gender', pp.gender,
            'address', pp.address,
            'risk_level', pp.risk_level,
            'created_at', pp.created_at
        ) FROM person_profiles pp WHERE pp.person_id = p_person_id) as profile_data,
        
        (SELECT jsonb_agg(jsonb_build_object(
            'id', pcr.id,
            'crime_type', pcr.crime_type,
            'crime_description', pcr.crime_description,
            'date_committed', pcr.date_committed,
            'status', pcr.status,
            'court_name', pcr.court_name
        )) FROM person_criminal_records pcr WHERE pcr.person_id = p_person_id) as criminal_records,
        
        (SELECT jsonb_agg(jsonb_build_object(
            'id', pci.id,
            'case_id', pci.case_id,
            'involvement_type', pci.involvement_type,
            'involvement_details', pci.involvement_details,
            'status', pci.status
        )) FROM person_case_involvement pci WHERE pci.person_id = p_person_id) as case_involvements,
        
        (SELECT jsonb_agg(jsonb_build_object(
            'id', pi.id,
            'image_type', pi.image_type,
            'image_url', pi.image_url,
            'description', pi.description,
            'taken_date', pi.taken_date
        )) FROM person_images pi WHERE pi.person_id = p_person_id) as images;
END;
$$ LANGUAGE plpgsql;

-- Function to add criminal record
CREATE OR REPLACE FUNCTION add_criminal_record(
    p_person_id VARCHAR(255),
    p_crime_type VARCHAR(100),
    p_crime_description TEXT,
    p_date_committed DATE,
    p_date_arrested DATE,
    p_officer_id VARCHAR(255)
)
RETURNS JSON AS $$
DECLARE v_record_id INT; result JSON;
BEGIN
    INSERT INTO person_criminal_records (
        person_id, crime_type, crime_description, date_committed, 
        date_arrested, officer_in_charge, status
    ) VALUES (
        p_person_id, p_crime_type, p_crime_description, p_date_committed,
        p_date_arrested, p_officer_id, 'closed'
    ) RETURNING id INTO v_record_id;
    
    UPDATE person_profiles SET risk_level = 'high', updated_at = CURRENT_TIMESTAMP 
    WHERE person_id = p_person_id;
    
    result := json_build_object('success', true, 'message', 'Criminal record added', 'record_id', v_record_id);
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Function to create person profile
CREATE OR REPLACE FUNCTION create_person_profile(
    p_person_id VARCHAR(255),
    p_first_name VARCHAR(255),
    p_last_name VARCHAR(255),
    p_alias VARCHAR(255),
    p_date_of_birth DATE,
    p_gender VARCHAR(10),
    p_risk_level VARCHAR(20),
    p_created_by VARCHAR(255)
)
RETURNS JSON AS $$
DECLARE v_profile_id INT; result JSON;
BEGIN
    INSERT INTO person_profiles (
        person_id, first_name, last_name, alias, date_of_birth, 
        gender, risk_level, created_by
    ) VALUES (
        p_person_id, p_first_name, p_last_name, p_alias, p_date_of_birth,
        p_gender, p_risk_level, p_created_by
    ) RETURNING id INTO v_profile_id;
    
    result := json_build_object('success', true, 'message', 'Person profile created', 'profile_id', v_profile_id);
    RETURN result;
END;
$$ LANGUAGE plpgsql;
