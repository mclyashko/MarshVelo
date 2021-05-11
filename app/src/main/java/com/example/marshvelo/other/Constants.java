package com.example.marshvelo.other;

import android.graphics.Color;

public class Constants {
    public static final String RIDING_DATABASE_NAME = "riding_database";
    public static final int REQUEST_CODE_LOCATION_PERMISSION  = 123;

    public static final String ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE";
    public static final String ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE";
    public static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";
    public static final String ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT";

    public static final String NOTIFICATION_CHANNEL_ID = "tracking_channel";
    public static final String NOTIFICATION_CHANNEL_NAME = "Tracking";
    public static final int NOTIFICATION_ID = 1;

    public static final int POLYLINE_COLOR = Color.RED;
    public static final float POLYLINE_WIDTH = 8f;
    public static final float MAP_ZOOM = 15f;

    public static final int LOCATION_UPDATE_INTERVAL = 5000;
    public static final int FASTEST_LOCATION_INTERVAL = 2000;

}
