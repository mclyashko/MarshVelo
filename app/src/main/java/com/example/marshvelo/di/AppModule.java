package com.example.marshvelo.di;

import android.content.Context;

import androidx.room.Room;

import javax.inject.Singleton;

import dagger.Provides;
import com.example.marshvelo.database.RideDAO;
import com.example.marshvelo.other.Constants;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import com.example.marshvelo.database.RidingDatabase;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Singleton
    @Provides
    public RidingDatabase provideRidingDatabase(
            @ApplicationContext Context app
    ) {
        return Room.databaseBuilder(
                app,
                RidingDatabase.class,
                Constants.RIDING_DATABASE_NAME
        ).build();
    }

    @Singleton
    @Provides
    public RideDAO provideRideDao(RidingDatabase database) {
        return database.getRideDao();
    }
}
