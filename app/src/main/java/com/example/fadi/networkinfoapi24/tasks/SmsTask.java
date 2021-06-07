package com.example.fadi.networkinfoapi24.tasks;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.fadi.networkinfoapi24.reports.SmsReport;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hypertrack.hyperlog.HyperLog;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;

/**
 * A task that performs a SMS test.
 * <p>
 * Basically send a text, wait for the send confirmation and the delivery confirmation.
 */
public class SmsTask extends AsynchronousTask {
    private static final String TAG = "SmsTask";
    private static final String SENT = "SMS_SENT";
    private static final String DELIVERED = "SMS_DELIVERED";

    private final String phoneNumber;

    @JsonCreator
    public SmsTask(@JsonProperty("phoneNumber") String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    @Override
    public void run() {
        try {
            SmsManager smsManager = SmsManager.getDefault();

            final TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
            final String operator = tm.getNetworkOperatorName();
            final SmsReport report = new SmsReport(operator, phoneNumber);


            // Flags ?
            PendingIntent sentIntent = PendingIntent.getBroadcast(getContext(), 0, new Intent(SENT), 0);
            PendingIntent deliveryIntent = PendingIntent.getBroadcast(getContext(), 0, new Intent(DELIVERED), 0);

            final long startTime = System.currentTimeMillis();

            final BroadcastReceiver deliveryBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    byte[] pdu = intent.getByteArrayExtra("pdu");
                    String format = intent.getStringExtra("format");
                    SmsMessage message = SmsMessage.createFromPdu(pdu, format);
                    int statusCode = message.getStatus();
                    report.setStatusCode(statusCode);
                    switch (statusCode) {
                        case Telephony.Sms.STATUS_COMPLETE:
                            HyperLog.i(TAG, "SMS delivered");
                            break;
                        case Telephony.Sms.STATUS_FAILED:
                            HyperLog.e(TAG, "SMS not delivered");
                            break;
                        case Telephony.Sms.STATUS_PENDING:
                            HyperLog.i(TAG, "Delivery status pending");
                            break;
                        case Telephony.Sms.STATUS_NONE:
                            HyperLog.i(TAG, "NONE");
                            break;
                    }
                    int duration = (int) (System.currentTimeMillis() - startTime);
                    report.setDeliveryDuration(duration);
                    Log.i(TAG, String.format("Duration: %d ms", duration));

                    context.unregisterReceiver(this);
                    stop(report);


                }
            };

            final BroadcastReceiver sentBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int resultCode = getResultCode();
                    report.setResultCode(resultCode);

                    if (resultCode == Activity.RESULT_OK) {
                        HyperLog.i(TAG, "SMS sent");
                        report.setSendDuration((int) (System.currentTimeMillis() - startTime));
                    } else {
                        context.unregisterReceiver(deliveryBroadcastReceiver);

                        switch (resultCode) {
                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                HyperLog.e(TAG, "Error sending message: generic failure");
                                break;
                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                HyperLog.e(TAG, "Error sending message: no service");
                                break;
                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                HyperLog.e(TAG, "Error sending message: Null PDU");
                                break;
                            case SmsManager.RESULT_ERROR_RADIO_OFF:
                                HyperLog.e(TAG, "Error sending message: radio is off");
                                break;
                        }

                        stop(report);
                    }
                    context.unregisterReceiver(this);
                }
            };

            getContext().registerReceiver(sentBroadcastReceiver, new IntentFilter(SENT));
            getContext().registerReceiver(deliveryBroadcastReceiver, new IntentFilter(DELIVERED));

            String text = DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.format(new Date()) + " ";
            if (getOperator() != null && getTechnology() != null) {
                text += getOperator().toString() + " " +
                        getTechnology().toString();
            } else {
                text += operator;
            }

            smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    text,
                    sentIntent,
                    deliveryIntent
            );
        } catch (Exception e) {
            HyperLog.e(TAG, e.getMessage());
            stop(new SmsReport("", phoneNumber));
        }
    }

    @Override
    public String toString() {
        return "SMS";
    }
}
