package com.example.blottermanagementsystem.utils;

import android.util.Log;
import retrofit2.Callback;
import java.util.concurrent.Callable;

/**
 * Generic API callback interface
 */
interface ApiCallback<T> {
    void onSuccess(T result);
    void onError(String error);
}

/**
 * ✅ RETRY MANAGER
 * Handles API calls with exponential backoff retry logic
 */
public class RetryManager {
    private static final String TAG = "RetryManager";
    private static final int MAX_RETRIES = 3;
    private static final long BASE_DELAY_MS = 1000; // 1 second
    
    /**
     * Execute operation with automatic retry on failure
     */
    public static <T> void executeWithRetry(
            Callable<T> operation,
            ApiCallback<T> callback,
            String operationName) {
        
        new Thread(() -> {
            for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                try {
                    Log.d(TAG, "🔄 Attempt " + attempt + "/" + MAX_RETRIES + " for: " + operationName);
                    T result = operation.call();
                    
                    // Success
                    Log.d(TAG, "✅ Success on attempt " + attempt + ": " + operationName);
                    callback.onSuccess(result);
                    return;
                    
                } catch (Exception e) {
                    Log.w(TAG, "❌ Attempt " + attempt + " failed: " + e.getMessage());
                    
                    if (attempt == MAX_RETRIES) {
                        // Final failure
                        String errorMsg = operationName + " failed after " + MAX_RETRIES + " attempts: " + e.getMessage();
                        Log.e(TAG, errorMsg);
                        callback.onError(errorMsg);
                    } else {
                        // Wait before retry (exponential backoff)
                        try {
                            long delay = BASE_DELAY_MS * (long) Math.pow(2, attempt - 1);
                            Log.d(TAG, "⏳ Waiting " + delay + "ms before retry...");
                            Thread.sleep(delay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            callback.onError("Operation interrupted");
                            return;
                        }
                    }
                }
            }
        }).start();
    }
    
    /**
     * Execute operation with custom retry count
     */
    public static <T> void executeWithRetry(
            Callable<T> operation,
            ApiCallback<T> callback,
            String operationName,
            int maxRetries) {
        
        new Thread(() -> {
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    Log.d(TAG, "🔄 Attempt " + attempt + "/" + maxRetries + " for: " + operationName);
                    T result = operation.call();
                    
                    Log.d(TAG, "✅ Success on attempt " + attempt + ": " + operationName);
                    callback.onSuccess(result);
                    return;
                    
                } catch (Exception e) {
                    Log.w(TAG, "❌ Attempt " + attempt + " failed: " + e.getMessage());
                    
                    if (attempt == maxRetries) {
                        String errorMsg = operationName + " failed after " + maxRetries + " attempts";
                        Log.e(TAG, errorMsg);
                        callback.onError(errorMsg);
                    } else {
                        try {
                            long delay = BASE_DELAY_MS * (long) Math.pow(2, attempt - 1);
                            Thread.sleep(delay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            callback.onError("Operation interrupted");
                            return;
                        }
                    }
                }
            }
        }).start();
    }
    
    /**
     * Retry with custom delay
     */
    public static <T> void executeWithRetry(
            Callable<T> operation,
            ApiCallback<T> callback,
            String operationName,
            int maxRetries,
            long delayMs) {
        
        new Thread(() -> {
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    Log.d(TAG, "🔄 Attempt " + attempt + "/" + maxRetries + " for: " + operationName);
                    T result = operation.call();
                    
                    Log.d(TAG, "✅ Success on attempt " + attempt + ": " + operationName);
                    callback.onSuccess(result);
                    return;
                    
                } catch (Exception e) {
                    Log.w(TAG, "❌ Attempt " + attempt + " failed: " + e.getMessage());
                    
                    if (attempt == maxRetries) {
                        callback.onError(operationName + " failed after " + maxRetries + " attempts");
                    } else {
                        try {
                            Thread.sleep(delayMs);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            callback.onError("Operation interrupted");
                            return;
                        }
                    }
                }
            }
        }).start();
    }
}
