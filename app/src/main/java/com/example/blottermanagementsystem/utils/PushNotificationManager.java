package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.util.Log;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.Notification;
import java.util.concurrent.Executors;

/**
 * PushNotificationManager - SYNCED WITH KOTLIN VERSION
 * Handles push notifications display (system tray notifications)
 */
public class PushNotificationManager {
    private static final String TAG = "PushNotificationManager";
    private final Context context;
    private final PushNotificationHelper notificationHelper;
    
    public PushNotificationManager(Context context) {
        this.context = context;
        this.notificationHelper = new PushNotificationHelper(context);
    }
    
    // ==================== NEW REPORT ====================
    
    public void notifyNewReport(String caseNumber, String reportedBy, int reportId) {
        String title = "New Report Filed";
        String message = reportedBy + " filed a new report: " + caseNumber;
        notificationHelper.sendReportNotification(title, message, reportId);
    }
    
    // ==================== STATUS CHANGE ====================
    
    public void notifyStatusChange(String caseNumber, String newStatus, int reportId) {
        String title = "Status Update";
        String message = "Case " + caseNumber + " status changed to: " + newStatus;
        notificationHelper.sendReportNotification(title, message, reportId);
    }
    
    // ==================== HEARING SCHEDULED ====================
    
    public void notifyHearingScheduled(String caseNumber, String date, int reportId) {
        String title = "Hearing Scheduled";
        String message = "Hearing for " + caseNumber + " on " + date;
        notificationHelper.sendHearingNotification(title, message, reportId);
    }
    
    // ==================== CASE ASSIGNMENT ====================
    
    public void notifyCaseAssignment(String caseNumber, int reportId) {
        String title = "Case Assigned";
        String message = "You have been assigned to case: " + caseNumber;
        notificationHelper.sendReportNotification(title, message, reportId);
    }
    
    // ==================== CASE RESOLVED ====================
    
    public void notifyCaseResolved(String caseNumber, String resolutionType, int reportId) {
        String title = "Case Resolved";
        String message = "Case " + caseNumber + " has been resolved: " + resolutionType;
        notificationHelper.sendReportNotification(title, message, reportId);
    }
    
    // ==================== EVIDENCE ADDED ====================
    
    public void notifyEvidenceAdded(String caseNumber, String evidenceType, int reportId) {
        String title = "Evidence Added";
        String message = "New evidence (" + evidenceType + ") added to case " + caseNumber;
        notificationHelper.sendReportNotification(title, message, reportId);
    }
    
    // ==================== CASE UPDATE ====================
    
    public void notifyCaseUpdate(String caseNumber, String updateDescription, int reportId) {
        String title = "Case Update";
        String message = "Case " + caseNumber + " has been updated: " + updateDescription;
        notificationHelper.sendReportNotification(title, message, reportId);
    }
}
