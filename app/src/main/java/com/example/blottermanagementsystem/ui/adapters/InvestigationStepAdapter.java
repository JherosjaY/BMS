package com.example.blottermanagementsystem.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.model.InvestigationStep;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class InvestigationStepAdapter extends RecyclerView.Adapter<InvestigationStepAdapter.StepViewHolder> {
    
    private List<InvestigationStep> steps;
    private OnStepActionListener listener;
    private boolean isInvestigationStarted = false;
    private String userRole = "OFFICER";  // Default to OFFICER, can be OFFICER, ADMIN, or USER
    private int reportId = -1;  // For view dialogs
    private boolean showNumbers = false;  // ✅ Flag to show numbers instead of icons
    private boolean isCaseResolved = false;  // ✅ Flag to disable buttons when case is resolved
    
    public interface OnStepActionListener {
        void onStepAction(InvestigationStep step);
        void onViewWitnesses(int reportId);
        void onViewSuspects(int reportId);
        void onViewEvidence(int reportId);
        void onViewHearings(int reportId);
        void onViewResolution(int reportId);
        void onSendSms(InvestigationStep step);  // ✅ SMS button callback
    }
    
    public InvestigationStepAdapter(List<InvestigationStep> steps, OnStepActionListener listener) {
        this.steps = steps;
        this.listener = listener;
    }
    
    public InvestigationStepAdapter(List<InvestigationStep> steps, OnStepActionListener listener, boolean isInvestigationStarted) {
        this.steps = steps;
        this.listener = listener;
        this.isInvestigationStarted = isInvestigationStarted;
    }
    
    public void setUserRole(String role) {
        this.userRole = role != null ? role : "OFFICER";
    }
    
    public void setReportId(int reportId) {
        this.reportId = reportId;
    }
    
    public void setInvestigationStarted(boolean started) {
        this.isInvestigationStarted = started;
        notifyDataSetChanged();
    }
    
    public void setShowNumbers(boolean show) {
        this.showNumbers = show;
        notifyDataSetChanged();
    }
    
    public void setCaseResolved(boolean resolved) {
        this.isCaseResolved = resolved;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_investigation_step, parent, false);
        return new StepViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        InvestigationStep step = steps.get(position);
        
        // Set step title and description
        holder.tvStepTitle.setText(step.getTitle());
        holder.tvStepDescription.setText(step.getDescription());
        
        // Get the ShapeableImageView inside the FrameLayout
        ShapeableImageView circleView = (ShapeableImageView) ((android.widget.FrameLayout) holder.ivStatus).getChildAt(0);
        
        // ✅ Show numbers for Investigation Actions, icons for Case Progress
        if (showNumbers) {
            // Display step number (1, 2, 3, 4, 5)
            holder.tvStepNumber.setText(String.valueOf(position + 1));
            holder.tvStepNumber.setVisibility(View.VISIBLE);
        } else {
            // Hide number, show icon only
            holder.tvStepNumber.setVisibility(View.GONE);
        }
        
        // Update status indicator styling based on step status
        if (showNumbers) {
            // ✅ NUMBERED MODE (Investigation Actions) - Show only numbers, no icons
            circleView.setImageResource(R.drawable.ic_radio_button_unchecked);  // Plain circle background
            circleView.setColorFilter(holder.itemView.getContext()
                    .getColor(R.color.electric_blue));
            holder.tvStepNumber.setTextColor(holder.itemView.getContext()
                    .getColor(R.color.electric_blue));
        } else {
            // ✅ ICON MODE (Case Progress) - Show icons with status colors
            if (step.isCompleted()) {
                // ✓ Completed - Green checkmark
                circleView.setImageResource(R.drawable.ic_check_circle);
                circleView.setColorFilter(holder.itemView.getContext()
                        .getColor(R.color.success_green));
            } else if (step.isInProgress()) {
                // ⏳ In Progress - Yellow hourglass icon
                circleView.setImageResource(R.drawable.ic_hourglass);
                circleView.setColorFilter(holder.itemView.getContext()
                        .getColor(R.color.warning_yellow));
            } else {
                // ⭕ Pending - Gray outline circle
                circleView.setImageResource(R.drawable.ic_radio_button_unchecked);
                circleView.setColorFilter(holder.itemView.getContext()
                        .getColor(R.color.text_secondary));
            }
        }
        
        // Show/hide action button based on role
        if (step.getActionText() != null) {
            holder.btnAction.setVisibility(View.VISIBLE);
            
            if ("USER".equalsIgnoreCase(userRole)) {
                // USER ROLE: Show view buttons (always enabled, read-only)
                String buttonText = getViewButtonText(step.getTag());
                holder.btnAction.setText(buttonText);
                holder.btnAction.setIconResource(R.drawable.ic_visibility);  // Eye icon for view
                holder.btnAction.setEnabled(true);
                holder.btnAction.setAlpha(1.0f);
                
                holder.btnAction.setOnClickListener(v -> {
                    if (listener != null && reportId != -1) {
                        handleViewAction(step.getTag(), listener);
                    }
                });
            } else if ("OFFICER".equalsIgnoreCase(userRole)) {
                // OFFICER ROLE: Show add buttons (sequential unlock based on enabled flag)
                if (!step.isCompleted()) {
                    holder.btnAction.setText(step.getActionText());
                    holder.btnAction.setIconResource(step.getActionIcon());
                    
                    // ✅ Disable all buttons if case is resolved
                    if (isCaseResolved) {
                        holder.btnAction.setEnabled(false);
                        holder.btnAction.setAlpha(0.5f);
                        holder.btnAction.setOnClickListener(v -> {
                            android.widget.Toast.makeText(
                                holder.itemView.getContext(),
                                "🔒 Case is resolved - read-only mode",
                                android.widget.Toast.LENGTH_SHORT
                            ).show();
                        });
                    }
                    // ✅ Check if button is enabled (based on previous steps)
                    else if (step.isEnabled()) {
                        holder.btnAction.setEnabled(true);
                        holder.btnAction.setAlpha(1.0f);
                        holder.btnAction.setOnClickListener(v -> {
                            if (listener != null) {
                                listener.onStepAction(step);
                            }
                        });
                    } else {
                        // ❌ Button disabled - show why
                        holder.btnAction.setEnabled(false);
                        holder.btnAction.setAlpha(0.5f);
                        holder.btnAction.setOnClickListener(v -> {
                            android.widget.Toast.makeText(
                                holder.itemView.getContext(),
                                "⚠️ Complete previous steps first",
                                android.widget.Toast.LENGTH_SHORT
                            ).show();
                        });
                    }
                } else {
                    holder.btnAction.setVisibility(View.GONE);
                }
            } else {
                // ADMIN ROLE: No buttons (read-only)
                holder.btnAction.setVisibility(View.GONE);
            }
        } else {
            holder.btnAction.setVisibility(View.GONE);
        }
        
        // Show/hide timeline lines
        if (position == 0) {
            holder.vTimelineLineTop.setVisibility(View.INVISIBLE);
        } else {
            holder.vTimelineLineTop.setVisibility(View.VISIBLE);
        }
        
        if (position == steps.size() - 1) {
            holder.vTimelineLineBottom.setVisibility(View.INVISIBLE);
        } else {
            holder.vTimelineLineBottom.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public int getItemCount() {
        return steps != null ? steps.size() : 0;
    }
    
    public void updateSteps(List<InvestigationStep> newSteps) {
        this.steps = newSteps;
        notifyDataSetChanged();
    }
    
    private String getViewButtonText(String tag) {
        if (tag == null) return "View";
        
        switch (tag.toLowerCase()) {
            case "witnesses":
                return "👥 View Witnesses";
            case "suspects":
                return "🚨 View Suspects";
            case "evidence":
                return "📸 View Evidence";
            case "hearings":
                return "📅 View Hearings";
            case "resolution":
                return "✅ View Resolution";
            default:
                return "View Details";
        }
    }
    
    private void handleViewAction(String tag, OnStepActionListener listener) {
        if (tag == null) return;
        
        switch (tag.toLowerCase()) {
            case "witnesses":
                listener.onViewWitnesses(reportId);
                break;
            case "suspects":
                listener.onViewSuspects(reportId);
                break;
            case "evidence":
                listener.onViewEvidence(reportId);
                break;
            case "hearings":
                listener.onViewHearings(reportId);
                break;
            case "resolution":
                listener.onViewResolution(reportId);
                break;
        }
    }
    
    static class StepViewHolder extends RecyclerView.ViewHolder {
        View vTimelineLineTop;
        View vTimelineLineBottom;
        View ivStatus;  // ✅ Changed to View (FrameLayout)
        ShapeableImageView ivStatusCircle;  // ✅ Reference to circle inside
        TextView tvStepTitle;
        TextView tvStepDescription;
        MaterialButton btnAction;
        TextView tvStepNumber;  // ✅ For displaying step numbers
        
        StepViewHolder(@NonNull View itemView) {
            super(itemView);
            vTimelineLineTop = itemView.findViewById(R.id.vTimelineLineTop);
            vTimelineLineBottom = itemView.findViewById(R.id.vTimelineLineBottom);
            ivStatus = itemView.findViewById(R.id.ivStatus);  // ✅ FrameLayout
            tvStepTitle = itemView.findViewById(R.id.tvStepTitle);
            tvStepDescription = itemView.findViewById(R.id.tvStepDescription);
            btnAction = itemView.findViewById(R.id.btnAction);
            tvStepNumber = itemView.findViewById(R.id.tvStepNumber);  // ✅ Initialize
        }
    }
}
