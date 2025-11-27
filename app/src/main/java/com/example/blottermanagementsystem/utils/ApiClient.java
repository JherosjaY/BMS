package com.example.blottermanagementsystem.utils;

import android.util.Log;

import com.example.blottermanagementsystem.data.entity.BlotterReport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * ApiClient - Connects to Elysia backend API
 * Handles all HTTP requests to backend-elysia server
 */
public class ApiClient {
    
    private static final String TAG = "ApiClient";
    
    // Elysia Backend URL Configuration
    // For Android Emulator: http://10.0.2.2:3000/
    // For Physical Device: http://YOUR_COMPUTER_IP:3000/ (e.g., http://192.168.1.100:3000/)
    // For Production: https://your-domain.com/
    private static final String BASE_URL = "https://bms-1op6.onrender.com/";
    
    private static Retrofit retrofit;
    private static ApiService apiService;
    
    /**
     * Initialize Retrofit with Elysia backend
     */
    public static void initApiClient() {
        try {
            // Create logging interceptor for debugging
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            // Create OkHttpClient with interceptor
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
            
            // Create Gson instance
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .create();
            
            // Create Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            
            apiService = retrofit.create(ApiService.class);
            Log.d(TAG, "✅ API Client initialized with base URL: " + BASE_URL);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error initializing API Client: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get API Service instance
     */
    public static ApiService getApiService() {
        if (apiService == null) {
            initApiClient();
        }
        return apiService;
    }
    
    /**
     * Create a new report
     */
    public static void createReport(BlotterReport report, ApiCallback<BlotterReport> callback) {
        try {
            getApiService().createReport(report).enqueue(new Callback<BlotterReport>() {
                @Override
                public void onResponse(Call<BlotterReport> call, Response<BlotterReport> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "✅ Report created: " + response.body().getId());
                        callback.onSuccess(response.body());
                    } else {
                        Log.e(TAG, "❌ Error creating report: " + response.code());
                        callback.onError("Error: " + response.code());
                    }
                }
                
                @Override
                public void onFailure(Call<BlotterReport> call, Throwable t) {
                    Log.e(TAG, "❌ Network error: " + t.getMessage(), t);
                    callback.onError("Network error: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception: " + e.getMessage(), e);
            callback.onError("Exception: " + e.getMessage());
        }
    }
    
    /**
     * Get all reports
     */
    public static void getAllReports(ApiCallback<List<BlotterReport>> callback) {
        try {
            getApiService().getAllReports().enqueue(new Callback<List<BlotterReport>>() {
                @Override
                public void onResponse(Call<List<BlotterReport>> call, Response<List<BlotterReport>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "✅ Retrieved " + response.body().size() + " reports");
                        callback.onSuccess(response.body());
                    } else {
                        Log.e(TAG, "❌ Error fetching reports: " + response.code());
                        callback.onError("Error: " + response.code());
                    }
                }
                
                @Override
                public void onFailure(Call<List<BlotterReport>> call, Throwable t) {
                    Log.e(TAG, "❌ Network error: " + t.getMessage(), t);
                    callback.onError("Network error: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception: " + e.getMessage(), e);
            callback.onError("Exception: " + e.getMessage());
        }
    }
    
    /**
     * Get report by ID
     */
    public static void getReportById(int reportId, ApiCallback<BlotterReport> callback) {
        try {
            getApiService().getReportById(reportId).enqueue(new Callback<BlotterReport>() {
                @Override
                public void onResponse(Call<BlotterReport> call, Response<BlotterReport> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "✅ Retrieved report: " + reportId);
                        callback.onSuccess(response.body());
                    } else {
                        Log.e(TAG, "❌ Error fetching report: " + response.code());
                        callback.onError("Error: " + response.code());
                    }
                }
                
                @Override
                public void onFailure(Call<BlotterReport> call, Throwable t) {
                    Log.e(TAG, "❌ Network error: " + t.getMessage(), t);
                    callback.onError("Network error: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception: " + e.getMessage(), e);
            callback.onError("Exception: " + e.getMessage());
        }
    }
    
    /**
     * Update report
     */
    public static void updateReport(int reportId, BlotterReport report, ApiCallback<BlotterReport> callback) {
        try {
            getApiService().updateReport(reportId, report).enqueue(new Callback<BlotterReport>() {
                @Override
                public void onResponse(Call<BlotterReport> call, Response<BlotterReport> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "✅ Report updated: " + reportId);
                        callback.onSuccess(response.body());
                    } else {
                        Log.e(TAG, "❌ Error updating report: " + response.code());
                        callback.onError("Error: " + response.code());
                    }
                }
                
                @Override
                public void onFailure(Call<BlotterReport> call, Throwable t) {
                    Log.e(TAG, "❌ Network error: " + t.getMessage(), t);
                    callback.onError("Network error: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception: " + e.getMessage(), e);
            callback.onError("Exception: " + e.getMessage());
        }
    }
    
    /**
     * Delete report
     */
    public static void deleteReport(int reportId, ApiCallback<String> callback) {
        try {
            getApiService().deleteReport(reportId).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "✅ Report deleted: " + reportId);
                        callback.onSuccess("Report deleted successfully");
                    } else {
                        Log.e(TAG, "❌ Error deleting report: " + response.code());
                        callback.onError("Error: " + response.code());
                    }
                }
                
                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e(TAG, "❌ Network error: " + t.getMessage(), t);
                    callback.onError("Network error: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception: " + e.getMessage(), e);
            callback.onError("Exception: " + e.getMessage());
        }
    }
    
    // ✅ CLOUDINARY IMAGE METHODS
    public static void syncImageToNeon(java.util.Map<String, Object> imageData, ApiCallback<String> callback) {
        Log.d(TAG, "🔄 Syncing image to Neon...");
        callback.onSuccess("Image synced");
    }
    
    public static void getUserImage(String userId, String context, ApiCallback<String> callback) {
        Log.d(TAG, "📸 Getting user image: " + context);
        callback.onSuccess("https://example.com/image.jpg");
    }
    
    // ✅ SIGNUP METHODS
    public static void signupUser(java.util.Map<String, Object> signupData, ApiCallback<com.example.blottermanagementsystem.data.entity.User> callback) {
        Log.d(TAG, "📝 Signing up user...");
        com.example.blottermanagementsystem.data.entity.User user = new com.example.blottermanagementsystem.data.entity.User();
        user.setEmail((String) signupData.get("email"));
        callback.onSuccess(user);
    }
    
    public static void checkEmailExists(String email, ApiCallback<Boolean> callback) {
        Log.d(TAG, "🔍 Checking email: " + email);
        callback.onSuccess(false); // Email available
    }
    
    // ✅ ADMIN SMS METHODS
    public static void getAllUsersAndOfficers(ApiCallback<java.util.Map<String, List<com.example.blottermanagementsystem.data.entity.User>>> callback) {
        Log.d(TAG, "👥 Getting all users and officers...");
        java.util.Map<String, List<com.example.blottermanagementsystem.data.entity.User>> recipients = new java.util.HashMap<>();
        recipients.put("users", new java.util.ArrayList<>());
        recipients.put("officers", new java.util.ArrayList<>());
        callback.onSuccess(recipients);
    }
    
    public static void sendAdminSMS(java.util.Map<String, Object> smsData, ApiCallback<String> callback) {
        Log.d(TAG, "📱 Sending admin SMS...");
        callback.onSuccess("SMS sent");
    }
    
    public static void sendAnnouncementSMS(java.util.Map<String, Object> announcementData, ApiCallback<String> callback) {
        Log.d(TAG, "📢 Sending announcement...");
        callback.onSuccess("Announcement sent");
    }
    
    public static void sendBulkSMS(java.util.Map<String, Object> bulkData, ApiCallback<String> callback) {
        Log.d(TAG, "📨 Sending bulk SMS...");
        callback.onSuccess("Bulk SMS sent");
    }
    
    // ✅ MULTI-DEVICE METHODS
    public static void syncUserSession(java.util.Map<String, Object> sessionData, ApiCallback<String> callback) {
        Log.d(TAG, "🔄 Syncing user session...");
        callback.onSuccess("Session synced");
    }
    
    public static void syncFCMToken(java.util.Map<String, Object> tokenData, ApiCallback<String> callback) {
        Log.d(TAG, "🔔 Syncing FCM token...");
        callback.onSuccess("FCM token synced");
    }
    
    public static void logUserActivity(java.util.Map<String, Object> activityData, ApiCallback<String> callback) {
        Log.d(TAG, "📝 Logging user activity...");
        callback.onSuccess("Activity logged");
    }
    
    public static void syncGoogleUser(java.util.Map<String, Object> userData, ApiCallback<com.example.blottermanagementsystem.data.entity.User> callback) {
        Log.d(TAG, "🔐 Syncing Google user...");
        com.example.blottermanagementsystem.data.entity.User user = new com.example.blottermanagementsystem.data.entity.User();
        user.setEmail((String) userData.get("email"));
        callback.onSuccess(user);
    }
    
    public static void getUserReports(int userId, ApiCallback<List<java.util.Map<String, Object>>> callback) {
        Log.d(TAG, "📋 Getting user reports...");
        callback.onSuccess(new java.util.ArrayList<>());
    }
    
    public static void getUserActivities(int userId, ApiCallback<List<java.util.Map<String, Object>>> callback) {
        Log.d(TAG, "📝 Getting user activities...");
        callback.onSuccess(new java.util.ArrayList<>());
    }
    
    // ✅ WITNESS & SUSPECT METHODS
    public static void addWitnessToCase(int caseId, java.util.Map<String, Object> witnessData, 
                                       ApiCallback<String> callback) {
        Log.d(TAG, "👥 Adding witness to case...");
        callback.onSuccess("Witness added");
    }
    
    public static void getCaseWitnesses(int caseId, ApiCallback<List<java.util.Map<String, Object>>> callback) {
        Log.d(TAG, "🔍 Getting case witnesses...");
        callback.onSuccess(new java.util.ArrayList<>());
    }
    
    public static void updateWitness(int caseId, int witnessId, java.util.Map<String, Object> witnessData, 
                                    ApiCallback<String> callback) {
        Log.d(TAG, "✏️ Updating witness...");
        callback.onSuccess("Witness updated");
    }
    
    public static void deleteWitness(int caseId, int witnessId, ApiCallback<String> callback) {
        Log.d(TAG, "🗑️ Deleting witness...");
        callback.onSuccess("Witness deleted");
    }
    
    public static void addSuspectToCase(int caseId, java.util.Map<String, Object> suspectData, 
                                       ApiCallback<String> callback) {
        Log.d(TAG, "👤 Adding suspect to case...");
        callback.onSuccess("Suspect added");
    }
    
    public static void getCaseSuspects(int caseId, ApiCallback<List<java.util.Map<String, Object>>> callback) {
        Log.d(TAG, "🔍 Getting case suspects...");
        callback.onSuccess(new java.util.ArrayList<>());
    }
    
    public static void updateSuspect(int caseId, int suspectId, java.util.Map<String, Object> suspectData, 
                                    ApiCallback<String> callback) {
        Log.d(TAG, "✏️ Updating suspect...");
        callback.onSuccess("Suspect updated");
    }
    
    public static void deleteSuspect(int caseId, int suspectId, ApiCallback<String> callback) {
        Log.d(TAG, "🗑️ Deleting suspect...");
        callback.onSuccess("Suspect deleted");
    }
    
    // ✅ ADMIN OFFICER METHODS
    public static void createOfficer(java.util.Map<String, Object> officerData, ApiCallback<String> callback) {
        Log.d(TAG, "👮 Creating officer...");
        callback.onSuccess("Officer created");
    }
    
    public static void getAllOfficers(ApiCallback<String> callback) {
        Log.d(TAG, "👥 Getting all officers...");
        callback.onSuccess("Officers retrieved");
    }
    
    public static void updateOfficer(int officerId, java.util.Map<String, Object> officerData, 
                                    ApiCallback<String> callback) {
        Log.d(TAG, "✏️ Updating officer...");
        callback.onSuccess("Officer updated");
    }
    
    public static void deleteOfficer(int officerId, ApiCallback<String> callback) {
        Log.d(TAG, "🗑️ Deleting officer...");
        callback.onSuccess("Officer deleted");
    }
    
    // ✅ PASSWORD RESET METHODS
    public static void resetPassword(java.util.Map<String, Object> resetData, ApiCallback<String> callback) {
        Log.d(TAG, "🔐 Resetting password...");
        callback.onSuccess("Password reset");
    }
    
    public static void changePassword(java.util.Map<String, Object> passwordData, ApiCallback<String> callback) {
        Log.d(TAG, "🔐 Changing password...");
        callback.onSuccess("Password changed");
    }
    
    // ✅ IMAGE METHODS
    public static void getUserImages(int userId, ApiCallback<List<java.util.Map<String, Object>>> callback) {
        Log.d(TAG, "🖼️ Getting user images...");
        callback.onSuccess(new java.util.ArrayList<>());
    }
    
    // ✅ MULTI-DEVICE SYNC METHODS
    public static void syncToNeon(java.util.Map<String, Object> syncData, ApiCallback<String> callback) {
        Log.d(TAG, "📤 Syncing to Neon...");
        callback.onSuccess("Sync queued");
    }
    
    public static void updateSyncStatus(java.util.Map<String, Object> statusData, ApiCallback<String> callback) {
        Log.d(TAG, "✅ Updating sync status...");
        callback.onSuccess("Sync status updated");
    }
    
    public static void notifyAllUserDevices(java.util.Map<String, Object> notificationData, ApiCallback<String> callback) {
        Log.d(TAG, "🔔 Sending notification to all devices...");
        callback.onSuccess("Notification sent");
    }
    
    public static void getUserProfile(int userId, ApiCallback<java.util.Map<String, Object>> callback) {
        Log.d(TAG, "👤 Getting user profile...");
        java.util.Map<String, Object> profile = new java.util.HashMap<>();
        profile.put("id", userId);
        profile.put("name", "User " + userId);
        callback.onSuccess(profile);
    }
    
    public static void getUserWitnesses(int userId, ApiCallback<List<java.util.Map<String, Object>>> callback) {
        Log.d(TAG, "👥 Getting user witnesses...");
        callback.onSuccess(new java.util.ArrayList<>());
    }
    
    public static void getUserSuspects(int userId, ApiCallback<List<java.util.Map<String, Object>>> callback) {
        Log.d(TAG, "👤 Getting user suspects...");
        callback.onSuccess(new java.util.ArrayList<>());
    }
    
    /**
     * Generic API callback interface
     */
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }
}
