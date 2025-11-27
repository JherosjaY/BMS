package com.example.blottermanagementsystem.ui.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.Evidence;
import com.example.blottermanagementsystem.ui.adapters.PhotoPagerAdapter;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import com.example.blottermanagementsystem.utils.RoleAccessControl;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryActivity extends BaseActivity {
    
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TextView tvPhotoCount, tvDescription;
    private ImageView ivEmpty;
    private PhotoPagerAdapter adapter;
    private BlotterDatabase database;
    private int evidenceId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        PreferencesManager preferencesManager = new PreferencesManager(this);
        if (!RoleAccessControl.checkAnyRole(this, new String[]{"Admin", "Officer"}, preferencesManager)) {
            return;
        }
        
        setContentView(R.layout.activity_photo_gallery);
        
        database = BlotterDatabase.getDatabase(this);
        evidenceId = getIntent().getIntExtra("EVIDENCE_ID", -1);
        
        setupToolbar();
        initViews();
        loadPhotos();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Photo Gallery");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        // TODO: Add tabLayout to activity_photo_gallery.xml
        // tabLayout = findViewById(R.id.tabLayout);
        // TODO: Add tvPhotoCount to activity_photo_gallery.xml
        // tvPhotoCount = findViewById(R.id.tvPhotoCount);
        tvDescription = findViewById(R.id.tvDescription);
        // TODO: Add ivEmpty to activity_photo_gallery.xml
        // ivEmpty = findViewById(R.id.ivEmpty);
        
        adapter = new PhotoPagerAdapter();
        viewPager.setAdapter(adapter);
        
        // TODO: Re-enable when tabLayout is added to XML
        /*
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText("Photo " + (position + 1));
        }).attach();
        */
    }
    
    private void loadPhotos() {
        new Thread(() -> {
            List<Evidence> evidenceList;
            
            if (evidenceId != -1) {
                Evidence evidence = database.evidenceDao().getEvidenceById(evidenceId);
                evidenceList = evidence != null ? List.of(evidence) : new ArrayList<>();
            } else {
                evidenceList = database.evidenceDao().getAllEvidence();
            }
            
            List<Uri> photoUris = new ArrayList<>();
            for (Evidence evidence : evidenceList) {
                if (evidence.getPhotoUri() != null && !evidence.getPhotoUri().isEmpty()) {
                    photoUris.add(Uri.parse(evidence.getPhotoUri()));
                }
            }
            
            runOnUiThread(() -> {
                if (photoUris.isEmpty()) {
                    viewPager.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.GONE);
                    ivEmpty.setVisibility(View.VISIBLE);
                    tvPhotoCount.setText("No photos");
                } else {
                    viewPager.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);
                    ivEmpty.setVisibility(View.GONE);
                    tvPhotoCount.setText(photoUris.size() + " photos");
                    adapter.setPhotos(photoUris);
                    
                    if (!evidenceList.isEmpty()) {
                        tvDescription.setText(evidenceList.get(0).getDescription());
                    }
                }
            });
        }).start();
    }
}
