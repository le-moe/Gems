package com.example.fadi.networkinfoapi24.tasks;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.fadi.networkinfoapi24.NetworkTechnology;
import com.example.fadi.networkinfoapi24.reports.CallReport;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hypertrack.hyperlog.HyperLog;

import java.util.ArrayList;
import java.util.List;

/**
 * A task that performs a call test.
 * <p>
 * Basically, it tries to call the given phone number, wait for the phone to accept the phone and hang up.
 * There is a timeout of 10 seconds if nothing happens.
 */
public class CallTask extends AsynchronousTask {
    private static final String TAG = "CallTask";
    private static final int TIMEOUT = 10000; // 10 seconds max
    private static final List<PhoneStateListener> listeners = new ArrayList<>();
    public static final PhoneStateListener mainListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            for (PhoneStateListener listener : listeners) {
                listener.onCallStateChanged(state, incomingNumber);
            }
        }
    };
    private final String phoneNumber;

    @JsonCreator
    public CallTask(@JsonProperty("phoneNumber") String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "Call";
    }

    @Override
    public void run() {

        try {
            final TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);

            final Handler handler = new Handler();
            final Runnable timeoutCallBack = () -> {
                HyperLog.w(TAG, "Timeout");
                stop(new CallReport("", phoneNumber, TIMEOUT, true));
                tm.endCall();
            };
            handler.postDelayed(timeoutCallBack, TIMEOUT);


            assert tm != null;
            //tm.listen(mainListener, PhoneStateListener.LISTEN_CALL_STATE);

            // Check if the phone is set to LTE ONLY
            // Because calls does not work in LTE only
            if (getTechnology() == NetworkTechnology.LTE)
                HyperLog.e(TAG, "Trying a call task using LTE ONLY");

            final String operator = tm.getNetworkOperatorName();

            PhoneStateListener callListener = new PhoneStateListener() {
                private long startTime = 0;

                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    switch (state) {
                        case TelephonyManager.CALL_STATE_RINGING:
                            HyperLog.i(TAG, "RINGING, number: " + incomingNumber);
                            break;
                        case TelephonyManager.CALL_STATE_OFFHOOK:
                            startTime = System.currentTimeMillis();
                            HyperLog.i(TAG, "OFFHOOK");
                            break;
                        case TelephonyManager.CALL_STATE_IDLE:
                            HyperLog.i(TAG, "IDLE");
                            if (startTime != 0) {
                                tm.endCall();
                                int timeDifference = (int) (System.currentTimeMillis() - startTime);

                                // Remove timeout callback
                                handler.removeCallbacks(timeoutCallBack);
                                Log.i(TAG, "Time difference : " + timeDifference);

                                stop(new CallReport(operator, phoneNumber, timeDifference, false));
                            }
                            break;
                    }

                }
            };

            listeners.add(callListener);

            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            callIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);

            getContext().startActivity(callIntent);
        } catch (Exception e) {
            // Error
            HyperLog.e(TAG, e.getMessage());
            stop(new CallReport("", phoneNumber, -1, true));
        }

    }

}
