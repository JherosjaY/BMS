package com.example.blottermanagementsystem.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.entity.Officer;
import com.google.android.material.chip.Chip;

import java.util.List;

public class OfficerAdapter extends RecyclerView.Adapter<OfficerAdapter.OfficerViewHolder> {
    
    private List<Officer> officers;
    private OnOfficerClickListener listener;
    
    public interface OnOfficerClickListener {
        void onOfficerClick(Officer officer);
    }
    
    public OfficerAdapter(List<Officer> officers, OnOfficerClickListener listener) {
        this.officers = officers;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public OfficerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_officer, parent, false);
        return new OfficerViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull OfficerViewHolder holder, int position) {
        Officer officer = officers.get(position);
        holder.bind(officer, listener);
    }
    
    @Override
    public int getItemCount() {
        return officers.size();
    }
    
    public void updateOfficers(List<Officer> newOfficers) {
        this.officers = newOfficers;
        notifyDataSetChanged();
    }
    
    static class OfficerViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAvatar, tvName, tvRank, tvContact;
        private Chip chipStatus;
        
        public OfficerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvContact = itemView.findViewById(R.id.tvContact);
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }
        
        public void bind(Officer officer, OnOfficerClickListener listener) {
            tvName.setText(officer.getName());
            tvRank.setText(officer.getRank());
            tvContact.setText(officer.getContactNumber() != null ? officer.getContactNumber() : "No contact");
            
            // Set avatar initials from full name
            String initials = "";
            String name = officer.getName();
            if (name != null && !name.isEmpty()) {
                String[] nameParts = name.split(" ");
                if (nameParts.length > 0 && !nameParts[0].isEmpty()) {
                    initials += nameParts[0].charAt(0);
                }
                if (nameParts.length > 1 && !nameParts[nameParts.length - 1].isEmpty()) {
                    initials += nameParts[nameParts.length - 1].charAt(0);
                }
            }
            tvAvatar.setText(initials.toUpperCase());
            
            // Set status
            if (officer.isActive()) {
                chipStatus.setText("Active");
                chipStatus.setChipBackgroundColorResource(R.color.success_green);
            } else {
                chipStatus.setText("Inactive");
                chipStatus.setChipBackgroundColorResource(R.color.text_secondary);
            }
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOfficerClick(officer);
                }
            });
        }
    }
}
