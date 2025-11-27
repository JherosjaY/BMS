package com.example.blottermanagementsystem.ui.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.entity.Evidence;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EvidenceListAdapter extends RecyclerView.Adapter<EvidenceListAdapter.ViewHolder> {
    
    private List<Evidence> evidenceList;
    private boolean isReadOnly;
    
    public EvidenceListAdapter(List<Evidence> evidenceList, boolean isReadOnly) {
        this.evidenceList = evidenceList;
        this.isReadOnly = isReadOnly;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_evidence_card, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Evidence evidence = evidenceList.get(position);
        
        // Parse description (format: "Title\nDescription")
        String[] parts = evidence.getDescription().split("\n", 2);
        String title = parts[0];
        String description = parts.length > 1 ? parts[1] : "";
        
        holder.tvTitle.setText(title);
        holder.tvDescription.setText(description.isEmpty() ? "No description" : description);
        holder.tvType.setText(evidence.getEvidenceType().toUpperCase());
        holder.tvCollectedBy.setText("Collected by: " + evidence.getCollectedBy());
        
        // Format date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        String dateStr = sdf.format(new Date(evidence.getCollectedDate()));
        holder.tvDate.setText(dateStr);
        
        // Load thumbnail from file paths
        String photoUris = evidence.getPhotoUris();
        if (photoUris != null && !photoUris.isEmpty()) {
            String[] filePaths = photoUris.split(",");
            if (filePaths.length > 0) {
                loadThumbnail(filePaths[0], holder.ivThumbnail);
            }
        }
    }
    
    @Override
    public int getItemCount() {
        return evidenceList.size();
    }
    
    private void loadThumbnail(String filePath, ImageView imageView) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                imageView.setImageResource(R.drawable.ic_add_photo);
                return;
            }
            
            boolean isVideo = filePath.toLowerCase().endsWith(".mp4") 
                    || filePath.toLowerCase().endsWith(".mkv")
                    || filePath.toLowerCase().endsWith(".avi");
            
            if (isVideo) {
                loadVideoThumbnail(filePath, imageView);
            } else {
                loadImageThumbnail(filePath, imageView);
            }
        } catch (Exception e) {
            android.util.Log.e("EvidenceAdapter", "Error loading thumbnail: " + e.getMessage());
            imageView.setImageResource(R.drawable.ic_add_photo);
        }
    }
    
    private void loadImageThumbnail(String filePath, ImageView imageView) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            android.util.Log.e("EvidenceAdapter", "Error loading image: " + e.getMessage());
        }
    }
    
    private void loadVideoThumbnail(String filePath, ImageView imageView) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath);
            Bitmap bitmap = retriever.getFrameAtTime(0);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
            retriever.release();
        } catch (Exception e) {
            android.util.Log.e("EvidenceAdapter", "Error loading video thumbnail: " + e.getMessage());
        }
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvType;
        TextView tvCollectedBy;
        TextView tvDate;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivEvidenceThumbnail);
            tvTitle = itemView.findViewById(R.id.tvEvidenceTitle);
            tvDescription = itemView.findViewById(R.id.tvEvidenceDescription);
            tvType = itemView.findViewById(R.id.tvEvidenceType);
            tvCollectedBy = itemView.findViewById(R.id.tvCollectedBy);
            tvDate = itemView.findViewById(R.id.tvCollectionDate);
        }
    }
}
