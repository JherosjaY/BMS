package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "suspects",
    foreignKeys = @ForeignKey(
        entity = BlotterReport.class,
        parentColumns = "id",
        childColumns = "blotterReportId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("blotterReportId")}
)
public class Suspect {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int blotterReportId;
    private String name;
    private String alias;
    private Integer age;
    private String gender;
    private String address;
    private String description;
    private String photoUri;
    private long dateAdded;

    public Suspect() {
        this.dateAdded = System.currentTimeMillis();
    }

    @Ignore
    public Suspect(int reportId, String name, String alias, String address, String contact) {
        this.blotterReportId = reportId;
        this.name = name;
        this.alias = alias;
        this.address = address;
        this.description = contact;
        this.dateAdded = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBlotterReportId() { return blotterReportId; }
    public void setBlotterReportId(int blotterReportId) { this.blotterReportId = blotterReportId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPhotoUri() { return photoUri; }
    public void setPhotoUri(String photoUri) { this.photoUri = photoUri; }
    public long getDateAdded() { return dateAdded; }
    public void setDateAdded(long dateAdded) { this.dateAdded = dateAdded; }
}
