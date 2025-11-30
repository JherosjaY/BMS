package com.example.blottermanagementsystem.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
// ❌ REMOVED: import com.example.blottermanagementsystem.services.HearingsService; (Pure online mode)
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class HearingsCalendarActivity extends BaseActivity {
    private static final String TAG = "HearingsCalendar";

    private Toolbar toolbar;
    private TextView tvMonthYear, tvNoHearings;
    private RecyclerView rvHearings;
    private LinearLayout emptyState;
    private MaterialButton btnPrevMonth, btnNextMonth, btnScheduleHearing;
    private MaterialCardView cardCalendar;

    // ❌ REMOVED: private HearingsService hearingsService; (Pure online mode)
    private Calendar currentCalendar;
    private String currentUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hearings_calendar);

        // ❌ REMOVED: hearingsService = new HearingsService(this); (Pure online mode)
        currentUserRole = getSharedPreferences("UserSession", MODE_PRIVATE).getString("role", "User");
        currentCalendar = Calendar.getInstance();

        initializeViews();
        setupToolbar();
        setupListeners();
        loadHearingsForCurrentMonth();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvNoHearings = findViewById(R.id.tvNoHearings);
        rvHearings = findViewById(R.id.rvHearings);
        emptyState = findViewById(R.id.emptyState);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        btnScheduleHearing = findViewById(R.id.btnScheduleHearing);
        cardCalendar = findViewById(R.id.cardCalendar);

        rvHearings.setLayoutManager(new LinearLayoutManager(this));
        updateMonthYearDisplay();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Hearings Calendar");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupListeners() {
        btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateMonthYearDisplay();
            loadHearingsForCurrentMonth();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateMonthYearDisplay();
            loadHearingsForCurrentMonth();
        });

        btnScheduleHearing.setOnClickListener(v -> {
            if ("Officer".equalsIgnoreCase(currentUserRole) || "Admin".equalsIgnoreCase(currentUserRole)) {
                startActivity(new Intent(this, ScheduleHearingActivity.class));
            } else {
                Toast.makeText(this, "Only Officers and Admins can schedule hearings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMonthYearDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonthYear.setText(sdf.format(currentCalendar.getTime()));
    }

    private void loadHearingsForCurrentMonth() {
        Log.d(TAG, "📅 Loading hearings for month");
        Toast.makeText(this, "Loading hearings...", Toast.LENGTH_SHORT).show();

        Calendar startCal = (Calendar) currentCalendar.clone();
        startCal.set(Calendar.DAY_OF_MONTH, 1);

        Calendar endCal = (Calendar) currentCalendar.clone();
        endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = sdf.format(startCal.getTime());
        String endDate = sdf.format(endCal.getTime());

        hearingsService.getHearingsForCalendar(startDate, endDate, new HearingsService.HearingsCallback() {
            @Override
            public void onSuccess(Map<String, Object> hearings) {
                Log.d(TAG, "✅ Hearings loaded successfully");
                displayHearings(hearings);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error loading hearings: " + error);
                showEmptyState(error);
            }
        });
    }

    private void displayHearings(Map<String, Object> hearings) {
        if (hearings == null || hearings.isEmpty()) {
            showEmptyState("No hearings scheduled for this month");
            return;
        }

        emptyState.setVisibility(View.GONE);
        rvHearings.setVisibility(View.VISIBLE);

        Log.d(TAG, "📊 Displaying " + hearings.size() + " hearings");
        Toast.makeText(this, "✅ Hearings loaded", Toast.LENGTH_SHORT).show();
    }

    private void showEmptyState(String message) {
        emptyState.setVisibility(View.VISIBLE);
        rvHearings.setVisibility(View.GONE);
        tvNoHearings.setText(message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "📋 Activity resumed - refreshing hearings");
        loadHearingsForCurrentMonth();
    }
}
