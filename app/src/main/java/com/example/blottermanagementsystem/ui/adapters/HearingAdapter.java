package com.example.blottermanagementsystem.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.entity.Hearing;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HearingAdapter extends RecyclerView.Adapter<HearingAdapter.ViewHolder> {
    
    private List<Hearing> hearings = new ArrayList<>();
    private OnHearingClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    
    public interface OnHearingClickListener {
        void onHearingClick(Hearing hearing);
    }
    
    public HearingAdapter(OnHearingClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_hearing, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hearing hearing = hearings.get(position);
        
        holder.tvHearingTitle.setText(hearing.getTitle());
        holder.tvHearingDate.setText(dateFormat.format(new Date(hearing.getHearingDate())));
        holder.tvHearingTime.setText(timeFormat.format(new Date(hearing.getHearingDate())));
        holder.tvLocation.setText("📍 " + hearing.getLocation());
        holder.tvStatus.setText(hearing.getStatus());
        
        // Color code status
        int statusColor;
        switch (hearing.getStatus()) {
            case "Scheduled":
                statusColor = R.color.info_blue;
                break;
            case "Completed":
                statusColor = R.color.success_green;
                break;
            case "Cancelled":
                statusColor = R.color.error_red;
                break;
            default:
                statusColor = R.color.text_secondary;
        }
        holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(statusColor));
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onHearingClick(hearing);
        });
    }
    
    @Override
    public int getItemCount() {
        return hearings.size();
    }
    
    public void setHearings(List<Hearing> hearings) {
        this.hearings = hearings;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHearingTitle, tvHearingDate, tvHearingTime, tvLocation, tvStatus;
        
        ViewHolder(View itemView) {
            super(itemView);
            // TODO: Add tvHearingTitle to item_hearing.xml
            // tvHearingTitle = itemView.findViewById(R.id.tvHearingTitle);
            tvHearingDate = itemView.findViewById(R.id.tvHearingDate);
            // TODO: Add tvHearingTime to item_hearing.xml
            // tvHearingTime = itemView.findViewById(R.id.tvHearingTime);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
