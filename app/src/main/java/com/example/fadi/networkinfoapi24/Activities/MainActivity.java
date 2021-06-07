package com.example.fadi.networkinfoapi24.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.example.fadi.networkinfoapi24.R;
import com.hypertrack.hyperlog.HyperLog;
import com.hypertrack.hyperlog.LogFormat;

/**
 * This is the entry-point of the app. Initialize stuff and ask for permissions.
 */
public class MainActivity extends AppCompatActivity {
    public static final String CHANNEL_ID = "CHANNEL_ID";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeHyperLog();
        createNotificationChannel();
        checkBatteryOptimizations();

        addButtonLink(findViewById(R.id.wifi), VisualWifiScan.class);
        addButtonLink(findViewById(R.id.gsm), VisualGsmScan.class);
        addButtonLink(findViewById(R.id.scenarioButton), ScenarioActivity.class);
        addButtonLink(findViewById(R.id.logButton), LogActivity.class);

        askForPermissions();
    }


    /**
     * Initialize HyperLog. HyperLog is a utility logger library for storing logs into database.
     * It allows to read logs in {@link LogActivity}.
     */
    private void initializeHyperLog() {
        // Initialize HyperLog
        int expiryTime = 7 * 24 * 60 * 60; // 7 days
        HyperLog.initialize(this, expiryTime);
        HyperLog.setLogLevel(Log.VERBOSE);
        HyperLog.setLogFormat(new LogFormat(this) {
            @Override
            public String getFormattedLogMessage(String logLevelName, String tag, String message, String timeStamp, String senderName, String osVersion, String deviceUUID) {
                return timeStamp + " | [" + logLevelName + "/" + tag + "]: " + message;
            }
        });
    }

    /**
     * Create a main notification channel only on API 26+
     */
    private void createNotificationChannel() {

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Main channel";
            String description = "";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void checkBatteryOptimizations() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean ignore = pm.isIgnoringBatteryOptimizations(getPackageName());
        if (!ignore) {
            new AlertDialog.Builder(this)
                    .setTitle("Error: the app is not in the doze whitelist")
                    .setMessage("The app must be in the doze whitelist to work properly.\n" +
                            "Please refer to the documentation to add this app to the whitelist.")
                    .show();
            HyperLog.e(TAG, "The app is not in the doze whitelist");
        }
    }

    /**
     * Set a button's onClickListener to start a new activity.
     *
     * @param button        The button that is going to be linked.
     * @param activityClass The class of the activity that will be launched.
     */
    private void addButtonLink(Button button, Class<? extends AppCompatActivity> activityClass) {
        button.setOnClickListener(view ->
                startActivity(new Intent(getApplicationContext(), activityClass))
        );
    }

    private void askForPermissions() {
        int ALL_PERMISSIONS = 101;

        // We need one permission for each group (Location, Phone, SMS and Storage)
        final String[] permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSIONS);
    }
}

