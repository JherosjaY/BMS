package com.example.blottermanagementsystem.data.model;

public class InvestigationStep {
    private String id;
    private String title;
    private String description;
    private boolean completed;
    private boolean inProgress;
    private String actionText;
    private int actionIcon;
    private String tag; // To identify the step for click handling
    private boolean enabled; // ✅ For sequential unlock of action buttons

    public InvestigationStep(String id, String title, String description, String tag) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.tag = tag;
        this.completed = false;
        this.inProgress = false;
        this.enabled = true; // ✅ Default to enabled
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public String getActionText() {
        return actionText;
    }

    public void setActionText(String actionText) {
        this.actionText = actionText;
    }

    public int getActionIcon() {
        return actionIcon;
    }

    public void setActionIcon(int actionIcon) {
        this.actionIcon = actionIcon;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
