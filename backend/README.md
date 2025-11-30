# Blotter Management System (BMS) - Backend API

Production-ready backend for the Blotter Management System with complete officer workflow management, case handling, and evidence tracking.

## 🚀 Features

- **Authentication System**: JWT-based auth with Google OAuth support
- **User Management**: Role-based access control (Admin, Officer, User)
- **Case Management**: Create, assign, and track cases with priority levels
- **Blotter Reports**: Manage incident reports with officer assignment
- **Evidence Management**: Upload and verify case evidence
- **Officer Workflow**: Case assignment, acceptance/rejection, and completion tracking
- **Notifications**: Real-time notifications for case assignments
- **Dashboard Analytics**: Comprehensive system statistics and performance metrics
- **Activity Logging**: Track all system activities

## 📋 Tech Stack

- **Runtime**: Node.js
- **Framework**: Express.js
- **Database**: PostgreSQL (Neon)
- **Authentication**: JWT + bcryptjs
- **File Upload**: Multer
- **Security**: Helmet, CORS, Rate Limiting
- **Validation**: Express Validator

## 🔧 Installation

### Prerequisites
- Node.js v16+ 
- PostgreSQL database (Neon recommended)
- npm or yarn

### Setup Steps

1. **Clone and install dependencies**
```bash
cd backend
npm install
```

2. **Configure environment variables**
```bash
cp .env.example .env
```

Edit `.env` with your configuration:
```
DATABASE_URL=postgresql://user:password@host:port/database
JWT_SECRET=your_super_secret_key_change_in_production
JWT_EXPIRY=7d
PORT=5000
NODE_ENV=production
CORS_ORIGIN=http://localhost:3000,http://localhost:8080
```

3. **Run database migrations**
```bash
npm run migrate
```

4. **Seed initial data (admin user + sample officers)**
```bash
npm run seed
```

5. **Start the server**
```bash
npm start
```

For development with auto-reload:
```bash
npm run dev
```

## 🔐 Authentication System

### Two Authentication Methods:

**1. Normal Registration (Email/Password)**
- User creates account with email, password, and name
- Auto-assigned 'user' role
- Returns JWT token for immediate login
- Password hashed with bcryptjs (10 salt rounds)

**2. Google OAuth (One-Click Signup/Login)**
- User clicks "Continue with Google"
- Firebase handles Google authentication
- Backend creates account or logs in existing user
- Auto-assigned 'user' role for new signups
- Returns JWT token for immediate login

### Complete Authentication Flows

See **AUTHENTICATION_FLOWS.md** for detailed documentation on:
- Normal registration flow
- Google OAuth flow (signup & login)
- Normal login flow
- Role-based routing
- Token management
- Error handling
- Security features

## 📚 API Documentation

### Base URL
```
http://localhost:5000/api
```

### Authentication Endpoints

#### Register User
```
POST /auth/register
Content-Type: application/json

{
  "username": "user123",
  "email": "user@example.com",
  "password": "securepassword",
  "first_name": "John",
  "last_name": "Doe"
}
```

#### Login
```
POST /auth/login
Content-Type: application/json

{
  "username": "user123",
  "password": "securepassword"
}
```

#### Google Sign-In
```
POST /auth/google
Content-Type: application/json

{
  "google_id": "google_uid",
  "email": "user@gmail.com",
  "first_name": "John",
  "last_name": "Doe",
  "profile_picture": "https://..."
}
```

#### Check Email Availability
```
POST /auth/check-email
Content-Type: application/json

{
  "email": "user@example.com"
}
```

### User Management Endpoints

#### Get All Users (Admin only)
```
GET /users
Authorization: Bearer <jwt_token>
```

#### Get User Profile
```
GET /users/:id
Authorization: Bearer <jwt_token>
```

#### Update User Profile
```
PUT /users/:id
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "first_name": "John",
  "last_name": "Doe",
  "phone_number": "+1234567890",
  "profile_picture": "https://..."
}
```

#### Create Officer (Admin only)
```
POST /users/officers
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "username": "officer123",
  "email": "officer@bms.gov",
  "password": "securepassword",
  "first_name": "Juan",
  "last_name": "Santos",
  "badge_number": "BDG001",
  "department": "Police"
}
```

### Case Management Endpoints

#### Create Case
```
POST /cases
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "title": "Theft Report",
  "description": "Case description",
  "priority": "high",
  "incident_date": "2025-01-15T10:30:00Z",
  "incident_location": "Main Street"
}
```

#### Get All Cases
```
GET /cases?status=pending&priority=high&page=1&limit=10
Authorization: Bearer <jwt_token>
```

#### Get Case Details
```
GET /cases/:id
Authorization: Bearer <jwt_token>
```

#### Update Case
```
PUT /cases/:id
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "title": "Updated Title",
  "description": "Updated description"
}
```

#### Assign Case to Officer
```
POST /cases/:id/assign
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "officer_id": 5
}
```

#### Update Case Status
```
PUT /cases/:id/status
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "status": "in-progress"
}
```

### Blotter Report Endpoints

#### Create Blotter Report
```
POST /blotters
Authorization: Bearer <jwt_token>
Content-Type: application/json

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

#### Get All Blotters
```
GET /blotters?status=pending&page=1&limit=10
Authorization: Bearer <jwt_token>
```

#### Assign Blotter to Officer
```
POST /blotters/:id/assign
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "officer_id": 5
}
```

### Officer Workflow Endpoints

#### Assign Case to Officer
```
POST /officers/assign-case
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "officer_id": 5,
  "case_id": 10
}
```

#### Get Officer's Assigned Cases
```
GET /officers/:officerId/cases?status=in-progress&page=1&limit=10
Authorization: Bearer <jwt_token>
```

#### Get All Officers Workload (Admin)
```
GET /officers/workload
Authorization: Bearer <jwt_token>
```

#### Get Officer Availability (Admin)
```
GET /officers/availability
Authorization: Bearer <jwt_token>
```

#### Accept Case Assignment
```
PUT /officers/case/:caseId/accept
Authorization: Bearer <jwt_token>
```

#### Reject Case Assignment
```
PUT /officers/case/:caseId/reject
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "rejection_reason": "Too busy with current cases"
}
```

#### Complete Case
```
PUT /officers/case/:caseId/complete
Authorization: Bearer <jwt_token>
```

### Evidence Management Endpoints

#### Upload Evidence
```
POST /evidence
Authorization: Bearer <jwt_token>
Content-Type: multipart/form-data

file: <binary_file>
case_id: 10
description: "Evidence description"
```

#### Get Case Evidence
```
GET /evidence/case/:caseId
Authorization: Bearer <jwt_token>
```

#### Verify Evidence
```
POST /evidence/:id/verify
Authorization: Bearer <jwt_token>
```

### Notification Endpoints

#### Get User Notifications
```
GET /notifications/user/:userId?is_read=false&page=1&limit=20
Authorization: Bearer <jwt_token>
```

#### Mark Notification as Read
```
PUT /notifications/:id/read
Authorization: Bearer <jwt_token>
```

#### Mark All as Read
```
PUT /notifications/user/:userId/read-all
Authorization: Bearer <jwt_token>
```

### Dashboard Endpoints

#### Get System Statistics (Admin)
```
GET /dashboard/stats
Authorization: Bearer <jwt_token>
```

#### Get Officer Workload Stats (Admin)
```
GET /dashboard/officer-workload
Authorization: Bearer <jwt_token>
```

#### Get Case Status Statistics (Admin)
```
GET /dashboard/case-status
Authorization: Bearer <jwt_token>
```

#### Get Blotter Analytics (Admin)
```
GET /dashboard/blotter-analytics
Authorization: Bearer <jwt_token>
```

## 🔐 Default Credentials

After seeding, you can login with:

**Admin Account:**
- Username: `bms.admin`
- Password: `Admin@123`
- Email: `admin@bms.gov.ph`

**Sample Officers:**
- Username: `officer.santos` | Password: `Officer@123`
- Username: `officer.cruz` | Password: `Officer@123`
- Username: `officer.reyes` | Password: `Officer@123`

## 📊 Database Schema

### Core Tables
- `users` - User accounts with roles
- `cases` - Case management
- `blotter_reports` - Incident reports
- `case_evidence` - Evidence files
- `officer_assignments` - Case assignments
- `officer_performance` - Officer metrics
- `notifications` - User notifications
- `activity_logs` - System activity tracking

## 🚀 Deployment

### Deploy to Render

1. Push code to GitHub
2. Connect GitHub repo to Render
3. Set environment variables in Render dashboard
4. Deploy

### Environment Variables for Production
```
DATABASE_URL=postgresql://...
JWT_SECRET=<strong_random_key>
NODE_ENV=production
PORT=5000
CORS_ORIGIN=https://yourdomain.com
```

## 📝 API Response Format

All responses follow this format:

**Success Response:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "token": "jwt_token" // for auth endpoints
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Error description",
  "errors": [ ... ] // validation errors
}
```

## 🔒 Security Features

- ✅ JWT token-based authentication
- ✅ Password hashing with bcryptjs (10 salt rounds)
- ✅ Role-based access control
- ✅ CORS protection
- ✅ Helmet security headers
- ✅ Rate limiting on auth endpoints
- ✅ Input validation and sanitization
- ✅ SQL injection prevention (parameterized queries)

## 📞 Support

For issues or questions, please create an issue in the repository.

## 📄 License

ISC
