package com.example.blottermanagementsystem.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.entity.LegalDocument;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LegalDocumentAdapter extends RecyclerView.Adapter<LegalDocumentAdapter.ViewHolder> {
    
    private List<LegalDocument> documents = new ArrayList<>();
    private OnDocumentClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    
    public interface OnDocumentClickListener {
        void onDocumentClick(LegalDocument document);
    }
    
    public LegalDocumentAdapter(OnDocumentClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_legal_document, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LegalDocument document = documents.get(position);
        
        holder.tvDocumentType.setText(document.getDocumentType());
        holder.tvTitle.setText(document.getTitle());
        holder.tvDocumentNumber.setText("Doc #: " + document.getDocumentNumber());
        holder.tvCreatedDate.setText(dateFormat.format(new Date(document.getCreatedAt())));
        holder.tvStatus.setText(document.getStatus());
        
        // Color code status
        int statusColor;
        switch (document.getStatus()) {
            case "Pending":
                statusColor = R.color.warning_yellow;
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
            if (listener != null) listener.onDocumentClick(document);
        });
    }
    
    @Override
    public int getItemCount() {
        return documents.size();
    }
    
    public void setDocuments(List<LegalDocument> documents) {
        this.documents = documents;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDocumentType, tvTitle, tvDocumentNumber, tvCreatedDate, tvStatus;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvDocumentType = itemView.findViewById(R.id.tvDocumentType);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDocumentNumber = itemView.findViewById(R.id.tvDocumentNumber);
            tvCreatedDate = itemView.findViewById(R.id.tvCreatedDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
