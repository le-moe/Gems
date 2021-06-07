package com.example.fadi.networkinfoapi24.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.telecom.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.example.fadi.networkinfoapi24.R;
import com.example.fadi.networkinfoapi24.Utilities;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;

/**
 * Activity that performs a speed test.
 * Currently uses legacy code instead of {@link com.example.fadi.networkinfoapi24.tasks.SpeedTask}
 */
public class SpeedTest extends AppCompatActivity {
    String s1 = "";
    String s2 = "";
    //private String downloadUrl = "http://ipv4.download.thinkbroadband.com:8080/5MB.zip";
    //private String downloadUrl = "http://speedtest.tele2.net/100MB.zip";
    private String downloadUrl = "http://ovh.net/files/1Gio.dat";
    //private String uploadURL = "http://uk.testmy.net/SmarTest/up";
    private Runnable initialiseConnection;
    private Runnable startRecords;
    private Runnable endRecords;
    private Runnable initialiseUpload;
    private Handler handler;
    private Handler backgroundHandler;
    private int downloadWaitTime = 1000; // The waiting time after the download starts
    private int uploadWaitTime = 3000; // The time necessary to load the upload page
    private int recordInterval = 1000; // The interval between two consecutive records
    private long startTime;
    private long endTime;
    private long totalTxBeforeTest;
    private long totalRxBeforeTest;
    private long totalTxAfterTest;
    private long totalRxAfterTest;
    private int turnOnDelay = 6500; // The maximum time needed to connect to the Internet by WiFi or mobile data.
    private String source;
    private DecimalFormat percentageFormat = new DecimalFormat("00.00");
    private double converter = 0.001; // Byte to Kilobyte converter. Use 0.008 for Byte to Kilobit conversion
    private boolean scanIsRunning = false;
    private boolean isUpload = false; // Upload test is running or not
    private boolean isDownload = false; // Download test is running or not
    private boolean failure = false; // True when we don't receive upload/download bits for more than 10 seconds (-> stop the test)
    private boolean uploadPageLoadingEnded = false; // True when the upload web page stop uploading (-> reload the page to continue the test)
    private int counter = 0; // Tracks the number of measurements taken so far
    private int measurementsNumber = 15; // Total number of measurement
    private double downloadMax = 0;
    private double uploadMax = 0;
    private double downloadSum = 0;
    private double uploadSum = 0;
    private double failureCounter = 0;
    private Button scanBtn;
    private TextView tvDownload;
    private TextView tvUpload;
    private WifiManager wm;
    private ConnectivityManager cm;
    private WebView wv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_test);

        wv = findViewById(R.id.wv);
        wv.setVisibility(View.INVISIBLE);

        wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        cm = (ConnectivityManager) SpeedTest.this.getSystemService(Context.CONNECTIVITY_SERVICE);

        Intent intent = getIntent();
        source = intent.getStringExtra("Coming from");

        /* Initialise the parameters and call proper runnables. Also enable WiFi or mobile data
        depending on the activity that started this activity: if the user was in a WiFi activity
        it enables WiFi, if he was in GSM activity it enables mobile data.
         */
        scanBtn = findViewById(R.id.start_speed_test_btn);
        scanBtn.setOnClickListener(v -> {
            boolean addDelay = false;
            if (source != null && !source.equals("GSM")) {
                if (Utilities.isMobileDataEnabled()) {
                    Utilities.setDataEnabled(false);
                }
                if (!wm.isWifiEnabled()) {
                    wm.setWifiEnabled(true);
                    addDelay = true;
                    Snackbar.make(findViewById(R.id.constraintLayout), "Enabling WiFi", Snackbar.LENGTH_LONG).show();
                }
            } else {
                if (wm.isWifiEnabled()) {
                    wm.setWifiEnabled(false);
                }
                if (!Utilities.isMobileDataEnabled()) {
                    Snackbar.make(findViewById(R.id.constraintLayout), "Enabling Mobile Data", Snackbar.LENGTH_LONG).show();
                    Utilities.setDataEnabled(true);
                    addDelay = true;
                }

            }
            if (!scanIsRunning) {
                scanIsRunning = true;
                int delay = 0;
                if (addDelay)
                    delay = turnOnDelay;
                backgroundHandler.postDelayed(initialiseConnection, delay);

                Snackbar.make(findViewById(R.id.constraintLayout), "Starting a new speed test", Snackbar.LENGTH_LONG).show();
                tvUpload.setText("");
            } else
                Snackbar.make(findViewById(R.id.constraintLayout), "A speed test is already running, please wait", Snackbar.LENGTH_LONG).show();
        });

        HandlerThread mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        backgroundHandler = new Handler(mHandlerThread.getLooper());
        handler = new Handler();

        tvDownload = findViewById(R.id.tv_down_speed);
        tvUpload = findViewById(R.id.tv_up_speed);
        tvDownload.setText("");
        tvUpload.setText("");

        // Connect to the download URL and call the runnables that counts the bytes
        initialiseConnection = () -> {
            try {
                isUpload = false;
                isDownload = true;
                URL url = new URL(downloadUrl);
                URLConnection urlConnection = url.openConnection();
                InputStream is = urlConnection.getInputStream();
                handler.postDelayed(startRecords, downloadWaitTime);
                BufferedInputStream bis = new BufferedInputStream(is);

                //String path = "/storage/emulated/0/MonitoringReport2/100Mio.dat";
                //FileOutputStream fos = new FileOutputStream(path, true);

                long size = 0;
                byte[] buf = new byte[1024];
                int red = 0;
                while (((red = bis.read(buf)) != -1)) {
                    //fos.write(buf, 0, red);
                    size += red;
                    if (!isDownload) {
                        is.close();
                        break;
                    }
                }

/*                    fos.flush();
                fos.close();*/

            } catch (IOException e) {
                Log.i("Scan", "Error: " + e);
                Snackbar.make(findViewById(R.id.constraintLayout), "Couldn't connect to server, please check your internet connection", Snackbar.LENGTH_LONG).show();
                scanIsRunning = false;
            }
        };

        // Load the upload page and call the runnables that count the bytes
        initialiseUpload = () -> {
            wv.setWebViewClient(new WebViewClient());
            wv.loadUrl("http://uk.testmy.net/SmarTest/up");//wv.loadUrl("http://uk.testmy.net/SmarTest/up");
            wv.getSettings().setJavaScriptEnabled(true);
            wv.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            isUpload = true;
            handler.postDelayed(startRecords, uploadWaitTime);
        };

        // Note the current time and exchanged bytes
        startRecords = () -> {
            startTime = System.currentTimeMillis();
            totalTxBeforeTest = TrafficStats.getTotalTxBytes();
            totalRxBeforeTest = TrafficStats.getTotalRxBytes();
            handler.postDelayed(endRecords, recordInterval);
        };

        // Calculate the download/ upload speed = byte difference/time difference and convert to bytes
        endRecords = () -> {
            totalTxAfterTest = TrafficStats.getTotalTxBytes();
            totalRxAfterTest = TrafficStats.getTotalRxBytes();
            endTime = System.currentTimeMillis();

            double TimeDifference = endTime - startTime;

            double rxDifference = totalRxAfterTest - totalRxBeforeTest;
            double txDifference = totalTxAfterTest - totalTxBeforeTest;
            if ((rxDifference != 0) || (txDifference != 0)) {
                counter++;
                double rxKbps = (rxDifference * converter / (TimeDifference / 1000)); // total rx bytes per second.
                double txKbps = (txDifference * converter / (TimeDifference / 1000)); // total tx bytes per second.
                if (isDownload) {
                    if (rxKbps > downloadMax)
                        downloadMax = rxKbps;
                    s1 = "Download speed: " + percentageFormat.format(rxKbps) + " Kb/s." + "\nMaximum download speed = " + percentageFormat.format(downloadMax) + " Kb/s.";
                    downloadSum += rxKbps;
                    tvDownload.setText(s1);
                } else if (isUpload) {
                    if (txKbps > uploadMax)
                        uploadMax = txKbps;
                    s2 = "Upload speed: " + percentageFormat.format(txKbps) + " Kb/s." + "\nMaximum upload speed = " + percentageFormat.format(uploadMax) + " Kb/s.";
                    uploadSum += txKbps;
                    tvUpload.setText(s2);
                }

                //Log.i ("DownloadManager", "download: " + s1 + "  -------- upload:" + s2 + "Time difference: " + TimeDifference);
            } else if (isUpload) {
                failureCounter++;
                uploadPageLoadingEnded = true;
                if (failureCounter >= 10)
                    failure = true;
            } else if (isDownload) {
                failureCounter++;
                if (failureCounter >= 10)
                    failure = true;
            }


            if (counter > measurementsNumber || failure) {
                if (isDownload) {
                    double averageDownload = downloadSum / (counter * recordInterval / 1000);
                    s1 += "\nAverage download speed = " + percentageFormat.format(averageDownload) + " Kb/s.";
                    tvDownload.setText(s1);
                    isDownload = false;
                    downloadMax = 0;
                    downloadSum = 0;
                    failureCounter = 0;
                    failure = false;
                    handler.post(initialiseUpload);
                } else {
                    double averageUpload = uploadSum / (counter * recordInterval / 1000);
                    s2 += "\nAverage upload speed = " + percentageFormat.format(averageUpload) + "Kb/s.";
                    tvUpload.setText(s2);
                    isUpload = false;
                    uploadMax = 0;
                    uploadSum = 0;
                    failureCounter = 0;
                    failure = false;
                    scanIsRunning = false;
                }
                counter = 0;
            }
/*                else if ((counter+11) % 10 == 0 && isUpload)
                handler.post(initialiseUpload);*/
            else if (uploadPageLoadingEnded) {
                uploadPageLoadingEnded = false;
                handler.post(initialiseUpload);
            } else {
                handler.post(startRecords);
            }
        };

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (source.equals("GSM")) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.gsm_menu, menu);
        } else {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.wifi_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.homeBtn:
                Intent homeIntent = new Intent(this, MainActivity.class);
                startActivity(homeIntent);
                break;
            case R.id.speedTest_menu:
                break;
            case R.id.calltest_menu:
                Intent i1 = new Intent(getApplicationContext(), CallTest.class);
                startActivity(i1);
                break;
            case R.id.smsping_menu:
                Intent i2 = new Intent(getApplicationContext(), SmsTest.class);
                startActivity(i2);
                break;
            case R.id.cellInfo_menu:
                Intent i3 = new Intent(getApplicationContext(), VisualGsmScan.class);
                startActivity(i3);
                break;
            case R.id.networkoptions_menu:
                Intent i4 = new Intent(getApplicationContext(), NetworkOptions.class);
                startActivity(i4);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
