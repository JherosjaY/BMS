package com.example.blottermanagementsystem.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.Notification;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class NotificationViewModel extends AndroidViewModel {
    private final BlotterDatabase database;
    private final PreferencesManager preferencesManager;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<Notification>> notifications = new MutableLiveData<>();
    private final MutableLiveData<Integer> unreadCount = new MutableLiveData<>(0);
    
    public NotificationViewModel(@NonNull Application application) {
        super(application);
        database = BlotterDatabase.getDatabase(application);
        preferencesManager = new PreferencesManager(application);
        loadNotifications();
    }
    
    private void loadNotifications() {
        int userId = preferencesManager.getUserId();
        
        executorService.execute(() -> {
            List<Notification> notifList = database.notificationDao()
                .getNotificationsByUserId(userId);
            notifications.postValue(notifList);
            
            int unread = (int) notifList.stream().filter(n -> !n.isRead()).count();
            unreadCount.postValue(unread);
        });
    }
    
    public void markAsRead(Notification notification) {
        executorService.execute(() -> {
            notification.setRead(true);
            database.notificationDao().updateNotification(notification);
            loadNotifications();
        });
    }
    
    public void markAllAsRead() {
        int userId = preferencesManager.getUserId();
        
        executorService.execute(() -> {
            database.notificationDao().markAllAsRead(userId);
            loadNotifications();
        });
    }
    
    public LiveData<List<Notification>> getNotifications() {
        return notifications;
    }
    
    public LiveData<Integer> getUnreadCount() {
        return unreadCount;
    }
    
    // ✅ LIFECYCLE MANAGEMENT - Prevent memory leaks
    @Override
    protected void onCleared() {
        super.onCleared();
        // Shutdown executor service gracefully
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            android.util.Log.d("NotificationViewModel", "✅ NotificationViewModel cleared - executor service shutdown");
        }
    }
}
