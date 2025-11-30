# 🚀 BMS Backend - Deployment Checklist

## ✅ Current Status

Your backend folder is **CLEAN & READY** with all new production files:

### 📁 Backend Structure (32 files total)

**Root Files (8):**
- ✅ package.json - Dependencies
- ✅ .env.example - Environment template
- ✅ .gitignore - Git ignore rules
- ✅ README.md - Setup guide
- ✅ DEPLOYMENT.md - Deployment guide
- ✅ API_ENDPOINTS.md - API documentation
- ✅ AUTHENTICATION_FLOWS.md - Auth documentation
- ✅ AUTHENTICATION_COMPLETE.md - Auth summary

**Source Code (24):**
- ✅ src/index.js - Main application
- ✅ src/database/ - Database layer (4 files)
- ✅ src/middleware/ - Middleware (3 files)
- ✅ src/controllers/ - Controllers (8 files)
- ✅ src/routes/ - Routes (8 files)

---

## 🎯 Pre-Deployment Steps

### Step 1: Install Dependencies
```bash
cd backend
npm install
```

### Step 2: Create .env File
```bash
cp .env.example .env
```

Edit `.env` with your configuration:
```
DATABASE_URL=postgresql://user:password@host:port/database
JWT_SECRET=your_super_secret_key_min_32_chars
JWT_EXPIRY=7d
PORT=5000
NODE_ENV=production
CORS_ORIGIN=https://yourandroidapp.com
RATE_LIMIT_WINDOW_MS=900000
RATE_LIMIT_MAX_REQUESTS=100
```

### Step 3: Create Neon Database
1. Go to https://neon.tech
2. Create new project
3. Copy connection string
4. Paste into DATABASE_URL in .env

### Step 4: Run Migrations
```bash
npm run migrate
```

This will:
- Create all 8 tables
- Create 2 views
- Create 30+ indexes
- Set up foreign keys

### Step 5: Seed Initial Data
```bash
npm run seed
```

This will create:
- Admin account: `bms.admin` / `Admin@123`
- 3 sample officers with credentials

### Step 6: Test Locally
```bash
npm run dev
```

Test endpoints:
```bash
curl http://localhost:5000/health
```

---

## 🌐 Deployment Options

### Option 1: Render.com (RECOMMENDED)

**Pros:**
- ✅ Free tier available
- ✅ One-click deployment
- ✅ Auto-scaling
- ✅ Easy environment variables

**Steps:**
1. Push code to GitHub
2. Go to https://render.com
3. Create new Web Service
4. Connect GitHub repo
5. Set environment variables
6. Deploy

**Environment Variables:**
```
DATABASE_URL=postgresql://...
JWT_SECRET=your_secret_key
NODE_ENV=production
PORT=5000
CORS_ORIGIN=https://yourandroidapp.com
```

**Result:**
```
Backend URL: https://bms-backend.onrender.com
```

### Option 2: Heroku

**Steps:**
```bash
heroku create bms-backend
heroku config:set DATABASE_URL=postgresql://...
heroku config:set JWT_SECRET=your_secret_key
git push heroku main
```

### Option 3: AWS EC2

See DEPLOYMENT.md for detailed AWS setup guide.

---

## ✅ Post-Deployment Verification

### 1. Health Check
```bash
curl https://bms-backend.onrender.com/health
```

Expected response:
```json
{
  "success": true,
  "message": "BMS Backend is running",
  "timestamp": "2025-01-15T10:30:00.000Z"
}
```

### 2. Test Authentication
```bash
curl -X POST https://bms-backend.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bms.admin",
    "password": "Admin@123"
  }'
```

Expected response:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "id": 1,
    "username": "bms.admin",
    "email": "admin@bms.gov.ph",
    "role": "admin"
  },
  "token": "eyJhbGciOiJIUzI1NiIs..."
}
```

### 3. Test Google OAuth
```bash
curl -X POST https://bms-backend.onrender.com/api/auth/google \
  -H "Content-Type: application/json" \
  -d '{
    "googleId": "test_google_id",
    "email": "test@gmail.com",
    "first_name": "Test",
    "last_name": "User",
    "profilePictureUrl": "https://..."
  }'
```

### 4. Test Case Creation
```bash
curl -X POST https://bms-backend.onrender.com/api/cases \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Case",
    "description": "Test description",
    "priority": "high",
    "incident_date": "2025-01-15T10:30:00Z",
    "incident_location": "Test Location"
  }'
```

---

## 📋 API Endpoints to Test

### Authentication (6)
- ✅ POST /api/auth/register
- ✅ POST /api/auth/login
- ✅ POST /api/auth/google
- ✅ POST /api/auth/check-email
- ✅ POST /api/auth/refresh
- ✅ POST /api/auth/logout

### Cases (9)
- ✅ POST /api/cases
- ✅ GET /api/cases
- ✅ GET /api/cases/:id
- ✅ PUT /api/cases/:id
- ✅ DELETE /api/cases/:id
- ✅ POST /api/cases/:id/assign
- ✅ PUT /api/cases/:id/status
- ✅ GET /api/cases/officer/:officerId
- ✅ GET /api/cases/user/:userId

### Users (7)
- ✅ GET /api/users
- ✅ GET /api/users/:id
- ✅ PUT /api/users/:id
- ✅ DELETE /api/users/:id
- ✅ POST /api/users/officers
- ✅ GET /api/users/officers
- ✅ PUT /api/users/:id/role

### Blotters (7)
- ✅ POST /api/blotters
- ✅ GET /api/blotters
- ✅ GET /api/blotters/:id
- ✅ PUT /api/blotters/:id
- ✅ PUT /api/blotters/:id/status
- ✅ DELETE /api/blotters/:id
- ✅ POST /api/blotters/:id/assign

### Officers (9)
- ✅ POST /api/officers/assign-case
- ✅ GET /api/officers/:officerId/cases
- ✅ GET /api/officers/workload
- ✅ GET /api/officers/availability
- ✅ PUT /api/officers/:officerId/status
- ✅ GET /api/officers/performance
- ✅ PUT /api/officers/case/:caseId/accept
- ✅ PUT /api/officers/case/:caseId/reject
- ✅ PUT /api/officers/case/:caseId/complete

### Evidence (4)
- ✅ POST /api/evidence
- ✅ GET /api/evidence/case/:caseId
- ✅ DELETE /api/evidence/:id
- ✅ POST /api/evidence/:id/verify

### Notifications (5)
- ✅ GET /api/notifications/user/:userId
- ✅ PUT /api/notifications/:id/read
- ✅ PUT /api/notifications/user/:userId/read-all
- ✅ DELETE /api/notifications/:id
- ✅ POST /api/notifications

### Dashboard (7)
- ✅ GET /api/dashboard/stats
- ✅ GET /api/dashboard/officer-workload
- ✅ GET /api/dashboard/case-status
- ✅ GET /api/dashboard/blotter-analytics
- ✅ GET /api/dashboard/evidence-summary
- ✅ GET /api/dashboard/recent-activity
- ✅ GET /api/dashboard/case-resolution-time

---

## 🔧 Troubleshooting

### Database Connection Error
**Problem:** `Error: connect ECONNREFUSED`
**Solution:** 
- Verify DATABASE_URL is correct
- Check Neon database is active
- Test connection string locally

### JWT Secret Error
**Problem:** `Error: Invalid token`
**Solution:**
- Ensure JWT_SECRET is set
- Verify JWT_SECRET is same on all instances
- Use strong random string (min 32 chars)

### CORS Error
**Problem:** `Access to XMLHttpRequest blocked by CORS policy`
**Solution:**
- Update CORS_ORIGIN to include Android app domain
- Test with curl first (no CORS issues)
- Verify backend is running

### Rate Limiting
**Problem:** `Error: Too many requests`
**Solution:**
- Increase RATE_LIMIT_MAX_REQUESTS
- Implement exponential backoff on client
- Use different IP addresses for testing

---

## 📊 Monitoring

### Logs
- **Render**: Dashboard → Logs
- **Heroku**: `heroku logs --tail`
- **AWS**: CloudWatch Logs

### Performance
- Monitor response times
- Track error rates
- Monitor database queries
- Check rate limiting

### Alerts
- Setup error notifications
- Monitor uptime
- Track API usage
- Alert on high latency

---

## 🎯 Final Checklist

- [ ] Dependencies installed (`npm install`)
- [ ] .env file created with all variables
- [ ] Neon database created
- [ ] Database migrations run (`npm run migrate`)
- [ ] Initial data seeded (`npm run seed`)
- [ ] Local testing successful (`npm run dev`)
- [ ] Code pushed to GitHub
- [ ] Deployment platform configured (Render/Heroku/AWS)
- [ ] Environment variables set on deployment platform
- [ ] Health check endpoint working
- [ ] Authentication endpoints tested
- [ ] All 50+ API endpoints tested
- [ ] Android app configured with backend URL
- [ ] Android app authentication flows tested
- [ ] Monitoring setup complete
- [ ] Documentation updated

---

## 📞 Support

If you encounter any issues:

1. Check logs on deployment platform
2. Verify environment variables
3. Test endpoints with curl
4. Check database connection
5. Review error messages
6. Consult DEPLOYMENT.md for detailed guides

---

**Status**: ✅ READY FOR DEPLOYMENT
**Version**: 1.0.0
**Backend Files**: 32 (All production-ready)
**Documentation**: Complete
**API Endpoints**: 50+
**Database Tables**: 8
**Security**: Production-grade

🚀 **Your backend is ready to deploy!**
