package com.example.marshvelo.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(
        entities = {com.example.marshvelo.database.Ride.class},
        version = 1
)
@TypeConverters(
        com.example.marshvelo.database.Converters.class
)
public abstract class RidingDatabase extends RoomDatabase {
    public abstract com.example.marshvelo.database.RideDAO getRideDao();
}
