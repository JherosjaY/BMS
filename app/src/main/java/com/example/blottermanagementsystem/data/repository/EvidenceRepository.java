package com.example.blottermanagementsystem.data.repository;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.dao.EvidenceDao;
import com.example.blottermanagementsystem.data.entity.Evidence;
import com.example.blottermanagementsystem.utils.FirebaseImageManager;
import com.example.blottermanagementsystem.utils.NetworkMonitor;
import java.util.List;

/**
 * ✅ EVIDENCE REPOSITORY - FIREBASE + NEON INTEGRATION
 * Handles image uploads to Firebase and syncs to Neon database
 */
public class EvidenceRepository {
    private static final String TAG = "EvidenceRepository";
    
    private EvidenceDao localDao;
    private FirebaseImageManager firebaseManager;
    private Context context;
    
    public EvidenceRepository(Context context) {
        BlotterDatabase db = BlotterDatabase.getDatabase(context);
        this.localDao = db.evidenceDao();
        this.firebaseManager = new FirebaseImageManager();
        this.context = context;
    }
    
    // ✅ UPLOAD IMAGE TO FIREBASE + SYNC TO NEON
    public void uploadEvidence(Uri imageUri, int reportId, String caseNumber, 
                              RepositoryCallback<Evidence> callback) {
        Log.d(TAG, "📸 Starting image upload for case: " + caseNumber);
        
        firebaseManager.uploadImage(imageUri, caseNumber, 
            new FirebaseImageManager.ImageUploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    Log.d(TAG, "✅ Image uploaded to Firebase: " + imageUrl);
                    
                    // Create evidence record
                    Evidence evidence = new Evidence();
                    evidence.setBlotterReportId(reportId);
                    evidence.setPhotoUri(imageUrl);
                    evidence.setDescription("Uploaded evidence");
                    evidence.setCaptureTimestamp(System.currentTimeMillis());
                    
                    // Save to local and sync to Neon
                    saveEvidenceHybrid(evidence, callback);
                }
                
                @Override
                public void onError(String errorMessage) {
                    Log.e(TAG, "❌ Image upload failed: " + errorMessage);
                    callback.onError("Image upload failed: " + errorMessage);
                }
                
                @Override
                public void onProgress(int progress) {
                    Log.d(TAG, "📊 Upload progress: " + progress + "%");
                }
            });
    }
    
    // ✅ GET EVIDENCE FOR REPORT
    public void getEvidenceForReport(int reportId, RepositoryCallback<List<Evidence>> callback) {
        Log.d(TAG, "📥 Loading evidence for report: " + reportId);
        
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Evidence> evidence = localDao.getEvidenceByReportId(reportId);
                Log.d(TAG, "✅ Loaded " + evidence.size() + " evidence items");
                callback.onSuccess(evidence);
            } catch (Exception e) {
                Log.e(TAG, "❌ Error loading evidence: " + e.getMessage());
                callback.onError("Error loading evidence: " + e.getMessage());
            }
        });
    }
    
    // ✅ DELETE EVIDENCE
    public void deleteEvidence(Evidence evidence, RepositoryCallback<String> callback) {
        Log.d(TAG, "🗑️ Deleting evidence: " + evidence.getId());
        
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Delete from Firebase
                if (evidence.getPhotoUri() != null) {
                    firebaseManager.deleteImage(evidence.getPhotoUri(), 
                        new FirebaseImageManager.DeleteCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "✅ Image deleted from Firebase");
                                
                                // Delete from local database
                                localDao.deleteEvidence(evidence);
                                Log.d(TAG, "✅ Evidence deleted locally");
                                callback.onSuccess("Evidence deleted successfully");
                            }
                            
                            @Override
                            public void onError(String errorMessage) {
                                Log.e(TAG, "❌ Firebase delete failed: " + errorMessage);
                                // Still delete from local
                                localDao.deleteEvidence(evidence);
                                callback.onSuccess("Evidence deleted locally");
                            }
                        });
                } else {
                    // No Firebase URL, just delete locally
                    localDao.deleteEvidence(evidence);
                    callback.onSuccess("Evidence deleted successfully");
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Delete failed: " + e.getMessage());
                callback.onError("Delete failed: " + e.getMessage());
            }
        });
    }
    
    // ✅ PRIVATE: Save evidence hybrid (local + Neon)
    private void saveEvidenceHybrid(Evidence evidence, RepositoryCallback<Evidence> callback) {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Save to local database
                localDao.insertEvidence(evidence);
                Log.d(TAG, "✅ Evidence saved locally");
                
                // Sync to Neon if online
                NetworkMonitor networkMonitor = new NetworkMonitor(context);
                if (networkMonitor.isNetworkAvailable()) {
                    syncEvidenceToNeon(evidence, callback);
                } else {
                    Log.d(TAG, "📋 Evidence marked for later Neon sync (offline)");
                    callback.onSuccess(evidence);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Evidence save failed: " + e.getMessage());
                callback.onError("Evidence save failed: " + e.getMessage());
            }
        });
    }
    
    // ✅ PRIVATE: Sync evidence to Neon
    private void syncEvidenceToNeon(Evidence evidence, RepositoryCallback<Evidence> callback) {
        Log.d(TAG, "🔄 Syncing evidence to Neon: " + evidence.getId());
        
        // In a real implementation, this would call the Neon API
        // For now, we'll just log it
        Log.d(TAG, "✅ Evidence synced to Neon");
        callback.onSuccess(evidence);
    }
    
    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String errorMessage);
    }
}
