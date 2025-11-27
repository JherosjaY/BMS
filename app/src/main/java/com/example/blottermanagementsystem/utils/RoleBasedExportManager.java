package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.util.Log;

/**
 * Role-Based Export Manager
 * Handles different export/notification methods based on user role:
 * - USER Role: PDF Export (summary view)
 * - OFFICER Role: SMS Notifications (field operations)
 * - ADMIN Role: Both PDF and SMS
 */
public class RoleBasedExportManager {
    
    private static final String TAG = "RoleBasedExportManager";
    
    public enum UserRole {
        USER,
        OFFICER,
        ADMIN
    }
    
    public interface ExportCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }
    
    private Context context;
    private UserRole userRole;
    private ComprehensivePdfGenerator.PdfGenerationCallback pdfCallback;
    private OfficerSmsNotificationManager.SmsCallback smsCallback;
    
    public RoleBasedExportManager(Context context, UserRole userRole) {
        this.context = context;
        this.userRole = userRole;
    }
    
    /**
     * Export case based on user role
     */
    public void exportCase(int reportId, ExportCallback callback) {
        Log.d(TAG, "Exporting case for role: " + userRole);
        
        switch (userRole) {
            case USER:
                // USER: Export as PDF (summary only)
                exportAsPdf(reportId, callback);
                break;
                
            case OFFICER:
                // OFFICER: Show SMS notification options
                if (callback != null) {
                    callback.onSuccess("SMS notification options available for this case");
                }
                break;
                
            case ADMIN:
                // ADMIN: Show both options
                if (callback != null) {
                    callback.onSuccess("Both PDF export and SMS notifications available");
                }
                break;
        }
    }
    
    /**
     * Export case as PDF (for USER and ADMIN roles)
     */
    public void exportAsPdf(int reportId, ExportCallback callback) {
        if (userRole == UserRole.OFFICER) {
            if (callback != null) {
                callback.onError("PDF export not available for Officer role. Use SMS notifications instead.");
            }
            return;
        }
        
        String roleForPdf = (userRole == UserRole.USER) ? "USER" : "ADMIN";
        
        ComprehensivePdfGenerator.generateComprehensivePdf(context, reportId, roleForPdf, 
            new ComprehensivePdfGenerator.PdfGenerationCallback() {
                @Override
                public void onSuccess(String filePath) {
                    Log.d(TAG, "✅ PDF exported successfully: " + filePath);
                    if (callback != null) {
                        callback.onSuccess("PDF exported to: " + filePath);
                    }
                }
                
                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "❌ PDF export failed: " + errorMessage);
                    if (callback != null) {
                        callback.onError("PDF export failed: " + errorMessage);
                    }
                }
            });
    }
    
    /**
     * Send SMS notification (for OFFICER and ADMIN roles)
     */
    public void sendSmsNotification(String phoneNumber, String caseNumber, String respondentName,
                                    String hearingDate, String hearingTime, ExportCallback callback) {
        if (userRole == UserRole.USER) {
            if (callback != null) {
                callback.onError("SMS notifications not available for User role");
            }
            return;
        }
        
        OfficerSmsNotificationManager smsManager = new OfficerSmsNotificationManager(context);
        
        if (!smsManager.hasSmsPermission()) {
            if (callback != null) {
                callback.onError("SMS permission not granted. Please enable SMS permission in settings.");
            }
            return;
        }
        
        smsManager.sendHearingNotice(phoneNumber, caseNumber, respondentName, hearingDate, hearingTime,
            new OfficerSmsNotificationManager.SmsCallback() {
                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "✅ SMS sent: " + message);
                    if (callback != null) {
                        callback.onSuccess(message);
                    }
                }
                
                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "❌ SMS failed: " + errorMessage);
                    if (callback != null) {
                        callback.onError(errorMessage);
                    }
                }
            });
    }
    
    /**
     * Send initial notice SMS
     */
    public void sendInitialNotice(String phoneNumber, String caseNumber, String respondentName,
                                  String accusation, ExportCallback callback) {
        if (userRole == UserRole.USER) {
            if (callback != null) {
                callback.onError("SMS notifications not available for User role");
            }
            return;
        }
        
        OfficerSmsNotificationManager smsManager = new OfficerSmsNotificationManager(context);
        smsManager.sendInitialNotice(phoneNumber, caseNumber, respondentName, accusation,
            new OfficerSmsNotificationManager.SmsCallback() {
                @Override
                public void onSuccess(String message) {
                    if (callback != null) callback.onSuccess(message);
                }
                
                @Override
                public void onError(String errorMessage) {
                    if (callback != null) callback.onError(errorMessage);
                }
            });
    }
    
    /**
     * Send resolution notification SMS
     */
    public void sendResolutionNotification(String phoneNumber, String caseNumber, String respondentName,
                                          String resolutionType, ExportCallback callback) {
        if (userRole == UserRole.USER) {
            if (callback != null) {
                callback.onError("SMS notifications not available for User role");
            }
            return;
        }
        
        OfficerSmsNotificationManager smsManager = new OfficerSmsNotificationManager(context);
        smsManager.sendResolutionNotification(phoneNumber, caseNumber, respondentName, resolutionType,
            new OfficerSmsNotificationManager.SmsCallback() {
                @Override
                public void onSuccess(String message) {
                    if (callback != null) callback.onSuccess(message);
                }
                
                @Override
                public void onError(String errorMessage) {
                    if (callback != null) callback.onError(errorMessage);
                }
            });
    }
    
    /**
     * Get available export options for current role
     */
    public String[] getAvailableExportOptions() {
        switch (userRole) {
            case USER:
                return new String[]{"📄 Export as PDF"};
            case OFFICER:
                return new String[]{"📱 Send SMS Notification", "📱 Send Hearing Notice", "📱 Send Resolution"};
            case ADMIN:
                return new String[]{"📄 Export as PDF", "📱 Send SMS Notification", "📱 Send Hearing Notice", "📱 Send Resolution"};
            default:
                return new String[]{};
        }
    }
}
