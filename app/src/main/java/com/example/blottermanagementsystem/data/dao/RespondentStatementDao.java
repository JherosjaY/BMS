package com.example.blottermanagementsystem.data.dao;

import androidx.room.*;
import com.example.blottermanagementsystem.data.entity.RespondentStatement;
import java.util.List;

@Dao
public interface RespondentStatementDao {
    @Query("SELECT * FROM respondent_statements WHERE respondentId = :respondentId ORDER BY submittedDate DESC")
    List<RespondentStatement> getStatementsByRespondentId(int respondentId);
    
    @Query("SELECT * FROM respondent_statements WHERE blotterReportId = :reportId ORDER BY submittedDate DESC")
    List<RespondentStatement> getStatementsByReportId(int reportId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertStatement(RespondentStatement statement);
    
    @Update
    void updateStatement(RespondentStatement statement);
    
    @Delete
    void deleteStatement(RespondentStatement statement);
}
