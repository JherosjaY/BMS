package com.example.blottermanagementsystem.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.blottermanagementsystem.R;

/**
 * Modern Toast with Icons and Advanced Styling
 * Usage: ModernToast.success(context, "Success message");
 *        ModernToast.error(context, "Error message");
 *        ModernToast.warning(context, "Warning message");
 *        ModernToast.info(context, "Info message");
 */
public class ModernToast {
    
    public static final int SUCCESS = 1;
    public static final int ERROR = 2;
    public static final int WARNING = 3;
    public static final int INFO = 4;
    
    /**
     * Show success toast with checkmark icon
     */
    public static void success(Context context, String message) {
        showCustomToast(context, message, SUCCESS);
    }
    
    /**
     * Show error toast with X icon
     */
    public static void error(Context context, String message) {
        showCustomToast(context, message, ERROR);
    }
    
    /**
     * Show warning toast with warning icon
     */
    public static void warning(Context context, String message) {
        showCustomToast(context, message, WARNING);
    }
    
    /**
     * Show info toast with info icon
     */
    public static void info(Context context, String message) {
        showCustomToast(context, message, INFO);
    }
    
    private static void showCustomToast(Context context, String message, int type) {
        try {
            // Create custom layout programmatically
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(24, 16, 24, 16);
            layout.setGravity(Gravity.CENTER_VERTICAL);
            
            // Create background with rounded corners
            GradientDrawable background = new GradientDrawable();
            background.setCornerRadius(12f);
            
            // Set colors and icon based on type
            String iconText;
            int backgroundColor, textColor;
            
            switch (type) {
                case SUCCESS:
                    backgroundColor = Color.parseColor("#10B981"); // Green
                    textColor = Color.WHITE;
                    iconText = "✅"; // Checkmark
                    break;
                case ERROR:
                    backgroundColor = Color.parseColor("#EF4444"); // Red
                    textColor = Color.WHITE;
                    iconText = "❌"; // X mark
                    break;
                case WARNING:
                    backgroundColor = Color.parseColor("#F59E0B"); // Yellow
                    textColor = Color.WHITE;
                    iconText = "⚠️"; // Warning
                    break;
                case INFO:
                default:
                    backgroundColor = Color.parseColor("#3B82F6"); // Blue
                    textColor = Color.WHITE;
                    iconText = "ℹ️"; // Info
                    break;
            }
            
            background.setColor(backgroundColor);
            layout.setBackground(background);
            
            // Add app logo icon instead of warning/error icons
            ImageView iconView = new ImageView(context);
            iconView.setImageResource(R.drawable.ic_app_logo); // App logo
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                48, 48); // 48dp x 48dp
            iconParams.setMargins(0, 0, 16, 0);
            iconView.setLayoutParams(iconParams);
            layout.addView(iconView);
            
            // Add message text
            TextView messageView = new TextView(context);
            messageView.setText(message);
            messageView.setTextColor(textColor);
            messageView.setTextSize(14);
            messageView.setMaxLines(2);
            messageView.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
            messageView.setLayoutParams(textParams);
            layout.addView(messageView);
            
            // Create and show toast
            Toast toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
            toast.show();
            
        } catch (Exception e) {
            // Fallback to regular toast
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Show custom toast with custom icon and colors
     */
    public static void custom(Context context, String message, String icon, String backgroundColor, String textColor) {
        try {
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(24, 16, 24, 16);
            layout.setGravity(Gravity.CENTER_VERTICAL);
            
            // Create background
            GradientDrawable background = new GradientDrawable();
            background.setCornerRadius(12f);
            background.setColor(Color.parseColor(backgroundColor));
            layout.setBackground(background);
            
            // Add app logo icon instead of custom icon
            ImageView iconView = new ImageView(context);
            iconView.setImageResource(R.drawable.ic_app_logo); // App logo
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                48, 48); // 48dp x 48dp
            iconParams.setMargins(0, 0, 16, 0);
            iconView.setLayoutParams(iconParams);
            layout.addView(iconView);
            
            // Add message
            TextView messageView = new TextView(context);
            messageView.setText(message);
            messageView.setTextColor(Color.parseColor(textColor));
            messageView.setTextSize(14);
            messageView.setMaxLines(2);
            layout.addView(messageView);
            
            // Show toast
            Toast toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
            toast.show();
            
        } catch (Exception e) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Show modern success toast with advanced styling
     */
    public static void modernSuccess(Context context, String title, String message) {
        showModernToast(context, title, message, "✅", "#10B981");
    }
    
    /**
     * Show modern error toast with advanced styling
     */
    public static void modernError(Context context, String title, String message) {
        showModernToast(context, title, message, "❌", "#EF4444");
    }
    
    /**
     * Show modern warning toast with advanced styling
     */
    public static void modernWarning(Context context, String title, String message) {
        showModernToast(context, title, message, "⚠️", "#F59E0B");
    }
    
    /**
     * Show modern info toast with advanced styling
     */
    public static void modernInfo(Context context, String title, String message) {
        showModernToast(context, title, message, "ℹ️", "#3B82F6");
    }
    
    private static void showModernToast(Context context, String title, String message, String icon, String color) {
        try {
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(20, 18, 20, 18);
            layout.setGravity(Gravity.CENTER_VERTICAL);
            
            // Modern background with shadow effect
            GradientDrawable background = new GradientDrawable();
            background.setCornerRadius(16f);
            background.setColor(Color.parseColor(color));
            // Add subtle shadow effect
            layout.setElevation(8f);
            layout.setBackground(background);
            
            // App logo icon instead of emoji icon
            ImageView iconView = new ImageView(context);
            iconView.setImageResource(R.drawable.ic_app_logo); // App logo
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                56, 56); // 56dp x 56dp for modern toast
            iconParams.setMargins(0, 0, 16, 0);
            iconView.setLayoutParams(iconParams);
            layout.addView(iconView);
            
            // Text container
            LinearLayout textContainer = new LinearLayout(context);
            textContainer.setOrientation(LinearLayout.VERTICAL);
            
            // Title text
            TextView titleView = new TextView(context);
            titleView.setText(title);
            titleView.setTextColor(Color.WHITE);
            titleView.setTextSize(16);
            titleView.setTypeface(null, android.graphics.Typeface.BOLD);
            textContainer.addView(titleView);
            
            // Message text
            TextView messageView = new TextView(context);
            messageView.setText(message);
            messageView.setTextColor(Color.parseColor("#E5E7EB"));
            messageView.setTextSize(14);
            messageView.setMaxLines(3);
            LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
            messageParams.setMargins(0, 4, 0, 0);
            messageView.setLayoutParams(messageParams);
            textContainer.addView(messageView);
            
            layout.addView(textContainer);
            
            // Create and show toast
            Toast toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 120);
            toast.show();
            
        } catch (Exception e) {
            Toast.makeText(context, title + ": " + message, Toast.LENGTH_LONG).show();
        }
    }
}
