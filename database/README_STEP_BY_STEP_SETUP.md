# 📋 BMS DATABASE SETUP - STEP BY STEP

## ⚠️ IMPORTANT: Clean Start

This is a **fresh, step-by-step setup** for the Neon database. All tables will be created incrementally.

---

## 🚀 SETUP INSTRUCTIONS

### STEP 0: DELETE EXISTING TABLES (Optional - Only if you want to start fresh)

If you have existing tables and want to start completely fresh:

1. Open **Neon Console** → SQL Editor
2. Copy & paste: `CLEANUP_DELETE_ALL_TABLES.sql`
3. Execute
4. Wait for completion
5. Verify all tables are deleted

⚠️ **WARNING**: This will DELETE ALL DATA! Use only if you want a clean start.

---

### STEP 1: CREATE CORE TABLES (Users & User Images)

**File**: `BMS_STEP_1_CORE_TABLES.sql`

**What it creates:**
- ✅ `users` table (authentication)
- ✅ `user_images` table (profile pictures)
- ✅ Pre-created admin account

**How to run:**
1. Open Neon Console → SQL Editor
2. Copy & paste entire `BMS_STEP_1_CORE_TABLES.sql`
3. Execute
4. Verify: Should see 2 tables created + 1 admin user

**Tables created**: 2
**Rows inserted**: 1 (admin account)

---

### STEP 2: CREATE HEARINGS TABLES

**File**: `BMS_STEP_2_HEARINGS_TABLES.sql`

**What it creates:**
- ✅ `hearings` table (court hearings)
- ✅ `hearing_attendees` table (people attending)
- ✅ `hearing_minutes` table (notes/minutes)
- ✅ `hearing_notifications` table (notifications)

**How to run:**
1. Open Neon Console → SQL Editor
2. Copy & paste entire `BMS_STEP_2_HEARINGS_TABLES.sql`
3. Execute
4. Verify: Should see 4 tables created

**Tables created**: 4
**Total so far**: 6 tables

---

### STEP 3: CREATE PERSON HISTORY TABLES

**File**: `BMS_STEP_3_PERSON_HISTORY_TABLES.sql`

**What it creates:**
- ✅ `person_profiles` table (individual information)
- ✅ `criminal_records` table (criminal history)
- ✅ `case_involvement` table (person's involvement in cases)
- ✅ `person_risk_levels` table (risk assessment)

### STEP 4: CREATE CASE MANAGEMENT TABLES

**File**: `BMS_STEP_4_CASE_MANAGEMENT_TABLES.sql`

**What it creates:**
- ✅ `case_timeline` table (main case info)
- ✅ `case_status_history` table (status changes)
- ✅ `case_investigation_log` table (investigation activities)
- ✅ `case_admin_notes` table (admin notes)
- ✅ `case_user_updates` table (user-visible updates)

**How to run:**
1. Open Neon Console → SQL Editor
2. Copy & paste entire `BMS_STEP_4_CASE_MANAGEMENT_TABLES.sql`
3. Execute
4. Verify: Should see 5 tables created

**Tables created**: 5
**Total so far**: 15 tables

---

### STEP 5: CREATE ADDITIONAL TABLES (Part 1)

**File**: `BMS_STEP_5_ADDITIONAL_TABLES.sql`

**What it creates:**
- ✅ `activity_logs` table (system activity tracking)
- ✅ `admin_sms_logs` table (SMS sent by admins)
- ✅ `blotter_reports` table (police blotter reports)
- ✅ `case_evidence` table (evidence management)
- ✅ `fcm_tokens` table (Firebase Cloud Messaging tokens)
- ✅ `sms_notifications` table (SMS notifications)

**How to run:**
1. Open Neon Console → SQL Editor
2. Copy & paste entire `BMS_STEP_5_ADDITIONAL_TABLES.sql`
3. Execute
4. Verify: Should see 6 tables created

**Tables created**: 6
**Total so far**: 21 tables

---

### STEP 6: CREATE FINAL TABLES (Part 2)

**File**: `BMS_STEP_6_FINAL_TABLES.sql`

**What it creates:**
- ✅ `suspects` table (suspect information)
- ✅ `witnesses` table (witness information)
- ✅ `user_activities` table (detailed user activity tracking)
- ✅ `user_fcm_tokens` table (user-specific FCM tokens)
- ✅ `user_sessions` table (session management)

**How to run:**
1. Open Neon Console → SQL Editor
2. Copy & paste entire `BMS_STEP_6_FINAL_TABLES.sql`
3. Execute
4. Verify: Should see 5 tables created

**Tables created**: 5
**Total so far**: 26 tables ✅

---

## 📊 FINAL VERIFICATION

After all 6 steps, run this query to verify all tables:

```sql
SELECT 
    schemaname,
    tablename,
    (SELECT COUNT(*) FROM information_schema.columns 
     WHERE table_name = pg_tables.tablename) as column_count
FROM pg_tables
WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
ORDER BY tablename;
```

**Expected output**: 26 tables

---

## 📋 TABLE SUMMARY

### Core (2 tables)
1. `users` - User authentication
2. `user_images` - Profile pictures

### Hearings (4 tables)
3. `hearings` - Court hearings
4. `hearing_attendees` - Attendees
5. `hearing_minutes` - Minutes/notes
6. `hearing_notifications` - Notifications

### Person History (4 tables)
7. `person_profiles` - Individual info
8. `criminal_records` - Criminal history
9. `case_involvement` - Case involvement
10. `person_risk_levels` - Risk assessment

### Case Management (5 tables)
11. `case_timeline` - Main case info
12. `case_status_history` - Status changes
13. `case_investigation_log` - Investigation logs
14. `case_admin_notes` - Admin notes
15. `case_user_updates` - User updates

### Additional Tables (6 tables)
16. `activity_logs` - System activity tracking
17. `admin_sms_logs` - SMS sent by admins
18. `blotter_reports` - Police blotter reports
19. `case_evidence` - Evidence management
20. `fcm_tokens` - Firebase Cloud Messaging tokens
21. `sms_notifications` - SMS notifications

### Final Tables (5 tables)
22. `suspects` - Suspect information
23. `witnesses` - Witness information
24. `user_activities` - Detailed user activity tracking
25. `user_fcm_tokens` - User-specific FCM tokens
26. `user_sessions` - Session management

---

## 🔐 ADMIN ACCOUNT (Pre-created in STEP 1)

```
Username: bms.official.admin
Password: BMS2025
Email: official.bms.2025@gmail.com
Role: Admin
```

Use this to login and test the app!

---

## ✅ WHAT'S INCLUDED

✅ **26 Complete Tables** - All functionality
✅ **50+ Indexes** - Performance optimized
✅ **Foreign Keys** - Data integrity
✅ **Constraints** - Data validation
✅ **Admin Account** - Pre-created for testing
✅ **Step-by-step** - Easy to follow
✅ **6 SQL Scripts** - Organized setup

---

## 🎯 NEXT STEPS

After all tables are created:

1. ✅ Test login with admin account
2. ✅ Test user registration
3. ✅ Test Google Sign-In
4. ✅ Test officer creation
5. ✅ Verify real-time sync to Neon

---

## 📝 NOTES

- Each step is **independent** - can be run separately
- Tables have **proper indexes** for performance
- **Foreign keys** maintain data integrity
- **Constraints** validate data
- **Timestamps** track changes
- **Roles** support multi-user access

---

## 🚀 STATUS

✅ **STEP-BY-STEP SETUP READY**
✅ **CLEAN AND ORGANIZED**
✅ **PRODUCTION READY**

**Ready to build?** Start with STEP 1! 🎯
