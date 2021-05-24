package com.example.marshvelo.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
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

    private static boolean isFirstRide = true;

    private static MutableLiveData<Long> timeRideInSeconds = new MutableLiveData<>();

    public static MutableLiveData<Boolean> serviceKilled = new MutableLiveData<>();
    public static MutableLiveData<Long> timeRideInMillis = new MutableLiveData<>();
    public static MutableLiveData<Boolean> isTracking = new MutableLiveData<>();
    public static MutableLiveData<ArrayList<ArrayList<LatLng>>> pathPoints = new MutableLiveData<>();
    private FusedLocationProviderClient fusedLocationProviderClient;

    private void postInitialValues() {
        Timber.d("TRACKING_SERVICE: Tracking LiveData initialized");
        serviceKilled.postValue(true);
        timeRideInMillis.postValue(0L);
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

    private long pauseStartTimeInMillis = 0;
    private long pauseStopTimeInMillis = 0;
    private long timePauseInMillis = 0;
    private long timeStarted = 0;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            timeRideInMillis.postValue(System.currentTimeMillis() - timeStarted - timePauseInMillis);
            timerHandler.postDelayed(this, 10);
        }
    };

    private void startTimer() {
        Timber.e("TRACKING SERVICE: timer started");
        pauseStopTimeInMillis = System.currentTimeMillis();
        if (pauseStartTimeInMillis == 0) {
            timePauseInMillis = 0;
        } else {
            timePauseInMillis += pauseStopTimeInMillis - pauseStartTimeInMillis;
        }
        if (timeStarted == 0) {
            timeStarted = System.currentTimeMillis();
        }
        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void pauseService() {
        pauseStartTimeInMillis = System.currentTimeMillis();
        timerHandler.removeCallbacks(timerRunnable);
        isTracking.postValue(false);
    }

    private void killService() {
        serviceKilled.postValue(true);
        isFirstRide = true;
        pauseService();
        postInitialValues();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case ACTION_START_OR_RESUME_SERVICE:
                if (isFirstRide) {
                    serviceKilled.postValue(false);
                    startForegroundService();
                    isFirstRide = false;
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
                killService();
                Timber.d("TRACKING_SERVICE: ACTION_STOP_SERVICE");
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void addEmptyPolyline() {
        ArrayList polylines = pathPoints.getValue();
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


    private void updateLocationTracking(boolean isTracking) {
        Timber.d("TRACKING_SERVICE: trying to update Location Tracking");
        if (isTracking) {
            LocationRequest request = new LocationRequest();
            request.setInterval(LOCATION_UPDATE_INTERVAL);
            request.setFastestInterval(FASTEST_LOCATION_INTERVAL);
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback(),
                    Looper.getMainLooper()
            );
            Timber.d("TRACKING_SERVICE: Location Tracking is updated");
        } else  {
            Timber.d("TRACKING_SERVICE: Location Tracking remote");
            fusedLocationProviderClient.removeLocationUpdates(locationCallback());
        }
    }

    private LocationCallback locationCallback() {
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Timber.e("WWWWWWWWWWWWW");
                if (isTracking.getValue()) {
                    Timber.e("TRACKING_SERVICE: NEW LOVATION AGAIN WTF");
                    List<Location> locations = locationResult.getLocations();
                    if (locations != null && !locations.isEmpty()) {
                        ArrayList polylines = (ArrayList) pathPoints.getValue();
                        if (pathPoints != null && !polylines.isEmpty()) {
                            ArrayList polyline = (ArrayList) polylines.get(polylines.size() - 1);
                            if (!polyline.isEmpty()) {
                                LatLng lastLatLng = (LatLng) polyline.get(polyline.size() - 1);
                                Location location = locations.get(locations.size() - 1);
                                if (location.getLongitude() != lastLatLng.longitude || location.getLatitude() != lastLatLng.latitude) {
                                    addPathPoint(locations.get(locations.size() - 1));
                                    Timber.d("TRACKING_SERVICE: NEW LOCATION: " + locations.get(locations.size() - 1).getLatitude() + ", " + locations.get(locations.size() - 1).getLongitude());
                                }
                            } else {
                                addPathPoint(locations.get(locations.size() - 1));
                            }
                        } else if (polylines.isEmpty()){
                            addPathPoint(locations.get(locations.size() - 1));
                        }
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
        startTimer();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(false) // Notification always active
                .setOngoing(true) // Notification can't be swiped away
                .setSmallIcon(R.drawable.bike)
                .setContentTitle("MarshVelo")
                .setContentText("You are riding now!")
                .setContentIntent(getMainActivityPendingIntent());
        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }


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