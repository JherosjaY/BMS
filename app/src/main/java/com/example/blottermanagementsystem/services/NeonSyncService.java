package com.example.blottermanagementsystem.services;

import android.util.Log;

import com.example.blottermanagementsystem.utils.ApiClient;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 🚀 NEON SYNC SERVICE
 * 
 * Syncs Firebase authenticated users to Neon backend
 * - Creates/updates user in Neon after Firebase login
 * - Maintains multi-device sync
 * - Keeps Neon as backup/source of truth
 */
public class NeonSyncService {
    private static final String TAG = "NeonSyncService";
    private PreferencesManager preferencesManager;
    
    // Callback interface for sync results
    public interface SyncCallback {
        void onSyncSuccess(String userId, String role);
        void onSyncError(String errorMessage);
        void onSyncing();
    }
    
    public NeonSyncService(PreferencesManager preferencesManager) {
        this.preferencesManager = preferencesManager;
    }
    
    /**
     * 🚀 SYNC FIREBASE USER TO NEON
     * Called after Firebase authentication succeeds
     * Creates or updates user in Neon database
     */
    public void syncFirebaseUserToNeon(FirebaseUser firebaseUser, String firebaseToken, SyncCallback callback) {
        if (callback != null) callback.onSyncing();
        
        Log.d(TAG, "🚀 Syncing Firebase user to Neon: " + firebaseUser.getEmail());
        
        try {
            // Prepare sync data
            Map<String, Object> syncData = new HashMap<>();
            syncData.put("firebaseUid", firebaseUser.getUid());
            syncData.put("email", firebaseUser.getEmail());
            syncData.put("displayName", firebaseUser.getDisplayName());
            syncData.put("photoUrl", firebaseUser.getPhotoUrl() != null ? 
                firebaseUser.getPhotoUrl().toString() : null);
            syncData.put("firebaseToken", firebaseToken);
            syncData.put("authProvider", "firebase");
            
            // Call backend sync endpoint
            ApiClient.getApiService().syncFirebaseUser(syncData)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, Object> responseBody = response.body();
                            
                            Log.d(TAG, "✅ Firebase user synced to Neon successfully");
                            
                            // Extract user data from response
                            String userId = responseBody.get("userId") != null ? 
                                responseBody.get("userId").toString() : firebaseUser.getUid();
                            String role = responseBody.get("role") != null ? 
                                responseBody.get("role").toString() : "user";
                            
                            // Update local preferences with Neon data
                            preferencesManager.setUserId(Integer.parseInt(userId));
                            preferencesManager.setUserRole(role);
                            preferencesManager.setEmail(firebaseUser.getEmail());
                            preferencesManager.setFirebaseToken(firebaseToken);
                            
                            Log.d(TAG, "✅ Local preferences updated with Neon data");
                            Log.d(TAG, "✅ User role: " + role);
                            
                            if (callback != null) {
                                callback.onSyncSuccess(userId, role);
                            }
                        } else {
                            String errorMessage = "Sync failed: " + response.code();
                            Log.e(TAG, "❌ " + errorMessage);
                            if (callback != null) {
                                callback.onSyncError(errorMessage);
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        String errorMessage = t.getMessage() != null ? t.getMessage() : "Network error";
                        Log.e(TAG, "❌ Sync failed: " + errorMessage);
                        
                        // Fallback: Use Firebase data if Neon sync fails
                        Log.d(TAG, "⚠️ Falling back to Firebase data (offline mode)");
                        preferencesManager.setUserId(firebaseUser.getUid().hashCode());
                        preferencesManager.setUserRole("user");
                        preferencesManager.setFirebaseToken(firebaseToken);
                        
                        if (callback != null) {
                            callback.onSyncError("Offline: Using cached Firebase data");
                        }
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception during sync: " + e.getMessage());
            if (callback != null) {
                callback.onSyncError(e.getMessage());
            }
        }
    }
    
    /**
     * 🔄 SYNC OFFLINE USER DATA TO NEON
     * Called when device comes online
     * Syncs any pending changes to Neon
     */
    public void syncOfflineDataToNeon(SyncCallback callback) {
        if (callback != null) callback.onSyncing();
        
        Log.d(TAG, "🔄 Syncing offline data to Neon");
        
        try {
            String firebaseToken = preferencesManager.getFirebaseToken();
            String email = preferencesManager.getEmail();
            
            if (firebaseToken == null || email == null) {
                Log.d(TAG, "⚠️ No cached user data to sync");
                if (callback != null) {
                    callback.onSyncError("No cached user data");
                }
                return;
            }
            
            // Prepare sync data
            Map<String, Object> syncData = new HashMap<>();
            syncData.put("firebaseToken", firebaseToken);
            syncData.put("email", email);
            syncData.put("lastSyncTime", System.currentTimeMillis());
            
            // Call backend sync endpoint
            ApiClient.getApiService().syncOfflineData(syncData)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "✅ Offline data synced to Neon successfully");
                            if (callback != null) {
                                callback.onSyncSuccess("sync", "success");
                            }
                        } else {
                            Log.e(TAG, "❌ Offline sync failed: " + response.code());
                            if (callback != null) {
                                callback.onSyncError("Sync failed: " + response.code());
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Log.e(TAG, "❌ Offline sync failed: " + t.getMessage());
                        if (callback != null) {
                            callback.onSyncError(t.getMessage());
                        }
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception during offline sync: " + e.getMessage());
            if (callback != null) {
                callback.onSyncError(e.getMessage());
            }
        }
    }
}
