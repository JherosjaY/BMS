package com.example.blottermanagementsystem.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.User;
import com.example.blottermanagementsystem.ui.adapters.UserManagementAdapter;
import com.example.blottermanagementsystem.utils.ApiClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminManageUsersActivity extends BaseActivity {
    
    private static final String TAG = "AdminManageUsers";
    
    private Toolbar toolbar;
    private RecyclerView rvUsers;
    private TextInputEditText etSearchUser;
    private MaterialButton btnSearch;
    private View emptyState;
    
    private BlotterDatabase database;
    private UserManagementAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private List<User> filteredList = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_users);
        
        database = BlotterDatabase.getDatabase(this);
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadAllUsers();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvUsers = findViewById(R.id.rvUsers);
        etSearchUser = findViewById(R.id.etSearchUser);
        btnSearch = findViewById(R.id.btnSearch);
        emptyState = findViewById(R.id.emptyState);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Users");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupRecyclerView() {
        adapter = new UserManagementAdapter(filteredList, user -> showUserOptions(user));
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);
    }
    
    private void setupListeners() {
        btnSearch.setOnClickListener(v -> searchUsers());
        etSearchUser.setOnEditorActionListener((v, actionId, event) -> {
            searchUsers();
            return true;
        });
    }
    
    private void loadAllUsers() {
        Log.d(TAG, "📥 Loading all users...");
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                userList = database.userDao().getAllUsers();
                filteredList.clear();
                filteredList.addAll(userList);
                
                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    Log.d(TAG, "✅ Loaded " + userList.size() + " users");
                });
            } catch (Exception e) {
                Log.e(TAG, "❌ Error loading users: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error loading users", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void searchUsers() {
        String query = etSearchUser.getText().toString().trim().toLowerCase();
        
        if (query.isEmpty()) {
            filteredList.clear();
            filteredList.addAll(userList);
        } else {
            filteredList.clear();
            for (User user : userList) {
                if (user.getUsername().toLowerCase().contains(query) ||
                    user.getEmail().toLowerCase().contains(query) ||
                    user.getFirstName().toLowerCase().contains(query) ||
                    user.getLastName().toLowerCase().contains(query)) {
                    filteredList.add(user);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        updateEmptyState();
        Log.d(TAG, "🔍 Search found " + filteredList.size() + " users");
    }
    
    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvUsers.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvUsers.setVisibility(View.VISIBLE);
        }
    }
    
    private void showUserOptions(User user) {
        Log.d(TAG, "👤 Showing options for user: " + user.getUsername());
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("User: " + user.getUsername())
            .setMessage("Select action for this user")
            .setPositiveButton("View Details", (dialog, which) -> {
                showUserDetails(user);
            })
            .setNegativeButton("Terminate Account", (dialog, which) -> {
                showTerminateConfirmation(user);
            })
            .setNeutralButton("Cancel", null)
            .show();
    }
    
    private void showUserDetails(User user) {
        Log.d(TAG, "📋 Showing details for: " + user.getUsername());
        
        String details = "Username: " + user.getUsername() + "\n" +
                        "Email: " + user.getEmail() + "\n" +
                        "Name: " + user.getFirstName() + " " + user.getLastName() + "\n" +
                        "Role: " + user.getRole() + "\n" +
                        "Gender: " + user.getGender() + "\n" +
                        "Created: " + user.getAccountCreated();
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("User Details")
            .setMessage(details)
            .setPositiveButton("Close", null)
            .show();
    }
    
    private void showTerminateConfirmation(User user) {
        Log.d(TAG, "⚠️ Showing terminate confirmation for: " + user.getUsername());
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("⚠️ Terminate Account?")
            .setMessage("Are you sure you want to terminate the account for " + user.getUsername() + "?\n\n" +
                       "This action will:\n" +
                       "• Delete from local database\n" +
                       "• Delete from Neon database\n" +
                       "• Remove all profile data\n" +
                       "• This CANNOT be undone!")
            .setPositiveButton("TERMINATE", (dialog, which) -> {
                terminateUserAccount(user);
            })
            .setNegativeButton("CANCEL", null)
            .setCancelable(false)
            .show();
    }
    
    private void terminateUserAccount(User user) {
        Log.d(TAG, "🗑️ Terminating account for: " + user.getUsername());
        
        // Show loading
        Toast.makeText(this, "Terminating account...", Toast.LENGTH_SHORT).show();
        
        // Step 1: Delete from Neon database (backend)
        deleteFromNeonDatabase(user);
    }
    
    private void deleteFromNeonDatabase(User user) {
        Log.d(TAG, "☁️ Deleting from Neon database: " + user.getId());
        
        try {
            ApiClient.getApiService().deleteUser(String.valueOf(user.getId()))
                .enqueue(new Callback<java.util.Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<java.util.Map<String, Object>> call, 
                                         Response<java.util.Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "✅ Deleted from Neon: " + user.getId());
                            deleteFromLocalDatabase(user);
                        } else {
                            Log.e(TAG, "❌ Neon delete failed: " + response.code());
                            // Still delete locally even if Neon fails
                            deleteFromLocalDatabase(user);
                        }
                    }

                    @Override
                    public void onFailure(Call<java.util.Map<String, Object>> call, Throwable t) {
                        Log.e(TAG, "❌ Network error deleting from Neon: " + t.getMessage());
                        // Still delete locally even if network fails
                        deleteFromLocalDatabase(user);
                    }
                });
        } catch (Exception e) {
            Log.e(TAG, "❌ Error calling delete API: " + e.getMessage());
            deleteFromLocalDatabase(user);
        }
    }
    
    private void deleteFromLocalDatabase(User user) {
        Log.d(TAG, "💾 Deleting from local database: " + user.getId());
        
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                database.userDao().deleteUser(user);
                Log.d(TAG, "✅ Deleted from local database: " + user.getId());
                
                runOnUiThread(() -> {
                    // Remove from UI immediately
                    userList.remove(user);
                    filteredList.remove(user);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    
                    Toast.makeText(AdminManageUsersActivity.this, 
                        "Account terminated: " + user.getUsername(), 
                        Toast.LENGTH_SHORT).show();
                    
                    Log.d(TAG, "✅ UI updated - user removed");
                });
            } catch (Exception e) {
                Log.e(TAG, "❌ Error deleting from local database: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(AdminManageUsersActivity.this, 
                        "Error terminating account", 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
