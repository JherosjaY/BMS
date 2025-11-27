package com.example.blottermanagementsystem.ui.activities;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.utils.PreferencesManager;
import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends BaseActivity {
    
    private ImageView ivIcon;
    private TextView tvTitle, tvDescription;
    private MaterialButton btnNext;
    private View indicator1, indicator2, indicator3, indicator4;
    private androidx.cardview.widget.CardView contentCard;
    private PreferencesManager preferencesManager;
    private GestureDetector gestureDetector;
    
    private int currentPage = 0;
    private final int TOTAL_PAGES = 4;
    
    // Onboarding content - Modern Icons!
    private final int[] icons = {
        R.drawable.ic_onboarding_clipboard,  // Clipboard - Reports
        R.drawable.ic_onboarding_search,     // Magnifying Glass - Search/Record
        R.drawable.ic_onboarding_chart,      // Chart - Analytics/Track
        R.drawable.ic_onboarding_bell        // Bell - Notifications
    };
    
    private final String[] titles = {
        "Manage Blotter Reports Efficiently",
        "Record Suspects, Witnesses, and Evidence",
        "Track Case Progress and Hearings",
        "Stay Notified Anytime, Anywhere"
    };
    
    private final String[] descriptions = {
        "Streamline your case management with our comprehensive blotter system. Track, update, and manage all reports in one place.\n\nEfficiently organize incident records, assign cases to officers, and monitor progress with real-time updates.",
        "Easily document all case details including suspects, witnesses, and evidence with organized data entry.\n\nCapture photos, record statements, and maintain a complete digital trail of all case-related information.",
        "Monitor case status updates and schedule hearings with automated notifications and reminders.\n\nStay on top of deadlines, track case milestones, and ensure timely resolution of all incidents.",
        "Receive real-time notifications about case updates, hearing schedules, and important events.\n\nNever miss critical updates with instant alerts delivered directly to your device, keeping you informed 24/7."
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_onboarding_page);
        
        android.util.Log.d("OnboardingActivity", "✅ OnboardingActivity.onCreate() - Screen is displaying!");
        
        preferencesManager = new PreferencesManager(this);
        
        initViews();
        setupGestureDetector();
        setupListeners();
        showPage(0);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // If onboarding is already completed, skip this screen
        if (preferencesManager.isOnboardingCompleted()) {
            android.util.Log.d("OnboardingActivity", "⚠️ Onboarding already completed - skipping to PermissionsSetupActivity");
            startActivity(new Intent(this, PermissionsSetupActivity.class));
            finish();
        }
    }
    
    private void initViews() {
        contentCard = findViewById(R.id.contentCard);
        ivIcon = findViewById(R.id.ivIcon);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        btnNext = findViewById(R.id.btnNext);
        indicator1 = findViewById(R.id.indicator1);
        indicator2 = findViewById(R.id.indicator2);
        indicator3 = findViewById(R.id.indicator3);
        indicator4 = findViewById(R.id.indicator4);
    }
    
    private void showPage(int page) {
        currentPage = page;
        
        // Smooth card animation (fade + scale + slide)
        contentCard.animate()
            .alpha(0.7f)
            .scaleX(0.98f)
            .scaleY(0.98f)
            .translationY(-10f)
            .setDuration(150)
            .withEndAction(() -> {
                contentCard.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationY(0f)
                    .setDuration(200)
                    .start();
            }).start();
        
        // Simple icon fade animation (no scaling to prevent bouncing)
        ivIcon.animate().alpha(0f).setDuration(150).withEndAction(() -> {
            // Update icon
            ivIcon.setImageResource(icons[page]);
            
            // Fade in animation
            ivIcon.animate().alpha(1f).setDuration(150).start();
        }).start();
        
        // Slide + fade animation for title
        tvTitle.animate().alpha(0f).translationX(-50f).setDuration(200).withEndAction(() -> {
            tvTitle.setText(titles[page]);
            tvTitle.setTranslationX(50f);
            tvTitle.animate().alpha(1f).translationX(0f).setDuration(300).start();
        }).start();
        
        // Slide + fade animation for description
        tvDescription.animate().alpha(0f).translationX(-50f).setDuration(200).withEndAction(() -> {
            tvDescription.setText(descriptions[page]);
            tvDescription.setTranslationX(50f);
            tvDescription.animate().alpha(1f).translationX(0f).setDuration(300).setStartDelay(50).start();
        }).start();
        
        // Update indicators with animation
        updateIndicators(page);
        
        // Button animation
        btnNext.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() -> {
            // Update button text and icon
            if (page == TOTAL_PAGES - 1) {
                btnNext.setText("Get Started");
                btnNext.setIcon(getDrawable(R.drawable.ic_rocket));
            } else {
                btnNext.setText("Next");
                btnNext.setIcon(getDrawable(R.drawable.ic_arrow_forward));
            }
            btnNext.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
        }).start();
    }
    
    private void updateIndicators(int position) {
        View[] indicators = {indicator1, indicator2, indicator3, indicator4};
        for (int i = 0; i < indicators.length; i++) {
            if (i == position) {
                indicators[i].setBackgroundResource(R.drawable.indicator_dot_active);
            } else {
                indicators[i].setBackgroundResource(R.drawable.indicator_dot);
            }
        }
    }
    
    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;
            
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX < 0) {
                        // Swipe left - next page
                        if (currentPage < TOTAL_PAGES - 1) {
                            showPage(currentPage + 1);
                        }
                    } else {
                        // Swipe right - previous page
                        if (currentPage > 0) {
                            showPage(currentPage - 1);
                        }
                    }
                    return true;
                }
                return false;
            }
        });
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }
    
    private void setupListeners() {
        btnNext.setOnClickListener(v -> {
            if (currentPage < TOTAL_PAGES - 1) {
                showPage(currentPage + 1);
            } else {
                finishOnboarding();
            }
        });
    }
    
    private void finishOnboarding() {
        android.util.Log.d("OnboardingActivity", "✅ finishOnboarding() - Setting onboarding_completed = true");
        preferencesManager.setOnboardingCompleted(true);
        android.util.Log.d("OnboardingActivity", "🔐 finishOnboarding() - Launching PermissionsSetupActivity");
        startActivity(new Intent(this, PermissionsSetupActivity.class));
        finish();
    }
    
}
