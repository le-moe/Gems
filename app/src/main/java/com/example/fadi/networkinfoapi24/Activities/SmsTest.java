package com.example.fadi.networkinfoapi24.Activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.telecom.Log;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.fadi.networkinfoapi24.R;

/**
 * Activity that performs a SMS ping.
 * Currently uses legacy code instead of {@link com.example.fadi.networkinfoapi24.tasks.SmsTask}
 */
public class SmsTest extends AppCompatActivity {

    private static final String SENT = "SMS_SENT";
    private static final String DELIVERED = "SMS_DELIVERED";

    private EditText smsEt;
    private EditText phoneNumberEt;
    private TextView sentResultTv;
    private TextView deliveredResultTv;

    private String phoneNumber = "0032491105267";

    /* sendPI and deliveredPI are the intents that need to be passed to sendTextMessage method in
    SmsManager in order to get notified when the SMS is sent and delivered
     */
    private PendingIntent sentPI;
    private PendingIntent deliveredPI;
    private BroadcastReceiver sentSmsListener;
    private BroadcastReceiver deliveredSmsListener;
    private Runnable timeout;
    private boolean waitingResponse = false;
    private int timeoutDuration = 30 * 1000;
    private Handler handler = new Handler();

    private SmsManager smsManager = SmsManager.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_test);

        smsEt = findViewById(R.id.sms_text_et);
        smsEt.setText("Test SMS message");

        phoneNumberEt = findViewById(R.id.phone_number_sms);
        phoneNumberEt.setText(phoneNumber);

        sentResultTv = findViewById(R.id.sent_sms_result_tv);
        sentResultTv.setText("");
        deliveredResultTv = findViewById(R.id.delivered_sms_reslut_tv);
        deliveredResultTv.setText("");

        // This broadcast receiver gets notified about the send status of an SMS
        sentSmsListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getAction();
                Log.i("SENT INTENT", "-------->" + s);
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        sentResultTv.setText("SMS sent");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        sentResultTv.setText("Error sending message: generic failure");
                        unregister();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        sentResultTv.setText("Error sending message: no service");
                        unregister();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        sentResultTv.setText("Error sending message: Null PDU");
                        unregister();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        sentResultTv.setText("Error sending message: radio is off");
                        unregister();
                        break;
                }
                try {
                    unregisterReceiver(this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        // This broadcast receiver gets notified about the delivery status of the SMS
        deliveredSmsListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getAction();
                SmsMessage message;
                byte[] pdu = intent.getByteArrayExtra("pdu");
                String format = intent.getStringExtra("format");
                message = SmsMessage.createFromPdu(pdu, format);
                int statusCode = message.getStatus();
                Log.i("DELIVERED INTENT", "-------->" + s);
                switch (statusCode) {
                    case Telephony.Sms.STATUS_COMPLETE:
                        deliveredResultTv.setText("SMS delivered");
                        unregister();
                        break;
                    case Telephony.Sms.STATUS_FAILED:
                        deliveredResultTv.setText("SMS not delivered");
                        unregister();
                        break;
                    case Telephony.Sms.STATUS_PENDING:
                        deliveredResultTv.setText("Delivery status pending");
                        unregister();
                        break;
                    case Telephony.Sms.STATUS_NONE:
                        deliveredResultTv.setText("NONE");
                        unregister();
                        break;
                    default:
                        deliveredResultTv.setText("");
                }
            }
        };

        // Send an SMS and register the necessary broadcast receivers to track its status
        Button sendSmsBtn = findViewById(R.id.send_sms_btn);
        sendSmsBtn.setOnClickListener(v -> {
            if (!waitingResponse) {
                String sentFilter = SENT + System.currentTimeMillis();
                String deliveredFilter = DELIVERED + System.currentTimeMillis();
                deliveredPI = PendingIntent.getBroadcast(SmsTest.this, (int) System.currentTimeMillis(), new Intent(deliveredFilter), PendingIntent.FLAG_UPDATE_CURRENT);
                sentPI = PendingIntent.getBroadcast(SmsTest.this, (int) System.currentTimeMillis(), new Intent(sentFilter), PendingIntent.FLAG_UPDATE_CURRENT);
                Log.i("SYSTEM TIME", "--------> " + (int) System.currentTimeMillis() + " LONG: " + System.currentTimeMillis());
                sentResultTv.setText("");
                deliveredResultTv.setText("");
                registerReceiver(sentSmsListener, new IntentFilter(sentFilter));
                registerReceiver(deliveredSmsListener, new IntentFilter(deliveredFilter));
                Log.i("REGISTERED SENT", "--------> " + sentFilter);
                Log.i("REGISTERED DELIVERED", "--------> " + deliveredFilter);
                phoneNumber = phoneNumberEt.getText().toString();
                waitingResponse = true;
                if (phoneNumber != null && !phoneNumber.equals("")) {
                    smsManager.sendTextMessage(phoneNumber, null, smsEt.getText().toString(), sentPI, deliveredPI);
                } else
                    Snackbar.make(findViewById(R.id.constraint_layout_smstest), "Please enter a phone number.", Snackbar.LENGTH_LONG).show();
                handler.postDelayed(timeout, timeoutDuration);
            } else {
                Snackbar.make(findViewById(R.id.constraint_layout_smstest), "Waiting the delivery status of another message. Please wait.", Snackbar.LENGTH_LONG).show();
            }
        });

        // Unregister the receivers and get ready to send new messages if the waiting time exceeds the max
        timeout = () -> {
            unregister();
            deliveredResultTv.setText("Waiting time has exceeded the maximum allowed (" + timeoutDuration / 1000 + " seconds).");
        };
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
                Intent i2 = new Intent(getApplicationContext(), CallTest.class);
                startActivity(i2);
                break;
            case R.id.smsping_menu:
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

/*    @Override
    public void onPause () {
        super.onPause();
        try {
            unregisterReceiver(sentSmsListener);
            unregisterReceiver(deliveredSmsListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    private void unregister() {
        handler.removeCallbacks(timeout);
        try {
            unregisterReceiver(sentSmsListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            unregisterReceiver(deliveredSmsListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
        waitingResponse = false;
    }
}
