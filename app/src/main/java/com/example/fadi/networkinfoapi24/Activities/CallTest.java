package com.example.fadi.networkinfoapi24.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.fadi.networkinfoapi24.R;

/**
 * Activity that performs a call test.
 * Currently uses legacy code instead of {@link com.example.fadi.networkinfoapi24.tasks.CallTask}
 */
public class CallTest extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 9;
    private EditText numberEt;
    private Runnable returnToActivity;
    private Runnable endCall;
    private Handler handler;
    private TextView result;
    private TelephonyManager tm;
    private long startTime;
    private long endTime;
    private boolean wasCalling = false;
    private int timeOut = 30 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_test);

        EndCallListener callListener = new EndCallListener();
        tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);

        numberEt = findViewById(R.id.number_et);
        String number = "0032491105267";
        numberEt.setText(number);

        result = findViewById(R.id.result);
        result.setText("");

        handler = new Handler();

        // Return to current activity after calling the number
        returnToActivity = () -> {
            Intent intent = new Intent(CallTest.this, CallTest.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        };

        endCall = () -> {
            if (wasCalling) {
                tm.endCall();
                endTime = System.currentTimeMillis();
                String toReport = "Call was timed out: duration has exceeded the maximum allowed call duration (" + String.valueOf(timeOut / 1000) + ").";
                result.setText(toReport);
                wasCalling = false;
            }
        };

        // Call the desired phone number. CALL_PHONE permission must be checked before every attempt to call
        Button startCallBtn = findViewById(R.id.start_call_btn);
        startCallBtn.setOnClickListener(v -> {
            if (!wasCalling) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                String phoneNumber = numberEt.getText().toString();
                if (!phoneNumber.equals("")) {
                    callIntent.setData(Uri.parse("tel:" + phoneNumber));
                    if (ActivityCompat.checkSelfPermission(CallTest.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(CallTest.this,
                                new String[]{Manifest.permission.CALL_PHONE},
                                MY_PERMISSIONS_REQUEST_CALL_PHONE);
                        return;
                    }
                    handler.postDelayed(returnToActivity, 1500);
                    handler.postDelayed(endCall, timeOut);
                    startActivity(callIntent);
                } else
                    Snackbar.make(findViewById(R.id.constraint_layout_calltest), "Please enter a phone number.", Snackbar.LENGTH_LONG).show();
            } else
                Snackbar.make(findViewById(R.id.constraint_layout_calltest), "A call is already in progress, please wait.", Snackbar.LENGTH_LONG).show();
        });
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
            case R.id.speedTest_menu:
                Intent i1 = new Intent(getApplicationContext(), SpeedTest.class);
                i1.putExtra("Coming from", "GSM");
                startActivity(i1);
                break;
            case R.id.calltest_menu:
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

    /**
     * Call listener that gets notified when the call state changes. The result of the call test
     * depends on the call duration:
     * Too short: the test has failed
     * Short enough: the receiver has probably hanged off automatically and the test has succeeded
     * Other than that: the test has failed
     */
    public class EndCallListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (TelephonyManager.CALL_STATE_RINGING == state) {
                Log.i("Calling", "RINGING, number: " + incomingNumber);
            }
            if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
                //wait for phone to go offhook (probably set a boolean flag) so you know your app initiated the call.
                startTime = System.currentTimeMillis();
                wasCalling = true;
                Log.i("Calling", "OFFHOOK");
            }
            if (TelephonyManager.CALL_STATE_IDLE == state) {
                //when this state occurs, and your flag is set, restart your app
                if (wasCalling) {
                    endTime = System.currentTimeMillis();
                    double diff = (endTime - startTime) / 1000;
                    String toReport;
                    if (diff < 1)
                        toReport = "Call duration: " + String.valueOf(diff) + "\nTest failed, duration is too short, probably a connexion failure.";
                    else if (diff < 10)
                        toReport = "Call duration: " + String.valueOf(diff) + "\nTest succeeded.";
                    else
                        toReport = "Call duration: " + String.valueOf(diff) + "\nTest failed.";
                    result.setText(toReport);
                    wasCalling = false;
                }
                Log.i("Calling", "IDLE");
            }
        }
    }

}


