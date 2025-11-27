package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "respondent_statements",
    foreignKeys = {
        @ForeignKey(entity = Respondent.class, parentColumns = "id", childColumns = "respondentId", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = BlotterReport.class, parentColumns = "id", childColumns = "blotterReportId", onDelete = ForeignKey.CASCADE)
    },
    indices = {@Index("respondentId"), @Index("blotterReportId")}
)
public class RespondentStatement {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int respondentId;
    private int blotterReportId;
    private String statement;
    private String evidenceUris;
    private long submittedDate;
    private String submittedVia;
    private boolean isVerified;
    private String verifiedBy;
    private Long verifiedDate;
    private String officerNotes;

    public RespondentStatement(int respondentId, int blotterReportId, String statement) {
        this.respondentId = respondentId;
        this.blotterReportId = blotterReportId;
        this.statement = statement;
        this.submittedDate = System.currentTimeMillis();
        this.submittedVia = "In Person";
        this.isVerified = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getRespondentId() { return respondentId; }
    public void setRespondentId(int respondentId) { this.respondentId = respondentId; }
    public int getBlotterReportId() { return blotterReportId; }
    public void setBlotterReportId(int blotterReportId) { this.blotterReportId = blotterReportId; }
    public String getStatement() { return statement; }
    public void setStatement(String statement) { this.statement = statement; }
    public String getEvidenceUris() { return evidenceUris; }
    public void setEvidenceUris(String evidenceUris) { this.evidenceUris = evidenceUris; }
    public long getSubmittedDate() { return submittedDate; }
    public void setSubmittedDate(long submittedDate) { this.submittedDate = submittedDate; }
    public String getSubmittedVia() { return submittedVia; }
    public void setSubmittedVia(String submittedVia) { this.submittedVia = submittedVia; }
    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }
    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }
    public Long getVerifiedDate() { return verifiedDate; }
    public void setVerifiedDate(Long verifiedDate) { this.verifiedDate = verifiedDate; }
    public String getOfficerNotes() { return officerNotes; }
    public void setOfficerNotes(String officerNotes) { this.officerNotes = officerNotes; }
}
