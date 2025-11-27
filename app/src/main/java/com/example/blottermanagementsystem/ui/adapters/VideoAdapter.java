package com.example.blottermanagementsystem.ui.adapters;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    
    private final List<Uri> videoList;
    private final OnVideoClickListener listener;
    private final boolean showDeleteButton;
    
    public interface OnVideoClickListener {
        void onVideoClick(Uri uri);
        void onVideoDelete(int position);
    }
    
    public VideoAdapter(List<Uri> videoList, OnVideoClickListener listener) {
        this(videoList, listener, true);
    }
    
    public VideoAdapter(List<Uri> videoList, OnVideoClickListener listener, boolean showDeleteButton) {
        this.videoList = videoList;
        this.listener = listener;
        this.showDeleteButton = showDeleteButton;
    }
    
    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Uri videoUri = videoList.get(position);
        
        // Set default placeholder first
        holder.ivThumbnail.setImageResource(R.drawable.ic_video_placeholder);
        holder.tvDuration.setText("00:00");
        
        // Load video thumbnail and duration in background
        new Thread(() -> {
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(holder.itemView.getContext(), videoUri);
                
                // Get thumbnail
                Bitmap thumbnail = retriever.getFrameAtTime(0); // First frame
                
                // Get duration
                String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long duration = 0;
                if (durationStr != null) {
                    try {
                        duration = Long.parseLong(durationStr);
                    } catch (NumberFormatException e) {
                        android.util.Log.w("VideoAdapter", "Invalid duration: " + durationStr);
                    }
                }
                
                retriever.release();
                
                // Update UI on main thread
                final Bitmap finalThumbnail = thumbnail;
                final long finalDuration = duration;
                holder.itemView.post(() -> {
                    if (finalThumbnail != null) {
                        holder.ivThumbnail.setImageBitmap(finalThumbnail);
                    }
                    if (finalDuration > 0) {
                        holder.tvDuration.setText(formatDuration(finalDuration));
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("VideoAdapter", "Error loading video thumbnail: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
        
        holder.itemView.setOnClickListener(v -> listener.onVideoClick(videoUri));
        
        if (showDeleteButton) {
            if (holder.deleteContainer != null) {
                holder.deleteContainer.setVisibility(View.VISIBLE);
            }
            // Use getBindingAdapterPosition() to get the correct position at click time
            holder.btnDelete.setOnClickListener(v -> {
                int currentPosition = holder.getBindingAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    listener.onVideoDelete(currentPosition);
                }
            });
        } else {
            if (holder.deleteContainer != null) {
                holder.deleteContainer.setVisibility(View.GONE);
            }
        }
    }
    
    @Override
    public int getItemCount() {
        return videoList.size();
    }
    
    private String formatDuration(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvDuration;
        ImageButton btnDelete;
        View deleteContainer;
        
        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            deleteContainer = btnDelete.getParent() instanceof View ? (View) btnDelete.getParent() : null;
        }
    }
}
