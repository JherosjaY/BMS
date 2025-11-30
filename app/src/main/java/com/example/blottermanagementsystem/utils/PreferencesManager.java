package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * PreferencesManager - Synced with Kotlin version
 * Manages all app preferences including user sessions, settings, and per-user data
 */
public class PreferencesManager {
    private static final String PREFS_NAME = "blotter_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_PROFILE_PHOTO = "profile_photo";
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
    private static final String KEY_GUIDE_COMPLETED = "guide_completed";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_PROFILE_EMOJI = "profile_emoji";
    private static final String KEY_HAS_SELECTED_PFP = "has_selected_pfp";
    private static final String KEY_LANGUAGE = "user_language";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_NOTIFICATION_SOUND = "notification_sound";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    private static final String KEY_LAST_USER_ID = "last_user_id";
    private static final String KEY_FCM_TOKEN = "fcm_token";
    private static final String KEY_PERMISSIONS_GRANTED = "permissions_granted";
    private static final String KEY_GOOGLE_EMAIL = "google_email";
    private static final String KEY_GOOGLE_DISPLAY_NAME = "google_display_name";
    private static final String KEY_GOOGLE_PHOTO_URL = "google_photo_url";
    private static final String KEY_IS_GOOGLE_ACCOUNT = "is_google_account";
    private static final String KEY_JWT_TOKEN = "jwt_token";
    private static final String KEY_TOKEN_EXPIRY = "token_expiry";
    
    private final SharedPreferences prefs;
    
    public PreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    // ==================== Login State ====================
    
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    public void setLoggedIn(boolean value) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).commit();
        android.util.Log.d("PreferencesManager", "✅ setLoggedIn(" + value + ") - Value saved to preferences");
        android.util.Log.d("PreferencesManager", "✅ Verification: isLoggedIn() = " + isLoggedIn());
    }
    
    // ==================== User ID ====================
    
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }
    
    public void setUserId(int value) {
        prefs.edit().putInt(KEY_USER_ID, value).commit();
    }
    
    // Last logged-in user ID (persists after logout)
    public int getLastUserId() {
        return prefs.getInt(KEY_LAST_USER_ID, -1);
    }
    
    private void setLastUserId(int value) {
        prefs.edit().putInt(KEY_LAST_USER_ID, value).apply();
    }
    
    // ==================== User Info ====================
    
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }
    
    public void setUsername(String value) {
        prefs.edit().putString(KEY_USERNAME, value).apply();
    }
    
    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, null);
    }
    
    public void setUserRole(String value) {
        prefs.edit().putString(KEY_USER_ROLE, value).commit();
    }
    
    public String getFirstName() {
        return prefs.getString(KEY_FIRST_NAME, null);
    }
    
    public void setFirstName(String value) {
        prefs.edit().putString(KEY_FIRST_NAME, value).commit();
    }
    
    public String getLastName() {
        return prefs.getString(KEY_LAST_NAME, null);
    }
    
    public void setLastName(String value) {
        prefs.edit().putString(KEY_LAST_NAME, value).commit();
    }
    
    public String getProfilePhoto() {
        return prefs.getString(KEY_PROFILE_PHOTO, null);
    }
    
    public void setProfilePhoto(String value) {
        prefs.edit().putString(KEY_PROFILE_PHOTO, value).apply();
    }
    
    public String getGender() {
        return prefs.getString("user_gender", "Male");
    }
    
    public void setGender(String value) {
        prefs.edit().putString("user_gender", value).commit();
    }
    
    // ==================== Onboarding ====================
    
    public boolean isOnboardingCompleted() {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);
    }
    
    public void setOnboardingCompleted(boolean value) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).commit();
        android.util.Log.d("PreferencesManager", "✅ setOnboardingCompleted(" + value + ") - Saved synchronously");
    }
    
    // ==================== Guide ====================
    
    public boolean isGuideCompleted() {
        return prefs.getBoolean(KEY_GUIDE_COMPLETED, false);
    }
    
    public void setGuideCompleted(boolean value) {
        prefs.edit().putBoolean(KEY_GUIDE_COMPLETED, value).apply();
    }
    
    // ==================== Profile Picture ====================
    
    public boolean hasSelectedPfp() {
        return prefs.getBoolean(KEY_HAS_SELECTED_PFP, false);
    }
    
    public void setHasSelectedPfp(boolean value) {
        prefs.edit().putBoolean(KEY_HAS_SELECTED_PFP, value).apply();
    }
    
    // ==================== Dark Mode (Per-User) ====================
    
    public boolean isDarkMode() {
        int currentUserId = getUserId();
        if (currentUserId != -1) {
            return prefs.getBoolean("dark_mode_user_" + currentUserId, true);
        } else {
            return prefs.getBoolean(KEY_DARK_MODE, true);
        }
    }
    
    public void setDarkMode(boolean value) {
        int currentUserId = getUserId();
        if (currentUserId != -1) {
            prefs.edit().putBoolean("dark_mode_user_" + currentUserId, value).apply();
        } else {
            prefs.edit().putBoolean(KEY_DARK_MODE, value).apply();
        }
    }
    
    // ==================== Profile Emoji (Per-User) ====================
    
    public String getProfileEmoji() {
        int currentUserId = getUserId();
        if (currentUserId > 0) {
            return prefs.getString("profile_emoji_" + currentUserId, "👤");
        } else {
            return "👤";
        }
    }
    
    public void setProfileEmoji(String value) {
        int currentUserId = getUserId();
        if (currentUserId > 0) {
            prefs.edit().putString("profile_emoji_" + currentUserId, value).apply();
        }
    }
    
    // ==================== Profile Image URI (Per-User) ====================
    
    public String getProfileImageUri() {
        int currentUserId = getUserId();
        if (currentUserId > 0) {
            return prefs.getString("profile_image_uri_" + currentUserId, null);
        } else {
            return null;
        }
    }
    
    public void setProfileImageUri(String value) {
        int currentUserId = getUserId();
        if (currentUserId > 0) {
            prefs.edit().putString("profile_image_uri_" + currentUserId, value).apply();
        }
    }
    
    // ==================== Has Selected Profile Picture (Per-User) ====================
    
    public boolean hasSelectedProfilePicture() {
        int currentUserId = getUserId();
        if (currentUserId != -1) {
            return prefs.getBoolean("has_selected_pfp_user_" + currentUserId, false);
        } else {
            return prefs.getBoolean(KEY_HAS_SELECTED_PFP, false);
        }
    }
    
    public void setHasSelectedProfilePicture(boolean value) {
        int currentUserId = getUserId();
        if (currentUserId != -1) {
            prefs.edit().putBoolean("has_selected_pfp_user_" + currentUserId, value).commit();
        } else {
            prefs.edit().putBoolean(KEY_HAS_SELECTED_PFP, value).apply();
        }
    }
    
    // ==================== Must Change Password (Per-User) ====================
    
    public boolean mustChangePassword() {
        int currentUserId = getUserId();
        if (currentUserId != -1) {
            return prefs.getBoolean("must_change_password_user_" + currentUserId, false);
        } else {
            return prefs.getBoolean("must_change_password", false);
        }
    }
    
    public void setMustChangePassword(boolean value) {
        int currentUserId = getUserId();
        if (currentUserId != -1) {
            prefs.edit().putBoolean("must_change_password_user_" + currentUserId, value).apply();
        } else {
            prefs.edit().putBoolean("must_change_password", value).apply();
        }
    }
    
    // ==================== Has Changed Password (Per-User) ====================
    
    public boolean hasPasswordChanged() {
        int currentUserId = getUserId();
        if (currentUserId != -1) {
            return prefs.getBoolean("password_changed_user_" + currentUserId, false);
        } else {
            return prefs.getBoolean("password_changed", false);
        }
    }
    
    public void setPasswordChanged(boolean value) {
        int currentUserId = getUserId();
        if (currentUserId != -1) {
            prefs.edit().putBoolean("password_changed_user_" + currentUserId, value).commit();
        } else {
            prefs.edit().putBoolean("password_changed", value).commit();
        }
    }
    
    // ==================== Language (Per-User) ====================
    
    public String getUserLanguage() {
        int currentUserId = getUserId();
        if (currentUserId != -1) {
            return prefs.getString("language_user_" + currentUserId, "en");
        } else {
            return prefs.getString(KEY_LANGUAGE, "en");
        }
    }
    
    public void setUserLanguage(String value) {
        int currentUserId = getUserId();
        if (currentUserId != -1) {
            prefs.edit().putString("language_user_" + currentUserId, value).apply();
        } else {
            prefs.edit().putString(KEY_LANGUAGE, value).apply();
        }
    }
    
    // ==================== Role Helper ====================
    
    public String getRole() {
        String role = getUserRole();
        return role != null ? role : "User";
    }
    
    // ==================== Notification Settings ====================
    
    public boolean isNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }
    
    public void setNotificationsEnabled(boolean value) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply();
    }
    
    public boolean isNotificationSoundEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATION_SOUND, true);
    }
    
    public void setNotificationSoundEnabled(boolean value) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_SOUND, value).apply();
    }
    
    // ==================== Biometric (Per-User) ====================
    
    public boolean isBiometricEnabled() {
        int currentUserId = getUserId();
        if (currentUserId != -1) {
            return prefs.getBoolean("biometric_enabled_user_" + currentUserId, false);
        } else {
            return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);
        }
    }
    
    public void setBiometricEnabled(boolean value) {
        int currentUserId = getUserId();
        if (currentUserId != -1) {
            prefs.edit().putBoolean("biometric_enabled_user_" + currentUserId, value).apply();
        } else {
            prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply();
        }
    }
    
    public String getBiometricPin() {
        int currentUserId = getUserId();
        if (currentUserId != -1) {
            return prefs.getString("biometric_pin_user_" + currentUserId, "");
        } else {
            return "";
        }
    }
    
    public void setBiometricPin(String value) {
        int currentUserId = getUserId();
        if (currentUserId != -1) {
            prefs.edit().putString("biometric_pin_user_" + currentUserId, value).apply();
        }
    }
    
    // Helper to check if biometric is enabled for a specific user
    public boolean isBiometricEnabledForUser(int userId) {
        return prefs.getBoolean("biometric_enabled_user_" + userId, false);
    }
    
    // Helper to get PIN for a specific user
    public String getBiometricPinForUser(int userId) {
        String pin = prefs.getString("biometric_pin_user_" + userId, "");
        return pin != null ? pin : "";
    }
    
    // ==================== FCM Token ====================
    
    public String getFcmToken() {
        return prefs.getString(KEY_FCM_TOKEN, null);
    }
    
    public void setFcmToken(String value) {
        prefs.edit().putString(KEY_FCM_TOKEN, value).apply();
    }
    
    // ==================== Permissions ====================
    
    public boolean isPermissionsGranted() {
        return prefs.getBoolean(KEY_PERMISSIONS_GRANTED, false);
    }
    
    public void setPermissionsGranted(boolean value) {
        prefs.edit().putBoolean(KEY_PERMISSIONS_GRANTED, value).commit();
        android.util.Log.d("PreferencesManager", "✅ setPermissionsGranted(" + value + ") - Saved synchronously");
    }
    
    // ==================== Save User Session ====================
    
    public void saveUserSession(int userId, String username, String role, 
                                String firstName, String lastName, String profilePhoto) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putInt(KEY_LAST_USER_ID, userId);  // Save last user ID
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_USER_ROLE, role);
        editor.putString("user_role_" + userId, role);  // Save role per user
        editor.putString(KEY_FIRST_NAME, firstName);
        editor.putString(KEY_LAST_NAME, lastName);
        editor.putString(KEY_PROFILE_PHOTO, profilePhoto);
        editor.commit();  // Use commit() for immediate save
    }
    
    // ==================== Clear Session ====================
    
    public void clearSession() {
        int currentUserId = getUserId();
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_USER_ROLE);
        editor.remove(KEY_FIRST_NAME);
        editor.remove(KEY_LAST_NAME);
        editor.remove(KEY_PROFILE_PHOTO);
        
        // Clear Google account info to prevent cross-user contamination
        editor.remove(KEY_GOOGLE_EMAIL);
        editor.remove(KEY_GOOGLE_DISPLAY_NAME);
        editor.remove(KEY_GOOGLE_PHOTO_URL);
        editor.remove(KEY_IS_GOOGLE_ACCOUNT);
        
        // Clear JWT token on logout
        editor.remove(KEY_JWT_TOKEN);
        editor.remove(KEY_TOKEN_EXPIRY);
        
        // ✅ IMPORTANT: DO NOT remove password_changed flag on logout!
        // Each officer's password_changed flag is stored per-user ID (password_changed_user_X)
        // Removing it would force password change on next login even if already changed
        // Password should only be reset if admin explicitly resets it
        
        // DON'T remove per-user profile data (profile_image_uri_*, profile_emoji_*, etc.)
        // DON'T remove FCM token - it's device-specific, not user-specific
        // This allows users to keep their profile pictures when they log back in
        editor.apply();
        
        android.util.Log.d("PreferencesManager", "✅ Session cleared (JWT token cleared, password_changed flag preserved for each officer)");
    }
    
    // ==================== Generic Helpers ====================
    
    public void saveBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).commit();
        android.util.Log.d("PreferencesManager", "✅ saveBoolean(" + key + ", " + value + ") - Saved synchronously");
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }
    
    public void saveString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }
    
    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }
    
    public void saveInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }
    
    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }
    
    // Legacy methods for compatibility
    public void logout() {
        clearSession();
    }
    
    public boolean isPushNotificationsEnabled() {
        return isNotificationsEnabled();
    }
    
    public void setPushNotificationsEnabled(boolean value) {
        setNotificationsEnabled(value);
    }
    
    public boolean isEmailNotificationsEnabled() {
        return getBoolean("email_notifications", true);
    }
    
    public void setEmailNotificationsEnabled(boolean value) {
        saveBoolean("email_notifications", value);
    }
    
    public boolean isSmsNotificationsEnabled() {
        return getBoolean("sms_notifications", true);
    }
    
    public void setSmsNotificationsEnabled(boolean value) {
        saveBoolean("sms_notifications", value);
    }
    
    // ==================== Google Account Info ====================
    
    public void saveGoogleAccountInfo(String email, String displayName, String photoUrl) {
        prefs.edit()
            .putString(KEY_GOOGLE_EMAIL, email)
            .putString(KEY_GOOGLE_DISPLAY_NAME, displayName)
            .putString(KEY_GOOGLE_PHOTO_URL, photoUrl)
            .putBoolean(KEY_IS_GOOGLE_ACCOUNT, true)
            .apply();
    }
    
    public boolean isGoogleAccount() {
        return prefs.getBoolean(KEY_IS_GOOGLE_ACCOUNT, false);
    }
    
    public String getGoogleEmail() {
        return prefs.getString(KEY_GOOGLE_EMAIL, null);
    }
    
    public String getGoogleDisplayName() {
        return prefs.getString(KEY_GOOGLE_DISPLAY_NAME, null);
    }
    
    public String getGooglePhotoUrl() {
        return prefs.getString(KEY_GOOGLE_PHOTO_URL, null);
    }
    
    public void clearGoogleAccountInfo() {
        prefs.edit()
            .remove(KEY_GOOGLE_EMAIL)
            .remove(KEY_GOOGLE_DISPLAY_NAME)
            .remove(KEY_GOOGLE_PHOTO_URL)
            .remove(KEY_IS_GOOGLE_ACCOUNT)
            .apply();
    }
    
    // ==================== JWT Token (Neon Authentication) ====================
    
    /**
     * Save JWT token from Neon backend
     */
    public void setJwtToken(String token) {
        prefs.edit().putString(KEY_JWT_TOKEN, token).apply();
        android.util.Log.d("PreferencesManager", "✅ JWT token saved");
    }
    
    /**
     * Get JWT token for API requests
     */
    public String getJwtToken() {
        return prefs.getString(KEY_JWT_TOKEN, null);
    }
    
    /**
     * Check if JWT token exists and is valid
     */
    public boolean hasValidJwtToken() {
        String token = getJwtToken();
        return token != null && !token.isEmpty();
    }
    
    /**
     * Save token expiry time
     */
    public void setTokenExpiry(long expiryTime) {
        prefs.edit().putLong(KEY_TOKEN_EXPIRY, expiryTime).apply();
    }
    
    /**
     * Get token expiry time
     */
    public long getTokenExpiry() {
        return prefs.getLong(KEY_TOKEN_EXPIRY, 0);
    }
    
    /**
     * Check if token is expired
     */
    public boolean isTokenExpired() {
        long expiryTime = getTokenExpiry();
        return expiryTime > 0 && System.currentTimeMillis() > expiryTime;
    }
    
    /**
     * Clear JWT token (logout)
     */
    public void clearJwtToken() {
        prefs.edit()
            .remove(KEY_JWT_TOKEN)
            .remove(KEY_TOKEN_EXPIRY)
            .apply();
        android.util.Log.d("PreferencesManager", "✅ JWT token cleared");
    }
    
    // ==================== Firebase Token ====================
    
    private static final String KEY_FIREBASE_TOKEN = "firebase_token";
    
    public String getFirebaseToken() {
        return prefs.getString(KEY_FIREBASE_TOKEN, null);
    }
    
    public void setFirebaseToken(String token) {
        prefs.edit().putString(KEY_FIREBASE_TOKEN, token).commit();
        android.util.Log.d("PreferencesManager", "✅ Firebase token saved");
    }
    
    public void clearFirebaseToken() {
        prefs.edit().remove(KEY_FIREBASE_TOKEN).apply();
        android.util.Log.d("PreferencesManager", "✅ Firebase token cleared");
    }
    
    // ==================== Email ====================
    
    private static final String KEY_EMAIL = "email";
    
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }
    
    public void setEmail(String email) {
        prefs.edit().putString(KEY_EMAIL, email).commit();
    }
    
    // ==================== First Time User ====================
    
    private static final String KEY_IS_FIRST_TIME_USER = "is_first_time_user";
    
    public boolean isFirstTimeUser() {
        return prefs.getBoolean(KEY_IS_FIRST_TIME_USER, true); // Default: true (first time)
    }
    
    public void setFirstTimeUser(boolean value) {
        prefs.edit().putBoolean(KEY_IS_FIRST_TIME_USER, value).commit();
        android.util.Log.d("PreferencesManager", "✅ isFirstTimeUser set to: " + value);
    }
    
    // ==================== Clear All User Data ====================
    
    public void clearUserData() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USERNAME)
            .remove(KEY_USER_ROLE)
            .remove(KEY_FIRST_NAME)
            .remove(KEY_LAST_NAME)
            .remove(KEY_EMAIL)
            .remove(KEY_FIREBASE_TOKEN)
            .remove(KEY_JWT_TOKEN)
            .remove(KEY_TOKEN_EXPIRY)
            .apply();
        android.util.Log.d("PreferencesManager", "✅ All user data cleared");
    }
}
