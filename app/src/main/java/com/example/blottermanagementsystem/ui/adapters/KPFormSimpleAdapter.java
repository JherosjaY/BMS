package com.example.blottermanagementsystem.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.ui.dialogs.KPFormsDialogFragment;

import java.util.List;

public class KPFormSimpleAdapter extends RecyclerView.Adapter<KPFormSimpleAdapter.ViewHolder> {

    private List<KPFormsDialogFragment.KPFormItem> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(KPFormsDialogFragment.KPFormItem item);
    }

    public KPFormSimpleAdapter(List<KPFormsDialogFragment.KPFormItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_kp_form_simple, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        KPFormsDialogFragment.KPFormItem item = items.get(position);
        holder.tvFormName.setText(item.name);
        holder.tvFormDescription.setText(item.description);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFormName;
        TextView tvFormDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFormName = itemView.findViewById(R.id.tvFormName);
            tvFormDescription = itemView.findViewById(R.id.tvFormDescription);
        }
    }
}
