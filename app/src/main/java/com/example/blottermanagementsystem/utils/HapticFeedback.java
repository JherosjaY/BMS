package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

public class HapticFeedback {
    private final Vibrator vibrator;
    
    public HapticFeedback(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = 
                (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            this.vibrator = vibratorManager.getDefaultVibrator();
        } else {
            this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }
    
    public void lightTap() {
        vibrate(50);
    }
    
    public void mediumTap() {
        vibrate(100);
    }
    
    public void heavyTap() {
        vibrate(200);
    }
    
    public void success() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            long[] pattern = {0, 50, 50, 50};
            int[] amplitudes = {0, 100, 0, 100};
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1));
        } else {
            vibrator.vibrate(new long[]{0, 50, 50, 50}, -1);
        }
    }
    
    public void error() {
        vibrate(300);
    }
    
    public void warning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            long[] pattern = {0, 50, 100, 50, 100, 50};
            int[] amplitudes = {0, 150, 0, 150, 0, 150};
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1));
        } else {
            vibrator.vibrate(new long[]{0, 50, 100, 50, 100, 50}, -1);
        }
    }
    
    private void vibrate(long duration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(duration);
        }
    }
}
