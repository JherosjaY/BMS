package com.example.blottermanagementsystem.managers;

import android.content.Context;
import android.util.Log;
import com.example.blottermanagementsystem.utils.ApiClient;
import java.util.HashMap;
import java.util.Map;

/**
 * ✅ ENHANCED ADMIN MANAGER
 * Handles officer creation with automatic email credential sending
 */
public class EnhancedAdminManager {
    private static final String TAG = "EnhancedAdminManager";
    private Context context;
    private EmailServiceManager emailManager;
    
    public EnhancedAdminManager(Context context) {
        this.context = context;
        this.emailManager = new EmailServiceManager(context);
    }
    
    /**
     * Create officer with auto-generated credentials and auto-send email
     */
    public void createOfficerWithAutoEmail(String name, String email, String department, 
                                          String role, AdminCallback callback) {
        Log.d(TAG, "👮 Creating officer with auto-email: " + email);
        
        // Step 1: Auto-generate temporary password
        String tempPassword = emailManager.generateTempPassword();
        
        // Step 2: Prepare officer data
        Map<String, Object> officerData = new HashMap<>();
        officerData.put("name", name);
        officerData.put("email", email);
        officerData.put("password", tempPassword);
        officerData.put("department", department);
        officerData.put("role", role != null ? role : "officer");
        officerData.put("force_password_change", true);
        
        // Step 3: Create officer in database
        ApiClient.createOfficer(officerData, new ApiClient.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "✅ Officer created in database");
                
                // Step 4: Auto-send credentials via email
                emailManager.sendOfficerCredentialsEmail(email, name, tempPassword, 
                    new EmailServiceManager.ApiCallback<String>() {
                        @Override
                        public void onSuccess(String emailResult) {
                            Log.d(TAG, "✅ Officer credentials email sent automatically");
                            
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", true);
                            response.put("officer_email", email);
                            response.put("email_sent", true);
                            response.put("message", "Officer created and credentials sent automatically");
                            
                            callback.onSuccess(response);
                        }
                        
                        @Override
                        public void onError(String emailError) {
                            Log.w(TAG, "⚠️ Officer created but email failed: " + emailError);
                            
                            // Officer created successfully, but email failed
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", true);
                            response.put("officer_email", email);
                            response.put("email_sent", false);
                            response.put("temp_password", tempPassword); // Show to admin
                            response.put("email_error", emailError);
                            response.put("message", "Officer created but email failed - share credentials manually");
                            
                            callback.onSuccess(response);
                        }
                    });
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Officer creation failed: " + error);
                callback.onError("Officer creation failed: " + error);
            }
        });
    }
    
    /**
     * Create multiple officers with auto-email
     */
    public void createMultipleOfficers(java.util.List<Map<String, String>> officersList, 
                                      AdminCallback callback) {
        Log.d(TAG, "👮👮 Creating " + officersList.size() + " officers...");
        
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger failureCount = new java.util.concurrent.atomic.AtomicInteger(0);
        final int totalOfficers = officersList.size();
        
        for (Map<String, String> officerData : officersList) {
            String name = officerData.get("name");
            String email = officerData.get("email");
            String department = officerData.get("department");
            String role = officerData.get("role");
            
            createOfficerWithAutoEmail(name, email, department, role, new AdminCallback() {
                @Override
                public void onSuccess(Map<String, Object> result) {
                    int success = successCount.incrementAndGet();
                    Log.d(TAG, "✅ Officer " + success + "/" + totalOfficers + " created");
                    
                    if (success + failureCount.get() == totalOfficers) {
                        Map<String, Object> finalResult = new HashMap<>();
                        finalResult.put("total_officers", totalOfficers);
                        finalResult.put("success_count", success);
                        finalResult.put("failure_count", failureCount.get());
                        callback.onSuccess(finalResult);
                    }
                }
                
                @Override
                public void onError(String error) {
                    int failure = failureCount.incrementAndGet();
                    Log.e(TAG, "❌ Officer creation failed: " + error);
                    
                    if (successCount.get() + failure == totalOfficers) {
                        Map<String, Object> finalResult = new HashMap<>();
                        finalResult.put("total_officers", totalOfficers);
                        finalResult.put("success_count", successCount.get());
                        finalResult.put("failure_count", failure);
                        callback.onSuccess(finalResult);
                    }
                }
            });
        }
    }
    
    /**
     * Resend officer credentials email
     */
    public void resendOfficerCredentials(String email, String name, String tempPassword, 
                                        AdminCallback callback) {
        Log.d(TAG, "📧 Resending credentials to: " + email);
        
        emailManager.sendOfficerCredentialsEmail(email, name, tempPassword, 
            new EmailServiceManager.ApiCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    Log.d(TAG, "✅ Credentials resent to " + email);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "Credentials resent to " + email);
                    
                    callback.onSuccess(response);
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Failed to resend credentials: " + error);
                    callback.onError("Failed to resend credentials: " + error);
                }
            });
    }
    
    /**
     * Get all officers
     */
    public void getAllOfficers(AdminCallback callback) {
        Log.d(TAG, "🔍 Getting all officers...");
        
        ApiClient.getAllOfficers(new ApiClient.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "✅ Officers retrieved");
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", result);
                
                callback.onSuccess(response);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Failed to get officers: " + error);
                callback.onError("Failed to get officers: " + error);
            }
        });
    }
    
    /**
     * Update officer information
     */
    public void updateOfficer(int officerId, Map<String, Object> officerData, 
                             AdminCallback callback) {
        Log.d(TAG, "✏️ Updating officer: " + officerId);
        
        ApiClient.updateOfficer(officerId, officerData, new ApiClient.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "✅ Officer updated successfully");
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", result);
                
                callback.onSuccess(response);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Failed to update officer: " + error);
                callback.onError("Failed to update officer: " + error);
            }
        });
    }
    
    /**
     * Delete officer
     */
    public void deleteOfficer(int officerId, AdminCallback callback) {
        Log.d(TAG, "🗑️ Deleting officer: " + officerId);
        
        ApiClient.deleteOfficer(officerId, new ApiClient.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "✅ Officer deleted successfully");
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", result);
                
                callback.onSuccess(response);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Failed to delete officer: " + error);
                callback.onError("Failed to delete officer: " + error);
            }
        });
    }
    
    /**
     * Callback interface
     */
    public interface AdminCallback {
        void onSuccess(Map<String, Object> result);
        void onError(String error);
    }
}
