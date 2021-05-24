package com.example.marshvelo.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(
        entities = {Ride.class},
        version = 1
)
@TypeConverters(
        Converters.class
)
public abstract class RidingDatabase extends RoomDatabase {
    public abstract RideDAO getRideDao();
}
