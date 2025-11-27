package com.example.blottermanagementsystem.ui.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.blottermanagementsystem.R;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    
    private final List<Uri> imageList;
    private final OnImageClickListener listener;
    private final boolean showDeleteButton;
    
    public interface OnImageClickListener {
        void onImageClick(Uri uri);
        void onImageDelete(int position);
    }
    
    public ImageAdapter(List<Uri> imageList, OnImageClickListener listener) {
        this(imageList, listener, true);
    }
    
    public ImageAdapter(List<Uri> imageList, OnImageClickListener listener, boolean showDeleteButton) {
        this.imageList = imageList;
        this.listener = listener;
        this.showDeleteButton = showDeleteButton;
    }
    
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri imageUri = imageList.get(position);
        
        // Load image with Glide
        Glide.with(holder.itemView.getContext())
            .load(imageUri)
            .centerCrop()
            .placeholder(R.drawable.ic_image_placeholder)
            .into(holder.ivImage);
        
        holder.ivImage.setOnClickListener(v -> listener.onImageClick(imageUri));
        
        if (showDeleteButton) {
            if (holder.deleteContainer != null) {
                holder.deleteContainer.setVisibility(View.VISIBLE);
            }
            // Use getBindingAdapterPosition() to get the correct position at click time
            holder.btnDelete.setOnClickListener(v -> {
                int currentPosition = holder.getBindingAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    listener.onImageDelete(currentPosition);
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
        return imageList.size();
    }
    
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        ImageButton btnDelete;
        View deleteContainer;
        
        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            deleteContainer = btnDelete.getParent() instanceof View ? (View) btnDelete.getParent() : null;
        }
    }
}
