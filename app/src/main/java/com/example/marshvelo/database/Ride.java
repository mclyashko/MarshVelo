package com.example.marshvelo.database;


import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "riding_table")
public class Ride {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private Bitmap img = null;
    private long timestamp = 0L; // When your ride was
    private int distanceInMeters = 0;
    private long timeInMillis = 0L; // How long your ride was

    // Constructor

    public Ride(String name, Bitmap img, long timestamp, int distanceInMeters, long timeInMillis) {
        this.name = name;
        this.img = img;
        this.timestamp = timestamp;
        this.distanceInMeters = distanceInMeters;
        this.timeInMillis = timeInMillis;
    }

    // Getters
    public int getId() { return id; }

    public String getName() { return name; }

    public Bitmap getImg() {
        return img;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getDistanceInMeters() {
        return distanceInMeters;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    // Setters

    public void setId(int id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setImg(Bitmap img) {
        this.img = img;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setDistanceInMeters(int distanceInMeters) { this.distanceInMeters = distanceInMeters; }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

}
