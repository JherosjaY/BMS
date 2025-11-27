package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * ✅ PENDING SYNC ENTITY
 * Stores operations that need to be synced to Neon when online
 */
@Entity(tableName = "pending_sync")
public class PendingSync {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String operationType; // CREATE_REPORT, UPDATE_REPORT, CREATE_EVIDENCE, SEND_SMS
    private String endpoint; // API endpoint
    private String data; // JSON data
    private int recordId; // Local record ID
    private String status; // PENDING, PROCESSING, FAILED
    private int retryCount;
    private long createdAt;
    private Long lastAttempt;
    
    public PendingSync() {
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    
    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public Long getLastAttempt() { return lastAttempt; }
    public void setLastAttempt(Long lastAttempt) { this.lastAttempt = lastAttempt; }
}
