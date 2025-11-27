package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "kp_forms",
    foreignKeys = @ForeignKey(
        entity = BlotterReport.class,
        parentColumns = "id",
        childColumns = "blotterReportId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("blotterReportId")}
)
public class KPForm {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int blotterReportId;
    private String formType;
    private String formNumber;
    private String formTitle;
    private long issueDate;
    private String issuedBy;
    private String issuedByPosition;
    private Long hearingDate;
    private String hearingTime;
    private String hearingVenue;
    private String settlementTerms;
    private String complainantSignature;
    private String respondentSignature;
    private String witnessSignatures;
    private String luponSignatures;
    private Long settlementDate;
    private String certificationReason;
    private int attemptsMade;
    private Long lastAttemptDate;
    private String status;
    private String notes;
    private String documentPath;
    private String createdBy;
    private long createdDate;
    private long lastModifiedDate;

    public KPForm(int blotterReportId, String formType, String formNumber, String formTitle, String issuedBy, String createdBy) {
        this.blotterReportId = blotterReportId;
        this.formType = formType;
        this.formNumber = formNumber;
        this.formTitle = formTitle;
        this.issuedBy = issuedBy;
        this.createdBy = createdBy;
        this.issueDate = System.currentTimeMillis();
        this.issuedByPosition = "Punong Barangay";
        this.attemptsMade = 0;
        this.status = "Draft";
        this.createdDate = System.currentTimeMillis();
        this.lastModifiedDate = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBlotterReportId() { return blotterReportId; }
    public void setBlotterReportId(int blotterReportId) { this.blotterReportId = blotterReportId; }
    public String getFormType() { return formType; }
    public void setFormType(String formType) { this.formType = formType; }
    public String getFormNumber() { return formNumber; }
    public void setFormNumber(String formNumber) { this.formNumber = formNumber; }
    public String getFormTitle() { return formTitle; }
    public void setFormTitle(String formTitle) { this.formTitle = formTitle; }
    public long getIssueDate() { return issueDate; }
    public void setIssueDate(long issueDate) { this.issueDate = issueDate; }
    public String getIssuedBy() { return issuedBy; }
    public void setIssuedBy(String issuedBy) { this.issuedBy = issuedBy; }
    public String getIssuedByPosition() { return issuedByPosition; }
    public void setIssuedByPosition(String issuedByPosition) { this.issuedByPosition = issuedByPosition; }
    public Long getHearingDate() { return hearingDate; }
    public void setHearingDate(Long hearingDate) { this.hearingDate = hearingDate; }
    public String getHearingTime() { return hearingTime; }
    public void setHearingTime(String hearingTime) { this.hearingTime = hearingTime; }
    public String getHearingVenue() { return hearingVenue; }
    public void setHearingVenue(String hearingVenue) { this.hearingVenue = hearingVenue; }
    public String getSettlementTerms() { return settlementTerms; }
    public void setSettlementTerms(String settlementTerms) { this.settlementTerms = settlementTerms; }
    public String getComplainantSignature() { return complainantSignature; }
    public void setComplainantSignature(String complainantSignature) { this.complainantSignature = complainantSignature; }
    public String getRespondentSignature() { return respondentSignature; }
    public void setRespondentSignature(String respondentSignature) { this.respondentSignature = respondentSignature; }
    public String getWitnessSignatures() { return witnessSignatures; }
    public void setWitnessSignatures(String witnessSignatures) { this.witnessSignatures = witnessSignatures; }
    public String getLuponSignatures() { return luponSignatures; }
    public void setLuponSignatures(String luponSignatures) { this.luponSignatures = luponSignatures; }
    public Long getSettlementDate() { return settlementDate; }
    public void setSettlementDate(Long settlementDate) { this.settlementDate = settlementDate; }
    public String getCertificationReason() { return certificationReason; }
    public void setCertificationReason(String certificationReason) { this.certificationReason = certificationReason; }
    public int getAttemptsMade() { return attemptsMade; }
    public void setAttemptsMade(int attemptsMade) { this.attemptsMade = attemptsMade; }
    public Long getLastAttemptDate() { return lastAttemptDate; }
    public void setLastAttemptDate(Long lastAttemptDate) { this.lastAttemptDate = lastAttemptDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public long getCreatedDate() { return createdDate; }
    public void setCreatedDate(long createdDate) { this.createdDate = createdDate; }
    public long getLastModifiedDate() { return lastModifiedDate; }
    public void setLastModifiedDate(long lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }
}
