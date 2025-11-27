package com.example.blottermanagementsystem.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.blottermanagementsystem.MainActivity;
import com.example.blottermanagementsystem.R;

public class PushNotificationHelper {
    private static final String CHANNEL_ID_REPORTS = "blotter_reports";
    private static final String CHANNEL_ID_HEARINGS = "hearings";
    private static final String CHANNEL_ID_STATUS = "status_updates";
    
    private final Context context;
    private final NotificationManager notificationManager;
    
    public PushNotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannels();
    }
    
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel reportsChannel = new NotificationChannel(
                CHANNEL_ID_REPORTS, "Blotter Reports", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(reportsChannel);
            
            NotificationChannel hearingsChannel = new NotificationChannel(
                CHANNEL_ID_HEARINGS, "Hearings", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(hearingsChannel);
            
            NotificationChannel statusChannel = new NotificationChannel(
                CHANNEL_ID_STATUS, "Status Updates", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(statusChannel);
        }
    }
    
    public void sendReportNotification(String title, String message, int reportId) {
        // Send Android system notification ONLY
        // Database saving is handled by NotificationHelper
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("REPORT_ID", reportId);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, reportId, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_REPORTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("B.M.S • now")
            .setContentText(title)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(message)
                .setBigContentTitle(title))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);
        
        notificationManager.notify(reportId, builder.build());
        android.util.Log.d("PushNotificationHelper", "✅ Push notification sent");
    }
    
    public void sendHearingNotification(String title, String message, int hearingId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_HEARINGS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true);
        
        notificationManager.notify(hearingId, builder.build());
    }
}
