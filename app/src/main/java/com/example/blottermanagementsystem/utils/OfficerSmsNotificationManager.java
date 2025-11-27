package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.SmsNotification;

import java.util.concurrent.Executors;

/**
 * SMS Notification Manager for Officer Role
 * Sends SMS notifications to respondents, witnesses, and complainants
 * Validates Philippine phone numbers and handles multipart messages
 */
public class OfficerSmsNotificationManager {
    
    private static final String TAG = "OfficerSmsNotificationManager";
    private Context context;
    private BlotterDatabase database;
    
    public interface SmsCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }
    
    public OfficerSmsNotificationManager(Context context) {
        this.context = context;
        this.database = BlotterDatabase.getDatabase(context);
    }
    
    /**
     * Send hearing notice SMS to respondent
     */
    public void sendHearingNotice(String phoneNumber, String caseNumber, String respondentName, 
                                   String hearingDate, String hearingTime, SmsCallback callback) {
        String message = SmsHelper.generateHearingNotice(caseNumber, respondentName, hearingDate, hearingTime);
        sendSmsAndLog(phoneNumber, message, "HEARING_NOTICE", caseNumber, callback);
    }
    
    /**
     * Send initial notice SMS to respondent
     */
    public void sendInitialNotice(String phoneNumber, String caseNumber, String respondentName, 
                                   String accusation, SmsCallback callback) {
        String message = SmsHelper.generateInitialNotice(caseNumber, respondentName, accusation);
        sendSmsAndLog(phoneNumber, message, "INITIAL_NOTICE", caseNumber, callback);
    }
    
    /**
     * Send reminder SMS to respondent
     */
    public void sendReminder(String phoneNumber, String caseNumber, String respondentName, 
                             int daysRemaining, SmsCallback callback) {
        String message = SmsHelper.generateReminder(caseNumber, respondentName, daysRemaining);
        sendSmsAndLog(phoneNumber, message, "REMINDER", caseNumber, callback);
    }
    
    /**
     * Send custom SMS message
     */
    public void sendCustomMessage(String phoneNumber, String message, String messageType, 
                                   String caseNumber, SmsCallback callback) {
        sendSmsAndLog(phoneNumber, message, messageType, caseNumber, callback);
    }
    
    /**
     * Send case resolution notification
     */
    public void sendResolutionNotification(String phoneNumber, String caseNumber, String respondentName, 
                                           String resolutionType, SmsCallback callback) {
        String message = "CASE RESOLUTION\n\n" +
                        "Dear " + respondentName + ",\n\n" +
                        "Case #" + caseNumber + " has been resolved.\n\n" +
                        "Resolution: " + resolutionType + "\n\n" +
                        "For more details, please contact the Barangay Hall.\n\n" +
                        "- Barangay";
        sendSmsAndLog(phoneNumber, message, "RESOLUTION_NOTICE", caseNumber, callback);
    }
    
    /**
     * Core SMS sending method with logging
     */
    private void sendSmsAndLog(String phoneNumber, String message, String messageType, 
                               String caseNumber, SmsCallback callback) {
        // Validate on main thread first
        if (!SmsHelper.isValidPhilippineNumber(phoneNumber)) {
            String error = "Invalid Philippine phone number: " + phoneNumber;
            Log.e(TAG, error);
            if (callback != null) callback.onError(error);
            return;
        }
        
        // Format the phone number
        String formattedNumber = SmsHelper.formatPhilippineNumber(phoneNumber);
        
        // Send SMS on background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Check SMS permission
                if (!SmsHelper.hasSmsPermission(context)) {
                    String error = "SMS permission not granted";
                    Log.e(TAG, error);
                    if (callback != null) callback.onError(error);
                    return;
                }
                
                // Send SMS
                SmsManager smsManager = SmsManager.getDefault();
                
                if (message.length() > 160) {
                    smsManager.sendMultipartTextMessage(formattedNumber, null, 
                        smsManager.divideMessage(message), null, null);
                } else {
                    smsManager.sendTextMessage(formattedNumber, null, message, null, null);
                }
                
                Log.d(TAG, "✅ SMS sent successfully to " + formattedNumber);
                
                // Log to database
                logSmsToDatabase(caseNumber, formattedNumber, message, messageType, "SENT");
                
                if (callback != null) {
                    callback.onSuccess("SMS sent to " + formattedNumber);
                }
                
            } catch (Exception e) {
                String error = "Error sending SMS: " + e.getMessage();
                Log.e(TAG, error, e);
                
                // Log failed attempt to database
                logSmsToDatabase(caseNumber, formattedNumber, message, messageType, "FAILED");
                
                if (callback != null) callback.onError(error);
            }
        });
    }
    
    /**
     * Log SMS to database for record keeping
     * Note: We're logging without respondentId since we only have phone number
     * In a real system, you'd look up the respondent by phone number first
     */
    private void logSmsToDatabase(String caseNumber, String phoneNumber, String message, 
                                  String messageType, String status) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                if (database != null) {
                    // Create SMS notification with dummy respondentId (0) since we only have phone number
                    // In production, you'd look up respondentId from the database using phone number
                    int reportId = Integer.parseInt(caseNumber.replaceAll("[^0-9]", ""));
                    SmsNotification notification = new SmsNotification(0, reportId, messageType, message, phoneNumber);
                    notification.setDeliveryStatus(status);
                    
                    database.smsNotificationDao().insertSms(notification);
                    Log.d(TAG, "✅ SMS logged to database: " + messageType);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error logging SMS to database: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Check if SMS permission is granted
     */
    public boolean hasSmsPermission() {
        return SmsHelper.hasSmsPermission(context);
    }
}
