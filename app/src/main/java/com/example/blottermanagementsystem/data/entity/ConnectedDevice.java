package com.example.blottermanagementsystem.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "connected_devices")
public class ConnectedDevice {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int userId;
    private String username;
    private String deviceName;
    private String deviceModel;
    private String deviceId;
    private String ipAddress;
    private String location;
    private long lastActive;
    private long connectedAt;
    private boolean isActive;

    public ConnectedDevice() {
        this.connectedAt = System.currentTimeMillis();
        this.lastActive = System.currentTimeMillis();
        this.isActive = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getDeviceModel() { return deviceModel; }
    public void setDeviceModel(String deviceModel) { this.deviceModel = deviceModel; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public long getLastActive() { return lastActive; }
    public void setLastActive(long lastActive) { this.lastActive = lastActive; }
    public long getConnectedAt() { return connectedAt; }
    public void setConnectedAt(long connectedAt) { this.connectedAt = connectedAt; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
