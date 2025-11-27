package com.example.blottermanagementsystem.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class BlotterReportAdapter extends RecyclerView.Adapter<BlotterReportAdapter.ReportViewHolder> {
    
    private List<BlotterReport> reports;
    private OnReportClickListener listener;
    
    public interface OnReportClickListener {
        void onReportClick(BlotterReport report);
    }
    
    public BlotterReportAdapter(List<BlotterReport> reports, OnReportClickListener listener) {
        this.reports = reports;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_blotter_report, parent, false);
        return new ReportViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        BlotterReport report = reports.get(position);
        holder.bind(report, listener);
    }
    
    @Override
    public int getItemCount() {
        return reports.size();
    }
    
    public void updateReports(List<BlotterReport> newReports) {
        this.reports = newReports;
        notifyDataSetChanged();
    }
    
    static class ReportViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView tvCaseNumber, tvIncidentType, tvStatus, tvDate, tvLocation;
        private View statusIndicator;
        
        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardReport);
            tvCaseNumber = itemView.findViewById(R.id.tvCaseNumber);
            tvIncidentType = itemView.findViewById(R.id.tvIncidentType);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
        }
        
        public void bind(BlotterReport report, OnReportClickListener listener) {
            tvCaseNumber.setText(report.getCaseNumber());
            tvIncidentType.setText(report.getIncidentType());
            tvStatus.setText(report.getStatus());
            tvLocation.setText("📍 " + report.getLocation());
            
            // Format date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            tvDate.setText(dateFormat.format(new Date(report.getIncidentDate())));
            
            // Set status color - Color coding for all statuses
            int statusColor;
            switch (report.getStatus()) {
                case "Pending":
                    // 🔵 Pending - Electric Blue
                    statusColor = itemView.getContext().getColor(R.color.electric_blue);
                    break;
                case "Assigned":
                    // 🔵 Assigned - Electric Blue
                    statusColor = itemView.getContext().getColor(R.color.electric_blue);
                    break;
                case "Ongoing":
                case "Under Investigation":
                    // 🟡 Ongoing - Yellow
                    statusColor = itemView.getContext().getColor(R.color.warning_yellow);
                    break;
                case "Resolved":
                case "Closed":
                    // 🟢 Resolved - Green
                    statusColor = itemView.getContext().getColor(R.color.success_green);
                    break;
                default:
                    // ⚪ Unknown - Gray
                    statusColor = itemView.getContext().getColor(R.color.text_secondary);
            }
            
            // Set text color and background color for status badge
            tvStatus.setTextColor(android.graphics.Color.WHITE);  // White text for all badges
            
            // Create colored background for status badge
            android.graphics.drawable.GradientDrawable badgeBackground = new android.graphics.drawable.GradientDrawable();
            badgeBackground.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            badgeBackground.setColor(statusColor);
            badgeBackground.setCornerRadius(8);
            badgeBackground.setStroke(1, android.graphics.Color.argb(50, 255, 255, 255));
            tvStatus.setBackground(badgeBackground);
            
            if (statusIndicator != null) {
                statusIndicator.setBackgroundColor(statusColor);
            }
            
            // Click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReportClick(report);
                }
            });
        }
    }
}
