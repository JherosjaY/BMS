package com.example.blottermanagementsystem.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.entity.User;
import com.google.android.material.chip.Chip;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    
    private List<User> users;
    private OnUserClickListener listener;
    private OnUserLongClickListener longClickListener;
    
    public interface OnUserClickListener {
        void onUserClick(User user);
    }
    
    public interface OnUserLongClickListener {
        void onUserLongClick(User user);
    }
    
    public UserAdapter(List<User> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }
    
    public void setOnUserLongClickListener(OnUserLongClickListener listener) {
        this.longClickListener = listener;
    }
    
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user, listener, longClickListener);
    }
    
    @Override
    public int getItemCount() {
        return users.size();
    }
    
    public void updateUsers(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }
    
    static class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAvatar, tvName, tvUsername, tvEmail;
        private Chip chipRole;
        
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            chipRole = itemView.findViewById(R.id.chipRole);
        }
        
        public void bind(User user, OnUserClickListener listener, OnUserLongClickListener longClickListener) {
            String fullName = user.getFirstName() + " " + user.getLastName();
            tvName.setText(fullName);
            tvUsername.setText("@" + user.getUsername());
            tvEmail.setText(user.getEmail() != null ? user.getEmail() : "No email");
            
            // Set avatar initials
            String initials = "";
            if (!user.getFirstName().isEmpty()) {
                initials += user.getFirstName().charAt(0);
            }
            if (!user.getLastName().isEmpty()) {
                initials += user.getLastName().charAt(0);
            }
            tvAvatar.setText(initials.toUpperCase());
            
            // Set role chip
            chipRole.setText(capitalizeFirst(user.getRole()));
            int roleColor = getRoleColor(user.getRole());
            chipRole.setChipBackgroundColorResource(roleColor);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUserClick(user);
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onUserLongClick(user);
                    return true;
                }
                return false;
            });
        }
        
        private String capitalizeFirst(String text) {
            if (text == null || text.isEmpty()) return text;
            return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
        }
        
        private int getRoleColor(String role) {
            switch (role.toLowerCase()) {
                case "admin":
                    return R.color.error_red;
                case "officer":
                    return R.color.info_blue;
                case "user":
                default:
                    return R.color.success_green;
            }
        }
    }
}
