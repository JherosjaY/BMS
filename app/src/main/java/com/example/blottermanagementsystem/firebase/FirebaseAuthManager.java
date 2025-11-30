package com.example.blottermanagementsystem.firebase;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
// ❌ REMOVED: import com.example.blottermanagementsystem.services.NeonSyncService; (Pure online mode)
import com.example.blottermanagementsystem.utils.PreferencesManager;

/**
 * 🔥 FIREBASE AUTHENTICATION MANAGER
 * 
 * Handles all Firebase authentication:
 * - Google Sign-In
 * - Email/Password login
 * - Email/Password registration
 * - Offline support (local caching)
 * - Sync with Neon backend
 */
public class FirebaseAuthManager {
    private static final String TAG = "FirebaseAuthManager";
    private FirebaseAuth firebaseAuth;
    private PreferencesManager preferencesManager;
    // ❌ REMOVED: private NeonSyncService neonSyncService; (Pure online mode)
    private Context context;
    
    // Callback interface for auth results
    public interface AuthCallback {
        void onSuccess(FirebaseUser user, String token);
        void onError(String errorMessage);
        void onLoading();
    }
    
    public FirebaseAuthManager(Context context, PreferencesManager preferencesManager) {
        this.context = context;
        this.preferencesManager = preferencesManager;
        this.firebaseAuth = FirebaseAuth.getInstance();
        // ❌ REMOVED: this.neonSyncService = new NeonSyncService(preferencesManager); (Pure online mode)
    }
    
    /**
     * 🚀 GOOGLE SIGN-IN WITH FIREBASE
     * Works online and offline (cached locally)
     */
    public void googleSignIn(String googleIdToken, AuthCallback callback) {
        if (callback != null) callback.onLoading();
        
        Log.d(TAG, "🔥 Starting Firebase Google Sign-In");
        
        try {
            AuthCredential credential = GoogleAuthProvider.getCredential(googleIdToken, null);
            
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "✅ Firebase Google Sign-In successful: " + user.getEmail());
                            
                            // Get Firebase ID token for backend sync
                            user.getIdToken(false).addOnCompleteListener(tokenTask -> {
                                if (tokenTask.isSuccessful()) {
                                    String firebaseToken = tokenTask.getResult().getToken();
                                    
                                    // Cache user data locally for offline support
                                    cacheUserLocally(user, firebaseToken);
                                    
                                    // Sync to Neon backend
                                    syncToNeonBackend(user, firebaseToken);
                                    
                                    if (callback != null) {
                                        callback.onSuccess(user, firebaseToken);
                                    }
                                } else {
                                    Log.e(TAG, "❌ Failed to get Firebase token");
                                    if (callback != null) {
                                        callback.onError("Failed to get authentication token");
                                    }
                                }
                            });
                        }
                    } else {
                        String errorMessage = task.getException() != null ? 
                            task.getException().getMessage() : "Unknown error";
                        Log.e(TAG, "❌ Firebase Google Sign-In failed: " + errorMessage);
                        if (callback != null) {
                            callback.onError(errorMessage);
                        }
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception during Google Sign-In: " + e.getMessage());
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        }
    }
    
    /**
     * 🔥 EMAIL/PASSWORD LOGIN WITH FIREBASE
     * Works online and offline (cached locally)
     */
    public void emailPasswordLogin(String email, String password, AuthCallback callback) {
        if (callback != null) callback.onLoading();
        
        Log.d(TAG, "🔥 Starting Firebase Email/Password Login: " + email);
        
        try {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "✅ Firebase Email/Password Login successful: " + user.getEmail());
                            
                            // Get Firebase ID token
                            user.getIdToken(false).addOnCompleteListener(tokenTask -> {
                                if (tokenTask.isSuccessful()) {
                                    String firebaseToken = tokenTask.getResult().getToken();
                                    
                                    // Cache user data locally
                                    cacheUserLocally(user, firebaseToken);
                                    
                                    // Sync to Neon backend
                                    syncToNeonBackend(user, firebaseToken);
                                    
                                    if (callback != null) {
                                        callback.onSuccess(user, firebaseToken);
                                    }
                                } else {
                                    if (callback != null) {
                                        callback.onError("Failed to get authentication token");
                                    }
                                }
                            });
                        }
                    } else {
                        String errorMessage = task.getException() != null ? 
                            task.getException().getMessage() : "Login failed";
                        Log.e(TAG, "❌ Firebase Email/Password Login failed: " + errorMessage);
                        if (callback != null) {
                            callback.onError(errorMessage);
                        }
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception during Email/Password Login: " + e.getMessage());
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        }
    }
    
    /**
     * 🔥 EMAIL/PASSWORD REGISTRATION WITH FIREBASE
     * Creates new user in Firebase
     */
    public void emailPasswordRegister(String email, String password, String firstName, 
                                     String lastName, AuthCallback callback) {
        if (callback != null) callback.onLoading();
        
        Log.d(TAG, "🔥 Starting Firebase Email/Password Registration: " + email);
        
        try {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "✅ Firebase Registration successful: " + user.getEmail());
                            
                            // Update user profile
                            com.google.firebase.auth.UserProfileChangeRequest profileUpdates = 
                                new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                    .setDisplayName(firstName + " " + lastName)
                                    .build();
                            
                            user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                                if (profileTask.isSuccessful()) {
                                    // Get Firebase ID token
                                    user.getIdToken(false).addOnCompleteListener(tokenTask -> {
                                        if (tokenTask.isSuccessful()) {
                                            String firebaseToken = tokenTask.getResult().getToken();
                                            
                                            // Cache user data locally
                                            cacheUserLocally(user, firebaseToken);
                                            
                                            // Sync to Neon backend
                                            syncToNeonBackend(user, firebaseToken);
                                            
                                            if (callback != null) {
                                                callback.onSuccess(user, firebaseToken);
                                            }
                                        } else {
                                            if (callback != null) {
                                                callback.onError("Failed to get authentication token");
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    } else {
                        String errorMessage = task.getException() != null ? 
                            task.getException().getMessage() : "Registration failed";
                        Log.e(TAG, "❌ Firebase Registration failed: " + errorMessage);
                        if (callback != null) {
                            callback.onError(errorMessage);
                        }
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception during Registration: " + e.getMessage());
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        }
    }
    
    /**
     * 💾 CACHE USER DATA LOCALLY FOR OFFLINE SUPPORT
     */
    private void cacheUserLocally(FirebaseUser user, String token) {
        try {
            preferencesManager.setLoggedIn(true);
            // Convert String userId to int for preferences
            try {
                int userIdInt = Integer.parseInt(user.getUid());
                preferencesManager.setUserId(userIdInt);
            } catch (NumberFormatException e) {
                Log.w(TAG, "⚠️ Could not parse userId as int: " + user.getUid());
            }
            preferencesManager.setEmail(user.getEmail());
            preferencesManager.setFirstName(user.getDisplayName() != null ? 
                user.getDisplayName().split(" ")[0] : "User");
            preferencesManager.setLastName(user.getDisplayName() != null && 
                user.getDisplayName().contains(" ") ? 
                user.getDisplayName().split(" ")[1] : "");
            preferencesManager.setFirebaseToken(token);
            preferencesManager.setUserRole("user"); // Default role
            
            Log.d(TAG, "✅ User data cached locally for offline support");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error caching user data: " + e.getMessage());
        }
    }
    
    // ❌ REMOVED: syncToNeonBackend() method (Pure online mode - sync handled in LoginActivity)
    
    /**
     * 🎯 REDIRECT TO PROFILE PICTURE SELECTION
     * Used after Google Sign-In (auto-filled name)
     * Passes isFirstTimeUser flag for tooltips/onboarding
     */
    private void redirectToProfilePictureSelection(String userId, String firstName, 
                                                   String lastName, boolean isGoogleSignIn) {
        try {
            boolean isFirstTimeUser = preferencesManager.isFirstTimeUser();
            
            android.content.Intent intent = new android.content.Intent(context, 
                com.example.blottermanagementsystem.ui.activities.ProfilePictureSelectionActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("firstName", firstName);
            intent.putExtra("lastName", lastName);
            intent.putExtra("isGoogleSignIn", isGoogleSignIn); // true = auto-filled, false = empty
            intent.putExtra("isFirstTimeUser", isFirstTimeUser); // true = show tooltips, false = skip
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | 
                android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            
            Log.d(TAG, "✅ Redirected to ProfilePictureSelectionActivity (isFirstTimeUser: " + isFirstTimeUser + ")");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error redirecting to PFP selection: " + e.getMessage());
        }
    }
    
    /**
     * 🔓 LOGOUT
     */
    public void logout() {
        try {
            firebaseAuth.signOut();
            preferencesManager.setLoggedIn(false);
            preferencesManager.clearUserData();
            Log.d(TAG, "✅ User logged out successfully");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error during logout: " + e.getMessage());
        }
    }
    
    /**
     * 📱 GET CURRENT USER
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
    
    /**
     * ✅ IS USER LOGGED IN
     */
    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }
    
    /**
     * 💾 GET CACHED USER (OFFLINE SUPPORT)
     */
    public boolean hasCachedUser() {
        return preferencesManager.isLoggedIn();
    }
}
