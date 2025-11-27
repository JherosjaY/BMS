package com.example.blottermanagementsystem.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.entity.KPForm;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class KPFormAdapter extends RecyclerView.Adapter<KPFormAdapter.KPFormViewHolder> {
    
    private List<KPForm> forms;
    private OnKPFormDeleteListener deleteListener;
    private OnKPFormViewListener viewListener;
    
    public interface OnKPFormDeleteListener {
        void onDelete(KPForm form);
    }
    
    public interface OnKPFormViewListener {
        void onView(KPForm form);
    }
    
    public KPFormAdapter(List<KPForm> forms, OnKPFormDeleteListener deleteListener, OnKPFormViewListener viewListener) {
        this.forms = forms;
        this.deleteListener = deleteListener;
        this.viewListener = viewListener;
    }
    
    @Override
    public KPFormViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_kp_form, parent, false);
        return new KPFormViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(KPFormViewHolder holder, int position) {
        KPForm form = forms.get(position);
        holder.bind(form);
    }
    
    @Override
    public int getItemCount() {
        return forms.size();
    }
    
    public void updateList(List<KPForm> newForms) {
        this.forms = newForms;
        notifyDataSetChanged();
    }
    
    public class KPFormViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFormType, tvDescription, tvDate;
        private ImageButton btnDelete, btnView;
        
        public KPFormViewHolder(View itemView) {
            super(itemView);
            tvFormType = itemView.findViewById(R.id.tvFormType);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnView = itemView.findViewById(R.id.btnView);
        }
        
        public void bind(KPForm form) {
            tvFormType.setText("📋 " + form.getFormType());
            tvDescription.setText("📝 " + (form.getFormTitle() == null || form.getFormTitle().isEmpty() ? "No title" : form.getFormTitle()));
            
            String dateStr = "N/A";
            if (form.getCreatedDate() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                dateStr = sdf.format(new Date(form.getCreatedDate()));
            }
            tvDate.setText("📅 " + dateStr);
            
            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(form);
                }
            });
            
            btnView.setOnClickListener(v -> {
                if (viewListener != null) {
                    viewListener.onView(form);
                }
            });
        }
    }
}
