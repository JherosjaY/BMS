package com.example.blottermanagementsystem.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.entity.Officer;
import com.google.android.material.checkbox.MaterialCheckBox;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectableOfficerAdapter extends RecyclerView.Adapter<SelectableOfficerAdapter.OfficerViewHolder> {
    
    private List<Officer> officers;
    private Set<Integer> selectedOfficerIds;
    private OnOfficerSelectionListener listener;
    private int maxSelections;
    
    public interface OnOfficerSelectionListener {
        void onSelectionChanged(int selectedCount);
    }
    
    public SelectableOfficerAdapter(List<Officer> officers, Set<Integer> preSelectedIds, int maxSelections, OnOfficerSelectionListener listener) {
        this.officers = officers != null ? officers : new ArrayList<>();
        this.selectedOfficerIds = preSelectedIds != null ? new HashSet<>(preSelectedIds) : new HashSet<>();
        this.maxSelections = maxSelections;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public OfficerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_officer_selectable, parent, false);
        return new OfficerViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull OfficerViewHolder holder, int position) {
        Officer officer = officers.get(position);
        boolean isSelected = selectedOfficerIds.contains(officer.getId());
        
        // Set officer info
        holder.tvName.setText(officer.getName());
        holder.tvRank.setText(officer.getRank() != null ? officer.getRank() : "N/A");
        holder.tvBadgeNumber.setText("Badge: " + (officer.getBadgeNumber() != null ? officer.getBadgeNumber() : "N/A"));
        
        // Set avatar initials
        String initials = getInitials(officer.getName());
        holder.tvAvatar.setText(initials);
        
        // Set checkbox state
        holder.checkboxOfficer.setChecked(isSelected);
        
        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            if (isSelected) {
                // Deselect
                selectedOfficerIds.remove(officer.getId());
                holder.checkboxOfficer.setChecked(false);
            } else {
                // Check if max selections reached
                if (selectedOfficerIds.size() >= maxSelections) {
                    android.widget.Toast.makeText(
                        holder.itemView.getContext(),
                        "Maximum " + maxSelections + " officers can be selected",
                        android.widget.Toast.LENGTH_SHORT
                    ).show();
                    return;
                }
                // Select
                selectedOfficerIds.add(officer.getId());
                holder.checkboxOfficer.setChecked(true);
            }
            
            if (listener != null) {
                listener.onSelectionChanged(selectedOfficerIds.size());
            }
        });
    }
    
    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "PO";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        } else if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return "PO";
    }
    
    public Set<Integer> getSelectedOfficerIds() {
        return new HashSet<>(selectedOfficerIds);
    }
    
    public List<Officer> getSelectedOfficers() {
        List<Officer> selected = new ArrayList<>();
        for (Officer officer : officers) {
            if (selectedOfficerIds.contains(officer.getId())) {
                selected.add(officer);
            }
        }
        return selected;
    }
    
    @Override
    public int getItemCount() {
        return officers.size();
    }
    
    static class OfficerViewHolder extends RecyclerView.ViewHolder {
        MaterialCheckBox checkboxOfficer;
        TextView tvAvatar, tvName, tvRank, tvBadgeNumber;
        
        OfficerViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxOfficer = itemView.findViewById(R.id.checkboxOfficer);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvBadgeNumber = itemView.findViewById(R.id.tvBadgeNumber);
        }
    }
}

