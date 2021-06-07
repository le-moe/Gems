package com.example.fadi.networkinfoapi24.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.fadi.networkinfoapi24.NetworkOperator;
import com.example.fadi.networkinfoapi24.R;
import com.example.fadi.networkinfoapi24.measurements.CellMeasurement;


/**
 * Activity that displays scans from the cellular network.
 */
public class VisualGsmScan extends AppCompatActivity {


    private ArrayAdapter<String> adapter;
    private Handler handler;
    private TelephonyManager tm;
    private Runnable scanPeriodically;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_gsm_scan);

        HandlerThread mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        handler = new Handler();

        // -------- Getting an instance of TelephonyManager --------
        tm = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);

        final ListView listView = findViewById(R.id.gsm_data);

        // ------- Set the list view to show scan result --------
        listView.setFastScrollEnabled(true);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);


        // -------- Runnable that updates cells info and call proper functions to save them ---------
        scanPeriodically = new Runnable() {
            @Override
            public void run() {
                adapter.clear();
                for (CellInfo cellInfo : tm.getAllCellInfo()) {
                    CellMeasurement measurement = CellMeasurement.fromCellInfo(cellInfo, NetworkOperator.BASE);
                    adapter.add(measurement.toString());
                }

                handler.postDelayed(this, 1000); // The runnable calls himself again after 1 second
            }
        };


        handler.post(scanPeriodically); // Call the scan Runnable and start the scan

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handler.removeCallbacks(scanPeriodically);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!handler.hasCallbacks(scanPeriodically))
            handler.post(scanPeriodically);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler.hasCallbacks(scanPeriodically))
            handler.removeCallbacks(scanPeriodically);
    }

    // --------- Handles the menu options and activity switching --------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gsm_menu, menu);

        ActionBar actionBar = getSupportActionBar();
        actionBar.show();

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
                Intent i1 = new Intent(getApplicationContext(), SpeedTest.class);
                i1.putExtra("Coming from", "GSM");
                startActivity(i1);
                break;
            case R.id.calltest_menu:
                Intent i2 = new Intent(getApplicationContext(), CallTest.class);
                startActivity(i2);
                break;
            case R.id.smsping_menu:
                Intent i3 = new Intent(getApplicationContext(), SmsTest.class);
                startActivity(i3);
                break;
            case R.id.cellInfo_menu:
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
