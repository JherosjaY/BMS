package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "officers")
public class Officer {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private Integer userId;
    private String name;
    private String rank;
    private String badgeNumber;
    private String contactNumber;
    private String email;
    private String gender;
    private int assignedCases;
    private boolean isAvailable;
    private boolean isActive;
    private long dateAdded;

    public Officer(String name, String rank, String badgeNumber) {
        this.name = name;
        this.rank = rank;
        this.badgeNumber = badgeNumber;
        this.assignedCases = 0;
        this.isAvailable = true;
        this.isActive = true;
        this.dateAdded = System.currentTimeMillis();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }
    public String getBadgeNumber() { return badgeNumber; }
    public void setBadgeNumber(String badgeNumber) { this.badgeNumber = badgeNumber; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public int getAssignedCases() { return assignedCases; }
    public void setAssignedCases(int assignedCases) { this.assignedCases = assignedCases; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public long getDateAdded() { return dateAdded; }
    public void setDateAdded(long dateAdded) { this.dateAdded = dateAdded; }
}
