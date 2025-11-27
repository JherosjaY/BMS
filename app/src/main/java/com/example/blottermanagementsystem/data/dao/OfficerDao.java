package com.example.blottermanagementsystem.data.dao;

import androidx.room.*;
import com.example.blottermanagementsystem.data.entity.Officer;
import java.util.List;

@Dao
public interface OfficerDao {
    @Query("SELECT * FROM officers")
    List<Officer> getAllOfficers();
    
    @Query("SELECT * FROM officers WHERE id = :officerId")
    Officer getOfficerById(int officerId);
    
    @Query("SELECT * FROM officers WHERE badgeNumber = :badgeNumber LIMIT 1")
    Officer getOfficerByBadgeNumber(String badgeNumber);
    
    @Query("SELECT * FROM officers WHERE name = :fullName LIMIT 1")
    Officer getOfficerByName(String fullName);
    
    @Query("SELECT * FROM officers WHERE userId = :userId LIMIT 1")
    Officer getOfficerByUserId(int userId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOfficer(Officer officer);
    
    @Update
    void updateOfficer(Officer officer);
    
    @Delete
    void deleteOfficer(Officer officer);
    
    @Query("SELECT COUNT(*) FROM officers WHERE isAvailable = 1")
    int getOfficerCount();
}
