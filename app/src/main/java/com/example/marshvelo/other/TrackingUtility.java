package com.example.marshvelo.other;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.os.Build;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.EasyPermissions;

public class TrackingUtility {
    public static float calculatePolylineLength(ArrayList<LatLng> polyline) {
        float distance = 0;
        float[] result = new float[1];
        for (int i  = 0; i < polyline.size() - 2; i++) {
            LatLng pos1 = polyline.get(i);
            LatLng pos2 = polyline.get(i + 1);
            Location.distanceBetween(pos1.latitude,
                    pos1.longitude,
                    pos2.latitude,
                    pos2.longitude,
                    result);
            distance += result[0];

        }
        return distance;
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
