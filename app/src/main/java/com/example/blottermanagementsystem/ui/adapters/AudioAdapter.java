package com.example.blottermanagementsystem.ui.adapters;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder> {
    
    private final List<AudioItem> audioList;
    private final OnAudioClickListener listener;
    private final boolean showDeleteButton;
    private int currentlyPlayingPosition = -1;
    
    public static class AudioItem {
        public Uri uri;
        public String name;
        public long duration;
        public boolean isPlaying;
        
        public AudioItem(Uri uri, String name, long duration) {
            this.uri = uri;
            this.name = name;
            this.duration = duration;
            this.isPlaying = false;
        }
    }
    
    public interface OnAudioClickListener {
        void onAudioPlay(int position, Uri uri);
        void onAudioDelete(int position);
    }
    
    public AudioAdapter(List<AudioItem> audioList, OnAudioClickListener listener) {
        this(audioList, listener, true);
    }
    
    public AudioAdapter(List<AudioItem> audioList, OnAudioClickListener listener, boolean showDeleteButton) {
        this.audioList = audioList;
        this.listener = listener;
        this.showDeleteButton = showDeleteButton;
    }
    
    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_audio, parent, false);
        return new AudioViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        AudioItem audio = audioList.get(position);
        
        holder.tvAudioName.setText(audio.name);
        holder.tvDuration.setText(formatDuration(audio.duration));
        
        // Update play/pause icon
        if (audio.isPlaying) {
            holder.btnPlay.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            holder.btnPlay.setImageResource(android.R.drawable.ic_media_play);
        }
        
        holder.btnPlay.setOnClickListener(v -> listener.onAudioPlay(position, audio.uri));
        
        if (showDeleteButton) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setEnabled(true);
            holder.btnDelete.setOnClickListener(v -> listener.onAudioDelete(position));
        } else {
            holder.btnDelete.setVisibility(View.INVISIBLE); // Use INVISIBLE to maintain layout
            holder.btnDelete.setEnabled(false);
        }
    }
    
    @Override
    public int getItemCount() {
        return audioList.size();
    }
    
    public void setPlaying(int position, boolean playing) {
        // Stop previous audio
        if (currentlyPlayingPosition != -1 && currentlyPlayingPosition != position) {
            audioList.get(currentlyPlayingPosition).isPlaying = false;
            notifyItemChanged(currentlyPlayingPosition);
        }
        
        // Update current audio
        if (position >= 0 && position < audioList.size()) {
            audioList.get(position).isPlaying = playing;
            currentlyPlayingPosition = playing ? position : -1;
            notifyItemChanged(position);
        }
    }
    
    private String formatDuration(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    static class AudioViewHolder extends RecyclerView.ViewHolder {
        TextView tvAudioName;
        TextView tvDuration;
        ImageButton btnPlay;
        ImageButton btnDelete;
        
        AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAudioName = itemView.findViewById(R.id.tvAudioName);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            btnPlay = itemView.findViewById(R.id.btnPlay);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
