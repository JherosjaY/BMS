package com.example.blottermanagementsystem.utils;

import android.util.Log;

/**
 * ✅ ENHANCED API CALLBACK
 * Provides comprehensive error handling for API calls
 */
public interface EnhancedApiCallback<T> {
    
    void onSuccess(T result);
    
    void onError(String error);
    
    // ✅ NETWORK ERROR HANDLING
    default void onNetworkError(boolean isOnline) {
        Log.w("ApiCallback", "Network error - Online: " + isOnline);
        onError("Network error - " + (isOnline ? "Connection failed" : "No internet connection"));
    }
    
    // ✅ AUTHENTICATION ERROR HANDLING
    default void onAuthError(boolean tokenExpired) {
        Log.w("ApiCallback", "Auth error - Token expired: " + tokenExpired);
        if (tokenExpired) {
            onError("Session expired - Please login again");
        } else {
            onError("Authentication failed");
        }
    }
    
    // ✅ SERVER ERROR HANDLING
    default void onServerError(int statusCode, String message) {
        Log.e("ApiCallback", "Server error " + statusCode + ": " + message);
        
        String errorMsg;
        switch (statusCode) {
            case 400:
                errorMsg = "Bad request - Invalid data";
                break;
            case 401:
                errorMsg = "Unauthorized - Please login";
                break;
            case 403:
                errorMsg = "Forbidden - Access denied";
                break;
            case 404:
                errorMsg = "Not found - Resource doesn't exist";
                break;
            case 500:
                errorMsg = "Server error - Please try again";
                break;
            case 502:
            case 503:
                errorMsg = "Server temporarily unavailable";
                break;
            default:
                errorMsg = "Server error: " + statusCode;
        }
        
        onError(errorMsg);
    }
    
    // ✅ TIMEOUT ERROR HANDLING
    default void onTimeout() {
        Log.w("ApiCallback", "Request timeout");
        onError("Request timeout - Please check your connection");
    }
    
    // ✅ VALIDATION ERROR HANDLING
    default void onValidationError(String fieldName, String message) {
        Log.w("ApiCallback", "Validation error on " + fieldName + ": " + message);
        onError("Validation error: " + message);
    }
}
