package com.example.fadi.networkinfoapi24.reports;

import android.location.Location;

import com.example.fadi.networkinfoapi24.tasks.TaskType;

import java.util.Date;

/**
 * A report that saves the current geographic location.
 */
public class LocationReport extends Report {

    public final double latitude;
    public final double longitude;
    public final double altitude;

    /**
     * The estimated horizontal accuracy of this location, radial, in meters.
     * @see Location#getAccuracy()
     */
    public final float accuracy;

    public LocationReport(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        altitude = location.getAltitude();
        accuracy = location.getAccuracy();
    }

    @Override
    public FileNameFormat fileNameFormat() {
        return new FileNameFormat(TaskType.LOCATION, new Date());
    }
}
