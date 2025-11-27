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

import java.io.File;
import java.util.List;

public class FilePreviewAdapter extends RecyclerView.Adapter<FilePreviewAdapter.ViewHolder> {
    
    private List<String> filePaths;
    
    public FilePreviewAdapter(List<String> filePaths) {
        this.filePaths = filePaths;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file_preview, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String filePath = filePaths.get(position);
        File file = new File(filePath);
        
        // Determine file type
        String fileName = file.getName();
        boolean isVideo = fileName.contains("video") || filePath.toLowerCase().endsWith(".mp4") 
                || filePath.toLowerCase().endsWith(".mkv") || filePath.toLowerCase().endsWith(".avi");
        
        holder.tvFileName.setText(file.getName());
        
        if (isVideo) {
            // Load video thumbnail
            loadVideoThumbnail(filePath, holder.ivPreview);
            holder.tvFileType.setText("🎥 Video");
        } else {
            // Load image thumbnail
            loadImageThumbnail(filePath, holder.ivPreview);
            holder.tvFileType.setText("📷 Photo");
        }
    }
    
    @Override
    public int getItemCount() {
        return filePaths.size();
    }
    
    private void loadImageThumbnail(String filePath, ImageView imageView) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            android.util.Log.e("FilePreview", "Error loading image: " + e.getMessage());
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
            android.util.Log.e("FilePreview", "Error loading video thumbnail: " + e.getMessage());
        }
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPreview;
        TextView tvFileName;
        TextView tvFileType;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPreview = itemView.findViewById(R.id.ivFilePreview);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileType = itemView.findViewById(R.id.tvFileType);
        }
    }
}
