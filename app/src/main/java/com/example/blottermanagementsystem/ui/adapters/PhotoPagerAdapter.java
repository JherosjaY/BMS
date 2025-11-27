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
import java.util.ArrayList;
import java.util.List;

public class PhotoPagerAdapter extends RecyclerView.Adapter<PhotoPagerAdapter.ViewHolder> {
    
    private List<Uri> photos = new ArrayList<>();
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_photo_pager, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri photoUri = photos.get(position);
        
        Glide.with(holder.itemView.getContext())
            .load(photoUri)
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_placeholder)
            .fitCenter()
            .into(holder.ivPhoto);
    }
    
    @Override
    public int getItemCount() {
        return photos.size();
    }
    
    public void setPhotos(List<Uri> photos) {
        this.photos = photos;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        
        ViewHolder(View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
        }
    }
}
