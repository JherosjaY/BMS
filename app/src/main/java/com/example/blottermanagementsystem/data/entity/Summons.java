package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "summons",
    foreignKeys = {
        @ForeignKey(entity = BlotterReport.class, parentColumns = "id", childColumns = "blotterReportId", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Respondent.class, parentColumns = "id", childColumns = "respondentId", onDelete = ForeignKey.CASCADE)
    },
    indices = {@Index("blotterReportId"), @Index("respondentId")}
)
public class Summons {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int blotterReportId;
    private int respondentId;
    private String summonsNumber;
    private String summonsType;
    private long issueDate;
    private Long hearingDate;
    private String hearingTime;
    private String hearingVenue;
    private String purpose;
    private String issuedBy;
    private String issuedByPosition;
    private Long receivedDate;
    private String receivedBy;
    private String receivedByRelation;
    private String deliveryMethod;
    private String deliveryStatus;
    private String deliveryNotes;
    private Long returnDate;
    private boolean isComplied;
    private Long complianceDate;
    private String complianceNotes;
    private String documentPath;

    public Summons(int blotterReportId, int respondentId, String summonsNumber, String summonsType, String purpose, String issuedBy) {
        this.blotterReportId = blotterReportId;
        this.respondentId = respondentId;
        this.summonsNumber = summonsNumber;
        this.summonsType = summonsType;
        this.purpose = purpose;
        this.issuedBy = issuedBy;
        this.issueDate = System.currentTimeMillis();
        this.hearingVenue = "Barangay Hall";
        this.issuedByPosition = "Punong Barangay";
        this.deliveryMethod = "Personal";
        this.deliveryStatus = "Pending";
        this.isComplied = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBlotterReportId() { return blotterReportId; }
    public void setBlotterReportId(int blotterReportId) { this.blotterReportId = blotterReportId; }
    public int getRespondentId() { return respondentId; }
    public void setRespondentId(int respondentId) { this.respondentId = respondentId; }
    public String getSummonsNumber() { return summonsNumber; }
    public void setSummonsNumber(String summonsNumber) { this.summonsNumber = summonsNumber; }
    public String getSummonsType() { return summonsType; }
    public void setSummonsType(String summonsType) { this.summonsType = summonsType; }
    public long getIssueDate() { return issueDate; }
    public void setIssueDate(long issueDate) { this.issueDate = issueDate; }
    public Long getHearingDate() { return hearingDate; }
    public void setHearingDate(Long hearingDate) { this.hearingDate = hearingDate; }
    public String getHearingTime() { return hearingTime; }
    public void setHearingTime(String hearingTime) { this.hearingTime = hearingTime; }
    public String getHearingVenue() { return hearingVenue; }
    public void setHearingVenue(String hearingVenue) { this.hearingVenue = hearingVenue; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getIssuedBy() { return issuedBy; }
    public void setIssuedBy(String issuedBy) { this.issuedBy = issuedBy; }
    public String getIssuedByPosition() { return issuedByPosition; }
    public void setIssuedByPosition(String issuedByPosition) { this.issuedByPosition = issuedByPosition; }
    public Long getReceivedDate() { return receivedDate; }
    public void setReceivedDate(Long receivedDate) { this.receivedDate = receivedDate; }
    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }
    public String getReceivedByRelation() { return receivedByRelation; }
    public void setReceivedByRelation(String receivedByRelation) { this.receivedByRelation = receivedByRelation; }
    public String getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(String deliveryMethod) { this.deliveryMethod = deliveryMethod; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
    public String getDeliveryNotes() { return deliveryNotes; }
    public void setDeliveryNotes(String deliveryNotes) { this.deliveryNotes = deliveryNotes; }
    public Long getReturnDate() { return returnDate; }
    public void setReturnDate(Long returnDate) { this.returnDate = returnDate; }
    public boolean isComplied() { return isComplied; }
    public void setComplied(boolean complied) { isComplied = complied; }
    public Long getComplianceDate() { return complianceDate; }
    public void setComplianceDate(Long complianceDate) { this.complianceDate = complianceDate; }
    public String getComplianceNotes() { return complianceNotes; }
    public void setComplianceNotes(String complianceNotes) { this.complianceNotes = complianceNotes; }
    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }
    
    // Alias methods for compatibility
    public String getRespondentName() { return "Respondent #" + respondentId; }
    public long getIssuedDate() { return issueDate; }
    public String getStatus() { return deliveryStatus != null ? deliveryStatus : "Pending"; }
}
