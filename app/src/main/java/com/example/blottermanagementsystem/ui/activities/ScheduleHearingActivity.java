package com.example.blottermanagementsystem.ui.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.example.blottermanagementsystem.R;
// ❌ REMOVED: import com.example.blottermanagementsystem.services.HearingsService; (Pure online mode)
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ScheduleHearingActivity extends BaseActivity {
    private static final String TAG = "ScheduleHearing";

    private Toolbar toolbar;
    private TextInputEditText etCaseId, etHearingDate, etHearingTime, etLocation, etPresidingOfficer;
    private CheckBox cbNotifyComplainant, cbNotifyRespondent, cbNotifyOfficer;
    private MaterialButton btnSchedule, btnCancel;

    // ❌ REMOVED: private HearingsService hearingsService; (Pure online mode)
    private String currentUserId;
    private Calendar selectedDate;
    private Calendar selectedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_hearing);

        // ❌ REMOVED: hearingsService = new HearingsService(this); (Pure online mode)
        currentUserId = getSharedPreferences("UserSession", MODE_PRIVATE).getString("userId", "");
        selectedDate = Calendar.getInstance();
        selectedTime = Calendar.getInstance();

        initializeViews();
        setupToolbar();
        setupListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        etCaseId = findViewById(R.id.etCaseId);
        etHearingDate = findViewById(R.id.etHearingDate);
        etHearingTime = findViewById(R.id.etHearingTime);
        etLocation = findViewById(R.id.etLocation);
        etPresidingOfficer = findViewById(R.id.etPresidingOfficer);
        cbNotifyComplainant = findViewById(R.id.cbNotifyComplainant);
        cbNotifyRespondent = findViewById(R.id.cbNotifyRespondent);
        cbNotifyOfficer = findViewById(R.id.cbNotifyOfficer);
        btnSchedule = findViewById(R.id.btnSchedule);
        btnCancel = findViewById(R.id.btnCancel);

        // Set default checkboxes
        cbNotifyComplainant.setChecked(true);
        cbNotifyRespondent.setChecked(true);
        cbNotifyOfficer.setChecked(true);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Schedule Hearing");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupListeners() {
        etHearingDate.setOnClickListener(v -> showDatePicker());
        etHearingTime.setOnClickListener(v -> showTimePicker());

        btnSchedule.setOnClickListener(v -> scheduleHearing());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                etHearingDate.setText(sdf.format(selectedDate.getTime()));
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minute) -> {
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedTime.set(Calendar.MINUTE, minute);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                etHearingTime.setText(sdf.format(selectedTime.getTime()));
            },
            selectedTime.get(Calendar.HOUR_OF_DAY),
            selectedTime.get(Calendar.MINUTE),
            true
        );
        timePickerDialog.show();
    }

    private void scheduleHearing() {
        String caseId = etCaseId.getText().toString().trim();
        String hearingDate = etHearingDate.getText().toString().trim();
        String hearingTime = etHearingTime.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String presidingOfficer = etPresidingOfficer.getText().toString().trim();

        if (caseId.isEmpty() || hearingDate.isEmpty() || hearingTime.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "📅 Scheduling hearing for case: " + caseId);
        Toast.makeText(this, "Scheduling hearing...", Toast.LENGTH_SHORT).show();

        hearingsService.scheduleHearing(
            caseId,
            hearingDate,
            hearingTime,
            location,
            presidingOfficer.isEmpty() ? currentUserId : presidingOfficer,
            currentUserId,
            new HearingsService.DataOperationCallback() {
                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "✅ " + message);
                    Toast.makeText(ScheduleHearingActivity.this, message, Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ " + error);
                    Toast.makeText(ScheduleHearingActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
}
