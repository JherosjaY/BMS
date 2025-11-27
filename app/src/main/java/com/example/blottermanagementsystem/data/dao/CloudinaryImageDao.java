package com.example.blottermanagementsystem.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.blottermanagementsystem.data.entity.CloudinaryImage;

import java.util.List;

/**
 * CloudinaryImageDao - Database operations for Cloudinary images
 */
@Dao
public interface CloudinaryImageDao {
    
    /**
     * Insert a new image record
     */
    @Insert
    long insertImage(CloudinaryImage image);
    
    /**
     * Update an existing image record
     */
    @Update
    void updateImage(CloudinaryImage image);
    
    /**
     * Delete an image record
     */
    @Delete
    void deleteImage(CloudinaryImage image);
    
    /**
     * Delete image by public ID
     */
    @Query("DELETE FROM cloudinary_images WHERE publicId = :publicId")
    void deleteImageByPublicId(String publicId);
    
    /**
     * Get all images for a specific user
     */
    @Query("SELECT * FROM cloudinary_images WHERE userId = :userId ORDER BY uploadedAt DESC")
    List<CloudinaryImage> getImagesByUserId(int userId);
    
    /**
     * Get a specific image by public ID
     */
    @Query("SELECT * FROM cloudinary_images WHERE publicId = :publicId")
    CloudinaryImage getImageByPublicId(String publicId);
    
    /**
     * Get all images (for admin)
     */
    @Query("SELECT * FROM cloudinary_images ORDER BY uploadedAt DESC")
    List<CloudinaryImage> getAllImages();
    
    /**
     * Get image count for a user
     */
    @Query("SELECT COUNT(*) FROM cloudinary_images WHERE userId = :userId")
    int getImageCountForUser(int userId);
    
    /**
     * Delete all images for a user (when account is deleted)
     */
    @Query("DELETE FROM cloudinary_images WHERE userId = :userId")
    void deleteAllImagesForUser(int userId);
}
