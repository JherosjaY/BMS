package com.example.blottermanagementsystem.ui.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.blottermanagementsystem.R;

public class OfficerAnalyticsActivity extends BaseActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_analytics);
        
        setupToolbar();
        Toast.makeText(this, "Analytics - Under construction", Toast.LENGTH_SHORT).show();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Analytics");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}
