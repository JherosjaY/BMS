package com.example.blottermanagementsystem.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.entity.BlotterReport;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentCaseAdapter extends RecyclerView.Adapter<RecentCaseAdapter.CaseViewHolder> {
    
    private List<BlotterReport> cases;
    private OnCaseClickListener listener;
    
    public interface OnCaseClickListener {
        void onCaseClick(BlotterReport report);
    }
    
    public RecentCaseAdapter(List<BlotterReport> cases, OnCaseClickListener listener) {
        this.cases = cases;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public CaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_recent_case, parent, false);
        return new CaseViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CaseViewHolder holder, int position) {
        BlotterReport report = cases.get(position);
        holder.bind(report, listener);
    }
    
    @Override
    public int getItemCount() {
        return cases.size();
    }
    
    public void updateCases(List<BlotterReport> newCases) {
        this.cases = newCases;
        notifyDataSetChanged();
    }
    
    static class CaseViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private ImageView ivCaseIcon;
        private TextView tvCaseNumber, tvIncidentType, tvDate, tvStatus;
        
        public CaseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardRecentCase);
            ivCaseIcon = itemView.findViewById(R.id.ivCaseIcon);
            tvCaseNumber = itemView.findViewById(R.id.tvCaseNumber);
            tvIncidentType = itemView.findViewById(R.id.tvIncidentType);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
        
        public void bind(BlotterReport report, OnCaseClickListener listener) {
            tvCaseNumber.setText(report.getCaseNumber());
            tvIncidentType.setText(report.getIncidentType());
            tvStatus.setText(report.getStatus());
            
            // Format date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            tvDate.setText(dateFormat.format(new Date(report.getIncidentDate())));
            
            // Set status color
            int statusColor;
            switch (report.getStatus()) {
                case "Pending":
                    statusColor = itemView.getContext().getColor(R.color.warning_yellow);
                    break;
                case "Ongoing":
                case "Under Investigation":
                    statusColor = itemView.getContext().getColor(R.color.info_blue);
                    break;
                case "Resolved":
                case "Closed":
                    statusColor = itemView.getContext().getColor(R.color.success_green);
                    break;
                default:
                    statusColor = itemView.getContext().getColor(R.color.text_secondary);
            }
            
            tvStatus.setTextColor(statusColor);
            
            // Set icon based on incident type
            int iconRes = R.drawable.ic_folder;
            switch (report.getIncidentType().toLowerCase()) {
                case "theft":
                case "robbery":
                    iconRes = R.drawable.ic_warning;
                    break;
                case "assault":
                case "violence":
                    iconRes = R.drawable.ic_alert;
                    break;
                case "dispute":
                    iconRes = R.drawable.ic_people;
                    break;
                default:
                    iconRes = R.drawable.ic_folder;
            }
            ivCaseIcon.setImageResource(iconRes);
            
            // Click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCaseClick(report);
                }
            });
        }
    }
}
