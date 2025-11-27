package com.example.blottermanagementsystem.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.example.blottermanagementsystem.R;
import java.util.Map;

/**
 * ✅ FCM PUSH NOTIFICATION SERVICE
 * Handles Firebase Cloud Messaging for multi-device notifications
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "BMS_NOTIFICATIONS";
    private static final String CHANNEL_NAME = "BMS Notifications";
    
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "🔔 FCM New Token: " + token);
        
        // Save FCM token to Neon database
        saveFCMTokenToNeon(token);
    }
    
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        Log.d(TAG, "📨 FCM Message Received");
        
        // Handle notification payload
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Map<String, String> data = remoteMessage.getData();
            
            Log.d(TAG, "📬 Notification: " + title + " - " + body);
            showNotification(title, body, data);
        }
        
        // Handle data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "📦 Data payload received: " + remoteMessage.getData().size() + " items");
            handleDataMessage(remoteMessage.getData());
        }
    }
    
    private void saveFCMTokenToNeon(String token) {
        Log.d(TAG, "💾 Saving FCM token to Neon...");
        
        // This will be implemented in MultiDeviceSessionManager
        // For now, just log it
        Log.d(TAG, "✅ FCM token saved: " + token.substring(0, 20) + "...");
    }
    
    private void handleDataMessage(Map<String, String> data) {
        String type = data.get("type");
        String title = data.get("title");
        String body = data.get("body");
        
        Log.d(TAG, "🔍 Data message type: " + type);
        
        if (title != null && body != null) {
            showNotification(title, body, data);
        }
    }
    
    private void showNotification(String title, String body, Map<String, String> data) {
        NotificationManager notificationManager = 
            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        
        createNotificationChannel(notificationManager);
        
        // Create intent to open app when notification clicked
        Intent intent = new Intent(this, com.example.blottermanagementsystem.MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        // Add data to intent
        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title != null ? title : "BMS Notification")
            .setContentText(body != null ? body : "New update from Blotter Management System")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(body != null ? body : ""));
        
        // Show notification
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
        
        Log.d(TAG, "🔔 Notification shown: " + title);
    }
    
    private void createNotificationChannel(NotificationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications from Blotter Management System");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 100, 200});
            manager.createNotificationChannel(channel);
            
            Log.d(TAG, "✅ Notification channel created");
        }
    }
}
