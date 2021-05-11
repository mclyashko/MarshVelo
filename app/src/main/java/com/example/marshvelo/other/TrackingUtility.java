package com.example.marshvelo.other;

import android.Manifest;
import android.content.Context;
import android.os.Build;

import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.EasyPermissions;

public class TrackingUtility {
    public static boolean hasLocationPermissions(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return EasyPermissions.hasPermissions(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            );
        } else {
            return EasyPermissions.hasPermissions(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            );
        }
    }

    public static String getFormattedStopWath(long ms, boolean includeMillis) {
        long milliseconds = ms;
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        milliseconds -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        milliseconds -=TimeUnit.SECONDS.toMillis(seconds);
        milliseconds /= 10;

        String stringTime = "";

        if (hours < 10) {
            stringTime += "0";
        }
        stringTime += hours + ":";
        if (minutes < 10) {
            stringTime += "0";
        }
        stringTime += minutes + ":";
        if (seconds < 10) {
            stringTime += "0";
        }
        stringTime += seconds;
        if (!includeMillis) {
            return stringTime;
        }
        stringTime += ":";
        if (milliseconds < 10) {
            stringTime += "0";
        }
        stringTime += milliseconds;
        return stringTime;
    }
}