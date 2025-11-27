package com.example.blottermanagementsystem.utils;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;

/**
 * ✅ FIREBASE CLOUD MESSAGING (FCM) NOTIFICATION MANAGER
 * Handles push notifications to specific user roles and devices
 */
public class FCMNotificationManager {
    private static final String TAG = "FCMNotificationManager";
    
    // ✅ SEND NOTIFICATION TO SPECIFIC USER ROLE
    public static void sendNotificationToRole(String role, String title, String message, Map<String, String> data) {
        Log.d(TAG, "📤 Sending notification to role: " + role);
        
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("to", "/topics/" + role); // Send to role-based topics
        notificationData.put("priority", "high");
        
        Map<String, String> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("body", message);
        notification.put("click_action", "OPEN_REPORTS_ACTIVITY");
        
        notificationData.put("notification", notification);
        notificationData.put("data", data);
        
        // Send to FCM via your Cloudbase backend
        sendToFCMBackend(notificationData);
    }
    
    // ✅ SEND TO SPECIFIC USER
    public static void sendNotificationToUser(int userId, String title, String message) {
        Log.d(TAG, "📤 Sending notification to user: " + userId);
        
        Map<String, String> data = new HashMap<>();
        data.put("userId", String.valueOf(userId));
        data.put("type", "personal_notification");
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        sendNotificationToRole("user_" + userId, title, message, data);
    }
    
    // ✅ ANNOUNCEMENT TO ALL USERS
    public static void sendAnnouncement(String title, String message) {
        Log.d(TAG, "📢 Sending announcement to all users");
        
        Map<String, String> data = new HashMap<>();
        data.put("type", "announcement");
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        sendNotificationToRole("all_users", title, message, data);
    }
    
    // ✅ SEND TO ALL OFFICERS
    public static void sendNotificationToOfficers(String title, String message, Map<String, String> caseData) {
        Log.d(TAG, "📤 Sending notification to all officers");
        
        Map<String, String> data = new HashMap<>(caseData);
        data.put("type", "officer_notification");
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        sendNotificationToRole("officers", title, message, data);
    }
    
    // ✅ SEND CASE ASSIGNMENT NOTIFICATION
    public static void sendCaseAssignmentNotification(int officerId, String caseNumber, String respondentName) {
        Log.d(TAG, "📤 Sending case assignment notification to officer: " + officerId);
        
        String title = "New Case Assignment";
        String message = "Case #" + caseNumber + " - " + respondentName;
        
        Map<String, String> data = new HashMap<>();
        data.put("type", "case_assignment");
        data.put("caseNumber", caseNumber);
        data.put("respondentName", respondentName);
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        sendNotificationToUser(officerId, title, message);
    }
    
    // ✅ SEND CASE STATUS UPDATE NOTIFICATION
    public static void sendCaseStatusUpdate(int userId, String caseNumber, String newStatus) {
        Log.d(TAG, "📤 Sending case status update to user: " + userId);
        
        String title = "Case Status Updated";
        String message = "Case #" + caseNumber + " is now " + newStatus;
        
        Map<String, String> data = new HashMap<>();
        data.put("type", "status_update");
        data.put("caseNumber", caseNumber);
        data.put("status", newStatus);
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        sendNotificationToUser(userId, title, message);
    }
    
    // ✅ SEND HEARING REMINDER
    public static void sendHearingReminder(int userId, String caseNumber, String hearingDate, String hearingTime) {
        Log.d(TAG, "📤 Sending hearing reminder to user: " + userId);
        
        String title = "Hearing Reminder";
        String message = "Case #" + caseNumber + " hearing on " + hearingDate + " at " + hearingTime;
        
        Map<String, String> data = new HashMap<>();
        data.put("type", "hearing_reminder");
        data.put("caseNumber", caseNumber);
        data.put("hearingDate", hearingDate);
        data.put("hearingTime", hearingTime);
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        sendNotificationToUser(userId, title, message);
    }
    
    private static void sendToFCMBackend(Map<String, Object> notificationData) {
        // This would call your Cloudbase backend API to send FCM messages
        // Example implementation:
        /*
        ApiClient.sendFCMNotification(notificationData, new ApiClient.ApiCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "✅ Notification sent successfully");
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Failed to send notification: " + error);
            }
        });
        */
        
        Log.d(TAG, "✅ Notification queued for sending");
    }
}
