# Admin User Management System

## 🎯 Overview

Admin can now manage all users in the system with the ability to:
- **View all users** (Admin, Officer, User)
- **Search users** by username, email, or name
- **View user details** (role, auth provider, creation date)
- **Terminate user accounts** (both Google and Email signup)
- **Delete all user-related data** (CASCADE DELETE)

---

## 📱 Features

### 1. User List View
- **RecyclerView** displaying all users
- **Search functionality** to find users quickly
- **User information displayed:**
  - Username
  - Full name
  - Email
  - Role (Admin/Officer/User)
  - Auth provider (Google/Email)

### 2. User Actions
- **View Details** - See full user information
- **Terminate Account** - Delete user and all related data

### 3. Cascade Delete
When an account is terminated, ALL related data is deleted:

**For USER accounts:**
- ✅ User account deleted
- ✅ All filed cases deleted
- ✅ All case statuses deleted
- ✅ All case notes deleted
- ✅ All profile pictures deleted
- ✅ All user-related data deleted

**For OFFICER accounts:**
- ✅ Officer account deleted
- ✅ All assigned cases deleted
- ✅ All investigations deleted
- ✅ All case assignments deleted
- ✅ All officer-related data deleted

---

## 🗄️ Database Changes

### CASCADE DELETE Constraints
All foreign keys now have `ON DELETE CASCADE`:

```sql
-- Users → Blotter Reports
ALTER TABLE blotter_reports
ADD CONSTRAINT fk_blotter_reports_user_id 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Users → Case Statuses
ALTER TABLE case_statuses
ADD CONSTRAINT fk_case_statuses_user_id 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Users → Case Assignments (Officers)
ALTER TABLE case_assignments
ADD CONSTRAINT fk_case_assignments_officer_id 
FOREIGN KEY (officer_id) REFERENCES users(id) ON DELETE CASCADE;

-- Users → Investigations (Officers)
ALTER TABLE investigations
ADD CONSTRAINT fk_investigations_officer_id 
FOREIGN KEY (officer_id) REFERENCES users(id) ON DELETE CASCADE;

-- And more...
```

### Delete Function
```sql
CREATE OR REPLACE FUNCTION delete_user_account_cascade(p_user_id VARCHAR(255))
RETURNS JSON AS $$
-- Deletes user and all related data
-- Returns count of deleted records
$$ LANGUAGE plpgsql;
```

---

## 📁 Files Created/Updated

### Android App

**New Activities:**
- `AdminManageUsersActivity.java` - Main user management screen

**New Adapters:**
- `UserManagementAdapter.java` - RecyclerView adapter for user list

**Updated:**
- `ApiService.java` - Added `deleteUser()` endpoint

**New Layouts:**
- `activity_admin_manage_users.xml` - Activity layout
- `item_user_management.xml` - User list item layout

### Database

**New SQL Script:**
- `CASCADE_DELETE_SETUP.sql` - Complete cascade delete setup

---

## 🚀 How to Use

### Step 1: Update Database
1. Go to Neon SQL Editor
2. Copy content from `database/CASCADE_DELETE_SETUP.sql`
3. Paste and execute
4. Verify CASCADE DELETE constraints are set up

### Step 2: Add to Admin Dashboard
In `AdminDashboardActivity.java`, add button to launch User Management:

```java
btnManageUsers.setOnClickListener(v -> {
    startActivity(new Intent(this, AdminManageUsersActivity.class));
});
```

### Step 3: Test
1. Pull latest code
2. Rebuild app
3. Login as Admin
4. Go to User Management
5. Search for a user
6. Click "Terminate Account"
7. Confirm deletion
8. User and all data deleted ✅

---

## 🔄 Deletion Flow

```
Admin clicks "Terminate Account"
    ↓
Confirmation dialog shown
    ↓
Admin confirms deletion
    ↓
DELETE FROM users WHERE id = user_id
    ↓
CASCADE DELETE triggers:
    ├─ blotter_reports (user's cases)
    ├─ case_statuses (case statuses)
    ├─ case_assignments (officer assignments)
    ├─ investigations (officer investigations)
    ├─ case_notes (case notes)
    ├─ evidence (case evidence)
    ├─ witnesses (case witnesses)
    ├─ suspects (case suspects)
    ├─ hearings (case hearings)
    ├─ resolutions (case resolutions)
    ├─ kp_forms (KP forms)
    ├─ summons (summons)
    └─ user_images (profile pictures)
    ↓
UI updated immediately
    ↓
User removed from list
    ↓
Toast: "Account terminated: username"
```

---

## 📊 Data Deletion Summary

### What Gets Deleted

| Data Type | Deleted | Reason |
|-----------|---------|--------|
| User Account | ✅ | Primary record |
| Profile Pictures | ✅ | User images |
| Cases Filed | ✅ | User-owned data |
| Case Statuses | ✅ | Case-related data |
| Case Notes | ✅ | Case-related data |
| Evidence | ✅ | Case-related data |
| Witnesses | ✅ | Case-related data |
| Suspects | ✅ | Case-related data |
| Hearings | ✅ | Case-related data |
| Resolutions | ✅ | Case-related data |
| KP Forms | ✅ | Case-related data |
| Summons | ✅ | Case-related data |
| Case Assignments | ✅ | Officer-related data |
| Investigations | ✅ | Officer-related data |

---

## ⚠️ Important Notes

1. **Irreversible** - Account termination CANNOT be undone
2. **Complete Deletion** - All user data is permanently deleted
3. **Cascade Delete** - Database automatically deletes related records
4. **Immediate UI Update** - User removed from list immediately
5. **Both Databases** - Deleted from both Local SQLite and Neon PostgreSQL

---

## 🔐 Security Considerations

1. **Admin Only** - Only Admin role can terminate accounts
2. **Confirmation Required** - Two-step confirmation before deletion
3. **Logging** - All deletions logged in database function
4. **Audit Trail** - Can be tracked via database logs

---

## 📞 API Endpoint

**Delete User Account:**
```
DELETE /api/auth/users/{userId}
```

**Response:**
```json
{
  "success": true,
  "message": "User account and all related data deleted successfully",
  "deleted_records": {
    "user_account": 1,
    "reports": 5,
    "case_assignments": 3,
    "investigations": 2
  }
}
```

---

## ✅ Implementation Checklist

- [x] AdminManageUsersActivity created
- [x] UserManagementAdapter created
- [x] Activity layout created
- [x] Item layout created
- [x] ApiService.deleteUser() added
- [x] CASCADE DELETE SQL script created
- [x] Delete function created
- [x] Documentation provided

---

## 🎉 Status

✅ **COMPLETE & READY FOR TESTING**

**Next Steps:**
1. Run CASCADE_DELETE_SETUP.sql in Neon
2. Add button to AdminDashboardActivity
3. Test user termination
4. Verify cascade delete works
5. Deploy to production

---

**Last Updated**: 2025-11-28  
**Version**: 1.0
