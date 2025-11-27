package com.example.blottermanagementsystem.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.entity.Suspect;
import java.util.List;

public class SuspectAdapter extends RecyclerView.Adapter<SuspectAdapter.SuspectViewHolder> {
    
    private List<Suspect> suspects;
    private OnSuspectDeleteListener deleteListener;
    
    public interface OnSuspectDeleteListener {
        void onDelete(Suspect suspect);
    }
    
    public SuspectAdapter(List<Suspect> suspects, OnSuspectDeleteListener deleteListener) {
        this.suspects = suspects;
        this.deleteListener = deleteListener;
    }
    
    @Override
    public SuspectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_suspect, parent, false);
        return new SuspectViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(SuspectViewHolder holder, int position) {
        Suspect suspect = suspects.get(position);
        holder.bind(suspect);
    }
    
    @Override
    public int getItemCount() {
        return suspects.size();
    }
    
    public void updateList(List<Suspect> newSuspects) {
        this.suspects = newSuspects;
        notifyDataSetChanged();
    }
    
    public class SuspectViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvAge, tvAddress, tvDescription;
        private ImageButton btnDelete;
        
        public SuspectViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAge = itemView.findViewById(R.id.tvAge);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
        
        public void bind(Suspect suspect) {
            tvName.setText("🚨 " + suspect.getName());
            tvAge.setText("👤 Age: " + (suspect.getAge() > 0 ? suspect.getAge() : "N/A"));
            tvAddress.setText("📍 " + (suspect.getAddress().isEmpty() ? "N/A" : suspect.getAddress()));
            tvDescription.setText("📝 " + (suspect.getDescription().isEmpty() ? "No description" : suspect.getDescription()));
            
            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(suspect);
                }
            });
        }
    }
}
