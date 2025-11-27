package com.example.blottermanagementsystem.ui.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.BlotterReport;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import java.util.List;

public class IncidentMapActivity extends BaseActivity implements OnMapReadyCallback {
    
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private BlotterDatabase database;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_map);
        
        database = BlotterDatabase.getDatabase(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        setupToolbar();
        setupPermissionLauncher();
        initMap();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Incident Map");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    enableMyLocation();
                }
            }
        );
    }
    
    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.recyclerView); // TODO: Add map fragment to layout
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }
    
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        
        // Enable location if permission granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        
        // Load incident markers
        loadIncidentMarkers();
        
        // Set map click listener
        googleMap.setOnMapClickListener(latLng -> {
            // Handle map click - could add new incident location
        });
    }
    
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            
            // Move camera to current location
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                    }
                });
        }
    }
    
    private void loadIncidentMarkers() {
        new Thread(() -> {
            List<BlotterReport> reports = database.blotterReportDao().getAllReports();
            
            runOnUiThread(() -> {
                for (BlotterReport report : reports) {
                    if (report.getLatitude() != 0.0 && report.getLongitude() != 0.0) {
                        LatLng position = new LatLng(report.getLatitude(), report.getLongitude());
                        
                        // Different marker colors based on status
                        float markerColor;
                        switch (report.getStatus()) {
                            case "Pending":
                                markerColor = BitmapDescriptorFactory.HUE_RED;
                                break;
                            case "Under Investigation":
                                markerColor = BitmapDescriptorFactory.HUE_ORANGE;
                                break;
                            case "Resolved":
                                markerColor = BitmapDescriptorFactory.HUE_GREEN;
                                break;
                            default:
                                markerColor = BitmapDescriptorFactory.HUE_BLUE;
                        }
                        
                        googleMap.addMarker(new MarkerOptions()
                            .position(position)
                            .title(report.getIncidentType())
                            .snippet(report.getDescription())
                            .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
                    }
                }
            });
        }).start();
    }
}
