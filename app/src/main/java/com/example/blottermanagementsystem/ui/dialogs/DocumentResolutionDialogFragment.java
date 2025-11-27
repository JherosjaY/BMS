package com.example.blottermanagementsystem.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.Resolution;
import com.example.blottermanagementsystem.utils.ApiClient;
import com.example.blottermanagementsystem.utils.NetworkMonitor;
import com.google.android.material.button.MaterialButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.concurrent.Executors;

public class DocumentResolutionDialogFragment extends DialogFragment {

    private Spinner spinnerResolutionType;
    private EditText etResolutionDetails;
    private MaterialButton btnSave, btnSkip;
    private int reportId;
    private OnResolutionSavedListener listener;

    public interface OnResolutionSavedListener {
        void onResolutionSaved(Resolution resolution);
    }

    public static DocumentResolutionDialogFragment newInstance(int reportId, OnResolutionSavedListener listener) {
        DocumentResolutionDialogFragment fragment = new DocumentResolutionDialogFragment();
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
        return inflater.inflate(R.layout.dialog_document_resolution, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupSpinner();
        setupListeners();
    }

    private void initViews(View view) {
        spinnerResolutionType = view.findViewById(R.id.spinnerResolutionType);
        etResolutionDetails = view.findViewById(R.id.etResolutionDetails);
        btnSave = view.findViewById(R.id.btnSaveResolution);
        btnSkip = view.findViewById(R.id.btnSkipResolution);
    }

    private void setupSpinner() {
        String[] resolutionTypes = {"Settled", "Dismissed", "Referred", "Pending", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
            android.R.layout.simple_spinner_item, resolutionTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerResolutionType.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveResolution());
        btnSkip.setOnClickListener(v -> skipResolution()); // ✅ SKIP button for testing
    }
    
    private void skipResolution() {
        // ✅ SKIP: Create dummy resolution and mark as completed (for testing flow)
        Resolution resolution = new Resolution(reportId, "Settled", "🧪 Test Resolution (Skipped)", 0);
        resolution.setResolvedDate(System.currentTimeMillis());
        resolution.setCreatedAt(System.currentTimeMillis());
        
        // Save to database in background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                BlotterDatabase database = BlotterDatabase.getDatabase(getContext());
                if (database != null) {
                    long id = database.resolutionDao().insertResolution(resolution);
                    resolution.setId((int) id);
                    
                    // Notify on main thread
                    getActivity().runOnUiThread(() -> {
                        if (listener != null) {
                            listener.onResolutionSaved(resolution);
                        }
                        Toast.makeText(getContext(), "⏭️ Resolution skipped (test mode)", Toast.LENGTH_SHORT).show();
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

    private void saveResolution() {
        String type = spinnerResolutionType.getSelectedItem().toString();
        String details = etResolutionDetails.getText().toString().trim();

        if (details.isEmpty()) {
            Toast.makeText(getContext(), "Resolution details are required", Toast.LENGTH_SHORT).show();
            return;
        }

        Resolution resolution = new Resolution(reportId, type, details, 0);
        resolution.setResolvedDate(System.currentTimeMillis());
        resolution.setCreatedAt(System.currentTimeMillis());

        // Save to database in background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                BlotterDatabase database = BlotterDatabase.getDatabase(getContext());
                if (database != null) {
                    long id = database.resolutionDao().insertResolution(resolution);
                    resolution.setId((int) id);
                    
                    // Sync to API if network available
                    NetworkMonitor networkMonitor = new NetworkMonitor(getContext());
                    if (networkMonitor.isNetworkAvailable()) {
                        ApiClient.getApiService().createResolution(resolution).enqueue(new Callback<Object>() {
                            @Override
                            public void onResponse(Call<Object> call, Response<Object> response) {
                                if (response.isSuccessful()) {
                                    android.util.Log.d("DocumentResolution", "Synced to API");
                                }
                            }
                            
                            @Override
                            public void onFailure(Call<Object> call, Throwable t) {
                                android.util.Log.w("DocumentResolution", "API sync failed: " + t.getMessage());
                            }
                        });
                    }
                    
                    getActivity().runOnUiThread(() -> {
                        if (listener != null) {
                            listener.onResolutionSaved(resolution);
                        }
                        Toast.makeText(getContext(), "Resolution documented!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    });
                }
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error saving resolution: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
