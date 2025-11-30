-- 📋 STEP 2: HEARINGS TABLES
-- Manages court hearings, attendees, minutes, and notifications
-- Run this AFTER STEP 1

-- ✅ TABLE 3: hearings (Court hearings)
CREATE TABLE hearings (
    id SERIAL PRIMARY KEY,
    caseId INTEGER,
    hearingDate TIMESTAMP NOT NULL,
    hearingLocation VARCHAR(255),
    hearingType VARCHAR(100), -- 'preliminary', 'trial', 'sentencing', etc.
    status VARCHAR(50) DEFAULT 'scheduled', -- 'scheduled', 'ongoing', 'completed', 'postponed', 'cancelled'
    judge VARCHAR(255),
    notes TEXT,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_hearing_status CHECK (status IN ('scheduled', 'ongoing', 'completed', 'postponed', 'cancelled'))
);

-- Create indexes for hearings table
CREATE INDEX idx_hearings_caseId ON hearings(caseId);
CREATE INDEX idx_hearings_hearingDate ON hearings(hearingDate);
CREATE INDEX idx_hearings_status ON hearings(status);

-- ✅ TABLE 4: hearing_attendees (People attending hearings)
CREATE TABLE hearing_attendees (
    id SERIAL PRIMARY KEY,
    hearingId INTEGER NOT NULL REFERENCES hearings(id) ON DELETE CASCADE,
    userId INTEGER REFERENCES users(id) ON DELETE SET NULL,
    personName VARCHAR(255),
    role VARCHAR(100), -- 'judge', 'prosecutor', 'defendant', 'witness', 'attorney', etc.
    attendanceStatus VARCHAR(50) DEFAULT 'present', -- 'present', 'absent', 'excused'
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for hearing_attendees table
CREATE INDEX idx_hearing_attendees_hearingId ON hearing_attendees(hearingId);
CREATE INDEX idx_hearing_attendees_userId ON hearing_attendees(userId);

-- ✅ TABLE 5: hearing_minutes (Minutes/notes from hearings)
CREATE TABLE hearing_minutes (
    id SERIAL PRIMARY KEY,
    hearingId INTEGER NOT NULL REFERENCES hearings(id) ON DELETE CASCADE,
    minutesContent TEXT,
    recordedBy INTEGER REFERENCES users(id) ON DELETE SET NULL,
    recordedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for hearing_minutes table
CREATE INDEX idx_hearing_minutes_hearingId ON hearing_minutes(hearingId);
CREATE INDEX idx_hearing_minutes_recordedBy ON hearing_minutes(recordedBy);

-- ✅ TABLE 6: hearing_notifications (Notifications for hearings)
CREATE TABLE hearing_notifications (
    id SERIAL PRIMARY KEY,
    hearingId INTEGER NOT NULL REFERENCES hearings(id) ON DELETE CASCADE,
    userId INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    notificationType VARCHAR(100), -- 'hearing_scheduled', 'hearing_postponed', 'hearing_reminder', etc.
    message TEXT,
    isRead BOOLEAN DEFAULT FALSE,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    readAt TIMESTAMP
);

-- Create indexes for hearing_notifications table
CREATE INDEX idx_hearing_notifications_hearingId ON hearing_notifications(hearingId);
CREATE INDEX idx_hearing_notifications_userId ON hearing_notifications(userId);
CREATE INDEX idx_hearing_notifications_isRead ON hearing_notifications(isRead);

-- ✅ VERIFY SETUP
SELECT 
    'hearings' as table_name,
    COUNT(*) as row_count
FROM hearings
UNION ALL
SELECT 
    'hearing_attendees' as table_name,
    COUNT(*) as row_count
FROM hearing_attendees
UNION ALL
SELECT 
    'hearing_minutes' as table_name,
    COUNT(*) as row_count
FROM hearing_minutes
UNION ALL
SELECT 
    'hearing_notifications' as table_name,
    COUNT(*) as row_count
FROM hearing_notifications;

-- ✅ STEP 2 COMPLETE
-- Hearings tables created successfully!
-- Next: Run BMS_STEP_3_PERSON_HISTORY_TABLES.sql
