package com.example.blottermanagementsystem.ui.adapters;

import android.net.Uri;
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
import java.util.ArrayList;
import java.util.List;

public class EvidenceAdapter extends RecyclerView.Adapter<EvidenceAdapter.ViewHolder> {
    
    private List<Evidence> evidenceList = new ArrayList<>();
    private OnEvidenceClickListener listener;
    
    public interface OnEvidenceClickListener {
        void onEvidenceClick(Evidence evidence);
    }
    
    public EvidenceAdapter(OnEvidenceClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_evidence, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Evidence evidence = evidenceList.get(position);
        
        holder.tvEvidenceType.setText(evidence.getEvidenceType());
        holder.tvDescription.setText(evidence.getDescription());
        
        // Load image if photo URI exists
        if (evidence.getPhotoUri() != null && !evidence.getPhotoUri().isEmpty()) {
            holder.ivEvidence.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                .load(Uri.parse(evidence.getPhotoUri()))
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .centerCrop()
                .into(holder.ivEvidence);
        } else {
            holder.ivEvidence.setVisibility(View.GONE);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEvidenceClick(evidence);
        });
    }
    
    @Override
    public int getItemCount() {
        return evidenceList.size();
    }
    
    public void setEvidenceList(List<Evidence> evidenceList) {
        this.evidenceList = evidenceList;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEvidence;
        TextView tvEvidenceType, tvDescription;
        
        ViewHolder(View itemView) {
            super(itemView);
            // TODO: Add IDs to item_evidence.xml
            // ivEvidence = itemView.findViewById(R.id.ivEvidence);
            // tvEvidenceType = itemView.findViewById(R.id.tvEvidenceType);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}
