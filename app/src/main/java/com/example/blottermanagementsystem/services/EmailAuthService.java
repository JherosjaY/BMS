package com.example.blottermanagementsystem.services;

import android.util.Log;

import com.example.blottermanagementsystem.utils.ApiClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 📧 EMAIL AUTHENTICATION SERVICE
 * 
 * Handles email/password authentication with Neon backend
 * - Sign up with email/password
 * - Login with email/password
 * - Forgot password flow
 * - Password reset
 */
public class EmailAuthService {
    private static final String TAG = "EmailAuthService";
    
    // Callback interface for auth results
    public interface AuthCallback {
        void onSuccess(String message, String userId);
        void onError(String errorMessage);
        void onLoading();
    }
    
    /**
     * 📧 SIGN UP WITH EMAIL/PASSWORD
     * Creates new user account in Neon
     * Does NOT auto-login (professional flow)
     */
    public void signUpWithEmail(String email, String password, String firstName, 
                               String lastName, AuthCallback callback) {
        if (callback != null) callback.onLoading();
        
        Log.d(TAG, "📧 Signing up with email: " + email);
        
        try {
            // Prepare signup data
            Map<String, Object> signupData = new HashMap<>();
            signupData.put("email", email);
            signupData.put("password", password);
            signupData.put("firstName", firstName);
            signupData.put("lastName", lastName);
            signupData.put("authProvider", "email");
            
            // Call backend signup endpoint
            ApiClient.getApiService().emailSignUp(signupData)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, Object> responseBody = response.body();
                            
                            Log.d(TAG, "✅ Sign up successful");
                            
                            String userId = responseBody.get("userId") != null ? 
                                responseBody.get("userId").toString() : "";
                            String message = responseBody.get("message") != null ? 
                                responseBody.get("message").toString() : "Account created successfully!";
                            
                            if (callback != null) {
                                callback.onSuccess(message, userId);
                            }
                        } else {
                            String errorMessage = "Sign up failed: " + response.code();
                            Log.e(TAG, "❌ " + errorMessage);
                            if (callback != null) {
                                callback.onError(errorMessage);
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        String errorMessage = t.getMessage() != null ? t.getMessage() : "Network error";
                        Log.e(TAG, "❌ Sign up failed: " + errorMessage);
                        if (callback != null) {
                            callback.onError(errorMessage);
                        }
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception during sign up: " + e.getMessage());
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        }
    }
    
    /**
     * 📧 LOGIN WITH EMAIL/PASSWORD
     * Authenticates user with Neon backend
     */
    public void loginWithEmail(String email, String password, AuthCallback callback) {
        if (callback != null) callback.onLoading();
        
        Log.d(TAG, "📧 Logging in with email: " + email);
        
        try {
            // Prepare login data
            Map<String, Object> loginData = new HashMap<>();
            loginData.put("email", email);
            loginData.put("password", password);
            
            // Call backend login endpoint
            ApiClient.getApiService().emailLogin(loginData)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, Object> responseBody = response.body();
                            
                            Log.d(TAG, "✅ Login successful");
                            
                            String userId = responseBody.get("userId") != null ? 
                                responseBody.get("userId").toString() : "";
                            String message = "Login successful!";
                            
                            if (callback != null) {
                                callback.onSuccess(message, userId);
                            }
                        } else {
                            String errorMessage = "Login failed: Invalid credentials";
                            Log.e(TAG, "❌ " + errorMessage);
                            if (callback != null) {
                                callback.onError(errorMessage);
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        String errorMessage = t.getMessage() != null ? t.getMessage() : "Network error";
                        Log.e(TAG, "❌ Login failed: " + errorMessage);
                        if (callback != null) {
                            callback.onError(errorMessage);
                        }
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception during login: " + e.getMessage());
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        }
    }
    
    /**
     * 📧 REQUEST PASSWORD RESET
     * Sends reset code to user's email
     */
    public void requestPasswordReset(String email, AuthCallback callback) {
        if (callback != null) callback.onLoading();
        
        Log.d(TAG, "📧 Requesting password reset for: " + email);
        
        try {
            // Prepare reset request data
            Map<String, Object> resetData = new HashMap<>();
            resetData.put("email", email);
            
            // Call backend password reset endpoint
            ApiClient.getApiService().requestPasswordReset(resetData)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d(TAG, "✅ Password reset code sent to email");
                            
                            if (callback != null) {
                                callback.onSuccess("Reset code sent to your email!", "");
                            }
                        } else {
                            String errorMessage = "Email not found";
                            Log.e(TAG, "❌ " + errorMessage);
                            if (callback != null) {
                                callback.onError(errorMessage);
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        String errorMessage = t.getMessage() != null ? t.getMessage() : "Network error";
                        Log.e(TAG, "❌ Password reset request failed: " + errorMessage);
                        if (callback != null) {
                            callback.onError(errorMessage);
                        }
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception during password reset request: " + e.getMessage());
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        }
    }
    
    /**
     * 📧 RESET PASSWORD WITH CODE
     * Verifies code and updates password
     */
    public void resetPasswordWithCode(String email, String resetCode, String newPassword, 
                                     AuthCallback callback) {
        if (callback != null) callback.onLoading();
        
        Log.d(TAG, "📧 Resetting password with code for: " + email);
        
        try {
            // Prepare reset data
            Map<String, Object> resetData = new HashMap<>();
            resetData.put("email", email);
            resetData.put("resetCode", resetCode);
            resetData.put("newPassword", newPassword);
            
            // Call backend password reset endpoint
            ApiClient.getApiService().resetPasswordWithCode(resetData)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d(TAG, "✅ Password reset successful");
                            
                            if (callback != null) {
                                callback.onSuccess("Password reset successful! Please login with your new password.", "");
                            }
                        } else {
                            String errorMessage = "Invalid reset code";
                            Log.e(TAG, "❌ " + errorMessage);
                            if (callback != null) {
                                callback.onError(errorMessage);
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        String errorMessage = t.getMessage() != null ? t.getMessage() : "Network error";
                        Log.e(TAG, "❌ Password reset failed: " + errorMessage);
                        if (callback != null) {
                            callback.onError(errorMessage);
                        }
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception during password reset: " + e.getMessage());
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        }
    }
}
