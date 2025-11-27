package com.example.blottermanagementsystem.data.dao;

import androidx.room.*;
import com.example.blottermanagementsystem.data.entity.Summons;
import java.util.List;

@Dao
public interface SummonsDao {
    @Query("SELECT * FROM summons WHERE blotterReportId = :reportId ORDER BY issueDate DESC")
    List<Summons> getSummonsByReportId(int reportId);
    
    @Query("SELECT * FROM summons ORDER BY issueDate DESC")
    List<Summons> getAllSummons();
    
    @Query("SELECT * FROM summons WHERE respondentId = :respondentId ORDER BY issueDate DESC")
    List<Summons> getSummonsByRespondentId(int respondentId);
    
    @Query("SELECT * FROM summons WHERE id = :summonsId")
    Summons getSummonsById(int summonsId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSummons(Summons summons);
    
    @Update
    void updateSummons(Summons summons);
    
    @Delete
    void deleteSummons(Summons summons);
}
