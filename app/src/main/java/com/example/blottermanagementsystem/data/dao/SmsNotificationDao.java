package com.example.blottermanagementsystem.data.dao;

import androidx.room.*;
import com.example.blottermanagementsystem.data.entity.SmsNotification;
import java.util.List;

@Dao
public interface SmsNotificationDao {
    @Query("SELECT * FROM sms_notifications WHERE respondentId = :respondentId ORDER BY sentDate DESC")
    List<SmsNotification> getSmsByRespondentId(int respondentId);
    
    @Query("SELECT * FROM sms_notifications WHERE blotterReportId = :reportId ORDER BY sentDate DESC")
    List<SmsNotification> getSmsByReportId(int reportId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSms(SmsNotification sms);
    
    @Update
    void updateSms(SmsNotification sms);
    
    @Delete
    void deleteSms(SmsNotification sms);
}
