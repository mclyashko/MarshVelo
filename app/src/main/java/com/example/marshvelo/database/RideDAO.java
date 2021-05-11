package com.example.marshvelo.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RideDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRide(com.example.marshvelo.database.Ride ride);

    @Delete
    void deleteRide(com.example.marshvelo.database.Ride ride);

    @Query("SELECT * FROM riding_table ORDER BY timestamp DESC")
    LiveData<List<com.example.marshvelo.database.Ride>> getAllRidesSortedByDate();
}