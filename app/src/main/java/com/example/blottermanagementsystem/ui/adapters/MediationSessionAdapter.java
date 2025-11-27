package com.example.blottermanagementsystem.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.entity.MediationSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MediationSessionAdapter extends RecyclerView.Adapter<MediationSessionAdapter.ViewHolder> {
    
    private List<MediationSession> sessions = new ArrayList<>();
    private OnSessionClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
    
    public interface OnSessionClickListener {
        void onSessionClick(MediationSession session);
    }
    
    public MediationSessionAdapter(OnSessionClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_mediation_session, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediationSession session = sessions.get(position);
        
        holder.tvSessionTitle.setText("Session #" + session.getId());
        holder.tvSessionDate.setText(dateFormat.format(new Date(session.getSessionDate())));
        holder.tvMediator.setText("Mediator: " + session.getMediatorName());
        holder.tvStatus.setText(session.getStatus());
        
        // Color code status
        int statusColor;
        switch (session.getStatus()) {
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
            if (listener != null) listener.onSessionClick(session);
        });
    }
    
    @Override
    public int getItemCount() {
        return sessions.size();
    }
    
    public void setSessions(List<MediationSession> sessions) {
        this.sessions = sessions;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSessionTitle, tvSessionDate, tvMediator, tvStatus;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvSessionTitle = itemView.findViewById(R.id.tvSessionTitle);
            tvSessionDate = itemView.findViewById(R.id.tvSessionDate);
            tvMediator = itemView.findViewById(R.id.tvMediator);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
