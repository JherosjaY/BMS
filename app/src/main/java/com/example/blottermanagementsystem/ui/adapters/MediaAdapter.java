package com.example.blottermanagementsystem.ui.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.blottermanagementsystem.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {
    
    private List<MediaItem> mediaList;
    private OnMediaClickListener listener;
    
    public interface OnMediaClickListener {
        void onMediaClick(MediaItem item, int position);
        void onDeleteClick(int position);
    }
    
    public static class MediaItem {
        public Uri uri;
        public boolean isVideo;
        
        public MediaItem(Uri uri, boolean isVideo) {
            this.uri = uri;
            this.isVideo = isVideo;
        }
    }
    
    public MediaAdapter(List<MediaItem> mediaList, OnMediaClickListener listener) {
        this.mediaList = mediaList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_media, parent, false);
        return new MediaViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        MediaItem item = mediaList.get(position);
        holder.bind(item, position, listener);
    }
    
    @Override
    public int getItemCount() {
        return mediaList.size();
    }
    
    static class MediaViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivMedia, ivPlayIcon;
        private MaterialButton btnDelete;
        
        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            ivPlayIcon = itemView.findViewById(R.id.ivPlayIcon);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
        
        public void bind(MediaItem item, int position, OnMediaClickListener listener) {
            // Load image/video thumbnail
            Glide.with(itemView.getContext())
                .load(item.uri)
                .centerCrop()
                .into(ivMedia);
            
            // Show play icon if video
            ivPlayIcon.setVisibility(item.isVideo ? View.VISIBLE : View.GONE);
            
            // Click to view/play
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMediaClick(item, position);
                }
            });
            
            // Delete button
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(position);
                }
            });
        }
    }
}
