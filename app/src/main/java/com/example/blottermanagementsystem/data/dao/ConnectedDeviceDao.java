package com.example.blottermanagementsystem.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.blottermanagementsystem.data.entity.ConnectedDevice;
import java.util.List;

@Dao
public interface ConnectedDeviceDao {
    
    @Insert
    long insertDevice(ConnectedDevice device);
    
    @Update
    void updateDevice(ConnectedDevice device);
    
    @Delete
    void deleteDevice(ConnectedDevice device);
    
    @Query("SELECT * FROM connected_devices ORDER BY lastActive DESC")
    List<ConnectedDevice> getAllDevices();
    
    @Query("SELECT * FROM connected_devices WHERE userId = :userId ORDER BY lastActive DESC")
    List<ConnectedDevice> getDevicesByUserId(int userId);
    
    @Query("SELECT * FROM connected_devices WHERE isActive = 1 ORDER BY lastActive DESC")
    List<ConnectedDevice> getActiveDevices();
    
    @Query("SELECT * FROM connected_devices WHERE deviceId = :deviceId LIMIT 1")
    ConnectedDevice getDeviceByDeviceId(String deviceId);
    
    @Query("UPDATE connected_devices SET isActive = 0 WHERE deviceId = :deviceId")
    void deactivateDevice(String deviceId);
    
    @Query("UPDATE connected_devices SET lastActive = :timestamp WHERE deviceId = :deviceId")
    void updateLastActive(String deviceId, long timestamp);
    
    @Query("DELETE FROM connected_devices WHERE userId = :userId")
    void deleteDevicesByUserId(int userId);
}
