package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "person_case_involvement")
public class CaseInvolvement implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String personId;
    private String caseId;
    private String involvementType;
    private String involvementDetails;
    private String status;
    private String officerNotes;
    private String createdBy;
    private long createdAt;
    private long updatedAt;
    private long lastSyncAt;
    
    @Ignore
    private int localId;
    @Ignore
    private String cloudId;

    public CaseInvolvement() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.lastSyncAt = System.currentTimeMillis();
    }

    @Ignore
    public CaseInvolvement(String personId, String caseId, String involvementType) {
        this.personId = personId;
        this.caseId = caseId;
        this.involvementType = involvementType;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.lastSyncAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getInvolvementType() {
        return involvementType;
    }

    public void setInvolvementType(String involvementType) {
        this.involvementType = involvementType;
    }

    public String getInvolvementDetails() {
        return involvementDetails;
    }

    public void setInvolvementDetails(String involvementDetails) {
        this.involvementDetails = involvementDetails;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOfficerNotes() {
        return officerNotes;
    }

    public void setOfficerNotes(String officerNotes) {
        this.officerNotes = officerNotes;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getLastSyncAt() {
        return lastSyncAt;
    }

    public void setLastSyncAt(long lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }

    public int getLocalId() {
        return localId;
    }

    public void setLocalId(int localId) {
        this.localId = localId;
    }

    public String getCloudId() {
        return cloudId;
    }

    public void setCloudId(String cloudId) {
        this.cloudId = cloudId;
    }
}
