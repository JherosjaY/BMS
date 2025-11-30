# 🔗 BMS Backend - Android Integration Guide

Complete guide for integrating the BMS Backend with your Android application.

---

## 🎯 Quick Start for Android Developers

### Backend URL
```
Production: https://bms-backend.onrender.com
Development: http://localhost:5000
```

### Health Check
```bash
GET https://bms-backend.onrender.com/health
```

---

## 🔐 Authentication Flow

### 1. Google Sign-In Flow

**Step 1: User clicks "Continue with Google"**
```
Android App → Firebase Authentication → Google OAuth
```

**Step 2: Send to Backend**
```java
POST /api/auth/google
Content-Type: application/json

{
  "google_id": "firebase_uid",
  "email": "user@gmail.com",
  "first_name": "John",
  "last_name": "Doe",
  "profile_picture": "https://lh3.googleusercontent.com/..."
}
```

**Step 3: Receive JWT Token**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@gmail.com",
    "first_name": "John",
    "last_name": "Doe",
    "role": "user",
    "profile_picture": "https://..."
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Step 4: Store Token**
```java
// Save in SharedPreferences
PreferencesManager.saveToken(token);
PreferencesManager.saveUserRole(role);
```

**Step 5: Navigate Based on Role**
```java
if (role.equals("admin")) {
    startActivity(new Intent(this, AdminDashboardActivity.class));
} else if (role.equals("officer")) {
    startActivity(new Intent(this, OfficerDashboardActivity.class));
} else {
    startActivity(new Intent(this, UserDashboardActivity.class));
}
```

### 2. Email/Password Registration

**Step 1: User enters credentials**
```
Email, Password, First Name, Last Name
```

**Step 2: Send to Backend**
```java
POST /api/auth/register
Content-Type: application/json

{
  "username": "user123",
  "email": "user@example.com",
  "password": "SecurePass123",
  "first_name": "John",
  "last_name": "Doe"
}
```

**Step 3: Receive Response**
```json
{
  "success": true,
  "message": "User registered successfully",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Step 4: Auto-Login or Redirect**
```java
// Option 1: Auto-login
PreferencesManager.saveToken(token);
startActivity(new Intent(this, UserDashboardActivity.class));

// Option 2: Redirect to login
startActivity(new Intent(this, LoginActivity.class));
```

### 3. Email/Password Login

**Step 1: User enters credentials**
```
Username/Email, Password
```

**Step 2: Send to Backend**
```java
POST /api/auth/login
Content-Type: application/json

{
  "username": "user123",
  "password": "SecurePass123"
}
```

**Step 3: Receive Response**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "role": "officer",
    "first_name": "Juan",
    "last_name": "Santos"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Step 4: Store & Navigate**
```java
PreferencesManager.saveToken(token);
PreferencesManager.saveUserRole(role);
// Navigate to appropriate dashboard
```

---

## 📡 API Request Format

### Headers
```java
// All authenticated requests must include:
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

### Example Request
```java
// Using Retrofit or OkHttp
Request request = new Request.Builder()
    .url("https://bms-backend.onrender.com/api/cases")
    .addHeader("Authorization", "Bearer " + token)
    .addHeader("Content-Type", "application/json")
    .get()
    .build();
```

### Response Format
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "pagination": { ... }
}
```

---

## 🔄 Common API Calls

### Create Case
```java
POST /api/cases
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Theft Report",
  "description": "Stolen laptop",
  "priority": "high",
  "incident_date": "2025-01-15T10:30:00Z",
  "incident_location": "Main Street"
}
```

### Get All Cases
```java
GET /api/cases?status=pending&priority=high&page=1&limit=10
Authorization: Bearer <token>
```

### Get Case Details
```java
GET /api/cases/1
Authorization: Bearer <token>
```

### Update Case Status
```java
PUT /api/cases/1/status
Authorization: Bearer <token>
Content-Type: application/json

{
  "status": "in-progress"
}
```

### Assign Case to Officer
```java
POST /api/cases/1/assign
Authorization: Bearer <token>
Content-Type: application/json

{
  "officer_id": 5
}
```

### Upload Evidence
```java
POST /api/evidence
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: <binary_file>
case_id: 1
description: "Crime scene photo"
```

### Get Notifications
```java
GET /api/notifications/user/1?is_read=false&page=1&limit=20
Authorization: Bearer <token>
```

### Mark Notification as Read
```java
PUT /api/notifications/1/read
Authorization: Bearer <token>
```

---

## 🛠️ Implementation Examples

### 1. Setup API Client

```java
// ApiClient.java
public class ApiClient {
    private static final String BASE_URL = "https://bms-backend.onrender.com/api/";
    private static ApiService apiService;

    public static ApiService getApiService() {
        if (apiService == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor())
                .build();

            Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}
```

### 2. Auth Interceptor

```java
// AuthInterceptor.java
public class AuthInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        
        // Get token from preferences
        String token = PreferencesManager.getToken();
        
        if (token != null) {
            Request newRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer " + token)
                .build();
            return chain.proceed(newRequest);
        }
        
        return chain.proceed(originalRequest);
    }
}
```

### 3. API Service Interface

```java
// ApiService.java
public interface ApiService {
    
    // Authentication
    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);
    
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);
    
    @POST("auth/google")
    Call<AuthResponse> googleSignIn(@Body GoogleSignInRequest request);
    
    // Cases
    @POST("cases")
    Call<ApiResponse<Case>> createCase(@Body CreateCaseRequest request);
    
    @GET("cases")
    Call<ApiResponse<List<Case>>> getAllCases(
        @Query("status") String status,
        @Query("priority") String priority,
        @Query("page") int page,
        @Query("limit") int limit
    );
    
    @GET("cases/{id}")
    Call<ApiResponse<Case>> getCaseById(@Path("id") int id);
    
    @PUT("cases/{id}/status")
    Call<ApiResponse<Case>> updateCaseStatus(
        @Path("id") int id,
        @Body UpdateStatusRequest request
    );
    
    // Notifications
    @GET("notifications/user/{userId}")
    Call<ApiResponse<List<Notification>>> getNotifications(
        @Path("userId") int userId,
        @Query("is_read") boolean isRead,
        @Query("page") int page,
        @Query("limit") int limit
    );
    
    @PUT("notifications/{id}/read")
    Call<ApiResponse<Notification>> markAsRead(@Path("id") int id);
    
    // Evidence
    @Multipart
    @POST("evidence")
    Call<ApiResponse<Evidence>> uploadEvidence(
        @Part MultipartBody.Part file,
        @Part("case_id") RequestBody caseId,
        @Part("description") RequestBody description
    );
    
    @GET("evidence/case/{caseId}")
    Call<ApiResponse<List<Evidence>>> getCaseEvidence(@Path("caseId") int caseId);
}
```

### 4. Handle Responses

```java
// LoginActivity.java
private void login(String username, String password) {
    LoginRequest request = new LoginRequest(username, password);
    
    ApiClient.getApiService().login(request).enqueue(new Callback<AuthResponse>() {
        @Override
        public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                AuthResponse authResponse = response.body();
                
                if (authResponse.isSuccess()) {
                    // Save token
                    PreferencesManager.saveToken(authResponse.getToken());
                    PreferencesManager.saveUserRole(authResponse.getData().getRole());
                    
                    // Navigate
                    navigateToDashboard(authResponse.getData().getRole());
                    
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, authResponse.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
            }
        }
        
        @Override
        public void onFailure(Call<AuthResponse> call, Throwable t) {
            Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    });
}
```

### 5. Error Handling

```java
// Handle 401 Unauthorized (Token expired)
if (response.code() == 401) {
    // Clear token
    PreferencesManager.clearToken();
    
    // Redirect to login
    startActivity(new Intent(this, LoginActivity.class));
    finish();
}

// Handle 403 Forbidden (Insufficient permissions)
if (response.code() == 403) {
    Toast.makeText(this, "You don't have permission to perform this action", Toast.LENGTH_SHORT).show();
}

// Handle 404 Not Found
if (response.code() == 404) {
    Toast.makeText(this, "Resource not found", Toast.LENGTH_SHORT).show();
}

// Handle 500 Server Error
if (response.code() == 500) {
    Toast.makeText(this, "Server error. Please try again later", Toast.LENGTH_SHORT).show();
}
```

---

## 🔍 Testing API Endpoints

### Using cURL

**Test Login**
```bash
curl -X POST https://bms-backend.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bms.admin",
    "password": "Admin@123"
  }'
```

**Test Get Cases**
```bash
curl -X GET "https://bms-backend.onrender.com/api/cases?page=1&limit=10" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Test Create Case**
```bash
curl -X POST https://bms-backend.onrender.com/api/cases \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Case",
    "description": "Test description",
    "priority": "high",
    "incident_date": "2025-01-15T10:30:00Z",
    "incident_location": "Test Location"
  }'
```

### Using Postman

1. Import API collection
2. Set environment variables:
   - `base_url`: https://bms-backend.onrender.com
   - `token`: Your JWT token
3. Test endpoints

---

## 📱 Role-Based Navigation

### Admin Dashboard
```java
// Access all admin endpoints
GET /api/dashboard/stats
GET /api/users
POST /api/users/officers
GET /api/officers/workload
```

### Officer Dashboard
```java
// Access officer-specific endpoints
GET /api/officers/:officerId/cases
PUT /api/officers/case/:caseId/accept
PUT /api/officers/case/:caseId/reject
PUT /api/officers/case/:caseId/complete
```

### User Dashboard
```java
// Access user-specific endpoints
POST /api/cases
GET /api/cases/user/:userId
GET /api/notifications/user/:userId
POST /api/evidence
```

---

## ⚠️ Error Handling

### Common Errors

**401 Unauthorized**
```json
{
  "success": false,
  "message": "No token provided"
}
```
**Action**: Redirect to login

**400 Bad Request**
```json
{
  "success": false,
  "message": "Validation error",
  "errors": [
    {
      "field": "email",
      "message": "Invalid email address"
    }
  ]
}
```
**Action**: Show validation errors to user

**403 Forbidden**
```json
{
  "success": false,
  "message": "Forbidden: Insufficient permissions"
}
```
**Action**: Show permission denied message

**404 Not Found**
```json
{
  "success": false,
  "message": "Case not found"
}
```
**Action**: Show not found message

**500 Server Error**
```json
{
  "success": false,
  "message": "Internal server error"
}
```
**Action**: Show retry option

---

## 🔄 Token Management

### Store Token
```java
PreferencesManager.saveToken(token);
```

### Retrieve Token
```java
String token = PreferencesManager.getToken();
```

### Clear Token (Logout)
```java
PreferencesManager.clearToken();
startActivity(new Intent(this, LoginActivity.class));
finish();
```

### Refresh Token
```java
POST /api/auth/refresh
Content-Type: application/json

{
  "token": "expired_token"
}
```

---

## 📊 Pagination

### Request with Pagination
```java
GET /api/cases?page=1&limit=10
```

### Response with Pagination
```json
{
  "success": true,
  "data": [ ... ],
  "pagination": {
    "total": 50,
    "page": 1,
    "limit": 10,
    "pages": 5
  }
}
```

### Implementation
```java
// Load more cases
int nextPage = currentPage + 1;
ApiClient.getApiService().getAllCases(null, null, nextPage, 10)
    .enqueue(new Callback<ApiResponse<List<Case>>>() {
        @Override
        public void onResponse(Call<ApiResponse<List<Case>>> call, 
                             Response<ApiResponse<List<Case>>> response) {
            if (response.isSuccessful()) {
                cases.addAll(response.body().getData());
                adapter.notifyDataSetChanged();
            }
        }
        
        @Override
        public void onFailure(Call<ApiResponse<List<Case>>> call, Throwable t) {
            Toast.makeText(MainActivity.this, "Error loading cases", Toast.LENGTH_SHORT).show();
        }
    });
```

---

## 🔗 File Upload

### Upload Evidence
```java
// Create file
File file = new File(filePath);
RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

// Create other fields
RequestBody caseId = RequestBody.create(MediaType.parse("text/plain"), "1");
RequestBody description = RequestBody.create(MediaType.parse("text/plain"), "Crime scene photo");

// Upload
ApiClient.getApiService().uploadEvidence(body, caseId, description)
    .enqueue(new Callback<ApiResponse<Evidence>>() {
        @Override
        public void onResponse(Call<ApiResponse<Evidence>> call, 
                             Response<ApiResponse<Evidence>> response) {
            if (response.isSuccessful()) {
                Toast.makeText(MainActivity.this, "Evidence uploaded", Toast.LENGTH_SHORT).show();
            }
        }
        
        @Override
        public void onFailure(Call<ApiResponse<Evidence>> call, Throwable t) {
            Toast.makeText(MainActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
        }
    });
```

---

## 🧪 Testing Checklist

- [ ] Google Sign-In flow
- [ ] Email/Password registration
- [ ] Email/Password login
- [ ] Token storage & retrieval
- [ ] Role-based navigation
- [ ] Create case
- [ ] Get cases list
- [ ] Update case status
- [ ] Assign case to officer
- [ ] Upload evidence
- [ ] Get notifications
- [ ] Mark notification as read
- [ ] Officer workflow (accept/reject/complete)
- [ ] Error handling (401, 403, 404, 500)
- [ ] Network error handling
- [ ] Pagination
- [ ] Logout & token cleanup

---

## 📞 Troubleshooting

### "No token provided" Error
**Solution**: Ensure token is saved after login
```java
PreferencesManager.saveToken(response.getToken());
```

### "Invalid token" Error
**Solution**: Token may have expired, refresh or re-login
```java
// Refresh token
ApiClient.getApiService().refreshToken(oldToken).enqueue(...);
```

### CORS Error
**Solution**: Backend CORS is configured, ensure correct domain
- Check backend CORS_ORIGIN environment variable
- Verify Android app is making requests to correct URL

### Network Timeout
**Solution**: Increase timeout or check network connectivity
```java
OkHttpClient client = new OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build();
```

### 401 Unauthorized on Every Request
**Solution**: Token not being sent in header
- Verify AuthInterceptor is added to OkHttpClient
- Check token is saved in SharedPreferences
- Verify token format: "Bearer <token>"

---

## 📚 Additional Resources

- Backend README: `/backend/README.md`
- API Endpoints: `/backend/API_ENDPOINTS.md`
- Deployment Guide: `/backend/DEPLOYMENT.md`
- Implementation Summary: `/backend/IMPLEMENTATION_SUMMARY.md`

---

**Last Updated**: 2025-01-15
**Version**: 1.0.0
**Status**: Production Ready

Happy coding! 🚀
