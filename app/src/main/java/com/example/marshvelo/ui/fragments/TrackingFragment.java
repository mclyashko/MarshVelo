package com.example.marshvelo.ui.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import com.example.marshvelo.R;
import com.example.marshvelo.database.Ride;
import com.example.marshvelo.other.TrackingUtility;
import com.example.marshvelo.services.TrackingService;
import com.example.marshvelo.ui.MainActivity;
import com.example.marshvelo.ui.viewmodels.MainViewModel;
import timber.log.Timber;

import static com.example.marshvelo.other.Constants.ACTION_PAUSE_SERVICE;
import static com.example.marshvelo.other.Constants.ACTION_START_OR_RESUME_SERVICE;
import static com.example.marshvelo.other.Constants.ACTION_STOP_SERVICE;
import static com.example.marshvelo.other.Constants.MAP_ZOOM;
import static com.example.marshvelo.other.Constants.POLYLINE_COLOR;
import static com.example.marshvelo.other.Constants.POLYLINE_WIDTH;
import static com.example.marshvelo.other.Constants.REQUEST_CODE_LOCATION_PERMISSION;

@AndroidEntryPoint
public class TrackingFragment extends Fragment implements OnMapReadyCallback {

    private static TrackingFragment trackingFragment = null;

    private boolean serviceKilled = true;
    private MainViewModel viewModel;
    private GoogleMap map = null;
    private MapView mapView;
    private Button btnToggleRide;
    private Button btnFinishRun;
    private TextView tvTimer;

    private String rideName = null;
    private boolean rideNameIsEmpty = true;

    private long currentTimeInMillis= 0;
    private static boolean isTracking = false;
    private static ArrayList<ArrayList<LatLng>> pathPoints = new ArrayList<>();

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.e("On create");
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = getView().findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        Timber.e("View created");
        btnToggleRide = getView().findViewById(R.id.btnToggleRun);
        btnFinishRun = getView().findViewById(R.id.btnFinishRun);
        tvTimer = getView().findViewById(R.id.tvTimer);

        mapView.getMapAsync(this);
        subscribeToObservers();

        // Create an instance of ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        btnToggleRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRide();
            }
        });

        btnFinishRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pathPoints == null || pathPoints.isEmpty()) {
                    return;
                } else if (pathPoints.get(pathPoints.size() - 1).isEmpty()){
                    return;
                }
                setNameForRide();

            }
        });
    }

    private void saveRideAndCreateFragment() {
        btnToggleRide.setText("Start");
        // menu.getItem(0).setVisible(true);
        btnFinishRun.setVisibility(getView().GONE);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        trackingFragment = new TrackingFragment();
        MainActivity.setTrackingFragment(trackingFragment);
        MainActivity.setCurrentFragment(trackingFragment);
        ft.replace(R.id.flFragment, trackingFragment).addToBackStack(null).commit();
    }

    private void setNameForRide() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        alert.setTitle("Name your ride");

        // Set an EditText view to get user input
        EditText input = new EditText(getContext());
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (input.getText() != null && !input.getText().toString().isEmpty()) {
                    zoomToSeeWholeTrack();
                    endAndSaveToDatabase(input.getText().toString());
                    saveRideAndCreateFragment();
                    Timber.d("TRACKING_FRAGMENT: SAVING YOUR ROUTE # 1: " + input.getText().toString());
                } else {
                    Snackbar.make(
                            requireActivity().findViewById(R.id.rootView),
                            "Enter correct name",
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        alert.show();
    }

    private void stopRide() {
        sendCommandToService(ACTION_STOP_SERVICE);
    }

    private void subscribeToObservers() {
        TrackingService.serviceKilled.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                serviceKilled = aBoolean;
            }
        });
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
        TrackingService.timeRideInMillis.observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                currentTimeInMillis = aLong;
                tvTimer.setText(TrackingUtility.getFormattedStopWath(aLong, true));
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
        if (!isTracking && !serviceKilled) {
            Timber.e("Here is a problem");
            btnToggleRide.setText("Resume");
            btnFinishRun.setVisibility(getView().VISIBLE);
            btnFinishRun.setText("Finish");
        } else if (!serviceKilled) {
            btnToggleRide.setText("Stop");
            // menu.getItem(0).setVisible(true);
            btnFinishRun.setVisibility(getView().GONE);
        } else if (serviceKilled) {
            btnToggleRide.setText("Start");
            // menu.getItem(0).setVisible(true);
            btnFinishRun.setVisibility(getView().GONE);
        }
    }

    private void zoomToSeeWholeTrack() {
        LatLngBounds.Builder bounds = LatLngBounds.builder();
        for (ArrayList<LatLng> polyline: pathPoints) {
            for (LatLng pos: polyline) {
                bounds.include(pos);
            }
        }

        if (map != null) {
            map.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(
                            bounds.build(),
                            mapView.getWidth(),
                            mapView.getHeight(),
                            (int) (mapView.getHeight() * 0.05f)
                    )
            );
        }
    }

    private void endAndSaveToDatabase(String name) {
        map.snapshot(bmp -> {
            int distanceInMeters = 0;
            for (ArrayList<LatLng> polyline: pathPoints) {
                distanceInMeters += (int) TrackingUtility.calculatePolylineLength(polyline);
            }
            long dateTimeStamp = Calendar.getInstance().getTimeInMillis();
            Ride ride = new Ride(name, bmp, dateTimeStamp, distanceInMeters, currentTimeInMillis);
            Timber.e("TRACKING_FRAGMENT: YOUR RIDE SUCCESSFULLY SAVED");
            viewModel.insertRide(ride);
            Snackbar.make(
                    requireActivity().findViewById(R.id.rootView),
                    "Ride saved successfully",
                    Snackbar.LENGTH_SHORT).show();
        });
        stopRide();
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
}