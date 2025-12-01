# Render Deployment with Bun - Setup Guide

## 🚀 Configure Render for Bun Runtime

### Step 1: Go to Render Dashboard
- https://render.com/dashboard
- Select your BMS service

### Step 2: Go to Settings
- Click **"Settings"** tab
- Scroll to **"Build & Deploy"** section

### Step 3: Set Build Command
**Replace the current build command with:**
```
bun install
```

### Step 4: Set Start Command
**Replace the current start command with:**
```
bun run src/index.ts
```

### Step 5: Environment Variables (if not set)
Make sure these are configured:
```
DATABASE_URL=postgresql://neondb_owner:npg_85KqyURZpkTC@ep-holy-voice-adnjcvie-pooler.c-2.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require
JWT_SECRET=bms_jwt_secret_2025_production_key
NODE_ENV=production
PORT=3000
GMAIL_USER=official.bms.2025@gmail.com
GMAIL_PASSWORD=bvg vyes knki yvgi
```

### Step 6: Save & Redeploy
1. Click **"Save"**
2. Click **"Manual Deploy"** or **"Redeploy latest commit"**
3. Wait 5-10 minutes

### Step 7: Verify Bun is Running
Check logs for:
```
==> Running 'bun run src/index.ts'
🚀 BMS Backend running on port 3000
```

---

## ✅ Expected Output

**Good (Bun):**
```
==> Running 'bun run src/index.ts'
🚀 BMS Backend running on port 3000
Environment: production
Database: Connected
```

**Bad (npm - old):**
```
==> Running 'npm start'
> bms-backend@1.0.0 start
> node src/index.js
```

---

## 🎯 Quick Checklist

- [ ] Go to Render Settings
- [ ] Set Build Command: `bun install`
- [ ] Set Start Command: `bun run src/index.ts`
- [ ] Verify environment variables
- [ ] Click Save
- [ ] Click Redeploy
- [ ] Wait 5-10 minutes
- [ ] Check logs for "bun run"
- [ ] Test API endpoint

---

## 📝 Notes

- Bun is **3x faster** than Node.js
- Elysia is **18x faster** than Express
- Combined = **54x faster** potential! 🚀
- First deploy may take longer (building Docker image)
- Subsequent deploys will be faster

---

**Status**: Ready to configure
**Time to deploy**: 5-10 minutes
**Expected result**: Pure Bun runtime ✅
