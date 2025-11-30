-- 📋 STEP 3: PERSON HISTORY TABLES
-- Manages person profiles, criminal records, case involvement, and risk levels
-- Run this AFTER STEP 2

-- ✅ TABLE 7: person_profiles (Individual person information)
CREATE TABLE person_profiles (
    id SERIAL PRIMARY KEY,
    firstName VARCHAR(100) NOT NULL,
    lastName VARCHAR(100) NOT NULL,
    middleName VARCHAR(100),
    dateOfBirth DATE,
    gender VARCHAR(20),
    address TEXT,
    contactNumber VARCHAR(20),
    email VARCHAR(255),
    nationality VARCHAR(100),
    idType VARCHAR(50), -- 'passport', 'drivers_license', 'national_id', etc.
    idNumber VARCHAR(100),
    profilePhotoUrl TEXT,
    status VARCHAR(50) DEFAULT 'active', -- 'active', 'inactive', 'deceased'
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for person_profiles table
CREATE INDEX idx_person_profiles_firstName ON person_profiles(firstName);
CREATE INDEX idx_person_profiles_lastName ON person_profiles(lastName);
CREATE INDEX idx_person_profiles_dateOfBirth ON person_profiles(dateOfBirth);
CREATE INDEX idx_person_profiles_idNumber ON person_profiles(idNumber);

-- ✅ TABLE 8: criminal_records (Criminal history)
CREATE TABLE criminal_records (
    id SERIAL PRIMARY KEY,
    personId INTEGER NOT NULL REFERENCES person_profiles(id) ON DELETE CASCADE,
    offenseType VARCHAR(255), -- 'theft', 'assault', 'drug_possession', etc.
    offenseDate DATE,
    location VARCHAR(255),
    status VARCHAR(50), -- 'convicted', 'acquitted', 'pending', 'dismissed'
    sentence VARCHAR(255),
    sentenceStartDate DATE,
    sentenceEndDate DATE,
    notes TEXT,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for criminal_records table
CREATE INDEX idx_criminal_records_personId ON criminal_records(personId);
CREATE INDEX idx_criminal_records_offenseType ON criminal_records(offenseType);
CREATE INDEX idx_criminal_records_offenseDate ON criminal_records(offenseDate);
CREATE INDEX idx_criminal_records_status ON criminal_records(status);

-- ✅ TABLE 9: case_involvement (Person's involvement in cases)
CREATE TABLE case_involvement (
    id SERIAL PRIMARY KEY,
    personId INTEGER NOT NULL REFERENCES person_profiles(id) ON DELETE CASCADE,
    caseId INTEGER,
    role VARCHAR(100), -- 'defendant', 'witness', 'victim', 'suspect', etc.
    involvement_details TEXT,
    status VARCHAR(50), -- 'active', 'closed', 'pending'
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for case_involvement table
CREATE INDEX idx_case_involvement_personId ON case_involvement(personId);
CREATE INDEX idx_case_involvement_caseId ON case_involvement(caseId);
CREATE INDEX idx_case_involvement_role ON case_involvement(role);

-- ✅ TABLE 10: person_risk_levels (Risk assessment for individuals)
CREATE TABLE person_risk_levels (
    id SERIAL PRIMARY KEY,
    personId INTEGER NOT NULL REFERENCES person_profiles(id) ON DELETE CASCADE,
    riskLevel VARCHAR(50), -- 'low', 'medium', 'high', 'critical'
    riskFactors TEXT, -- JSON or comma-separated list
    assessmentDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assessedBy INTEGER REFERENCES users(id) ON DELETE SET NULL,
    notes TEXT,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_risk_level CHECK (riskLevel IN ('low', 'medium', 'high', 'critical'))
);

-- Create indexes for person_risk_levels table
CREATE INDEX idx_person_risk_levels_personId ON person_risk_levels(personId);
CREATE INDEX idx_person_risk_levels_riskLevel ON person_risk_levels(riskLevel);
CREATE INDEX idx_person_risk_levels_assessmentDate ON person_risk_levels(assessmentDate);

-- ✅ VERIFY SETUP
SELECT 
    'person_profiles' as table_name,
    COUNT(*) as row_count
FROM person_profiles
UNION ALL
SELECT 
    'criminal_records' as table_name,
    COUNT(*) as row_count
FROM criminal_records
UNION ALL
SELECT 
    'case_involvement' as table_name,
    COUNT(*) as row_count
FROM case_involvement
UNION ALL
SELECT 
    'person_risk_levels' as table_name,
    COUNT(*) as row_count
FROM person_risk_levels;

-- ✅ STEP 3 COMPLETE
-- Person history tables created successfully!
-- Next: Run BMS_STEP_4_CASE_MANAGEMENT_TABLES.sql
