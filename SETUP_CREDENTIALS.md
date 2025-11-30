# 🔐 SETUP CREDENTIALS - Cloudinary, Firebase, FCM

## 1️⃣ CLOUDINARY SETUP (Profile Picture Upload)

### Step 1: Create Cloudinary Account
1. Go to https://cloudinary.com
2. Click "Sign Up"
3. Create account with email
4. Verify email

### Step 2: Get API Credentials
1. Go to Dashboard: https://cloudinary.com/console
2. Find "API Key" section
3. Copy these 3 values:
   - **Cloud Name** (e.g., "dh7xxxxxxx")
   - **API Key** (e.g., "123456789")
   - **API Secret** (e.g., "abcdefghijklmnop")

### Step 3: Add to Android App
In `CloudinaryManager.java`, replace:
```java
private static final String CLOUD_NAME = "YOUR_CLOUD_NAME";
private static final String API_KEY = "YOUR_API_KEY";
private static final String API_SECRET = "YOUR_API_SECRET";
```

With your actual values from Cloudinary dashboard.

### Step 4: Add to Backend
In `backend/.env`, add:
```
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

---

## 2️⃣ FIREBASE SETUP (Authentication + FCM)

### Step 1: Create Firebase Project
1. Go to https://console.firebase.google.com
2. Click "Create a project"
3. Enter project name: "BMS" or "Blotter Management System"
4. Accept terms and create

### Step 2: Enable Google Sign-In
1. Go to **Authentication** → **Sign-in method**
2. Click **Google**
3. Enable it
4. Set project support email
5. Save

### Step 3: Enable FCM (Push Notifications)
1. Go to **Cloud Messaging**
2. Copy **Server API Key** (you'll need this for backend)
3. Copy **Sender ID** (you'll need this for Android)

### Step 4: Get Android Credentials
1. Go to **Project Settings** (gear icon)
2. Click **Your apps** → **Android**
3. If no Android app, click "Add app"
4. Enter package name: `com.example.blottermanagementsystem`
5. Download `google-services.json`
6. Place in `app/` folder (already done)

### Step 5: Get Service Account Key (For Backend)
1. Go to **Project Settings** → **Service Accounts**
2. Click **Generate New Private Key**
3. Save as `firebase-service-account.json`
4. Keep this **SECRET** - never commit to GitHub!

---

## 3️⃣ ANDROID APP CONFIGURATION

### Step 1: Update build.gradle
Already done! Just verify:
```gradle
dependencies {
    implementation 'com.google.firebase:firebase-auth:22.3.0'
    implementation 'com.google.firebase:firebase-messaging:23.4.0'
    implementation 'com.google.android.gms:play-services-auth:20.7.0'
}
```

### Step 2: Update AndroidManifest.xml
Already done! Verify these permissions exist:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
```

### Step 3: Add google-services.json
1. Download from Firebase Console
2. Place in `app/` folder
3. Already configured in build.gradle

---

## 4️⃣ BACKEND CONFIGURATION

### Step 1: Create .env File
```bash
cd backend
cp .env.example .env
```

### Step 2: Add Credentials
```
# Cloudinary
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# Firebase
FIREBASE_PROJECT_ID=your_project_id
FIREBASE_PRIVATE_KEY=your_private_key
FIREBASE_CLIENT_EMAIL=your_client_email
FCM_SERVER_API_KEY=your_fcm_server_key

# Database
DATABASE_URL=postgresql://user:password@host:port/database

# JWT
JWT_SECRET=your_super_secret_key_min_32_chars
JWT_EXPIRY=7d

# Server
PORT=5000
NODE_ENV=production
CORS_ORIGIN=http://localhost:3000
```

### Step 3: Install Dependencies
```bash
npm install
```

### Step 4: Run Migrations
```bash
npm run migrate
```

### Step 5: Seed Data
```bash
npm run seed
```

---

## 📋 CHECKLIST

### Cloudinary ✅
- [ ] Create Cloudinary account
- [ ] Get Cloud Name, API Key, API Secret
- [ ] Update CloudinaryManager.java
- [ ] Update backend .env

### Firebase ✅
- [ ] Create Firebase project
- [ ] Enable Google Sign-In
- [ ] Enable FCM
- [ ] Download google-services.json
- [ ] Get Service Account Key
- [ ] Copy Server API Key
- [ ] Copy Sender ID

### Android App ✅
- [ ] Verify build.gradle dependencies
- [ ] Verify AndroidManifest.xml permissions
- [ ] Verify google-services.json in app/ folder
- [ ] Update CloudinaryManager.java

### Backend ✅
- [ ] Create .env file
- [ ] Add all credentials
- [ ] Install dependencies
- [ ] Run migrations
- [ ] Seed data

---

## 🚀 QUICK START

### 1. Cloudinary (5 minutes)
```
1. Sign up at https://cloudinary.com
2. Copy Cloud Name, API Key, API Secret
3. Update CloudinaryManager.java
4. Update backend .env
```

### 2. Firebase (10 minutes)
```
1. Create project at https://console.firebase.google.com
2. Enable Google Sign-In
3. Enable FCM
4. Download google-services.json
5. Get Service Account Key
```

### 3. Backend Setup (5 minutes)
```
1. cd backend
2. cp .env.example .env
3. Add all credentials to .env
4. npm install
5. npm run migrate
6. npm run seed
```

### 4. Deploy (10 minutes)
```
1. Push to GitHub
2. Deploy to Render/Heroku
3. Set environment variables
4. Test endpoints
```

---

## 🔗 USEFUL LINKS

- **Cloudinary Dashboard**: https://cloudinary.com/console
- **Firebase Console**: https://console.firebase.google.com
- **Render Deploy**: https://render.com
- **Heroku Deploy**: https://heroku.com
- **Neon Database**: https://neon.tech

---

## ⚠️ SECURITY NOTES

- **Never commit .env file** to GitHub
- **Never commit google-services.json** with secrets
- **Never commit firebase-service-account.json** to GitHub
- Use environment variables on deployment platform
- Rotate API keys regularly
- Keep API secrets private

---

## 📞 NEED HELP?

If you get stuck:
1. Check backend logs: `npm run dev`
2. Check Android logs: Android Studio Logcat
3. Check Firebase Console for errors
4. Check Cloudinary Dashboard for upload issues
5. Verify all credentials are correct

---

**Status**: Ready to setup! Follow the steps above.
**Time Estimate**: 30 minutes total
**Difficulty**: Easy
