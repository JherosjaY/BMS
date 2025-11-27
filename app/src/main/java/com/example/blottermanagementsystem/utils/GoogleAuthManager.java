package com.example.blottermanagementsystem.utils;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.entity.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * ✅ GOOGLE AUTHENTICATION WITH FIREBASE
 * Handles Google Sign-In and Sign-Up with Neon database sync
 */
public class GoogleAuthManager {
    private static final String TAG = "GoogleAuthManager";
    public static final int RC_SIGN_IN = 9001;
    
    private GoogleSignInClient googleSignInClient;
    private Activity activity;
    private FirebaseAuth firebaseAuth;
    
    public GoogleAuthManager(Activity activity) {
        this.activity = activity;
        this.firebaseAuth = FirebaseAuth.getInstance();
        
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build();
            
        googleSignInClient = GoogleSignIn.getClient(activity, gso);
        
        Log.d(TAG, "✅ GoogleAuthManager initialized");
    }
    
    // ✅ SIGN IN WITH GOOGLE
    public void signInWithGoogle() {
        Log.d(TAG, "🔐 Starting Google Sign-In...");
        Intent signInIntent = googleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    
    // ✅ HANDLE SIGN-IN RESULT
    public void handleSignInResult(Intent data, AuthCallback callback) {
        try {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount account = task.getResult(ApiException.class);
            
            if (account != null) {
                Log.d(TAG, "✅ Google Sign-In successful: " + account.getEmail());
                // Authenticate with Firebase
                firebaseAuthWithGoogle(account, callback);
            }
            
        } catch (ApiException e) {
            Log.e(TAG, "❌ Google sign-in failed: " + e.getStatusCode());
            callback.onError("Google sign-in failed: " + e.getStatusCode());
        }
    }
    
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct, AuthCallback callback) {
        Log.d(TAG, "🔐 Authenticating with Firebase...");
        
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(activity, task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "✅ Firebase authentication successful");
                    
                    // ✅ SYNC USER TO NEON DATABASE
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        syncUserToNeonDatabase(firebaseUser, callback);
                    }
                } else {
                    Log.e(TAG, "❌ Firebase authentication failed: " + task.getException());
                    callback.onError("Authentication failed: " + task.getException().getMessage());
                }
            });
    }
    
    private void syncUserToNeonDatabase(FirebaseUser firebaseUser, AuthCallback callback) {
        Log.d(TAG, "🔄 Syncing user to Neon database...");
        
        User user = new User();
        user.setUsername(firebaseUser.getEmail());
        user.setEmail(firebaseUser.getEmail());
        user.setFirstName(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "");
        user.setProfilePhotoUri(firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "");
        user.setRole("User"); // Default role for new users
        user.setActive(true);
        
        // ✅ SYNC TO CLOUDBASE/NEON DATABASE
        // This would call your backend API
        /*
        ApiClient.syncUserToBackend(user, new ApiClient.ApiCallback<User>() {
            @Override
            public void onSuccess(User syncedUser) {
                Log.d(TAG, "✅ User synced to Neon database");
                
                // Save to local preferences
                PreferencesManager preferences = new PreferencesManager(activity);
                preferences.setLoggedIn(true);
                preferences.setUserId(syncedUser.getId());
                preferences.setUserRole(syncedUser.getRole());
                preferences.setFirstName(syncedUser.getFirstName());
                preferences.setEmail(syncedUser.getEmail());
                
                callback.onSuccess(syncedUser);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ User sync failed: " + error);
                callback.onError("User sync failed: " + error);
            }
        });
        */
        
        // For now, save locally
        PreferencesManager preferences = new PreferencesManager(activity);
        preferences.setLoggedIn(true);
        preferences.setFirstName(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "");
        
        callback.onSuccess(user);
    }
    
    // ✅ SIGN OUT
    public void signOut(SignOutCallback callback) {
        Log.d(TAG, "🚪 Signing out...");
        
        firebaseAuth.signOut();
        googleSignInClient.signOut().addOnCompleteListener(activity, task -> {
            Log.d(TAG, "✅ Sign out successful");
            callback.onSuccess();
        });
    }
    
    // ✅ GET CURRENT USER
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
    
    // ✅ IS USER LOGGED IN
    public boolean isLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }
    
    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String errorMessage);
    }
    
    public interface SignOutCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
}
