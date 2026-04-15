package com.example.studentprofile.model;

import java.io.Serializable;

public class PositionPoint implements Serializable {
    private long id;
    private double latitude;
    private double longitude;
    private long timestamp;

    public PositionPoint() {
    }

    public PositionPoint(long id, double latitude, double longitude, long timestamp) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
