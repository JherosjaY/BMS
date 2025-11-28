-- ============================================================================
-- CASCADE DELETE SETUP FOR USER ACCOUNT TERMINATION
-- When a user/officer is deleted, ALL related data is automatically deleted
-- ============================================================================

-- STEP 1: Add CASCADE DELETE to all foreign keys related to users table
-- This ensures when a user is deleted, all their data is deleted too

-- If blotter_reports table exists, add CASCADE DELETE
ALTER TABLE IF EXISTS blotter_reports
DROP CONSTRAINT IF EXISTS fk_blotter_reports_user_id CASCADE;

ALTER TABLE IF EXISTS blotter_reports
ADD CONSTRAINT fk_blotter_reports_user_id 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- If case_statuses table exists, add CASCADE DELETE
ALTER TABLE IF EXISTS case_statuses
DROP CONSTRAINT IF EXISTS fk_case_statuses_user_id CASCADE;

ALTER TABLE IF EXISTS case_statuses
ADD CONSTRAINT fk_case_statuses_user_id 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- If case_assignments table exists, add CASCADE DELETE
ALTER TABLE IF EXISTS case_assignments
DROP CONSTRAINT IF EXISTS fk_case_assignments_officer_id CASCADE;

ALTER TABLE IF EXISTS case_assignments
ADD CONSTRAINT fk_case_assignments_officer_id 
FOREIGN KEY (officer_id) REFERENCES users(id) ON DELETE CASCADE;

-- If investigations table exists, add CASCADE DELETE
ALTER TABLE IF EXISTS investigations
DROP CONSTRAINT IF EXISTS fk_investigations_officer_id CASCADE;

ALTER TABLE IF EXISTS investigations
ADD CONSTRAINT fk_investigations_officer_id 
FOREIGN KEY (officer_id) REFERENCES users(id) ON DELETE CASCADE;

-- If case_notes table exists, add CASCADE DELETE
ALTER TABLE IF EXISTS case_notes
DROP CONSTRAINT IF EXISTS fk_case_notes_user_id CASCADE;

ALTER TABLE IF EXISTS case_notes
ADD CONSTRAINT fk_case_notes_user_id 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- If evidence table exists, add CASCADE DELETE
ALTER TABLE IF EXISTS evidence
DROP CONSTRAINT IF EXISTS fk_evidence_case_id CASCADE;

ALTER TABLE IF EXISTS evidence
ADD CONSTRAINT fk_evidence_case_id 
FOREIGN KEY (case_id) REFERENCES blotter_reports(id) ON DELETE CASCADE;

-- If witnesses table exists, add CASCADE DELETE
ALTER TABLE IF EXISTS witnesses
DROP CONSTRAINT IF EXISTS fk_witnesses_case_id CASCADE;

ALTER TABLE IF EXISTS witnesses
ADD CONSTRAINT fk_witnesses_case_id 
FOREIGN KEY (case_id) REFERENCES blotter_reports(id) ON DELETE CASCADE;

-- If suspects table exists, add CASCADE DELETE
ALTER TABLE IF EXISTS suspects
DROP CONSTRAINT IF EXISTS fk_suspects_case_id CASCADE;

ALTER TABLE IF EXISTS suspects
ADD CONSTRAINT fk_suspects_case_id 
FOREIGN KEY (case_id) REFERENCES blotter_reports(id) ON DELETE CASCADE;

-- If hearings table exists, add CASCADE DELETE
ALTER TABLE IF EXISTS hearings
DROP CONSTRAINT IF EXISTS fk_hearings_case_id CASCADE;

ALTER TABLE IF EXISTS hearings
ADD CONSTRAINT fk_hearings_case_id 
FOREIGN KEY (case_id) REFERENCES blotter_reports(id) ON DELETE CASCADE;

-- If resolutions table exists, add CASCADE DELETE
ALTER TABLE IF EXISTS resolutions
DROP CONSTRAINT IF EXISTS fk_resolutions_case_id CASCADE;

ALTER TABLE IF EXISTS resolutions
ADD CONSTRAINT fk_resolutions_case_id 
FOREIGN KEY (case_id) REFERENCES blotter_reports(id) ON DELETE CASCADE;

-- If kp_forms table exists, add CASCADE DELETE
ALTER TABLE IF EXISTS kp_forms
DROP CONSTRAINT IF EXISTS fk_kp_forms_case_id CASCADE;

ALTER TABLE IF EXISTS kp_forms
ADD CONSTRAINT fk_kp_forms_case_id 
FOREIGN KEY (case_id) REFERENCES blotter_reports(id) ON DELETE CASCADE;

-- If summons table exists, add CASCADE DELETE
ALTER TABLE IF EXISTS summons
DROP CONSTRAINT IF EXISTS fk_summons_case_id CASCADE;

ALTER TABLE IF EXISTS summons
ADD CONSTRAINT fk_summons_case_id 
FOREIGN KEY (case_id) REFERENCES blotter_reports(id) ON DELETE CASCADE;

-- If user_images table exists, add CASCADE DELETE (already done)
ALTER TABLE IF EXISTS user_images
DROP CONSTRAINT IF EXISTS fk_user_images_user CASCADE;

ALTER TABLE IF EXISTS user_images
ADD CONSTRAINT fk_user_images_user 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- ============================================================================
-- STEP 2: Create comprehensive delete function
-- ============================================================================

CREATE OR REPLACE FUNCTION delete_user_account_cascade(p_user_id VARCHAR(255))
RETURNS JSON AS $$
DECLARE
    v_user_count INT;
    v_reports_deleted INT;
    v_assignments_deleted INT;
    v_investigations_deleted INT;
    result JSON;
BEGIN
    -- Check if user exists
    SELECT COUNT(*) INTO v_user_count FROM users WHERE id = p_user_id;
    
    IF v_user_count = 0 THEN
        result := json_build_object(
            'success', false,
            'message', 'User not found'
        );
        RETURN result;
    END IF;
    
    -- Count related records before deletion
    SELECT COUNT(*) INTO v_reports_deleted FROM blotter_reports WHERE user_id = p_user_id;
    SELECT COUNT(*) INTO v_assignments_deleted FROM case_assignments WHERE officer_id = p_user_id;
    SELECT COUNT(*) INTO v_investigations_deleted FROM investigations WHERE officer_id = p_user_id;
    
    -- Delete user (CASCADE will delete all related data)
    DELETE FROM users WHERE id = p_user_id;
    
    result := json_build_object(
        'success', true,
        'message', 'User account and all related data deleted successfully',
        'deleted_records', json_build_object(
            'user_account', 1,
            'reports', v_reports_deleted,
            'case_assignments', v_assignments_deleted,
            'investigations', v_investigations_deleted
        )
    );
    
    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- STEP 3: Verification - Check CASCADE DELETE constraints
-- ============================================================================

SELECT 
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name,
    rc.update_rule,
    rc.delete_rule
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu ON ccu.constraint_name = tc.constraint_name
JOIN information_schema.referential_constraints AS rc ON rc.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
AND ccu.table_name = 'users'
ORDER BY tc.table_name;

-- ============================================================================
-- STEP 4: Test the cascade delete function (OPTIONAL - comment out if not testing)
-- ============================================================================

-- To test, uncomment and run:
-- SELECT * FROM delete_user_account_cascade('test_user_id');

-- ============================================================================
-- SUMMARY OF CASCADE DELETE SETUP
-- ============================================================================
-- ✅ All foreign keys now have ON DELETE CASCADE
-- ✅ When a user is deleted, all their data is automatically deleted:
--    - Blotter reports (cases)
--    - Case statuses
--    - Case assignments
--    - Investigations
--    - Case notes
--    - Evidence
--    - Witnesses
--    - Suspects
--    - Hearings
--    - Resolutions
--    - KP Forms
--    - Summons
--    - User images
-- ✅ Comprehensive delete function created for logging/tracking
-- ============================================================================
