package com.example.blottermanagementsystem.utils;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import java.io.File;
import java.io.IOException;

public class MediaManager {
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String currentAudioPath;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private int currentPlayingPosition = -1;
    
    public String startRecording(Context context) throws IOException {
        File audioFile = new File(context.getExternalFilesDir(null), "audio_" + System.currentTimeMillis() + ".3gp");
        currentAudioPath = audioFile.getAbsolutePath();
        
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(currentAudioPath);
        mediaRecorder.prepare();
        mediaRecorder.start();
        isRecording = true;
        
        return currentAudioPath;
    }
    
    public void stopRecording() {
        if (mediaRecorder != null && isRecording) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaRecorder = null;
            isRecording = false;
        }
    }
    
    public void startPlaying(Context context, Uri audioUri, int position) throws IOException {
        stopPlaying(); // Stop any currently playing audio
        
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(context, audioUri);
        mediaPlayer.prepare();
        mediaPlayer.start();
        isPlaying = true;
        currentPlayingPosition = position;
        
        mediaPlayer.setOnCompletionListener(mp -> {
            isPlaying = false;
            currentPlayingPosition = -1;
        });
    }
    
    public void stopPlaying() {
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            currentPlayingPosition = -1;
        }
    }
    
    public void pausePlaying() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }
    
    public void resumePlaying() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }
    
    public long getAudioDuration(Context context, Uri audioUri) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, audioUri);
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            retriever.release();
            return durationStr != null ? Long.parseLong(durationStr) : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public long getVideoDuration(Context context, Uri videoUri) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, videoUri);
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            retriever.release();
            return durationStr != null ? Long.parseLong(durationStr) : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public boolean isRecording() {
        return isRecording;
    }
    
    public boolean isPlaying() {
        return isPlaying;
    }
    
    public int getCurrentPlayingPosition() {
        return currentPlayingPosition;
    }
    
    public void release() {
        stopRecording();
        stopPlaying();
    }
}
