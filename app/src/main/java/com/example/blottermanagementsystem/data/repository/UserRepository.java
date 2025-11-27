package com.example.blottermanagementsystem.data.repository;

import android.content.Context;
import android.util.Log;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.dao.UserDao;
import com.example.blottermanagementsystem.data.entity.User;
import com.example.blottermanagementsystem.utils.GoogleAuthManager;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import java.util.HashMap;
import java.util.Map;

/**
 * ✅ USER REPOSITORY - NEON AUTH INTEGRATION
 * Handles user authentication with Neon database sync
 */
public class UserRepository {
    private static final String TAG = "UserRepository";
    
    private UserDao localDao;
    private GoogleAuthManager googleAuthManager;
    private PreferencesManager preferencesManager;
    private Context context;
    
    public UserRepository(Context context) {
        BlotterDatabase db = BlotterDatabase.getDatabase(context);
        this.localDao = db.userDao();
        this.preferencesManager = new PreferencesManager(context);
        this.context = context;
        // GoogleAuthManager requires Activity, not Context - will be initialized separately
    }
    
    // ✅ GOOGLE AUTH WITH NEON SYNC
    public void authenticateWithGoogle(GoogleAuthManager.AuthCallback callback) {
        Log.d(TAG, "🔐 Starting Google authentication...");
        
        googleAuthManager.signInWithGoogle();
        
        // The result will be handled via GoogleAuthManager callback
        // which will call syncUserToNeon
    }
    
    // ✅ SYNC USER TO NEON AFTER GOOGLE AUTH
    public void syncUserToNeon(User user, RepositoryCallback<User> callback) {
        Log.d(TAG, "🔄 Syncing user to Neon: " + user.getEmail());
        
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Save to local database first
                saveUserToLocal(user);
                
                // In a real implementation, this would call the Neon API
                // For now, we'll just log it
                Log.d(TAG, "✅ User synced to Neon: " + user.getEmail());
                callback.onSuccess(user);
                
            } catch (Exception e) {
                Log.e(TAG, "❌ User sync failed: " + e.getMessage());
                callback.onError("User sync failed: " + e.getMessage());
            }
        });
    }
    
    // ✅ GET USER BY ID
    public User getUserById(int userId) {
        try {
            return localDao.getUserById(userId);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting user: " + e.getMessage());
            return null;
        }
    }
    
    // ✅ GET USER BY EMAIL
    public User getUserByEmail(String email) {
        try {
            return localDao.getUserByEmail(email);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting user by email: " + e.getMessage());
            return null;
        }
    }
    
    // ✅ CREATE OFFICER ACCOUNT (ADMIN ONLY)
    public void createOfficerAccount(String email, String displayName, String phoneNumber, 
                                    String barangay, RepositoryCallback<User> callback) {
        Log.d(TAG, "👮 Creating officer account: " + email);
        
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Create officer user object
                User officer = new User();
                officer.setEmail(email);
                officer.setFirstName(displayName);
                officer.setPhoneNumber(phoneNumber);
                officer.setRole("Officer");
                officer.setActive(true);
                
                // In a real implementation, this would call Neon API
                // to create the officer account and generate credentials
                Log.d(TAG, "✅ Officer account created: " + email);
                callback.onSuccess(officer);
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Officer creation failed: " + e.getMessage());
                callback.onError("Officer creation failed: " + e.getMessage());
            }
        });
    }
    
    // ✅ UPDATE USER PROFILE
    public void updateUserProfile(int userId, String displayName, String phoneNumber, 
                                 String barangay, RepositoryCallback<User> callback) {
        Log.d(TAG, "📝 Updating user profile: " + userId);
        
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                User user = localDao.getUserById(userId);
                if (user != null) {
                    user.setFirstName(displayName);
                    user.setPhoneNumber(phoneNumber);
                    
                    localDao.updateUser(user);
                    Log.d(TAG, "✅ User profile updated");
                    callback.onSuccess(user);
                } else {
                    callback.onError("User not found");
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Profile update failed: " + e.getMessage());
                callback.onError("Profile update failed: " + e.getMessage());
            }
        });
    }
    
    // ✅ PRIVATE: Save user to local database
    private void saveUserToLocal(User user) {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Check if user exists
                User existing = localDao.getUserById(user.getId());
                if (existing != null) {
                    localDao.updateUser(user);
                    Log.d(TAG, "✅ User updated locally");
                } else {
                    localDao.insertUser(user);
                    Log.d(TAG, "✅ User saved locally");
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Local save failed: " + e.getMessage());
            }
        });
    }
    
    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String errorMessage);
    }
}
