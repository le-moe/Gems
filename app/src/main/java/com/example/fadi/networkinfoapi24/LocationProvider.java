package com.example.fadi.networkinfoapi24;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

/**
 * Provides location from Google APIs
 */
public class LocationProvider {

    private final FusedLocationProviderClient fusedLocationClient;

    public LocationProvider(Context context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public Task<Location> getLastLocation() {
        return fusedLocationClient.getLastLocation();
    }
}
