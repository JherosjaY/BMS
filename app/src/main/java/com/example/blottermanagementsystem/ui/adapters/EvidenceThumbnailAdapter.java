package com.example.blottermanagementsystem.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.entity.Evidence;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EvidenceThumbnailAdapter extends RecyclerView.Adapter<EvidenceThumbnailAdapter.ViewHolder> {
    
    private Context context;
    private List<Evidence> evidenceList;
    
    public EvidenceThumbnailAdapter(Context context, List<Evidence> evidenceList) {
        this.context = context;
        this.evidenceList = evidenceList;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_evidence_thumbnail, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Evidence evidence = evidenceList.get(position);
        
        Log.d("EvidenceAdapter", "Position: " + position + ", PhotoUris: " + evidence.getPhotoUris() + ", VideoUris: " + evidence.getVideoUris());
        
        // Reset views
        holder.tvDuration.setVisibility(View.GONE);
        
        // Load image or video thumbnail
        if (evidence.getPhotoUris() != null && !evidence.getPhotoUris().isEmpty()) {
            // It's an image
            String[] photoUris = evidence.getPhotoUris().split(",");
            if (photoUris.length > 0) {
                String photoPath = photoUris[0].trim();
                Log.d("EvidenceAdapter", "Loading image: " + photoPath);
                
                // Try as file path first
                File imageFile = new File(photoPath);
                if (imageFile.exists()) {
                    Log.d("EvidenceAdapter", "Image file exists: " + imageFile.getAbsolutePath());
                    Glide.with(context)
                        .load(imageFile)
                        .centerCrop()
                        .error(R.drawable.ic_image_placeholder)
                        .into(holder.ivThumbnail);
                    Log.d("EvidenceAdapter", "Image loaded successfully from file");
                } else {
                    // Try as content URI
                    Log.d("EvidenceAdapter", "File not found, trying as content URI: " + photoPath);
                    try {
                        android.net.Uri contentUri = android.net.Uri.parse(photoPath);
                        Glide.with(context)
                            .load(contentUri)
                            .centerCrop()
                            .error(R.drawable.ic_image_placeholder)
                            .into(holder.ivThumbnail);
                        Log.d("EvidenceAdapter", "Image loaded successfully from content URI");
                    } catch (Exception e) {
                        Log.e("EvidenceAdapter", "Failed to load image: " + e.getMessage());
                        holder.ivThumbnail.setImageResource(R.drawable.ic_image_placeholder);
                    }
                }
            }
        } else if (evidence.getVideoUris() != null && !evidence.getVideoUris().isEmpty()) {
            // It's a video - generate thumbnail and show duration
            String[] videoUris = evidence.getVideoUris().split(",");
            if (videoUris.length > 0) {
                String videoPath = videoUris[0].trim();
                Log.d("EvidenceAdapter", "Loading video: " + videoPath);
                
                // Try as file path first
                File videoFile = new File(videoPath);
                if (videoFile.exists()) {
                    Log.d("EvidenceAdapter", "Video file exists: " + videoFile.getAbsolutePath());
                    // Generate video thumbnail
                    generateVideoThumbnail(videoFile, holder.ivThumbnail);
                    
                    // Get video duration
                    String duration = getVideoDuration(videoPath);
                    holder.tvDuration.setText(duration);
                    holder.tvDuration.setVisibility(View.VISIBLE);
                    Log.d("EvidenceAdapter", "Video loaded successfully from file, duration: " + duration);
                } else {
                    // Try as content URI
                    Log.d("EvidenceAdapter", "File not found, trying as content URI: " + videoPath);
                    try {
                        android.net.Uri contentUri = android.net.Uri.parse(videoPath);
                        generateVideoThumbnailFromUri(contentUri, holder.ivThumbnail);
                        
                        // Get video duration from URI
                        String duration = getVideoDurationFromUri(contentUri);
                        holder.tvDuration.setText(duration);
                        holder.tvDuration.setVisibility(View.VISIBLE);
                        Log.d("EvidenceAdapter", "Video loaded successfully from content URI, duration: " + duration);
                    } catch (Exception e) {
                        Log.e("EvidenceAdapter", "Failed to load video: " + e.getMessage());
                        holder.ivThumbnail.setImageResource(R.drawable.ic_video_placeholder);
                    }
                }
            }
        } else {
            Log.w("EvidenceAdapter", "No photo or video URIs found for evidence at position " + position);
            holder.ivThumbnail.setImageResource(R.drawable.ic_image_placeholder);
        }
        
        // Set title (use evidence type or description)
        String title = evidence.getEvidenceType() != null ? evidence.getEvidenceType() : "Evidence";
        holder.tvTitle.setText(title);
    }
    
    @Override
    public int getItemCount() {
        return evidenceList != null ? evidenceList.size() : 0;
    }
    
    /**
     * Generate video thumbnail from video file
     */
    private void generateVideoThumbnail(File videoFile, ImageView imageView) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(videoFile.getAbsolutePath());
            Bitmap bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST);
            retriever.release();
            
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            // Fallback to default image
            imageView.setImageResource(R.drawable.ic_video_placeholder);
        }
    }
    
    /**
     * Generate video thumbnail from content URI
     */
    private void generateVideoThumbnailFromUri(android.net.Uri videoUri, ImageView imageView) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, videoUri);
            Bitmap bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST);
            retriever.release();
            
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.ic_video_placeholder);
            }
        } catch (Exception e) {
            Log.e("EvidenceAdapter", "Error generating thumbnail from URI: " + e.getMessage());
            imageView.setImageResource(R.drawable.ic_video_placeholder);
        }
    }
    
    /**
     * Get video duration in MM:SS format
     */
    private String getVideoDuration(String videoPath) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(videoPath);
            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            retriever.release();
            
            if (duration != null) {
                long durationMs = Long.parseLong(duration);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) - TimeUnit.MINUTES.toSeconds(minutes);
                return String.format("%02d:%02d", minutes, seconds);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "00:00";
    }
    
    /**
     * Get video duration from content URI in MM:SS format
     */
    private String getVideoDurationFromUri(android.net.Uri videoUri) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, videoUri);
            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            retriever.release();
            
            if (duration != null) {
                long durationMs = Long.parseLong(duration);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) - TimeUnit.MINUTES.toSeconds(minutes);
                return String.format("%02d:%02d", minutes, seconds);
            }
        } catch (Exception e) {
            Log.e("EvidenceAdapter", "Error getting duration from URI: " + e.getMessage());
        }
        return "00:00";
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle;
        TextView tvDuration;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivEvidenceThumbnail);
            tvTitle = itemView.findViewById(R.id.tvEvidenceTitle);
            tvDuration = itemView.findViewById(R.id.tvVideoDuration);
        }
    }
}
