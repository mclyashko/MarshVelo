package com.example.marshvelo.ui;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;

import com.example.marshvelo.R;
import com.example.marshvelo.ui.fragments.BluetoothFragment;
import com.example.marshvelo.ui.fragments.StatisticsFragment;
import com.example.marshvelo.ui.fragments.TrackingFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import static com.example.marshvelo.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private FragmentTransaction ft;

    private static Fragment currentFragment = null;
    private static Fragment trackingFragment = null;
    private static Fragment statisticsFragment = null;
    private static Fragment bluetoothFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.e("Main Activity created");

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        ft = getSupportFragmentManager().beginTransaction();
        trackingFragment = TrackingFragment.getInstance();
        currentFragment = trackingFragment;
        ft.replace(R.id.flFragment, currentFragment);
        ft.commit();

        try {
            getSupportActionBar().hide();
        } catch (NullPointerException e) { }


        navigateToTrackingFragmentIfNeed(getIntent());

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        navigation.setOnNavigationItemSelectedListener(OnNavigationItemSelectedListener);


        requestPerm();
    }

    protected void requestPerm(){
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 3);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.BLUETOOTH}, 4);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.FOREGROUND_SERVICE}, 5);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 6);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.BLUETOOTH_ADMIN}, 7);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 8);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.INTERNET}, 9);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_NOTIFICATION_POLICY}, 10 );
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.FOREGROUND_SERVICE}, 11);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        navigateToTrackingFragmentIfNeed(intent);
    }

    private void navigateToTrackingFragmentIfNeed(Intent intent) {
        if (intent.getAction() == ACTION_SHOW_TRACKING_FRAGMENT) {
            getSupportFragmentManager().beginTransaction().hide(currentFragment);
            currentFragment = trackingFragment;
            getSupportFragmentManager().beginTransaction().show(trackingFragment).commit();
        }
    }

    public static void setCurrentFragment(Fragment currentFrag) {
        currentFragment = currentFrag;
    }

    public static void setTrackingFragment(Fragment trackingFrag) {
        trackingFragment = trackingFrag;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener OnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener(){
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            switch (item.getItemId()) {
                case R.id.fragment_tracking:
                    fragmentManager.beginTransaction().hide(currentFragment).commit();
                    if (trackingFragment != null) {
                        Timber.e("MAIN ACTIVITY: open Tracking fragment");
                        fragmentManager.beginTransaction().show(trackingFragment).commit();
                    } else {
                        Timber.e("MAIN ACTIVITY: add new Tracking fragment");
                        trackingFragment = TrackingFragment.getInstance();
                        fragmentManager.beginTransaction().add(R.id.flFragment, trackingFragment).commit();
                    }
                    currentFragment = trackingFragment;
                    return true;
                case R.id.fragment_statistics:
                    fragmentManager.beginTransaction().hide(currentFragment).commit();
                    statisticsFragment = new StatisticsFragment();
                    Timber.e("MAIN ACTIVITY: create new statistics fragment");
                    fragmentManager.beginTransaction().add(R.id.flFragment, statisticsFragment).commit();
                    currentFragment = statisticsFragment;
                    return true;
                case R.id.fragment_bluetooth:
                    fragmentManager.beginTransaction().hide(currentFragment).commit();
                    bluetoothFragment = new BluetoothFragment();
                    Timber.e("MAIN ACTIVITY: create new bluetooth fragment");
                    fragmentManager.beginTransaction().add(R.id.flFragment, bluetoothFragment).commit();
                    currentFragment = bluetoothFragment;
                    return true;
            }
            return false;
        }
    };
}