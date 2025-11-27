# Blotter Management System - Backend Deployment Guide

## 🚀 Production Deployment to Cloudbase

### Prerequisites
- Bun runtime installed
- Cloudbase account and CLI
- PostgreSQL database (Cloudbase or external)
- Firebase project (for FCM notifications)

### Step 1: Environment Setup

1. **Copy environment variables:**
```bash
cp .env.example .env.production
```

2. **Update `.env.production` with your Cloudbase credentials:**
```env
DATABASE_URL=your-cloudbase-database-url
JWT_SECRET=your-production-jwt-secret-key
FIREBASE_PROJECT_ID=your-firebase-project-id
FIREBASE_PRIVATE_KEY=your-firebase-private-key
FIREBASE_CLIENT_EMAIL=your-firebase-client-email
CLOUDBASE_ENV_ID=your-cloudbase-environment-id
CLOUDBASE_API_KEY=your-cloudbase-api-key
ALLOWED_ORIGINS=https://yourdomain.com,https://app.yourdomain.com
NODE_ENV=production
```

### Step 2: Build & Test Locally

```bash
# Install dependencies
bun install

# Run database migrations
bun run db:push

# Seed database (optional)
bun run db:seed

# Start development server
bun run dev

# Run tests
bun run test
```

### Step 3: Deploy to Cloudbase

#### Option A: Using Wrangler (Cloudflare Workers)
```bash
# Login to Cloudbase
bunx wrangler login

# Deploy to production
bun run deploy

# Or deploy to staging
bunx wrangler deploy --env staging
```

#### Option B: Using Docker (Cloudbase Container)
```bash
# Build Docker image
docker build -t blotter-api:latest .

# Push to Cloudbase registry
docker tag blotter-api:latest your-registry/blotter-api:latest
docker push your-registry/blotter-api:latest

# Deploy via Cloudbase CLI
cloudbase deploy --image your-registry/blotter-api:latest
```

### Step 4: Verify Deployment

1. **Check API health:**
```bash
curl https://your-api-domain.com/health
```

2. **View API documentation:**
```
https://your-api-domain.com/swagger
```

3. **Test authentication:**
```bash
curl -X POST https://your-api-domain.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

### Step 5: Update Android App

Update the API base URL in `ApiClient.java`:

```java
public class ApiClient {
    private static final String BASE_URL = "https://your-api-domain.com/api/";
}
```

## 📊 API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration

### Reports
- `GET /api/reports` - Get all reports
- `GET /api/reports/:id` - Get single report
- `POST /api/reports` - Create new report
- `PUT /api/reports/:id` - Update report
- `DELETE /api/reports/:id` - Delete report

### Officers
- `GET /api/officers` - Get all officers
- `POST /api/officers/assign` - Assign officer to report

### Statistics
- `GET /api/dashboard` - Get dashboard statistics

### SMS Notifications
- `POST /api/notifications/sms` - Send SMS notification

## 🔒 Security Checklist

- [ ] JWT_SECRET changed to strong random string
- [ ] Database credentials secured
- [ ] CORS origins restricted to your domain
- [ ] Firebase credentials stored securely
- [ ] SMS API keys stored securely
- [ ] HTTPS enabled
- [ ] Rate limiting configured
- [ ] Input validation enabled
- [ ] Error logging configured
- [ ] Database backups scheduled

## 📈 Monitoring

### Logs
```bash
# View logs in Cloudbase dashboard
cloudbase logs --follow
```

### Metrics
- Monitor API response times
- Track error rates
- Monitor database connections
- Track JWT token usage

## 🔄 Continuous Deployment

### GitHub Actions (Optional)
Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to Cloudbase
on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: oven-sh/setup-bun@v1
      - run: bun install
      - run: bun run build
      - run: bunx wrangler deploy
        env:
          CLOUDFLARE_API_TOKEN: ${{ secrets.CLOUDFLARE_API_TOKEN }}
```

## 🆘 Troubleshooting

### Database Connection Issues
```bash
# Test database connection
bun run db:studio
```

### JWT Token Issues
- Verify JWT_SECRET is set correctly
- Check token expiration time
- Verify bearer token format: `Authorization: Bearer <token>`

### CORS Issues
- Verify ALLOWED_ORIGINS includes your app domain
- Check browser console for CORS errors
- Ensure credentials are included in requests

## 📞 Support

For issues or questions:
1. Check logs: `cloudbase logs`
2. Review API documentation: `/swagger`
3. Check database status
4. Verify environment variables

---

**Last Updated:** 2025-11-27
**Version:** 1.0.0
**Status:** Production Ready ✅
