-- ⚠️ CLEANUP SCRIPT - DELETE ALL EXISTING TABLES FROM NEON
-- This will completely wipe the database
-- Use this ONLY if you want to start fresh!

-- Step 1: Drop all tables (in reverse order of dependencies)
DROP TABLE IF EXISTS case_user_updates CASCADE;
DROP TABLE IF EXISTS case_admin_notes CASCADE;
DROP TABLE IF EXISTS case_investigation_log CASCADE;
DROP TABLE IF EXISTS case_status_history CASCADE;
DROP TABLE IF EXISTS case_timeline CASCADE;

DROP TABLE IF EXISTS person_risk_levels CASCADE;
DROP TABLE IF EXISTS case_involvement CASCADE;
DROP TABLE IF EXISTS criminal_records CASCADE;
DROP TABLE IF EXISTS person_profiles CASCADE;

DROP TABLE IF EXISTS hearing_notifications CASCADE;
DROP TABLE IF EXISTS hearing_minutes CASCADE;
DROP TABLE IF EXISTS hearing_attendees CASCADE;
DROP TABLE IF EXISTS hearings CASCADE;

DROP TABLE IF EXISTS user_images CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Step 2: Drop all functions
DROP FUNCTION IF EXISTS get_user_role(INTEGER) CASCADE;
DROP FUNCTION IF EXISTS get_officer_rank(INTEGER) CASCADE;
DROP FUNCTION IF EXISTS count_active_cases() CASCADE;
DROP FUNCTION IF EXISTS count_active_hearings() CASCADE;
DROP FUNCTION IF EXISTS get_person_risk_level(INTEGER) CASCADE;
DROP FUNCTION IF EXISTS update_case_status(INTEGER, TEXT) CASCADE;
DROP FUNCTION IF EXISTS log_case_activity(INTEGER, TEXT) CASCADE;
DROP FUNCTION IF EXISTS get_hearing_attendees_count(INTEGER) CASCADE;
DROP FUNCTION IF EXISTS archive_old_hearings() CASCADE;
DROP FUNCTION IF EXISTS get_user_cases(INTEGER) CASCADE;
DROP FUNCTION IF EXISTS get_officer_cases(INTEGER) CASCADE;
DROP FUNCTION IF EXISTS get_person_criminal_history(INTEGER) CASCADE;
DROP FUNCTION IF EXISTS check_duplicate_email(TEXT) CASCADE;
DROP FUNCTION IF EXISTS check_duplicate_username(TEXT) CASCADE;
DROP FUNCTION IF EXISTS create_audit_log(TEXT, TEXT, JSONB) CASCADE;
DROP FUNCTION IF EXISTS get_case_timeline(INTEGER) CASCADE;
DROP FUNCTION IF EXISTS sync_user_to_local(INTEGER) CASCADE;

-- Step 3: Drop all sequences
DROP SEQUENCE IF EXISTS users_id_seq CASCADE;
DROP SEQUENCE IF EXISTS user_images_id_seq CASCADE;
DROP SEQUENCE IF EXISTS hearings_id_seq CASCADE;
DROP SEQUENCE IF EXISTS hearing_attendees_id_seq CASCADE;
DROP SEQUENCE IF EXISTS hearing_minutes_id_seq CASCADE;
DROP SEQUENCE IF EXISTS hearing_notifications_id_seq CASCADE;
DROP SEQUENCE IF EXISTS person_profiles_id_seq CASCADE;
DROP SEQUENCE IF EXISTS criminal_records_id_seq CASCADE;
DROP SEQUENCE IF EXISTS case_involvement_id_seq CASCADE;
DROP SEQUENCE IF EXISTS person_risk_levels_id_seq CASCADE;
DROP SEQUENCE IF EXISTS case_timeline_id_seq CASCADE;
DROP SEQUENCE IF EXISTS case_status_history_id_seq CASCADE;
DROP SEQUENCE IF EXISTS case_investigation_log_id_seq CASCADE;
DROP SEQUENCE IF EXISTS case_admin_notes_id_seq CASCADE;
DROP SEQUENCE IF EXISTS case_user_updates_id_seq CASCADE;

-- Step 4: Verify cleanup
SELECT 
    schemaname,
    tablename
FROM pg_tables
WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
ORDER BY schemaname, tablename;

-- ✅ CLEANUP COMPLETE
-- All tables, functions, and sequences have been deleted
-- Database is now empty and ready for fresh setup
-- Next: Run BMS_STEP_1_CORE_TABLES.sql to start fresh
