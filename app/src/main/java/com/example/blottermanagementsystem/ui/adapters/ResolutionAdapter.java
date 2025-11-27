package com.example.blottermanagementsystem.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.entity.Resolution;
import java.util.List;

public class ResolutionAdapter extends RecyclerView.Adapter<ResolutionAdapter.ResolutionViewHolder> {
    
    private List<Resolution> resolutions;
    private OnResolutionDeleteListener deleteListener;
    
    public interface OnResolutionDeleteListener {
        void onDelete(Resolution resolution);
    }
    
    public ResolutionAdapter(List<Resolution> resolutions, OnResolutionDeleteListener deleteListener) {
        this.resolutions = resolutions;
        this.deleteListener = deleteListener;
    }
    
    @Override
    public ResolutionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_resolution, parent, false);
        return new ResolutionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(ResolutionViewHolder holder, int position) {
        Resolution resolution = resolutions.get(position);
        holder.bind(resolution);
    }
    
    @Override
    public int getItemCount() {
        return resolutions.size();
    }
    
    public void updateList(List<Resolution> newResolutions) {
        this.resolutions = newResolutions;
        notifyDataSetChanged();
    }
    
    public class ResolutionViewHolder extends RecyclerView.ViewHolder {
        private TextView tvType, tvDescription, tvRecommendation;
        private ImageButton btnDelete;
        
        public ResolutionViewHolder(View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvRecommendation = itemView.findViewById(R.id.tvRecommendation);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
        
        public void bind(Resolution resolution) {
            tvType.setText("📋 " + resolution.getResolutionType());
            tvDescription.setText("📝 " + (resolution.getResolutionDetails() == null || resolution.getResolutionDetails().isEmpty() ? "No details" : resolution.getResolutionDetails()));
            tvRecommendation.setText("💡 Resolved by: " + resolution.getResolvedBy());
            
            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(resolution);
                }
            });
        }
    }
}
