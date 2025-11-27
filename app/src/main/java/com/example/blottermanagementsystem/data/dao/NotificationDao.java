package com.example.blottermanagementsystem.data.dao;

import androidx.room.*;
import com.example.blottermanagementsystem.data.entity.Notification;
import java.util.List;

@Dao
public interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    List<Notification> getNotificationsByUserId(int userId);
    
    @Query("SELECT * FROM notifications WHERE id = :notificationId")
    Notification getNotificationById(int notificationId);
    
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    int getUnreadCount(int userId);
    
    @Query("SELECT * FROM notifications WHERE userId = :userId AND isRead = 0 ORDER BY timestamp DESC")
    List<Notification> getUnreadNotificationsForUser(int userId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertNotification(Notification notification);
    
    @Update
    void updateNotification(Notification notification);
    
    @Delete
    void deleteNotification(Notification notification);
    
    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    void markAsRead(int notificationId);
    
    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    void markAllAsRead(int userId);
    
    @Query("DELETE FROM notifications WHERE userId = :userId")
    void deleteAllByUserId(int userId);
}
