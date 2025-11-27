package com.example.blottermanagementsystem.ui.dialogs;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.Hearing;
import com.example.blottermanagementsystem.utils.ApiClient;
import com.example.blottermanagementsystem.utils.NetworkMonitor;
import com.google.android.material.button.MaterialButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ScheduleHearingDialogFragment extends DialogFragment {

    private EditText etDate, etTime, etLocation, etPurpose, etPresidingOfficer, etNotes;
    private MaterialButton btnSave, btnSkip;
    private int reportId;
    private OnHearingSavedListener listener;
    private Calendar selectedDate;

    public interface OnHearingSavedListener {
        void onHearingSaved(Hearing hearing);
    }

    public static ScheduleHearingDialogFragment newInstance(int reportId, OnHearingSavedListener listener) {
        ScheduleHearingDialogFragment fragment = new ScheduleHearingDialogFragment();
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
        return inflater.inflate(R.layout.dialog_schedule_hearing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUI(view);
        setupListeners();
    }

    private void setupUI(View view) {
        // Initialize calendar FIRST before anything else
        selectedDate = Calendar.getInstance();
        
        etLocation = view.findViewById(R.id.etHearingLocation);
        etDate = view.findViewById(R.id.etHearingDate);
        etTime = view.findViewById(R.id.etHearingTime);
        etPurpose = view.findViewById(R.id.etHearingPurpose);
        etPresidingOfficer = view.findViewById(R.id.etPresidingOfficer);
        etNotes = view.findViewById(R.id.etHearingNotes);
        btnSave = view.findViewById(R.id.btnScheduleHearing);
        btnSkip = view.findViewById(R.id.btnSkipHearing);
    }

    private void setupListeners() {
        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());
        btnSkip.setOnClickListener(v -> skipHearing()); // ✅ SKIP button for testing
    }
    
    private void skipHearing() {
        // ✅ SKIP: Create dummy hearing and mark as completed (for testing flow)
        Hearing hearing = new Hearing();
        hearing.setBlotterReportId(reportId);
        hearing.setHearingDate("🧪 Test Date");
        hearing.setHearingTime("10:00 AM");
        hearing.setLocation("Test Location");
        hearing.setPurpose("Skipped for testing");
        hearing.setStatus("Scheduled");
        hearing.setCreatedAt(System.currentTimeMillis());
        
        // Save to database in background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                BlotterDatabase database = BlotterDatabase.getDatabase(getContext());
                if (database != null) {
                    long id = database.hearingDao().insertHearing(hearing);
                    hearing.setId((int) id);
                    
                    // Notify on main thread
                    getActivity().runOnUiThread(() -> {
                        if (listener != null) {
                            listener.onHearingSaved(hearing);
                        }
                        Toast.makeText(getContext(), "⏭️ Hearing skipped (test mode)", Toast.LENGTH_SHORT).show();
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

    private void showDatePicker() {
        // Use primary dark blue theme for date picker (no violet accent)
        DatePickerDialog dialog = new DatePickerDialog(getContext(), R.style.DatePickerTheme,
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                etDate.setText(sdf.format(selectedDate.getTime()));
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showTimePicker() {
        // Use primary dark blue theme for time picker (no violet accent)
        TimePickerDialog dialog = new TimePickerDialog(getContext(), R.style.TimePickerTheme,
            (view, hourOfDay, minute) -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                etTime.setText(time);
            },
            selectedDate.get(Calendar.HOUR_OF_DAY),
            selectedDate.get(Calendar.MINUTE),
            true);
        dialog.show();
    }

    private void saveHearing() {
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String purpose = etPurpose.getText().toString().trim();

        if (date.isEmpty() || time.isEmpty() || location.isEmpty() || purpose.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Hearing hearing = new Hearing();
        hearing.setBlotterReportId(reportId);
        hearing.setHearingDate(date);
        hearing.setHearingTime(time);
        hearing.setLocation(location);
        hearing.setPurpose(purpose);
        hearing.setCreatedAt(System.currentTimeMillis());

        // Save to database in background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                BlotterDatabase database = BlotterDatabase.getDatabase(getContext());
                if (database != null) {
                    long id = database.hearingDao().insertHearing(hearing);
                    hearing.setId((int) id);
                    
                    // Sync to API if network available
                    NetworkMonitor networkMonitor = new NetworkMonitor(getContext());
                    if (networkMonitor.isNetworkAvailable()) {
                        ApiClient.getApiService().createHearing(hearing).enqueue(new Callback<Object>() {
                            @Override
                            public void onResponse(Call<Object> call, Response<Object> response) {
                                if (response.isSuccessful()) {
                                    android.util.Log.d("ScheduleHearing", "✅ Synced to API");
                                }
                            }
                            
                            @Override
                            public void onFailure(Call<Object> call, Throwable t) {
                                android.util.Log.w("ScheduleHearing", "⚠️ API sync failed: " + t.getMessage());
                            }
                        });
                    }
                    
                    getActivity().runOnUiThread(() -> {
                        if (listener != null) {
                            listener.onHearingSaved(hearing);
                        }
                        Toast.makeText(getContext(), "Hearing scheduled!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    });
                }
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error saving hearing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
