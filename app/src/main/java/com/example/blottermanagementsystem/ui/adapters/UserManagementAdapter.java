package com.example.blottermanagementsystem.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.entity.User;
import java.util.List;

public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.UserViewHolder> {
    
    private List<User> users;
    private OnUserClickListener listener;
    
    public interface OnUserClickListener {
        void onUserClick(User user);
    }
    
    public UserManagementAdapter(List<User> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_user_management, parent, false);
        return new UserViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user, listener);
    }
    
    @Override
    public int getItemCount() {
        return users.size();
    }
    
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvUsername;
        private TextView tvEmail;
        private TextView tvFullName;
        private TextView tvRole;
        private TextView tvAuthProvider;
        private ImageView ivUserIcon;
        
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvAuthProvider = itemView.findViewById(R.id.tvAuthProvider);
            ivUserIcon = itemView.findViewById(R.id.ivUserIcon);
        }
        
        public void bind(User user, OnUserClickListener listener) {
            tvUsername.setText("@" + user.getUsername());
            tvEmail.setText(user.getEmail());
            tvFullName.setText(user.getFirstName() + " " + user.getLastName());
            tvRole.setText(user.getRole());
            
            // Set auth provider with emoji
            String authProvider = user.getAuthProvider() != null ? user.getAuthProvider() : "email";
            if ("google".equalsIgnoreCase(authProvider)) {
                tvAuthProvider.setText("🔵 Google");
            } else {
                tvAuthProvider.setText("📧 Email");
            }
            
            // Set role icon based on role
            String role = user.getRole();
            if ("Admin".equalsIgnoreCase(role)) {
                ivUserIcon.setImageResource(android.R.drawable.ic_dialog_info);
            } else if ("Officer".equalsIgnoreCase(role)) {
                ivUserIcon.setImageResource(android.R.drawable.ic_menu_view);
            } else {
                ivUserIcon.setImageResource(android.R.drawable.ic_menu_myplaces);
            }
            
            // Click listener
            itemView.setOnClickListener(v -> listener.onUserClick(user));
        }
    }
}
