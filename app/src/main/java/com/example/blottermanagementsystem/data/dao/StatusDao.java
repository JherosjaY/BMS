package com.example.blottermanagementsystem.data.dao;

import androidx.room.*;
import com.example.blottermanagementsystem.data.entity.Status;
import java.util.List;

@Dao
public interface StatusDao {
    @Query("SELECT * FROM status")
    List<Status> getAllStatuses();
    
    @Query("SELECT * FROM status WHERE id = :statusId")
    Status getStatusById(int statusId);
    
    @Query("SELECT * FROM status WHERE name = :name LIMIT 1")
    Status getStatusByName(String name);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStatus(Status status);
    
    @Update
    void updateStatus(Status status);
    
    @Delete
    void deleteStatus(Status status);
}
