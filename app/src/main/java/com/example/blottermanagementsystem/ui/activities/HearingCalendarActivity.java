package com.example.blottermanagementsystem.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.database.BlotterDatabase;
import com.example.blottermanagementsystem.data.entity.Hearing;
import com.example.blottermanagementsystem.ui.adapters.HearingAdapter;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import com.example.blottermanagementsystem.utils.RoleAccessControl;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class HearingCalendarActivity extends BaseActivity {
    
    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private HearingAdapter adapter;
    private TextView tvEmpty, tvSelectedDate;
    private BlotterDatabase database;
    private long selectedDate;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        PreferencesManager preferencesManager = new PreferencesManager(this);
        if (!RoleAccessControl.checkAnyRole(this, new String[]{"Admin", "Officer"}, preferencesManager)) {
            return;
        }
        
        setContentView(R.layout.activity_hearing_calendar);
        
        database = BlotterDatabase.getDatabase(this);
        selectedDate = System.currentTimeMillis();
        
        setupToolbar();
        initViews();
        setupListeners();
        loadHearingsForDate(selectedDate);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Hearing Calendar");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void initViews() {
        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HearingAdapter(hearing -> {
            // Handle hearing click
        });
        recyclerView.setAdapter(adapter);
    }
    
    private void setupListeners() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth, 0, 0, 0);
            selectedDate = calendar.getTimeInMillis();
            loadHearingsForDate(selectedDate);
        });
    }
    
    private void loadHearingsForDate(long date) {
        new Thread(() -> {
            List<Hearing> allHearings = database.hearingDao().getAllHearings();
            
            // Filter hearings for selected date
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTimeInMillis(date);
            
            // TODO: Fix date filtering - hearingDate is String, needs conversion
            List<Hearing> hearingsForDate = allHearings; // Show all for now
            // .filter(hearing -> isSameDay(hearing.getHearingDate(), date))
            // .collect(Collectors.toList());
            
            runOnUiThread(() -> {
                tvSelectedDate.setText(android.text.format.DateFormat.format("MMMM dd, yyyy", date));
                
                if (hearingsForDate.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.GONE);
                    adapter.setHearings(hearingsForDate);
                }
            });
        }).start();
    }
    
    private boolean isSameDay(long date1, long date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(date1);
        cal2.setTimeInMillis(date2);
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
