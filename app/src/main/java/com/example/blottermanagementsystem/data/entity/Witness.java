package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "witnesses",
    foreignKeys = @ForeignKey(
        entity = BlotterReport.class,
        parentColumns = "id",
        childColumns = "blotterReportId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("blotterReportId")}
)
public class Witness {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int blotterReportId;
    private String name;
    private String contactNumber;
    private String address;
    private String statement;
    private long createdAt;

    public Witness() {
        this.createdAt = System.currentTimeMillis();
    }

    @Ignore
    public Witness(int blotterReportId, String name, String contactNumber, String address, String statement) {
        this.blotterReportId = blotterReportId;
        this.name = name;
        this.contactNumber = contactNumber;
        this.address = address;
        this.statement = statement;
        this.createdAt = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBlotterReportId() { return blotterReportId; }
    public void setBlotterReportId(int blotterReportId) { this.blotterReportId = blotterReportId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getStatement() { return statement; }
    public void setStatement(String statement) { this.statement = statement; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
