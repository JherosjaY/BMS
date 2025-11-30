-- 📋 STEP 6: FINAL TABLES (Part 2)
-- Manages suspects, witnesses, user sessions, and FCM tokens
-- Run this AFTER STEP 5

-- ✅ TABLE 22: suspects (Suspect information)
CREATE TABLE suspects (
    id SERIAL PRIMARY KEY,
    personId INTEGER NOT NULL REFERENCES person_profiles(id) ON DELETE CASCADE,
    caseId INTEGER REFERENCES case_timeline(id) ON DELETE SET NULL,
    suspectStatus VARCHAR(50) DEFAULT 'under_investigation', -- 'under_investigation', 'arrested', 'charged', 'acquitted', 'convicted'
    arrestDate DATE,
    chargeDescription TEXT,
    mugShotUrl TEXT,
    notes TEXT,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_suspect_status CHECK (suspectStatus IN ('under_investigation', 'arrested', 'charged', 'acquitted', 'convicted'))
);

-- Create indexes for suspects table
CREATE INDEX idx_suspects_personId ON suspects(personId);
CREATE INDEX idx_suspects_caseId ON suspects(caseId);
CREATE INDEX idx_suspects_suspectStatus ON suspects(suspectStatus);

-- ✅ TABLE 23: witnesses (Witness information)
CREATE TABLE witnesses (
    id SERIAL PRIMARY KEY,
    personId INTEGER NOT NULL REFERENCES person_profiles(id) ON DELETE CASCADE,
    caseId INTEGER REFERENCES case_timeline(id) ON DELETE SET NULL,
    witnessType VARCHAR(50), -- 'eyewitness', 'expert', 'character_witness', etc.
    statement TEXT,
    statementDate DATE,
    reliability VARCHAR(50), -- 'reliable', 'questionable', 'unreliable'
    contactPreference VARCHAR(50), -- 'phone', 'email', 'in_person'
    notes TEXT,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for witnesses table
CREATE INDEX idx_witnesses_personId ON witnesses(personId);
CREATE INDEX idx_witnesses_caseId ON witnesses(caseId);
CREATE INDEX idx_witnesses_witnessType ON witnesses(witnessType);

-- ✅ TABLE 24: user_activities (Detailed user activity tracking)
CREATE TABLE user_activities (
    id SERIAL PRIMARY KEY,
    userId INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    activityType VARCHAR(100), -- 'login', 'logout', 'view_case', 'create_report', etc.
    activityDescription TEXT,
    relatedEntityType VARCHAR(100), -- 'case', 'hearing', 'person', etc.
    relatedEntityId INTEGER,
    ipAddress VARCHAR(45),
    userAgent TEXT,
    activityDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for user_activities table
CREATE INDEX idx_user_activities_userId ON user_activities(userId);
CREATE INDEX idx_user_activities_activityType ON user_activities(activityType);
CREATE INDEX idx_user_activities_activityDate ON user_activities(activityDate);

-- ✅ TABLE 25: user_fcm_tokens (User-specific FCM tokens)
CREATE TABLE user_fcm_tokens (
    id SERIAL PRIMARY KEY,
    userId INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    fcmToken VARCHAR(500) NOT NULL,
    deviceType VARCHAR(50), -- 'android', 'ios', 'web'
    deviceName VARCHAR(255),
    isActive BOOLEAN DEFAULT TRUE,
    registeredAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    lastUsedAt TIMESTAMP,
    expiresAt TIMESTAMP
);

-- Create indexes for user_fcm_tokens table
CREATE INDEX idx_user_fcm_tokens_userId ON user_fcm_tokens(userId);
CREATE INDEX idx_user_fcm_tokens_fcmToken ON user_fcm_tokens(fcmToken);
CREATE INDEX idx_user_fcm_tokens_isActive ON user_fcm_tokens(isActive);

-- ✅ TABLE 26: user_sessions (User session management)
CREATE TABLE user_sessions (
    id SERIAL PRIMARY KEY,
    userId INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sessionToken VARCHAR(500) UNIQUE NOT NULL,
    jwtToken TEXT,
    ipAddress VARCHAR(45),
    userAgent TEXT,
    deviceType VARCHAR(50), -- 'android', 'ios', 'web', 'desktop'
    loginAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    lastActivityAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    logoutAt TIMESTAMP,
    isActive BOOLEAN DEFAULT TRUE,
    expiresAt TIMESTAMP
);

-- Create indexes for user_sessions table
CREATE INDEX idx_user_sessions_userId ON user_sessions(userId);
CREATE INDEX idx_user_sessions_sessionToken ON user_sessions(sessionToken);
CREATE INDEX idx_user_sessions_isActive ON user_sessions(isActive);
CREATE INDEX idx_user_sessions_loginAt ON user_sessions(loginAt);

-- ✅ VERIFY SETUP
SELECT 
    'suspects' as table_name,
    COUNT(*) as row_count
FROM suspects
UNION ALL
SELECT 
    'witnesses' as table_name,
    COUNT(*) as row_count
FROM witnesses
UNION ALL
SELECT 
    'user_activities' as table_name,
    COUNT(*) as row_count
FROM user_activities
UNION ALL
SELECT 
    'user_fcm_tokens' as table_name,
    COUNT(*) as row_count
FROM user_fcm_tokens
UNION ALL
SELECT 
    'user_sessions' as table_name,
    COUNT(*) as row_count
FROM user_sessions;

-- ✅ STEP 6 COMPLETE
-- 5 final tables created successfully!
-- Total tables: 26 ✅
-- All tables are now ready!
-- Next: Verify all 26 tables and test the system
