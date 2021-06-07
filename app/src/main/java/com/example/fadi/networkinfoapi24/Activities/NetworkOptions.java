package com.example.fadi.networkinfoapi24.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.internal.telephony.CellNetworkScanResult;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.OperatorInfo;
import com.example.fadi.networkinfoapi24.R;
import com.example.fadi.networkinfoapi24.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that can change operator and cellular technology.
 */
public class NetworkOptions extends AppCompatActivity {

    private static final int MAX_RETRY_COUNT = 10;
    private static final int NETWORK_CHANGE_WAITING_TIME = 5000;

    private TextView resultNetworkTv;
    private TextView resultTechnologyTv;
    private TextView networkTypeTv;
    private TextView technologyTypeTv;


    private String desiredNetwork;
    private String technology;
    private boolean orange;
    private boolean isRunning = false;
    private int networkChangeRetryCounter = 0;
    private boolean modemStatus = false;


    private TelephonyManager tm;
    private int subID = SubscriptionManager.getDefaultSubscriptionId();

    private Runnable changeNetwork;
    private Handler backgroundHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_options);

        HandlerThread mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        backgroundHandler = new Handler(mHandlerThread.getLooper());

        tm = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);

        Spinner networkSpinner = findViewById(R.id.network);
        Spinner technologySpinner = findViewById(R.id.technology);
        resultNetworkTv = findViewById(R.id.result_network);
        resultTechnologyTv = findViewById(R.id.result_technology);
        technologyTypeTv = findViewById(R.id.technology_type);
        networkTypeTv = findViewById(R.id.network_type);
        Button applyBtn = findViewById(R.id.apply);

        resultNetworkTv.setText("");
        resultTechnologyTv.setText("");
        networkTypeTv.setText(tm.getNetworkOperatorName(subID));
        technologyTypeTv.setText(Utilities.intToString(tm.getPreferredNetworkType(subID)));

        applyBtn.setOnClickListener(v -> {
            if (!isRunning)
                backgroundHandler.post(changeNetwork);
            else
                Snackbar.make(findViewById(R.id.constraint_layout_network_options),
                        "Please wait, a network change is already in progress",
                        Snackbar.LENGTH_LONG).show();

        });

        // Set up the network operator spinner
        ArrayList<String> networks_array = new ArrayList<>();
        for (String s : Utilities.NETWORKS_LIST) {
            if (!s.equals(Utilities.NULL)) {
                networks_array.add(s);
            }
        }
        ArrayAdapter<String> networkAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, networks_array);
        networkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        networkSpinner.setAdapter(networkAdapter);
        networkSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                desiredNetwork = (String) parent.getItemAtPosition(position);
                orange = desiredNetwork.equals(Utilities.ORANGE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Set up the network technology spinner
        String[] technologyString = {Utilities.GSM_ONLY, Utilities.WCDMA_ONLY, Utilities.LTE_ONLY, Utilities.LTE_GSM_WCDMA};
        ArrayAdapter<String> technologyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, technologyString);
        technologyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        technologySpinner.setAdapter(technologyAdapter);
        technologySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                technology = item;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /* Change network operator:
         1) Verify if the current operator is the same as the desired one
         2) If not, change the operator using setNetworkSelectionModeManual in ITelephony. Some of
         the code is a direct copy from setNetworkSelectionModeManual in TelephonyManager, due to
         a problem using this method directly.
        */
        changeNetwork = new Runnable() {
            @Override
            public void run() {
                if (!modemStatus) {  // true if busy, false if not
                    modemStatus = true;
                    boolean success = false;
                    boolean desiredNetworkDetected = false;
                    boolean mobistarCase = false;
                    String currentNetwork = tm.getNetworkOperatorName(subID);
                    isRunning = true;
                    if (orange)
                        mobistarCase = currentNetwork.toLowerCase().contains("mobistar");
                    boolean isDuplicate = true;
                    if (!mobistarCase) {
                        if (!(currentNetwork.toLowerCase().contains(desiredNetwork.toLowerCase())))
                            isDuplicate = false;
                    }
                    if (!isDuplicate) {
                        setText(resultNetworkTv, "Changing network to " + desiredNetwork);
                        CellNetworkScanResult networkCells = tm.getCellNetworkScanResults(subID);
                        int index = 0;
                        List<OperatorInfo> operatorList = networkCells.getOperators();
                        if (operatorList != null) {
                            for (OperatorInfo network : operatorList) {
                                //String tempNetwork = network.getOperatorAlphaLong().toLowerCase();
                                Log.i("ROUTINES", "-0-------->" + network.getOperatorAlphaLong().toLowerCase());
                                if ((network.getOperatorAlphaLong().toLowerCase()).contains(desiredNetwork.toLowerCase()))/*|| (orange&&tempNetwork.equals("Mobistar"))*/ {
                                    OperatorInfo oi = operatorList.get(index);
                                    desiredNetworkDetected = true;
                                    Log.i("ROUTINES", "-------DETECTED--------->" + network.getOperatorAlphaLong().toLowerCase());
                                    try {
                                        ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
                                        Log.i("Scan", "ITelephony created");
                                        if (telephony != null) {
                                            success = telephony.setNetworkSelectionModeManual(4, oi, true);
                                            if (!success) {
                                                setText(resultNetworkTv, "Failed to connect to " + desiredNetwork);
                                                setText(networkTypeTv, tm.getNetworkOperatorName(subID));
                                            }
                                        }
                                    } catch (RemoteException ex) {
                                        Log.e("Scan", "setNetworkSelectionModeManual RemoteException");
                                        setText(networkTypeTv, tm.getNetworkOperatorName(subID));
                                        desiredNetworkDetected = true;
                                        setText(resultNetworkTv, "Failed to connect to " + desiredNetwork);
                                    } catch (NullPointerException ex) {
                                        Log.e("Scan", "setNetworkSelectionModeManual NullPointerExeception");
                                        setText(networkTypeTv, tm.getNetworkOperatorName(subID));
                                        desiredNetworkDetected = true;
                                        setText(resultNetworkTv, "Failed to connect to " + desiredNetwork);
                                    }
                                    break;
                                }
                                index++;
                            }
                        }
                    } else {
                        setText(resultNetworkTv, "Already connected to " + desiredNetwork);
                        desiredNetworkDetected = true;
                    }
                    if (!desiredNetworkDetected) {
                        setText(resultNetworkTv, "Desired network not found");
                    } else if (success) {
                        setText(resultNetworkTv, "Network changed to " + desiredNetwork);
                        setText(networkTypeTv, desiredNetwork);
                    }

                    int technologyInteger = Utilities.stringToInt(technology);
                    if (tm.getPreferredNetworkType(subID) != technologyInteger) {
                        tm.setPreferredNetworkType(subID, technologyInteger);
                        if (tm.getPreferredNetworkType(subID) == technologyInteger) {
                            setText(technologyTypeTv, technology);
                            setText(resultTechnologyTv, "Technology set to " + technology);
                        } else
                            setText(resultTechnologyTv, "Failed to change technology");
                    } else
                        setText(resultTechnologyTv, "Technology already set to " + technology);

                    isRunning = false;
                    modemStatus = false;
                } else {
                    if (networkChangeRetryCounter < MAX_RETRY_COUNT) {
                        networkChangeRetryCounter++;
                        setText(resultNetworkTv, "The modem is already searching for a network. Retrying in " + String.valueOf(NETWORK_CHANGE_WAITING_TIME / 1000) + " seconds.\n");
                        backgroundHandler.postDelayed(this, NETWORK_CHANGE_WAITING_TIME);
                    } else {
                        setText(resultNetworkTv, "The modem is already searching for a network. Exceeded the maximum retry count (" + String.valueOf(MAX_RETRY_COUNT) + ")\n");
                    }
                }
            }
        };

    }

    private void setText(final TextView tv, final String s) {
        runOnUiThread(() -> tv.setText(s));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gsm_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.homeBtn:
                Intent homeIntent = new Intent(this, MainActivity.class);
                startActivity(homeIntent);
                break;
            case R.id.networkoptions_menu:
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
            case R.id.speedTest_menu:
                Intent i4 = new Intent(getApplicationContext(), SpeedTest.class);
                i4.putExtra("Coming from", "GSM");
                startActivity(i4);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}

