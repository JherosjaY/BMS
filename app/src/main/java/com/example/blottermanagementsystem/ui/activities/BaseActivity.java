package com.example.blottermanagementsystem.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.blottermanagementsystem.R;

public abstract class BaseActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Force navy blue status bar and navigation bar for ALL activities
        getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark_blue, null));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.primary_dark_blue, null));
    }
}
