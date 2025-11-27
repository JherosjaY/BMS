package com.example.blottermanagementsystem.data.dao;

import androidx.room.*;
import com.example.blottermanagementsystem.data.entity.Witness;
import java.util.List;

@Dao
public interface WitnessDao {
    @Query("SELECT * FROM witnesses WHERE blotterReportId = :reportId")
    List<Witness> getWitnessesByReportId(int reportId);
    
    @Query("SELECT * FROM witnesses WHERE id = :witnessId")
    Witness getWitnessById(int witnessId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertWitness(Witness witness);
    
    @Update
    void updateWitness(Witness witness);
    
    @Delete
    void deleteWitness(Witness witness);
    
    @Query("DELETE FROM witnesses WHERE blotterReportId = :reportId")
    void deleteWitnessesByReportId(int reportId);
    
    @Query("SELECT COUNT(*) FROM witnesses WHERE blotterReportId = :reportId")
    int getWitnessCountByReport(int reportId);
    
    @Query("SELECT * FROM witnesses WHERE blotterReportId = :reportId")
    List<Witness> getWitnessesByReport(int reportId);
}
