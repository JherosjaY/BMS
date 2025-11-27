package com.example.blottermanagementsystem.utils;

import android.content.Context;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.Notification;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * NotificationHelper - SYNCED WITH KOTLIN VERSION
 * Helper class to trigger notifications and activity logs together
 * This ensures realtime updates across the app
 * 
 * COMPLETE NOTIFICATION FLOW:
 * 1. User files case → Admin gets notified
 * 2. Admin assigns officer → Officer gets notified
 * 3. Officer updates case → User gets notified
 * 4. Status changes → All parties notified
 */
public class NotificationHelper {
    private final BlotterDatabase database;
    private final PushNotificationManager pushNotificationManager;
    private final Context context;
    
    public NotificationHelper(Context context) {
        this.context = context;
        this.database = BlotterDatabase.getDatabase(context);
        this.pushNotificationManager = new PushNotificationManager(context);
    }
    
    // ==================== NEW REPORT FILED ====================
    
    /**
     * Notify when a new report is filed
     * Notifies: User who filed it + ALL Admins (cross-role notification)
     * SYNCED WITH KOTLIN: Notifications go to multiple roles
     */
    public void notifyNewReport(int userWhoFiledId, String caseNumber, String reportedBy, 
                               int reportId, String performedBy) {
        Executors.newSingleThreadExecutor().execute(() -> {
            android.util.Log.d("NotificationHelper", "🔔 NEW REPORT FILED - Cross-role notification");
            
            // 1. Notify the USER who filed the report (confirmation)
            Notification userNotification = new Notification(
                userWhoFiledId,
                "Report Submitted",
                "Your report " + caseNumber + " has been filed successfully",
                "NEW_REPORT"
            );
            userNotification.setCaseId(reportId);
            long userNotifId = database.notificationDao().insertNotification(userNotification);
            android.util.Log.d("NotificationHelper", "✅ User notification saved: " + userNotifId);
            
            // 2. Notify ALL ADMINS about the new report
            List<com.example.blottermanagementsystem.data.entity.User> admins = 
                database.userDao().getUsersByRole("Admin");
            
            android.util.Log.d("NotificationHelper", "Found " + admins.size() + " admins to notify");
            
            for (com.example.blottermanagementsystem.data.entity.User admin : admins) {
                Notification adminNotification = new Notification(
                    admin.getId(),
                    "New Report Filed",
                    reportedBy + " filed a new report: " + caseNumber,
                    "NEW_REPORT"
                );
                adminNotification.setCaseId(reportId);
                long adminNotifId = database.notificationDao().insertNotification(adminNotification);
                android.util.Log.d("NotificationHelper", "✅ Admin notification saved for " + admin.getUsername() + ": " + adminNotifId);
            }
            
            // Show push notification
            pushNotificationManager.notifyNewReport(caseNumber, reportedBy, reportId);
            
            android.util.Log.d("NotificationHelper", "🎉 Cross-role notifications complete!");
        });
    }
    
    // ==================== STATUS CHANGE ====================
    
    /**
     * Notify when report status changes
     * Notifies: Report owner (user who filed it) + ALL Admins
     */
    public void notifyStatusChange(int userId, String caseNumber, String oldStatus, 
                                   String newStatus, int reportId, String performedBy) {
        Executors.newSingleThreadExecutor().execute(() -> {
            android.util.Log.d("NotificationHelper", "🔔 STATUS CHANGE - Cross-role notification");
            
            // 1. Notify the USER who filed the report
            Notification userNotification = new Notification(
                userId,
                "Report Status Updated",
                "Case " + caseNumber + " status changed from " + oldStatus + " to " + newStatus + " by " + performedBy,
                "STATUS_UPDATE"
            );
            userNotification.setCaseId(reportId);
            database.notificationDao().insertNotification(userNotification);
            
            // 2. Notify ALL ADMINS about the status change
            List<com.example.blottermanagementsystem.data.entity.User> admins = 
                database.userDao().getUsersByRole("Admin");
            
            for (com.example.blottermanagementsystem.data.entity.User admin : admins) {
                Notification adminNotification = new Notification(
                    admin.getId(),
                    "Case Status Updated",
                    "Officer " + performedBy + " updated case " + caseNumber + " from " + oldStatus + " to " + newStatus,
                    "OFFICER_STATUS_UPDATE"
                );
                adminNotification.setCaseId(reportId);
                database.notificationDao().insertNotification(adminNotification);
                
                android.util.Log.d("NotificationHelper", "✅ Admin " + admin.getFirstName() + " notified");
            }
            
            // Show push notification
            pushNotificationManager.notifyStatusChange(caseNumber, newStatus, reportId);
            
        });
    }
    
    // ==================== OFFICER ASSIGNMENT ====================
    
    /**
     * Notify when officer is assigned to a case
     * Notifies: Officer (assigned), Admin (who assigned)
     */
    public void notifyOfficerAssignment(int officerUserId, int adminUserId, String caseNumber,
                                       String officerName, int reportId, String performedBy) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Notify the officer
            Notification officerNotification = new Notification(
                officerUserId,
                "New Case Assigned",
                "You have been assigned to case " + caseNumber,
                "CASE_ASSIGNED"
            );
            officerNotification.setCaseId(reportId);
            database.notificationDao().insertNotification(officerNotification);
            
            // Show push notification to officer
            pushNotificationManager.notifyCaseAssignment(caseNumber, reportId);
            
            // Notify admin
            Notification adminNotification = new Notification(
                adminUserId,
                "Officer Assigned",
                "Officer " + officerName + " has been assigned to case " + caseNumber,
                "OFFICER_ASSIGNED"
            );
            adminNotification.setCaseId(reportId);
            database.notificationDao().insertNotification(adminNotification);
            
        });
    }
    
    // ==================== HEARING SCHEDULED ====================
    
    /**
     * Notify when hearing is scheduled
     * Notifies: Multiple users (complainant, respondent, officer)
     */
    public void notifyHearingScheduled(List<Integer> userIds, String caseNumber, 
                                      String hearingDate, int reportId, String performedBy) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Create notifications for all relevant users
            for (int userId : userIds) {
                Notification notification = new Notification(
                    userId,
                    "Hearing Scheduled",
                    "A hearing for case " + caseNumber + " has been scheduled on " + hearingDate,
                    "HEARING_SCHEDULED"
                );
                notification.setCaseId(reportId);
                database.notificationDao().insertNotification(notification);
                
                // Show push notification
                pushNotificationManager.notifyHearingScheduled(caseNumber, hearingDate, reportId);
            }
            
        });
    }
    
    // ==================== CASE RESOLVED ====================
    
    /**
     * Notify when case is resolved
     * Notifies: Complainant, Officer, Admin
     */
    public void notifyCaseResolved(List<Integer> userIds, String caseNumber, 
                                  String resolutionType, int reportId, String performedBy) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Create notifications for all relevant users
            for (int userId : userIds) {
                Notification notification = new Notification(
                    userId,
                    "Case Resolved",
                    "Case " + caseNumber + " has been resolved: " + resolutionType,
                    "CASE_RESOLVED"
                );
                notification.setCaseId(reportId);
                database.notificationDao().insertNotification(notification);
                
                // Show push notification
                pushNotificationManager.notifyCaseResolved(caseNumber, resolutionType, reportId);
            }
            
        });
    }
    
    // ==================== EVIDENCE ADDED ====================
    
    /**
     * Notify when evidence is added
     * Notifies: Admin, Officer
     */
    public void notifyEvidenceAdded(int officerUserId, int adminUserId, String caseNumber,
                                   String evidenceType, int reportId, String performedBy) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Notify admin
            Notification notification = new Notification(
                adminUserId,
                "Evidence Added",
                "New evidence (" + evidenceType + ") added to case " + caseNumber,
                "EVIDENCE_ADDED"
            );
            notification.setCaseId(reportId);
            database.notificationDao().insertNotification(notification);
            
            // Show push notification
            pushNotificationManager.notifyEvidenceAdded(caseNumber, evidenceType, reportId);
            
        });
    }
    
    // ==================== WITNESS ADDED ====================
    
    /**
     * Notify when witness is added
     * Notifies: Admin
     */
    public void notifyWitnessAdded(int adminUserId, String caseNumber, String witnessName,
                                  int reportId, String performedBy) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Notify admin
            Notification notification = new Notification(
                adminUserId,
                "Witness Added",
                "Witness " + witnessName + " added to case " + caseNumber,
                "WITNESS_ADDED"
            );
            notification.setCaseId(reportId);
            database.notificationDao().insertNotification(notification);
            
        });
    }
    
    // ==================== SUSPECT ADDED ====================
    
    /**
     * Notify when suspect is added
     * Notifies: Admin
     */
    public void notifySuspectAdded(int adminUserId, String caseNumber, String suspectName,
                                  int reportId, String performedBy) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Notify admin
            Notification notification = new Notification(
                adminUserId,
                "Suspect Added",
                "Suspect " + suspectName + " added to case " + caseNumber,
                "SUSPECT_ADDED"
            );
            notification.setCaseId(reportId);
            database.notificationDao().insertNotification(notification);
            
        });
    }
    
    // ==================== CASE UPDATE (OFFICER) ====================
    
    /**
     * Notify when officer updates a case
     * Notifies: User (who filed the report), Admin
     */
    public void notifyCaseUpdate(int userId, int adminUserId, String caseNumber,
                                String updateDescription, int reportId, String performedBy) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Notify user who filed the report
            Notification userNotification = new Notification(
                userId,
                "Case Update",
                "Case " + caseNumber + " has been updated: " + updateDescription,
                "CASE_UPDATE"
            );
            userNotification.setCaseId(reportId);
            database.notificationDao().insertNotification(userNotification);
            
            // Notify admin
            Notification adminNotification = new Notification(
                adminUserId,
                "Case Update",
                "Officer updated case " + caseNumber + ": " + updateDescription,
                "CASE_UPDATE"
            );
            adminNotification.setCaseId(reportId);
            database.notificationDao().insertNotification(adminNotification);
            
            // Show push notification
            pushNotificationManager.notifyCaseUpdate(caseNumber, updateDescription, reportId);
            
        });
    }
}
