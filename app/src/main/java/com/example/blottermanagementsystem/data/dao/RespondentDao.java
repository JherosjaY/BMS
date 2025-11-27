package com.example.blottermanagementsystem.data.dao;

import androidx.room.*;
import com.example.blottermanagementsystem.data.entity.Respondent;
import java.util.List;

@Dao
public interface RespondentDao {
    @Query("SELECT * FROM respondents WHERE blotterReportId = :reportId")
    List<Respondent> getRespondentsByReportId(int reportId);
    
    @Query("SELECT * FROM respondents WHERE id = :respondentId")
    Respondent getRespondentById(int respondentId);
    
    @Query("SELECT * FROM respondents WHERE personId = :personId")
    List<Respondent> getRespondentsByPersonId(int personId);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRespondent(Respondent respondent);
    
    @Update
    void updateRespondent(Respondent respondent);
    
    @Delete
    void deleteRespondent(Respondent respondent);
}
