package com.example.fadi.networkinfoapi24.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.fadi.networkinfoapi24.R;
import com.example.fadi.networkinfoapi24.measurements.WifiMeasurement;

import java.util.List;


/**
 * Activity that displays Wifi scans.
 */
public class VisualWifiScan extends AppCompatActivity {

    private WifiManager wifi;
    private ArrayAdapter<String> adapter;
    private List<ScanResult> wifiScanResult;
    private BroadcastReceiver scanResultReceiver;
    private IntentFilter scanResultIntentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_wifi_scan);

        // -------- Getting an instance of WifiManager --------
        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        final ListView listView = findViewById(R.id.wifi_data);

        // ------- Set the list view to show scan result --------
        listView.setFastScrollEnabled(true);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);


        /* The receiver gets notified when startScan method finishes scanning.
           It shows the scan results and saves them into an csv file.
         */
        scanResultReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                wifiScanResult = wifi.getScanResults();
                adapter.clear();

                for (ScanResult result : wifiScanResult) {
                    WifiMeasurement measurement = WifiMeasurement.fromScanResult(result);
                    adapter.add(measurement.toString());
                }

                wifi.startScan();
            }
        };

        registerReceiver(scanResultReceiver, scanResultIntentFilter);
        wifi.startScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(scanResultReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(scanResultReceiver, scanResultIntentFilter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.wifi_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.homeBtn:
                Intent homeIntent = new Intent(this, MainActivity.class);
                startActivity(homeIntent);
                break;
            case R.id.speedtest_wifi_menu:
                Intent intent = new Intent(getApplicationContext(), SpeedTest.class);
                intent.putExtra("Coming from", "WiFi");
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }

}


