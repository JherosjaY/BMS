package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sync_queue")
public class SyncQueue {
    @PrimaryKey(autoGenerate = true) private int id;
    private String entityType; private int entityId; private String action; private String data; private long timestamp; private int retryCount; private String lastError; private boolean synced;

    public SyncQueue(String entityType, int entityId, String action, String data) {
        this.entityType = entityType; this.entityId = entityId; this.action = action; this.data = data; this.timestamp = System.currentTimeMillis(); this.retryCount = 0; this.synced = false;
    }

    public int getId() { return id; } public void setId(int id) { this.id = id; }
    public String getEntityType() { return entityType; } public void setEntityType(String entityType) { this.entityType = entityType; }
    public int getEntityId() { return entityId; } public void setEntityId(int entityId) { this.entityId = entityId; }
    public String getAction() { return action; } public void setAction(String action) { this.action = action; }
    public String getData() { return data; } public void setData(String data) { this.data = data; }
    public long getTimestamp() { return timestamp; } public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public int getRetryCount() { return retryCount; } public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public String getLastError() { return lastError; } public void setLastError(String lastError) { this.lastError = lastError; }
    public boolean isSynced() { return synced; } public void setSynced(boolean synced) { this.synced = synced; }
}
