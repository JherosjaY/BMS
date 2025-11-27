package com.example.blottermanagementsystem.utils;

import android.animation.ValueAnimator;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ShimmerHelper {
    
    /**
     * Apply shimmer effect to a view
     */
    public static void applyShimmer(View view) {
        view.setAlpha(0.5f);
        
        ValueAnimator animator = ValueAnimator.ofFloat(0.5f, 1f, 0.5f);
        animator.setDuration(1500);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            view.setAlpha(value);
        });
        animator.start();
        
        view.setTag(animator);
    }
    
    /**
     * Stop shimmer effect
     */
    public static void stopShimmer(View view) {
        Object tag = view.getTag();
        if (tag instanceof ValueAnimator) {
            ((ValueAnimator) tag).cancel();
        }
        view.setAlpha(1f);
    }
    
    /**
     * Show shimmer loading state for a ViewGroup
     */
    public static void showShimmer(ViewGroup container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            applyShimmer(child);
        }
    }
    
    /**
     * Hide shimmer and show content
     */
    public static void hideShimmer(ViewGroup container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            stopShimmer(child);
        }
    }
    
    /**
     * Create pulsing animation for loading state
     */
    public static void pulseAnimation(View view) {
        ValueAnimator scaleDown = ValueAnimator.ofFloat(1f, 0.95f);
        scaleDown.setDuration(500);
        scaleDown.setRepeatCount(ValueAnimator.INFINITE);
        scaleDown.setRepeatMode(ValueAnimator.REVERSE);
        scaleDown.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            view.setScaleX(scale);
            view.setScaleY(scale);
        });
        scaleDown.start();
        
        view.setTag(scaleDown);
    }
    
    /**
     * Stop pulse animation
     */
    public static void stopPulse(View view) {
        Object tag = view.getTag();
        if (tag instanceof ValueAnimator) {
            ((ValueAnimator) tag).cancel();
        }
        view.setScaleX(1f);
        view.setScaleY(1f);
    }
}
