package com.example.blottermanagementsystem.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.ConnectedDevice;
import com.example.blottermanagementsystem.ui.adapters.ConnectedDeviceAdapter;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import com.example.blottermanagementsystem.utils.RoleAccessControl;
import java.util.List;

public class ConnectedDevicesActivity extends BaseActivity {
    
    private RecyclerView recyclerView;
    private ConnectedDeviceAdapter adapter;
    private TextView tvEmpty, tvDeviceCount;
    private BlotterDatabase database;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        PreferencesManager preferencesManager = new PreferencesManager(this);
        if (!RoleAccessControl.checkAccess(this, "Admin", preferencesManager)) {
            return;
        }
        
        setContentView(R.layout.activity_connected_devices);
        
        database = BlotterDatabase.getDatabase(this);
        
        setupToolbar();
        initViews();
        loadDevices();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Connected Devices");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvDeviceCount = findViewById(R.id.tvDeviceCount);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConnectedDeviceAdapter(device -> {
            // Handle device click - show details or disconnect
        });
        recyclerView.setAdapter(adapter);
    }
    
    private void loadDevices() {
        new Thread(() -> {
            List<ConnectedDevice> devices = database.connectedDeviceDao().getAllDevices();
            
            runOnUiThread(() -> {
                tvDeviceCount.setText(devices.size() + " devices");
                
                if (devices.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.GONE);
                    adapter.setDevices(devices);
                }
            });
        }).start();
    }
}
