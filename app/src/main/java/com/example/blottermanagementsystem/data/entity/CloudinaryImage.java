package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

/**
 * CloudinaryImage - Stores metadata for images uploaded to Cloudinary
 * Allows images to be synced across all user devices
 */
@Entity(tableName = "cloudinary_images")
public class CloudinaryImage {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private int userId;
    private String publicId;      // Cloudinary public ID
    private String secureUrl;     // HTTPS URL from Cloudinary
    private String fileName;      // Original file name
    private long uploadedAt;      // Timestamp of upload
    
    // Constructors
    public CloudinaryImage() {}
    
    @Ignore
    public CloudinaryImage(int userId, String publicId, String secureUrl, String fileName) {
        this.userId = userId;
        this.publicId = publicId;
        this.secureUrl = secureUrl;
        this.fileName = fileName;
        this.uploadedAt = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getPublicId() {
        return publicId;
    }
    
    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }
    
    public String getSecureUrl() {
        return secureUrl;
    }
    
    public void setSecureUrl(String secureUrl) {
        this.secureUrl = secureUrl;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public long getUploadedAt() {
        return uploadedAt;
    }
    
    public void setUploadedAt(long uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    
    @Override
    public String toString() {
        return "CloudinaryImage{" +
                "id=" + id +
                ", userId=" + userId +
                ", publicId='" + publicId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
}
