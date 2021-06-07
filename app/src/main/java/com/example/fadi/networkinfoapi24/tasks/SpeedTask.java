package com.example.fadi.networkinfoapi24.tasks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.example.fadi.networkinfoapi24.Duration;
import com.example.fadi.networkinfoapi24.measurements.SpeedMeasurement;
import com.example.fadi.networkinfoapi24.reports.SpeedReport;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hypertrack.hyperlog.HyperLog;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

/**
 * Task that performs a speed test.
 */
public class SpeedTask extends AsynchronousTask {
    public static final int PAUSE_DURATION = 5000;
    public static final String DOWNLOAD_FILE_100M = "http://ipv4.ikoula.testdebit.info/100M.iso";
    public static final String DOWNLOAD_FILE_1G = "http://ipv4.ikoula.testdebit.info/1G.iso";
    public static final String DOWNLOAD_FILE = DOWNLOAD_FILE_1G;
    public static final String UPLOAD_URL = "http://ipv4.ikoula.testdebit.info/";
    public static final int UPLOAD_FILE_SIZE = 1000000000;
    private static final String TAG = "SpeedTask";
    private boolean wifi;
    private Mode mode;
    private Duration duration;
    private Duration period;


    /**
     * Creates a new task that performs a speed test.
     *
     * @param mode     Either DOWNLOAD or UPLOAD.
     * @param duration The duration of the speed test.
     * @param period   The time interval between two measurements.
     */
    @JsonCreator
    public SpeedTask(@JsonProperty("mode") Mode mode,
                     @JsonProperty("duration") Duration duration,
                     @JsonProperty("period") Duration period,
                     @JsonProperty("wifi") boolean wifi) {
        this.mode = mode;
        this.duration = duration;
        this.period = period;
        this.wifi = wifi;
    }

    @Override
    public void run() {

        WifiManager wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(wifi);

        if (wifi) {
            try {
                Thread.sleep(PAUSE_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!isOnline()) {
                HyperLog.e(TAG, "Wifi does not provide internet");
            }
        }

        SpeedReport speedReport = wifi ? new SpeedReport(mode) : new SpeedReport(mode, getTechnology(), getOperator());

        SpeedTestSocket speedTestSocket = new SpeedTestSocket();
        speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {
            @Override
            public void onCompletion(SpeedTestReport report) {
                if (shouldDisableWifi())
                    wifiManager.setWifiEnabled(false);
                stop(speedReport);
            }

            @Override
            public void onProgress(float percent, SpeedTestReport report) {
                speedReport.add(new SpeedMeasurement(report.getTransferRateBit()));
            }

            @Override
            public void onError(SpeedTestError speedTestError, String errorMessage) {
                HyperLog.e(TAG, speedTestError.name() + " : " + errorMessage);
                if (shouldDisableWifi())
                    wifiManager.setWifiEnabled(false);
                stop(null);
            }
        });

        switch (mode) {
            case DOWNLOAD:
                speedTestSocket.startFixedDownload(DOWNLOAD_FILE, duration.toMilliseconds(), period.toMilliseconds());
                break;
            case UPLOAD:
                speedTestSocket.startFixedUpload(UPLOAD_URL, UPLOAD_FILE_SIZE, duration.toMilliseconds(), period.toMilliseconds());
                break;
        }

        if (wifi) {
            WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            speedReport.setBSSID(connectionInfo.getBSSID());
            speedReport.setSSID(connectionInfo.getSSID());
        }

    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public Mode getMode() {
        return mode;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Duration getPeriod() {
        return period;
    }

    public boolean isWifi() {
        return wifi;
    }

    public void setWifi(boolean wifi) {
        this.wifi = wifi;
    }

    public boolean shouldDisableWifi() {
        return isWifi();
    }

    @Override
    public String toString() {
        String tech = (wifi ? "wifi" : "cellular");
        return "SpeedTest: " + mode.toString() + " " + tech + "\n" +
                "Duration: " + duration + "\n" +
                "Period: " + period;
    }

    public enum Mode {
        DOWNLOAD,
        UPLOAD
    }
}
