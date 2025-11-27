package com.example.blottermanagementsystem.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.entity.Witness;
import java.util.List;

public class WitnessAdapter extends RecyclerView.Adapter<WitnessAdapter.WitnessViewHolder> {
    
    private List<Witness> witnesses;
    private OnWitnessDeleteListener deleteListener;
    
    public interface OnWitnessDeleteListener {
        void onDelete(Witness witness);
    }
    
    public WitnessAdapter(List<Witness> witnesses, OnWitnessDeleteListener deleteListener) {
        this.witnesses = witnesses;
        this.deleteListener = deleteListener;
    }
    
    @Override
    public WitnessViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_witness, parent, false);
        return new WitnessViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(WitnessViewHolder holder, int position) {
        Witness witness = witnesses.get(position);
        holder.bind(witness);
    }
    
    @Override
    public int getItemCount() {
        return witnesses.size();
    }
    
    public void updateList(List<Witness> newWitnesses) {
        this.witnesses = newWitnesses;
        notifyDataSetChanged();
    }
    
    public class WitnessViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvContact, tvAddress, tvStatement;
        private ImageButton btnDelete;
        
        public WitnessViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvStatement = itemView.findViewById(R.id.tvStatement);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
        
        public void bind(Witness witness) {
            tvName.setText("👤 " + witness.getName());
            tvContact.setText("📞 " + (witness.getContactNumber().isEmpty() ? "N/A" : witness.getContactNumber()));
            tvAddress.setText("📍 " + (witness.getAddress().isEmpty() ? "N/A" : witness.getAddress()));
            tvStatement.setText("📝 " + (witness.getStatement().isEmpty() ? "No statement" : witness.getStatement()));
            
            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(witness);
                }
            });
        }
    }
}
