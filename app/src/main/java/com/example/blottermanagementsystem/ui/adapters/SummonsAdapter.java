package com.example.blottermanagementsystem.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.entity.Summons;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SummonsAdapter extends RecyclerView.Adapter<SummonsAdapter.ViewHolder> {
    
    private List<Summons> summonsList = new ArrayList<>();
    private OnSummonsClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    
    public interface OnSummonsClickListener {
        void onSummonsClick(Summons summons);
    }
    
    public SummonsAdapter(OnSummonsClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_summons, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Summons summons = summonsList.get(position);
        
        holder.tvSummonsNumber.setText("Summons #" + summons.getSummonsNumber());
        holder.tvRespondent.setText("To: " + summons.getRespondentName());
        holder.tvIssueDate.setText("Issued: " + dateFormat.format(new Date(summons.getIssuedDate())));
        holder.tvHearingDate.setText("Hearing: " + dateFormat.format(new Date(summons.getHearingDate())));
        holder.tvStatus.setText(summons.getStatus());
        
        // Color code status
        int statusColor;
        switch (summons.getStatus()) {
            case "Pending":
                statusColor = R.color.warning_yellow;
                break;
            case "Served":
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
            if (listener != null) listener.onSummonsClick(summons);
        });
    }
    
    @Override
    public int getItemCount() {
        return summonsList.size();
    }
    
    public void setSummonsList(List<Summons> summonsList) {
        this.summonsList = summonsList;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSummonsNumber, tvRespondent, tvIssueDate, tvHearingDate, tvStatus;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvSummonsNumber = itemView.findViewById(R.id.tvSummonsNumber);
            tvRespondent = itemView.findViewById(R.id.tvRespondent);
            tvIssueDate = itemView.findViewById(R.id.tvIssueDate);
            tvHearingDate = itemView.findViewById(R.id.tvHearingDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
