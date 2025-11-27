package com.example.blottermanagementsystem.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.blottermanagementsystem.R;
import com.example.blottermanagementsystem.data.entity.ConnectedDevice;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConnectedDeviceAdapter extends RecyclerView.Adapter<ConnectedDeviceAdapter.ViewHolder> {
    
    private List<ConnectedDevice> devices = new ArrayList<>();
    private OnDeviceClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
    
    public interface OnDeviceClickListener {
        void onDeviceClick(ConnectedDevice device);
    }
    
    public ConnectedDeviceAdapter(OnDeviceClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_connected_device, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConnectedDevice device = devices.get(position);
        
        holder.tvDeviceName.setText(device.getDeviceName());
        holder.tvDeviceModel.setText(device.getDeviceModel());
        holder.tvUsername.setText(device.getUsername());
        holder.tvLastActive.setText("Last active: " + dateFormat.format(new Date(device.getLastActive())));
        holder.tvStatus.setText(device.isActive() ? "Active" : "Inactive");
        holder.tvStatus.setTextColor(device.isActive() ? 
            holder.itemView.getContext().getColor(R.color.success_green) : 
            holder.itemView.getContext().getColor(R.color.text_secondary));
        
        if (device.getLocation() != null && !device.getLocation().isEmpty()) {
            holder.tvLocation.setVisibility(View.VISIBLE);
            holder.tvLocation.setText("📍 " + device.getLocation());
        } else {
            holder.tvLocation.setVisibility(View.GONE);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDeviceClick(device);
        });
    }
    
    @Override
    public int getItemCount() {
        return devices.size();
    }
    
    public void setDevices(List<ConnectedDevice> devices) {
        this.devices = devices;
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceName, tvDeviceModel, tvUsername, tvLastActive, tvStatus, tvLocation;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            tvDeviceModel = itemView.findViewById(R.id.tvDeviceModel);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvLastActive = itemView.findViewById(R.id.tvLastActive);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvLocation = itemView.findViewById(R.id.tvLocation);
        }
    }
}
