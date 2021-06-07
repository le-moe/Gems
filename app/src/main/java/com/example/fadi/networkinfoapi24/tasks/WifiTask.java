package com.example.fadi.networkinfoapi24.tasks;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.example.fadi.networkinfoapi24.Duration;
import com.example.fadi.networkinfoapi24.measurements.WifiMeasurement;
import com.example.fadi.networkinfoapi24.reports.Report;
import com.example.fadi.networkinfoapi24.reports.WifiReport;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A task that measures Wifi data.
 */
public class WifiTask extends PeriodicTask {
    private static final String TAG = "WifiTask";
    private WifiReport report;
    private WifiManager wifiManager;

    @JsonCreator
    public WifiTask(@JsonProperty("duration") Duration duration,
                    @JsonProperty("period") Duration period) {
        super(duration, period);
    }

    @Override
    public void onStart() {
        report = new WifiReport();

        // Initialize WifiManager here to avoid doing it during scenario parsing tests.
        wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.setWifiEnabled(true)) {
            Log.e(TAG, "Cannot enable wifi");
        }

        Log.i(TAG, "Task launched");
    }

    @Override
    public void onNewPeriod() {
        List<ScanResult> results = wifiManager.getScanResults();
        for (ScanResult result : results) {
            WifiMeasurement measurement = WifiMeasurement.fromScanResult(result);
            report.add(measurement);
            Log.i(TAG, measurement.toString());
        }
        wifiManager.startScan();
    }

    @Override
    public Report onStop() {
        Log.i(TAG, "Task finished");
        Log.i(TAG, "Saving into a new file");
        if (!wifiManager.setWifiEnabled(false)) {
            Log.e(TAG, "Cannot disable wifi");
        }
        return report;
    }

    @Override
    public String toString() {
        return "Wifi \n" + super.toString();
    }
}
