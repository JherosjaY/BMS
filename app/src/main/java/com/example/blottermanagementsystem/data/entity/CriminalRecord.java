package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "person_criminal_records")
public class CriminalRecord implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String personId;
    private String caseNumber;
    private String crimeType;
    private String crimeDescription;
    private String dateCommitted;
    private String dateArrested;
    private String sentencing;
    private String status;
    private String officerInCharge;
    private String courtName;
    private long createdAt;
    private long lastSyncAt;
    
    @Ignore
    private int localId;
    @Ignore
    private String cloudId;

    public CriminalRecord() {
        this.status = "closed";
        this.createdAt = System.currentTimeMillis();
        this.lastSyncAt = System.currentTimeMillis();
    }

    @Ignore
    public CriminalRecord(String personId, String crimeType, String crimeDescription) {
        this.personId = personId;
        this.crimeType = crimeType;
        this.crimeDescription = crimeDescription;
        this.status = "closed";
        this.createdAt = System.currentTimeMillis();
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

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public String getCrimeType() {
        return crimeType;
    }

    public void setCrimeType(String crimeType) {
        this.crimeType = crimeType;
    }

    public String getCrimeDescription() {
        return crimeDescription;
    }

    public void setCrimeDescription(String crimeDescription) {
        this.crimeDescription = crimeDescription;
    }

    public String getDateCommitted() {
        return dateCommitted;
    }

    public void setDateCommitted(String dateCommitted) {
        this.dateCommitted = dateCommitted;
    }

    public String getDateArrested() {
        return dateArrested;
    }

    public void setDateArrested(String dateArrested) {
        this.dateArrested = dateArrested;
    }

    public String getSentencing() {
        return sentencing;
    }

    public void setSentencing(String sentencing) {
        this.sentencing = sentencing;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOfficerInCharge() {
        return officerInCharge;
    }

    public void setOfficerInCharge(String officerInCharge) {
        this.officerInCharge = officerInCharge;
    }

    public String getCourtName() {
        return courtName;
    }

    public void setCourtName(String courtName) {
        this.courtName = courtName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
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
