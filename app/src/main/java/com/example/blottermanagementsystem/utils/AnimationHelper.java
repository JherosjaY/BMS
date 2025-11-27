package com.example.blottermanagementsystem.utils;

import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;

public class AnimationHelper {
    
    /**
     * Apply slide-in-right transition when starting activity
     */
    public static void slideInRight(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
    
    /**
     * Apply slide-in-left transition when going back
     */
    public static void slideInLeft(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
    
    /**
     * Apply fade transition
     */
    public static void fade(Activity activity) {
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    
    /**
     * Apply scale transition
     */
    public static void scale(Activity activity) {
        activity.overridePendingTransition(R.anim.scale_in, R.anim.scale_out);
    }
    
    /**
     * Apply slide up transition (for bottom sheets/dialogs)
     */
    public static void slideUp(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_up, R.anim.fade_out);
    }
    
    /**
     * Animate a view with fade in
     */
    public static void fadeIn(View view) {
        Animation animation = android.view.animation.AnimationUtils.loadAnimation(
            view.getContext(), R.anim.fade_in);
        view.startAnimation(animation);
    }
    
    /**
     * Animate a view with fade out
     */
    public static void fadeOut(View view) {
        Animation animation = android.view.animation.AnimationUtils.loadAnimation(
            view.getContext(), R.anim.fade_out);
        view.startAnimation(animation);
    }
    
    /**
     * Animate a view with scale in
     */
    public static void scaleIn(View view) {
        Animation animation = android.view.animation.AnimationUtils.loadAnimation(
            view.getContext(), R.anim.scale_in);
        view.startAnimation(animation);
    }
    
    /**
     * Animate a view with slide up
     */
    public static void slideUp(View view) {
        Animation animation = android.view.animation.AnimationUtils.loadAnimation(
            view.getContext(), R.anim.slide_up);
        view.startAnimation(animation);
    }
    
    /**
     * Animate RecyclerView items with fall down effect
     */
    public static void animateRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutAnimation(android.view.animation.AnimationUtils.loadLayoutAnimation(
            recyclerView.getContext(), R.anim.item_animation_fall_down));
        recyclerView.scheduleLayoutAnimation();
    }
    
    /**
     * Stagger animation for multiple views
     */
    public static void staggerAnimation(View... views) {
        long delay = 0;
        for (View view : views) {
            view.setAlpha(0f);
            view.setTranslationY(50f);
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay(delay)
                .start();
            delay += 100;
        }
    }
    
    /**
     * Pulse animation for attention
     */
    public static void pulse(View view) {
        view.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(150)
            .withEndAction(() -> {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start();
            })
            .start();
    }
    
    /**
     * Shake animation for errors
     */
    public static void shake(View view) {
        view.animate()
            .translationX(-25f)
            .setDuration(100)
            .withEndAction(() -> {
                view.animate()
                    .translationX(25f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        view.animate()
                            .translationX(0f)
                            .setDuration(100)
                            .start();
                    })
                    .start();
            })
            .start();
    }
}
