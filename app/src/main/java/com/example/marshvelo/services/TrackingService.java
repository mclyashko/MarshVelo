package com.example.marshvelo.services;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;

import com.example.marshvelo.R;
import com.example.marshvelo.other.TrackingUtility;
import com.example.marshvelo.ui.MainActivity;
import timber.log.Timber;

import androidx.lifecycle.LifecycleService;

import static android.app.NotificationManager.IMPORTANCE_LOW;
import static com.example.marshvelo.other.Constants.ACTION_PAUSE_SERVICE;
import static com.example.marshvelo.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT;
import static com.example.marshvelo.other.Constants.ACTION_START_OR_RESUME_SERVICE;
import static com.example.marshvelo.other.Constants.ACTION_STOP_SERVICE;
import static com.example.marshvelo.other.Constants.FASTEST_LOCATION_INTERVAL;
import static com.example.marshvelo.other.Constants.LOCATION_UPDATE_INTERVAL;
import static com.example.marshvelo.other.Constants.NOTIFICATION_CHANNEL_ID;
import static com.example.marshvelo.other.Constants.NOTIFICATION_CHANNEL_NAME;
import static com.example.marshvelo.other.Constants.NOTIFICATION_ID;

import java.util.ArrayList;
import java.util.List;


public class TrackingService extends LifecycleService {

    private static boolean isFirstRun = true;

    private static MutableLiveData<Long> timeRideInSeconds = new MutableLiveData<>();

    public static MutableLiveData<Long> timeRideInMillis = new MutableLiveData<>();
    public static MutableLiveData<Boolean> isTracking = new MutableLiveData<>();
    public static MutableLiveData<ArrayList<ArrayList<LatLng>>> pathPoints = new MutableLiveData<>();
    private FusedLocationProviderClient fusedLocationProviderClient;


    private void postInitialValues() {
        Timber.d("TRACKING_SERVICE: Tracking LiveData initialized");
        isTracking.postValue(false);
        pathPoints.setValue(new ArrayList<>());
        pathPoints.postValue(pathPoints.getValue());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        postInitialValues();
        fusedLocationProviderClient = new FusedLocationProviderClient(this);
        isTracking.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Timber.d("TRACKING_SERVICE: Tracking observed");
                updateLocationTracking(isTracking.getValue());
            }
        });
    }

    private boolean isTimerEnabled = false;
    private long lapTime = 0;
    private long timeRide = 0;
    private long timeStarted = 0;
    private long lastSecondTimestamp = 0;

    private void startTimer() {
        isTracking.postValue(true);
        timeStarted = System.currentTimeMillis();
        isTimerEnabled = true;
    }

    private void pauseService() {
        isTracking.postValue(false);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case ACTION_START_OR_RESUME_SERVICE:
                if (isFirstRun) {
                    startTimer();
                    startForegroundService();
                    isFirstRun = false;
                    Timber.d("TRACKING_SERVICE: Start TrackingService");
                } else {
                    startForegroundService();
                    Timber.d("TRACKING_SERVICE: Resume service");
                }
                break;
            case ACTION_PAUSE_SERVICE:
                pauseService();
                Timber.d("TRACKING_SERVICE: ACTION_PAUSE_SERVICE");
                break;
            case ACTION_STOP_SERVICE:
                Timber.d("TRACKING_SERVICE: ACTION_STOP_SERVICE");
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void addEmptyPolyline() {
        ArrayList polylines =  pathPoints.getValue();
        if (polylines != null) {
            polylines.add(new ArrayList<>());
            pathPoints.postValue(polylines);
            Timber.e("TRACKING_SERVICE: empty PolyLine added");
        }
    }

    private void addPathPoint(Location location) {
        if (location != null) {
            Timber.d("TRACKING_SERVICE: trying to add path point");
            LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
            ArrayList polylines = pathPoints.getValue(); //     ArrayList<ArrayList<LatLng>>
            if (polylines != null) {
                ArrayList polyline = (ArrayList) polylines.get(polylines.size() - 1);
                polyline.add(pos);
                pathPoints.postValue(polylines);
                Timber.d("TRACKING_SERVICE: path point is added");
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void updateLocationTracking(boolean isTracking) {
        Timber.d("TRACKING_SERVICE: trying to update Location Tracking");
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                LocationRequest request = new LocationRequest();
                request.setInterval(LOCATION_UPDATE_INTERVAL);
                request.setFastestInterval(FASTEST_LOCATION_INTERVAL);
                request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                fusedLocationProviderClient.requestLocationUpdates(
                        request,
                        locationCallback(),
                        Looper.getMainLooper()
                );
                Timber.d("TRACKING_SERVICE: Location Tracking is updated");
            }
        } else  {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback());
        }
    }

    private LocationCallback locationCallback() {
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (isTracking.getValue()) {
                    List<Location> locations = locationResult.getLocations();
                    for (Location location: locations) {
                        addPathPoint(location);
                        Timber.d("TRACKING_SERVICE: NEW LOCATION: " + location.getLatitude() + ", " + location.getLongitude());
                    }
                }
            }

            @Override
            public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }
        };
        return locationCallback;
    }

    private void startForegroundService() {
        Timber.d("TRACKING_SERVICE: Starting foreground Service");
        addEmptyPolyline();
        isTracking.postValue(true);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            createNotificationChannel(notificationManager);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(false) // Notification always active
                .setOngoing(true) // Notification can't be swiped away
                .setSmallIcon(R.drawable.bike)
                .setContentTitle("Running App")
                .setContentText("00:00:00")
                .setContentIntent(getMainActivityPendingIntent());
        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }

    // Notification functions

    private PendingIntent getMainActivityPendingIntent() {
        Intent intent  = new Intent(this, MainActivity.class);
        intent.setAction(ACTION_SHOW_TRACKING_FRAGMENT);
        return PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(NotificationManager notificationManager) {
        NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(channel);
    }
}
