package com.example.blottermanagementsystem.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * ✅ CLOUDINARY CONFIGURATION
 * Manages Cloudinary SDK initialization with actual credentials
 */
public class CloudinaryConfig {
    private static Cloudinary cloudinary;
    
    // ✅ ACTUAL CREDENTIALS
    private static final String CLOUD_NAME = "do9ty8tem";
    private static final String API_KEY = "331777292844342";
    private static final String API_SECRET = "WadNuNA_5NiDBm80UhJbVHMOqkQ"; // Complete API secret
    
    /**
     * Get singleton Cloudinary instance
     */
    public static Cloudinary getCloudinary() {
        if (cloudinary == null) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", CLOUD_NAME);
            config.put("api_key", API_KEY);
            config.put("api_secret", API_SECRET);
            config.put("secure", "true");
            
            cloudinary = new Cloudinary(config);
        }
        return cloudinary;
    }
    
    /**
     * Generic upload options
     */
    public static Map<String, Object> getUploadOptions(String folder) {
        return ObjectUtils.asMap(
            "folder", "bms/" + folder,
            "resource_type", "auto",
            "quality", "auto:good",
            "width", 500,
            "height", 500,
            "crop", "limit",
            "format", "jpg"
        );
    }
    
    /**
     * Avatar upload options (face detection, circular crop)
     */
    public static Map<String, Object> getAvatarUploadOptions() {
        return ObjectUtils.asMap(
            "folder", "bms/avatars",
            "width", 200,
            "height", 200,
            "crop", "thumb",
            "gravity", "face",
            "quality", "auto:best",
            "format", "jpg"
        );
    }
    
    /**
     * Evidence upload options (high quality)
     */
    public static Map<String, Object> getEvidenceUploadOptions() {
        return ObjectUtils.asMap(
            "folder", "bms/evidence",
            "resource_type", "auto",
            "quality", "auto:good",
            "format", "jpg"
        );
    }
    
    /**
     * Report attachment options
     */
    public static Map<String, Object> getReportAttachmentOptions() {
        return ObjectUtils.asMap(
            "folder", "bms/attachments",
            "resource_type", "auto",
            "quality", "auto:good"
        );
    }
}
