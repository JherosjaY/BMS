# BMS Backend - Deployment Guide

Complete guide for deploying the Blotter Management System backend to production.

## 🎯 Deployment Options

### Option 1: Render.com (Recommended)

#### Step 1: Prepare Repository
```bash
# Ensure all files are committed
git add .
git commit -m "BMS Backend - Production Ready"
git push origin main
```

#### Step 2: Create Render Service
1. Go to https://render.com
2. Click "New +" → "Web Service"
3. Connect your GitHub repository
4. Configure:
   - **Name**: bms-backend
   - **Environment**: Node
   - **Build Command**: `npm install`
   - **Start Command**: `npm start`
   - **Instance Type**: Free or Starter

#### Step 3: Set Environment Variables
In Render Dashboard → Environment:
```
DATABASE_URL=postgresql://user:password@host:port/database
JWT_SECRET=your_production_secret_key_min_32_chars
JWT_EXPIRY=7d
NODE_ENV=production
PORT=5000
CORS_ORIGIN=https://yourandroidapp.com,https://youradmin.com
RATE_LIMIT_WINDOW_MS=900000
RATE_LIMIT_MAX_REQUESTS=100
```

#### Step 4: Deploy
- Click "Deploy"
- Wait for build to complete (2-3 minutes)
- Your backend will be live at: `https://bms-backend.onrender.com`

### Option 2: Heroku

#### Step 1: Install Heroku CLI
```bash
npm install -g heroku
heroku login
```

#### Step 2: Create Heroku App
```bash
heroku create bms-backend
```

#### Step 3: Set Environment Variables
```bash
heroku config:set DATABASE_URL=postgresql://...
heroku config:set JWT_SECRET=your_production_secret_key
heroku config:set NODE_ENV=production
```

#### Step 4: Deploy
```bash
git push heroku main
```

### Option 3: AWS EC2

#### Step 1: Launch EC2 Instance
- AMI: Ubuntu 20.04 LTS
- Instance Type: t2.micro (free tier)
- Security Group: Allow ports 22, 80, 443

#### Step 2: Connect and Setup
```bash
# SSH into instance
ssh -i your-key.pem ubuntu@your-instance-ip

# Install Node.js
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# Install PostgreSQL client
sudo apt-get install -y postgresql-client

# Clone repository
git clone https://github.com/yourusername/bms-backend.git
cd bms-backend

# Install dependencies
npm install

# Create .env file
nano .env
# Add your environment variables
```

#### Step 3: Setup PM2 for Process Management
```bash
npm install -g pm2
pm2 start src/index.js --name "bms-backend"
pm2 startup
pm2 save
```

#### Step 4: Setup Nginx Reverse Proxy
```bash
sudo apt-get install -y nginx

# Create Nginx config
sudo nano /etc/nginx/sites-available/bms-backend
```

Add:
```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:5000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }
}
```

Enable and restart:
```bash
sudo ln -s /etc/nginx/sites-available/bms-backend /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

## 🔐 Security Checklist

- [ ] Change `JWT_SECRET` to a strong random string (min 32 chars)
- [ ] Use HTTPS with SSL certificate (Let's Encrypt recommended)
- [ ] Set `NODE_ENV=production`
- [ ] Configure CORS to only allow your Android app domain
- [ ] Enable rate limiting
- [ ] Set up database backups
- [ ] Enable database encryption
- [ ] Use environment variables for all secrets
- [ ] Enable HTTPS only
- [ ] Set secure cookie flags
- [ ] Enable HSTS headers

## 📊 Database Setup

### Neon PostgreSQL (Recommended)

1. Go to https://neon.tech
2. Create account and project
3. Copy connection string
4. Set as `DATABASE_URL` in environment

### Run Migrations
```bash
npm run migrate
```

### Seed Initial Data
```bash
npm run seed
```

## 🚀 Post-Deployment

### Verify Deployment
```bash
curl https://your-backend-url/health
```

Expected response:
```json
{
  "success": true,
  "message": "BMS Backend is running",
  "timestamp": "2025-01-15T10:30:00.000Z"
}
```

### Test Authentication
```bash
curl -X POST https://your-backend-url/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bms.admin",
    "password": "Admin@123"
  }'
```

### Monitor Logs
- **Render**: Dashboard → Logs
- **Heroku**: `heroku logs --tail`
- **AWS**: `/var/log/pm2/` or CloudWatch

## 📈 Performance Optimization

### Database Optimization
```sql
-- Create indexes for frequently queried columns
CREATE INDEX idx_cases_status ON cases(status);
CREATE INDEX idx_cases_assigned_officer ON cases(assigned_officer_id);
CREATE INDEX idx_blotter_status ON blotter_reports(status);
CREATE INDEX idx_notifications_user ON notifications(user_id);
```

### Caching Strategy
- Implement Redis for session caching
- Cache dashboard statistics (5-minute TTL)
- Cache officer workload data (10-minute TTL)

### Load Balancing
- Use Render's auto-scaling
- Or setup multiple instances with load balancer

## 🔄 Continuous Deployment

### GitHub Actions Workflow
Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to Render

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Deploy to Render
        run: |
          curl -X POST https://api.render.com/deploy/srv-${{ secrets.RENDER_SERVICE_ID }}?key=${{ secrets.RENDER_API_KEY }}
```

## 🆘 Troubleshooting

### Database Connection Error
```
Error: connect ECONNREFUSED
```
**Solution**: Check `DATABASE_URL` environment variable

### JWT Token Error
```
Error: Invalid or expired token
```
**Solution**: Ensure `JWT_SECRET` is set correctly

### CORS Error
```
Access to XMLHttpRequest blocked by CORS policy
```
**Solution**: Update `CORS_ORIGIN` to include your Android app domain

### Rate Limiting Error
```
Error: Too many requests
```
**Solution**: Increase `RATE_LIMIT_MAX_REQUESTS` or implement exponential backoff

## 📞 Support

For deployment issues:
1. Check logs in your deployment platform
2. Verify environment variables
3. Test database connection
4. Check firewall/security group settings

## 📝 Deployment Checklist

- [ ] Database configured and migrated
- [ ] Environment variables set
- [ ] SSL certificate configured
- [ ] CORS properly configured
- [ ] Rate limiting enabled
- [ ] Logging configured
- [ ] Backups scheduled
- [ ] Monitoring setup
- [ ] Health check endpoint verified
- [ ] Test all API endpoints
- [ ] Document API endpoints for mobile team
- [ ] Setup error tracking (Sentry recommended)

---

**Deployment Status**: ✅ READY FOR PRODUCTION
**Last Updated**: 2025-01-15
**Version**: 1.0.0
