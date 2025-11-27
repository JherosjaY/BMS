package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AudioRecorder {
    private static final String TAG = "AudioRecorder";
    private MediaRecorder mediaRecorder;
    private String currentFilePath;
    private boolean isRecording = false;
    
    public void startRecording(Context context) {
        try {
            File audioDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "recordings");
            if (!audioDir.exists()) {
                audioDir.mkdirs();
            }
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
            String fileName = "recording_" + timestamp + ".3gp";
            currentFilePath = new File(audioDir, fileName).getAbsolutePath();
            
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(currentFilePath);
            
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            
            Log.d(TAG, "Recording started: " + currentFilePath);
        } catch (IOException e) {
            Log.e(TAG, "Error starting recording", e);
        }
    }
    
    public String stopRecording() {
        if (mediaRecorder != null && isRecording) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;
                
                Log.d(TAG, "Recording stopped: " + currentFilePath);
                return currentFilePath;
            } catch (Exception e) {
                Log.e(TAG, "Error stopping recording", e);
            }
        }
        return null;
    }
    
    public boolean isRecording() {
        return isRecording;
    }
    
    public void release() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }
}
