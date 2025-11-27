package com.example.blottermanagementsystem.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.blottermanagementsystem.data.entity.LegalDocument;
import java.util.List;

@Dao
public interface LegalDocumentDao {
    
    @Insert
    long insertDocument(LegalDocument document);
    
    @Update
    void updateDocument(LegalDocument document);
    
    @Delete
    void deleteDocument(LegalDocument document);
    
    @Query("SELECT * FROM legal_documents ORDER BY createdAt DESC")
    List<LegalDocument> getAllDocuments();
    
    @Query("SELECT * FROM legal_documents WHERE id = :id LIMIT 1")
    LegalDocument getDocumentById(int id);
    
    @Query("SELECT * FROM legal_documents WHERE reportId = :reportId ORDER BY createdAt DESC")
    List<LegalDocument> getDocumentsByReportId(int reportId);
    
    @Query("SELECT * FROM legal_documents WHERE documentType = :type ORDER BY createdAt DESC")
    List<LegalDocument> getDocumentsByType(String type);
    
    @Query("SELECT * FROM legal_documents WHERE status = :status ORDER BY createdAt DESC")
    List<LegalDocument> getDocumentsByStatus(String status);
    
    @Query("UPDATE legal_documents SET status = :status, updatedAt = :timestamp WHERE id = :id")
    void updateDocumentStatus(int id, String status, long timestamp);
    
    @Query("DELETE FROM legal_documents WHERE reportId = :reportId")
    void deleteDocumentsByReportId(int reportId);
}
