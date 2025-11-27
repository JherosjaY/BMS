package com.example.blottermanagementsystem.data.dao;

import androidx.room.*;
import com.example.blottermanagementsystem.data.entity.KPForm;
import java.util.List;

@Dao
public interface KPFormDao {
    @Query("SELECT * FROM kp_forms WHERE blotterReportId = :reportId ORDER BY issueDate DESC")
    List<KPForm> getFormsByReportId(int reportId);
    
    @Query("SELECT * FROM kp_forms WHERE id = :formId")
    KPForm getFormById(int formId);
    
    @Query("SELECT * FROM kp_forms WHERE formType = :formType ORDER BY issueDate DESC")
    List<KPForm> getFormsByType(String formType);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertForm(KPForm form);
    
    @Update
    void updateForm(KPForm form);
    
    @Delete
    void deleteForm(KPForm form);
}
