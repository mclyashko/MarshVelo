package com.example.marshvelo.ui.fragments;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import com.example.marshvelo.R;
import com.example.marshvelo.other.TrackingUtility;
import com.example.marshvelo.services.TrackingService;
import com.example.marshvelo.ui.MainActivity;
import com.example.marshvelo.ui.viewmodels.MainViewModel;

import static com.example.marshvelo.other.Constants.ACTION_PAUSE_SERVICE;
import static com.example.marshvelo.other.Constants.ACTION_START_OR_RESUME_SERVICE;
import static com.example.marshvelo.other.Constants.MAP_ZOOM;
import static com.example.marshvelo.other.Constants.POLYLINE_COLOR;
import static com.example.marshvelo.other.Constants.POLYLINE_WIDTH;
import static com.example.marshvelo.other.Constants.REQUEST_CODE_LOCATION_PERMISSION;

@AndroidEntryPoint
public class TrackingFragment extends Fragment implements  EasyPermissions.PermissionCallbacks, OnMapReadyCallback {

    private static TrackingFragment trackingFragment = null;

    private MainViewModel viewModel;
    private GoogleMap map = null;
    private MapView mapView;
    private Button btnToggleRide;
    private Button btnFinishRun;

    private static boolean isTracking = false;
    private static ArrayList<ArrayList<LatLng>> pathPoints = new ArrayList<>();

    // TODO: Singleton doesn't help :(
    public static TrackingFragment getInstance() {
        if (trackingFragment == null) {
            trackingFragment = new TrackingFragment();
        }
        return trackingFragment;
    }

    public TrackingFragment() {
        super(R.layout.fragment_tracking);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = getView().findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        btnToggleRide = getView().findViewById(R.id.btnToggleRun);
        btnFinishRun = getView().findViewById(R.id.btnFinishRun);

        // Request permissions from user
        requestPermission();
        // Create an instance of ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        btnToggleRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRide();
            }
        });
        mapView.getMapAsync(this);
        addAllPolylines();

        subscribeToObservers();
    }

    private void subscribeToObservers() {
        TrackingService.isTracking.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                updateTracking(aBoolean);
            }
        });
        TrackingService.pathPoints.observe(getViewLifecycleOwner(), new Observer<ArrayList<ArrayList<LatLng>>>() {
            @Override
            public void onChanged(ArrayList<ArrayList<LatLng>> arrayLists) {
                pathPoints = arrayLists;
                addLatestPolyline();
                moveCameraToUser();
            }
        });
    }

    private void toggleRide() {
        if (isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE);
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE);
        }
    }

    private void updateTracking(boolean isTrack) {
        isTracking = isTrack;
        if (!isTracking) {
            btnToggleRide.setText("Resume");
            btnFinishRun.setVisibility(getView().VISIBLE);
            btnFinishRun.setText("Finish");
        } else {
            btnToggleRide.setText("Stop");
            btnFinishRun.setVisibility(getView().GONE);
        }
    }

    private void moveCameraToUser() {
        if (!pathPoints.isEmpty() && !pathPoints.get(pathPoints.size() - 1).isEmpty()) {
            if (map != null) {
                ArrayList<LatLng> polyline = pathPoints.get(pathPoints.size() - 1);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        polyline.get(polyline.size() - 1),
                        MAP_ZOOM
                ));
            }
        }
    }

    private void addAllPolylines() {
        for (ArrayList polyline : pathPoints) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(POLYLINE_COLOR)
                    .width(POLYLINE_WIDTH)
                    .addAll(polyline);
            if (map != null) {
                map.addPolyline(polylineOptions);
            }
        }
    }
    private void addLatestPolyline() {
        if (!pathPoints.isEmpty()) {
            ArrayList<LatLng> polyline = pathPoints.get(pathPoints.size() - 1);
            if (polyline.size() > 1) {
                LatLng preLastLng = polyline.get(polyline.size() - 2);
                LatLng lastLng = polyline.get(polyline.size() - 1);
                PolylineOptions polylineOptions = new PolylineOptions()
                        .color(POLYLINE_COLOR)
                        .width(POLYLINE_WIDTH)
                        .add(preLastLng)
                        .add(lastLng);
                if (map != null) {
                    map.addPolyline(polylineOptions);
                }
            }
        }
    }

    private void sendCommandToService(String action) {
        Intent intent = new Intent(requireContext(), TrackingService.class);
        intent.setAction(action);
        requireContext().startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        addAllPolylines();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
    }


    @AfterPermissionGranted(REQUEST_CODE_LOCATION_PERMISSION)
    private void requestPermission() {
        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                    this,
                    "You need to accept location permissions to use MarshVelo",
                    REQUEST_CODE_LOCATION_PERMISSION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            );
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "You need to accept location permissions to use MarshVelo",
                    REQUEST_CODE_LOCATION_PERMISSION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            );
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        } else {
            requestPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions, grantResults, MainActivity.class);
    }
}
