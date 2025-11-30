# BMS Backend - Complete API Endpoints Reference

## 📋 Table of Contents
1. [Authentication](#authentication)
2. [User Management](#user-management)
3. [Case Management](#case-management)
4. [Blotter Reports](#blotter-reports)
5. [Officer Workflow](#officer-workflow)
6. [Evidence Management](#evidence-management)
7. [Notifications](#notifications)
8. [Dashboard](#dashboard)

---

## Authentication

### POST /api/auth/register
Register a new user account.

**Request:**
```json
{
  "username": "user123",
  "email": "user@example.com",
  "password": "SecurePass123",
  "first_name": "John",
  "last_name": "Doe"
}
```

**Response (201):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "username": "user123",
    "email": "user@example.com",
    "first_name": "John",
    "last_name": "Doe",
    "role": "user"
  },
  "token": "eyJhbGciOiJIUzI1NiIs..."
}
```

### POST /api/auth/login
Login with username/email and password.

**Request:**
```json
{
  "username": "user123",
  "password": "SecurePass123"
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "id": 1,
    "username": "user123",
    "email": "user@example.com",
    "first_name": "John",
    "last_name": "Doe",
    "role": "user",
    "profile_picture": "https://..."
  },
  "token": "eyJhbGciOiJIUzI1NiIs..."
}
```

### POST /api/auth/google
Google OAuth sign-in.

**Request:**
```json
{
  "google_id": "google_uid_123",
  "email": "user@gmail.com",
  "first_name": "John",
  "last_name": "Doe",
  "profile_picture": "https://lh3.googleusercontent.com/..."
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Google sign-in successful",
  "data": {
    "id": 1,
    "username": "user@gmail.com",
    "email": "user@gmail.com",
    "first_name": "John",
    "last_name": "Doe",
    "role": "user",
    "profile_picture": "https://..."
  },
  "token": "eyJhbGciOiJIUzI1NiIs..."
}
```

### POST /api/auth/check-email
Check if email is already registered.

**Request:**
```json
{
  "email": "user@example.com"
}
```

**Response (200):**
```json
{
  "success": true,
  "exists": false
}
```

### POST /api/auth/refresh
Refresh JWT token.

**Request:**
```json
{
  "token": "expired_token_here"
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Token refreshed",
  "token": "new_token_here"
}
```

### POST /api/auth/logout
Logout user (client-side token removal).

**Response (200):**
```json
{
  "success": true,
  "message": "Logout successful"
}
```

---

## User Management

### GET /api/users
Get all users (Admin only).

**Query Parameters:**
- `role`: Filter by role (admin, officer, user)
- `is_active`: Filter by active status (true/false)

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "username": "user123",
      "email": "user@example.com",
      "first_name": "John",
      "last_name": "Doe",
      "role": "user",
      "is_active": true,
      "created_at": "2025-01-15T10:30:00Z"
    }
  ],
  "total": 1
}
```

### GET /api/users/:id
Get user profile by ID.

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "user123",
    "email": "user@example.com",
    "first_name": "John",
    "last_name": "Doe",
    "role": "user",
    "badge_number": null,
    "department": null,
    "phone_number": "+1234567890",
    "profile_picture": "https://...",
    "is_active": true,
    "created_at": "2025-01-15T10:30:00Z"
  }
}
```

### PUT /api/users/:id
Update user profile.

**Request:**
```json
{
  "first_name": "John",
  "last_name": "Smith",
  "phone_number": "+1234567890",
  "profile_picture": "https://..."
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "User updated successfully",
  "data": {
    "id": 1,
    "username": "user123",
    "email": "user@example.com",
    "first_name": "John",
    "last_name": "Smith",
    "role": "user"
  }
}
```

### DELETE /api/users/:id
Delete user (Admin only).

**Response (200):**
```json
{
  "success": true,
  "message": "User deleted successfully"
}
```

### POST /api/users/officers
Create new officer (Admin only).

**Request:**
```json
{
  "username": "officer123",
  "email": "officer@bms.gov",
  "password": "SecurePass123",
  "first_name": "Juan",
  "last_name": "Santos",
  "badge_number": "BDG001",
  "department": "Police"
}
```

**Response (201):**
```json
{
  "success": true,
  "message": "Officer created successfully",
  "data": {
    "id": 5,
    "username": "officer123",
    "email": "officer@bms.gov",
    "first_name": "Juan",
    "last_name": "Santos",
    "badge_number": "BDG001",
    "department": "Police",
    "role": "officer"
  }
}
```

### GET /api/users/officers
Get all officers.

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 5,
      "username": "officer123",
      "email": "officer@bms.gov",
      "first_name": "Juan",
      "last_name": "Santos",
      "badge_number": "BDG001",
      "department": "Police",
      "is_active": true,
      "total_cases": 5,
      "completed_cases": 3,
      "rating": 4.5
    }
  ],
  "total": 1
}
```

### PUT /api/users/:id/role
Update user role (Admin only).

**Request:**
```json
{
  "role": "officer"
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "User role updated successfully",
  "data": {
    "id": 1,
    "username": "user123",
    "email": "user@example.com",
    "role": "officer"
  }
}
```

---

## Case Management

### POST /api/cases
Create new case.

**Request:**
```json
{
  "title": "Theft Report",
  "description": "Stolen laptop from office",
  "priority": "high",
  "incident_date": "2025-01-15T10:30:00Z",
  "incident_location": "Main Street, Building A"
}
```

**Response (201):**
```json
{
  "success": true,
  "message": "Case created successfully",
  "data": {
    "id": 1,
    "case_number": "CASE-2025-00001",
    "title": "Theft Report",
    "description": "Stolen laptop from office",
    "status": "pending",
    "priority": "high",
    "incident_date": "2025-01-15T10:30:00Z",
    "incident_location": "Main Street, Building A",
    "created_at": "2025-01-15T10:30:00Z"
  }
}
```

### GET /api/cases
Get all cases with filtering.

**Query Parameters:**
- `status`: pending, in-progress, resolved, closed
- `priority`: low, medium, high
- `assigned_officer_id`: Filter by officer
- `page`: Page number (default: 1)
- `limit`: Items per page (default: 10)

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "case_number": "CASE-2025-00001",
      "title": "Theft Report",
      "status": "pending",
      "priority": "high",
      "assigned_officer_id": null,
      "created_at": "2025-01-15T10:30:00Z"
    }
  ],
  "pagination": {
    "total": 10,
    "page": 1,
    "limit": 10,
    "pages": 1
  }
}
```

### GET /api/cases/:id
Get case details with evidence and history.

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "case_number": "CASE-2025-00001",
    "title": "Theft Report",
    "description": "Stolen laptop from office",
    "status": "pending",
    "priority": "high",
    "incident_date": "2025-01-15T10:30:00Z",
    "incident_location": "Main Street, Building A",
    "created_at": "2025-01-15T10:30:00Z",
    "evidence": [
      {
        "id": 1,
        "file_name": "photo.jpg",
        "file_url": "/uploads/photo.jpg",
        "file_type": "image",
        "verified": false
      }
    ],
    "history": []
  }
}
```

### PUT /api/cases/:id
Update case details.

**Request:**
```json
{
  "title": "Updated Title",
  "description": "Updated description",
  "priority": "medium"
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Case updated successfully",
  "data": {
    "id": 1,
    "case_number": "CASE-2025-00001",
    "title": "Updated Title",
    "status": "pending",
    "priority": "medium"
  }
}
```

### DELETE /api/cases/:id
Delete case (Admin only).

**Response (200):**
```json
{
  "success": true,
  "message": "Case deleted successfully"
}
```

### POST /api/cases/:id/assign
Assign case to officer.

**Request:**
```json
{
  "officer_id": 5
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Case assigned successfully",
  "data": {
    "id": 1,
    "case_number": "CASE-2025-00001",
    "status": "in-progress",
    "assigned_officer_id": 5
  }
}
```

### PUT /api/cases/:id/status
Update case status.

**Request:**
```json
{
  "status": "in-progress"
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Case status updated successfully",
  "data": {
    "id": 1,
    "case_number": "CASE-2025-00001",
    "status": "in-progress"
  }
}
```

### GET /api/cases/officer/:officerId
Get officer's assigned cases.

**Query Parameters:**
- `status`: Filter by status
- `page`: Page number
- `limit`: Items per page

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "case_number": "CASE-2025-00001",
      "title": "Theft Report",
      "status": "in-progress",
      "priority": "high"
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 10
  }
}
```

### GET /api/cases/user/:userId
Get user's reported cases.

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "case_number": "CASE-2025-00001",
      "title": "Theft Report",
      "status": "pending",
      "created_at": "2025-01-15T10:30:00Z"
    }
  ]
}
```

---

## Blotter Reports

### POST /api/blotters
Create blotter report.

**Request:**
```json
{
  "complainant_name": "John Doe",
  "complainant_contact": "09123456789",
  "respondent_name": "Jane Smith",
  "respondent_address": "123 Main St",
  "incident_date": "2025-01-15T10:30:00Z",
  "incident_location": "Main Street",
  "description": "Incident description"
}
```

**Response (201):**
```json
{
  "success": true,
  "message": "Blotter report created successfully",
  "data": {
    "id": 1,
    "report_number": "BLOTTER-2025-00001",
    "complainant_name": "John Doe",
    "incident_date": "2025-01-15T10:30:00Z",
    "incident_location": "Main Street",
    "status": "pending",
    "created_at": "2025-01-15T10:30:00Z"
  }
}
```

### GET /api/blotters
Get all blotter reports.

**Query Parameters:**
- `status`: pending, under-investigation, resolved, closed
- `page`: Page number
- `limit`: Items per page

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "report_number": "BLOTTER-2025-00001",
      "complainant_name": "John Doe",
      "status": "pending",
      "created_at": "2025-01-15T10:30:00Z"
    }
  ],
  "pagination": {
    "total": 5,
    "page": 1,
    "limit": 10,
    "pages": 1
  }
}
```

### GET /api/blotters/:id
Get blotter report details.

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "report_number": "BLOTTER-2025-00001",
    "complainant_name": "John Doe",
    "complainant_contact": "09123456789",
    "respondent_name": "Jane Smith",
    "respondent_address": "123 Main St",
    "incident_date": "2025-01-15T10:30:00Z",
    "incident_location": "Main Street",
    "description": "Incident description",
    "status": "pending",
    "created_at": "2025-01-15T10:30:00Z"
  }
}
```

### PUT /api/blotters/:id
Update blotter report.

**Request:**
```json
{
  "complainant_name": "John Doe Updated",
  "description": "Updated description"
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Blotter report updated successfully",
  "data": {
    "id": 1,
    "report_number": "BLOTTER-2025-00001",
    "complainant_name": "John Doe Updated"
  }
}
```

### PUT /api/blotters/:id/status
Update blotter status.

**Request:**
```json
{
  "status": "under-investigation"
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Blotter status updated successfully",
  "data": {
    "id": 1,
    "report_number": "BLOTTER-2025-00001",
    "status": "under-investigation"
  }
}
```

### DELETE /api/blotters/:id
Delete blotter report (Admin only).

**Response (200):**
```json
{
  "success": true,
  "message": "Blotter report deleted successfully"
}
```

### POST /api/blotters/:id/assign
Assign blotter to officer.

**Request:**
```json
{
  "officer_id": 5
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Blotter assigned to officer successfully",
  "data": {
    "id": 1,
    "report_number": "BLOTTER-2025-00001",
    "status": "under-investigation",
    "assigned_officer_id": 5
  }
}
```

---

## Officer Workflow

### POST /api/officers/assign-case
Assign case to officer (Admin only).

**Request:**
```json
{
  "officer_id": 5,
  "case_id": 10
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Case assigned to officer successfully",
  "data": {
    "id": 1,
    "officer_id": 5,
    "case_id": 10,
    "status": "assigned",
    "assigned_at": "2025-01-15T10:30:00Z"
  }
}
```

### GET /api/officers/:officerId/cases
Get officer's assigned cases.

**Query Parameters:**
- `status`: Filter by case status
- `page`: Page number
- `limit`: Items per page

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "case_number": "CASE-2025-00001",
      "title": "Theft Report",
      "status": "in-progress",
      "priority": "high",
      "assignment_status": "accepted"
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 10
  }
}
```

### GET /api/officers/workload
Get all officers workload (Admin only).

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 5,
      "first_name": "Juan",
      "last_name": "Santos",
      "badge_number": "BDG001",
      "department": "Police",
      "assigned_cases": 5,
      "completed_cases": 3,
      "rating": 4.5,
      "avg_resolution_time": 2.5
    }
  ]
}
```

### GET /api/officers/availability
Get officer availability (Admin only).

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 5,
      "first_name": "Juan",
      "last_name": "Santos",
      "badge_number": "BDG001",
      "active_cases": 2,
      "availability_status": "available"
    }
  ]
}
```

### PUT /api/officers/:officerId/status
Update officer status (Admin only).

**Request:**
```json
{
  "status": "on-leave"
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Officer status updated successfully",
  "data": {
    "id": 5,
    "first_name": "Juan",
    "last_name": "Santos",
    "is_active": false
  }
}
```

### GET /api/officers/performance
Get officer performance metrics (Admin only).

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 5,
      "first_name": "Juan",
      "last_name": "Santos",
      "badge_number": "BDG001",
      "department": "Police",
      "total_cases": 10,
      "completed_cases": 8,
      "avg_resolution_time": 2.5,
      "rating": 4.5,
      "completion_rate": 80.0
    }
  ]
}
```

### PUT /api/officers/case/:caseId/accept
Officer accepts case assignment.

**Response (200):**
```json
{
  "success": true,
  "message": "Case assignment accepted",
  "data": {
    "id": 1,
    "officer_id": 5,
    "case_id": 10,
    "status": "accepted",
    "accepted_at": "2025-01-15T10:30:00Z"
  }
}
```

### PUT /api/officers/case/:caseId/reject
Officer rejects case assignment.

**Request:**
```json
{
  "rejection_reason": "Too busy with current cases"
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Case assignment rejected",
  "data": {
    "id": 1,
    "officer_id": 5,
    "case_id": 10,
    "status": "rejected",
    "rejection_reason": "Too busy with current cases"
  }
}
```

### PUT /api/officers/case/:caseId/complete
Officer marks case as complete.

**Response (200):**
```json
{
  "success": true,
  "message": "Case marked as completed",
  "data": {
    "id": 10,
    "case_number": "CASE-2025-00001",
    "status": "resolved"
  }
}
```

---

## Evidence Management

### POST /api/evidence
Upload evidence file.

**Request (multipart/form-data):**
- `file`: Binary file (image, PDF, document)
- `case_id`: Case ID (integer)
- `description`: Evidence description (string)

**Response (201):**
```json
{
  "success": true,
  "message": "Evidence uploaded successfully",
  "data": {
    "id": 1,
    "file_name": "photo.jpg",
    "file_url": "/uploads/1234567890-photo.jpg",
    "file_type": "image",
    "description": "Crime scene photo",
    "uploaded_at": "2025-01-15T10:30:00Z"
  }
}
```

### GET /api/evidence/case/:caseId
Get case evidence.

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "file_name": "photo.jpg",
      "file_url": "/uploads/1234567890-photo.jpg",
      "file_type": "image",
      "description": "Crime scene photo",
      "verified": false,
      "uploaded_at": "2025-01-15T10:30:00Z"
    }
  ]
}
```

### DELETE /api/evidence/:id
Delete evidence file.

**Response (200):**
```json
{
  "success": true,
  "message": "Evidence deleted successfully"
}
```

### POST /api/evidence/:id/verify
Verify evidence (Officer/Admin only).

**Response (200):**
```json
{
  "success": true,
  "message": "Evidence verified successfully",
  "data": {
    "id": 1,
    "file_name": "photo.jpg",
    "verified": true,
    "verified_at": "2025-01-15T10:30:00Z"
  }
}
```

---

## Notifications

### GET /api/notifications/user/:userId
Get user notifications.

**Query Parameters:**
- `is_read`: Filter by read status (true/false)
- `page`: Page number
- `limit`: Items per page (default: 20)

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "user_id": 5,
      "title": "New Case Assignment",
      "message": "You have been assigned case #CASE-2025-00001",
      "type": "case_assigned",
      "is_read": false,
      "related_id": 10,
      "related_type": "case",
      "created_at": "2025-01-15T10:30:00Z"
    }
  ],
  "unread_count": 5,
  "pagination": {
    "page": 1,
    "limit": 20
  }
}
```

### PUT /api/notifications/:id/read
Mark notification as read.

**Response (200):**
```json
{
  "success": true,
  "message": "Notification marked as read",
  "data": {
    "id": 1,
    "is_read": true,
    "read_at": "2025-01-15T10:30:00Z"
  }
}
```

### PUT /api/notifications/user/:userId/read-all
Mark all notifications as read.

**Response (200):**
```json
{
  "success": true,
  "message": "All notifications marked as read"
}
```

### DELETE /api/notifications/:id
Delete notification.

**Response (200):**
```json
{
  "success": true,
  "message": "Notification deleted successfully"
}
```

### POST /api/notifications
Create notification (Admin only).

**Request:**
```json
{
  "user_id": 5,
  "title": "System Notification",
  "message": "Your case has been updated",
  "type": "case_update",
  "related_id": 10,
  "related_type": "case"
}
```

**Response (201):**
```json
{
  "success": true,
  "message": "Notification created successfully",
  "data": {
    "id": 1,
    "user_id": 5,
    "title": "System Notification",
    "message": "Your case has been updated",
    "type": "case_update",
    "is_read": false,
    "created_at": "2025-01-15T10:30:00Z"
  }
}
```

---

## Dashboard

### GET /api/dashboard/stats
Get system statistics (Admin only).

**Response (200):**
```json
{
  "success": true,
  "data": {
    "users": {
      "total": 50,
      "officers": 10
    },
    "cases": {
      "total": 100,
      "by_status": {
        "pending": 20,
        "in-progress": 30,
        "resolved": 40,
        "closed": 10
      }
    },
    "blotters": {
      "total": 80,
      "by_status": {
        "pending": 15,
        "under-investigation": 25,
        "resolved": 30,
        "closed": 10
      }
    },
    "evidence": {
      "total": 250
    }
  }
}
```

### GET /api/dashboard/officer-workload
Get officer workload statistics (Admin only).

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 5,
      "first_name": "Juan",
      "last_name": "Santos",
      "badge_number": "BDG001",
      "department": "Police",
      "assigned_cases": 10,
      "completed_cases": 8,
      "active_cases": 2,
      "rating": 4.5,
      "avg_resolution_time": 2.5
    }
  ]
}
```

### GET /api/dashboard/case-status
Get case status statistics (Admin only).

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "status": "pending",
      "total": 20,
      "high_priority": 5,
      "medium_priority": 10,
      "low_priority": 5,
      "avg_age_days": 3.5
    }
  ]
}
```

### GET /api/dashboard/blotter-analytics
Get blotter analytics (Admin only).

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "status": "pending",
      "total": 15,
      "assigned": 10,
      "unassigned": 5,
      "avg_age_days": 2.5
    }
  ]
}
```

### GET /api/dashboard/evidence-summary
Get evidence summary (Admin only).

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "file_type": "image",
      "total": 150,
      "verified": 120,
      "total_size": 5242880
    }
  ]
}
```

### GET /api/dashboard/recent-activity
Get recent system activity (Admin only).

**Query Parameters:**
- `limit`: Number of activities to return (default: 20)

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "action": "case_created",
      "description": "Case CASE-2025-00001 created",
      "created_at": "2025-01-15T10:30:00Z",
      "first_name": "John",
      "last_name": "Doe",
      "role": "user"
    }
  ]
}
```

### GET /api/dashboard/case-resolution-time
Get case resolution time statistics (Admin only).

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "resolution_days": 1,
      "case_count": 5
    },
    {
      "resolution_days": 2,
      "case_count": 8
    }
  ]
}
```

---

## Error Responses

All error responses follow this format:

```json
{
  "success": false,
  "message": "Error description",
  "errors": [
    {
      "field": "email",
      "message": "Invalid email address"
    }
  ]
}
```

### Common HTTP Status Codes
- `200`: Success
- `201`: Created
- `400`: Bad Request (validation error)
- `401`: Unauthorized (missing/invalid token)
- `403`: Forbidden (insufficient permissions)
- `404`: Not Found
- `500`: Internal Server Error

---

**Last Updated**: 2025-01-15
**Version**: 1.0.0
**Status**: Production Ready
