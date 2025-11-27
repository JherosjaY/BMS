package com.example.blottermanagementsystem.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.entity.InvestigationTask;

import java.util.List;

public class InvestigationTaskAdapter extends RecyclerView.Adapter<InvestigationTaskAdapter.TaskViewHolder> {
    
    private List<InvestigationTask> tasks;
    private OnTaskClickListener listener;
    
    public interface OnTaskClickListener {
        void onTaskClick(InvestigationTask task);
    }
    
    public InvestigationTaskAdapter(List<InvestigationTask> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_investigation_task, parent, false);
        return new TaskViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        InvestigationTask task = tasks.get(position);
        holder.bind(task, listener);
    }
    
    @Override
    public int getItemCount() {
        return tasks.size();
    }
    
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkboxTask;
        private TextView tvTaskName, tvDescription, tvPriority;
        
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxTask = itemView.findViewById(R.id.checkboxTask);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPriority = itemView.findViewById(R.id.tvPriority);
        }
        
        public void bind(InvestigationTask task, OnTaskClickListener listener) {
            tvTaskName.setText(task.getTaskName());
            tvDescription.setText(task.getDescription());
            checkboxTask.setChecked(task.isCompleted());
            
            // Set priority color
            String priorityText;
            int priorityColor;
            switch (task.getPriority()) {
                case 1:
                    priorityText = "HIGH";
                    priorityColor = itemView.getContext().getResources().getColor(R.color.warning_yellow, null);
                    break;
                case 2:
                    priorityText = "MEDIUM";
                    priorityColor = itemView.getContext().getResources().getColor(R.color.info_blue, null);
                    break;
                case 3:
                    priorityText = "LOW";
                    priorityColor = itemView.getContext().getResources().getColor(R.color.success_green, null);
                    break;
                default:
                    priorityText = "NORMAL";
                    priorityColor = itemView.getContext().getResources().getColor(R.color.text_secondary, null);
            }
            
            tvPriority.setText(priorityText);
            tvPriority.setTextColor(priorityColor);
            
            // Apply strikethrough if completed
            if (task.isCompleted()) {
                tvTaskName.setPaintFlags(tvTaskName.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                tvTaskName.setAlpha(0.6f);
            } else {
                tvTaskName.setPaintFlags(tvTaskName.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                tvTaskName.setAlpha(1.0f);
            }
            
            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });
            
            checkboxTask.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });
        }
    }
}
