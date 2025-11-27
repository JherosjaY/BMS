package com.example.blottermanagementsystem.ui.dialogs;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.Evidence;
import com.example.blottermanagementsystem.ui.adapters.FilePreviewAdapter;
import com.example.blottermanagementsystem.utils.ApiClient;
import com.example.blottermanagementsystem.utils.NetworkMonitor;
import com.google.android.material.button.MaterialButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class AddEvidenceDialogFragment extends DialogFragment {

    private EditText etTitle, etDescription;
    private MaterialButton btnSave, btnSkip, btnPickPhoto, btnPickVideo;
    private TextView tvSelectedFiles;
    private RecyclerView rvFilePreview;
    private int reportId;
    private OnEvidenceSavedListener listener;
    private List<Uri> selectedFiles = new ArrayList<>();
    private List<String> savedFilePaths = new ArrayList<>();
    
    // File picker launchers - GetContent takes String (MIME type) as input and returns Uri
    private ActivityResultLauncher<String> photoPickerLauncher;
    private ActivityResultLauncher<String> videoPickerLauncher;

    public interface OnEvidenceSavedListener {
        void onEvidenceSaved(Evidence evidence);
    }

    public static AddEvidenceDialogFragment newInstance(int reportId, OnEvidenceSavedListener listener) {
        AddEvidenceDialogFragment fragment = new AddEvidenceDialogFragment();
        Bundle args = new Bundle();
        args.putInt("report_id", reportId);
        fragment.setArguments(args);
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use transparent background to show the MaterialCardView properly
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Dialog_MinWidth);
        if (getArguments() != null) {
            reportId = getArguments().getInt("report_id");
        }
        
        // Initialize photo picker launcher - GetContent takes MIME type as input
        photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedFiles.add(uri);
                    updateSelectedFilesDisplay();
                }
            }
        );
        
        // Initialize video picker launcher - GetContent takes MIME type as input
        videoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedFiles.add(uri);
                    updateSelectedFilesDisplay();
                }
            }
        );
    }
    
    @Override
    public void onStart() {
        super.onStart();
        // Make dialog background transparent to show MaterialCardView
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // Set dialog to match parent width with padding
            android.view.WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            getDialog().getWindow().setAttributes(params);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_evidence, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        etTitle = view.findViewById(R.id.etEvidenceTitle);
        etDescription = view.findViewById(R.id.etEvidenceDescription);
        btnSave = view.findViewById(R.id.btnSaveEvidence);
        btnSkip = view.findViewById(R.id.btnSkipEvidence);
        btnPickPhoto = view.findViewById(R.id.btnPickPhoto);
        btnPickVideo = view.findViewById(R.id.btnPickVideo);
        tvSelectedFiles = view.findViewById(R.id.tvSelectedFiles);
        rvFilePreview = view.findViewById(R.id.rvFilePreview);
        rvFilePreview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupListeners() {
        // Photo picker button - launch with MIME type
        btnPickPhoto.setOnClickListener(v -> {
            photoPickerLauncher.launch("image/*");
        });
        
        // Video picker button - launch with MIME type
        btnPickVideo.setOnClickListener(v -> {
            videoPickerLauncher.launch("video/*");
        });
        
        btnSave.setOnClickListener(v -> saveEvidence());
        btnSkip.setOnClickListener(v -> skipEvidence()); // ✅ SKIP button for testing
    }
    
    private void skipEvidence() {
        // ✅ SKIP: Create dummy evidence and mark as completed (for testing flow)
        Evidence evidence = new Evidence();
        evidence.setBlotterReportId(reportId);
        evidence.setEvidenceType("image");
        evidence.setDescription("🧪 Test Evidence (Skipped)");
        evidence.setFilePath("test_evidence.jpg");
        evidence.setCollectedDate(System.currentTimeMillis());
        
        // Save to database in background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                BlotterDatabase database = BlotterDatabase.getDatabase(getContext());
                if (database != null) {
                    long id = database.evidenceDao().insertEvidence(evidence);
                    evidence.setId((int) id);
                    
                    // Notify on main thread
                    getActivity().runOnUiThread(() -> {
                        if (listener != null) {
                            listener.onEvidenceSaved(evidence);
                        }
                        Toast.makeText(getContext(), "⏭️ Evidence skipped (test mode)", Toast.LENGTH_SHORT).show();
                        dismiss();
                    });
                }
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void updateSelectedFilesDisplay() {
        if (selectedFiles.isEmpty()) {
            tvSelectedFiles.setText("No files selected");
            rvFilePreview.setVisibility(View.GONE);
        } else {
            tvSelectedFiles.setText("✅ " + selectedFiles.size() + " file(s) selected");
            rvFilePreview.setVisibility(View.VISIBLE);
            // Copy files to app cache and show preview
            copyFilesToCache();
        }
    }
    
    private void copyFilesToCache() {
        Executors.newSingleThreadExecutor().execute(() -> {
            savedFilePaths.clear();
            for (Uri uri : selectedFiles) {
                try {
                    String fileName = "evidence_" + System.currentTimeMillis() + "_" + selectedFiles.indexOf(uri);
                    File cacheDir = new File(getContext().getCacheDir(), "evidence");
                    if (!cacheDir.exists()) {
                        cacheDir.mkdirs();
                    }
                    
                    File destFile = new File(cacheDir, fileName);
                    InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
                    FileOutputStream outputStream = new FileOutputStream(destFile);
                    
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    
                    inputStream.close();
                    outputStream.close();
                    
                    savedFilePaths.add(destFile.getAbsolutePath());
                } catch (Exception e) {
                    android.util.Log.e("AddEvidence", "Error copying file: " + e.getMessage());
                }
            }
            
            // Update UI on main thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Show preview thumbnails
                    if (rvFilePreview != null && !savedFilePaths.isEmpty()) {
                        FilePreviewAdapter adapter = new FilePreviewAdapter(savedFilePaths);
                        rvFilePreview.setAdapter(adapter);
                    }
                });
            }
        });
    }

    private void saveEvidence() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        Evidence evidence = new Evidence();
        evidence.setBlotterReportId(reportId);
        evidence.setEvidenceType("photo"); // Default to photo
        // Store title and description together
        String fullDescription = title + (description.isEmpty() ? "" : "\n" + description);
        evidence.setDescription(fullDescription);
        evidence.setCollectedBy("Officer");
        evidence.setCollectedDate(System.currentTimeMillis());
        
        // Store file paths (comma-separated)
        if (!savedFilePaths.isEmpty()) {
            String photoUris = String.join(",", savedFilePaths);
            evidence.setPhotoUris(photoUris);
        }

        // Save to database in background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                BlotterDatabase database = BlotterDatabase.getDatabase(getContext());
                if (database != null) {
                    long id = database.evidenceDao().insertEvidence(evidence);
                    evidence.setId((int) id);
                    
                    // Sync to API if network available
                    NetworkMonitor networkMonitor = new NetworkMonitor(getContext());
                    if (networkMonitor.isNetworkAvailable()) {
                        // Upload files to server and get URLs
                        uploadFilesToServer(evidence, database);
                    } else {
                        // Offline mode - save locally
                        notifyEvidenceSaved(evidence);
                    }
                }
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error saving evidence: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void uploadFilesToServer(Evidence evidence, BlotterDatabase database) {
        // Upload files and sync with API
        ApiClient.getApiService().createEvidence(evidence).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    android.util.Log.d("AddEvidence", "✅ Evidence synced to API");
                    notifyEvidenceSaved(evidence);
                }
            }
            
            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                android.util.Log.w("AddEvidence", "⚠️ API sync failed: " + t.getMessage());
                // Still notify locally even if sync fails
                notifyEvidenceSaved(evidence);
            }
        });
    }
    
    private void notifyEvidenceSaved(Evidence evidence) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (listener != null) {
                    listener.onEvidenceSaved(evidence);
                }
                Toast.makeText(getContext(), "✅ Evidence added! Syncing with team...", Toast.LENGTH_SHORT).show();
                dismiss();
            });
        }
    }
}
