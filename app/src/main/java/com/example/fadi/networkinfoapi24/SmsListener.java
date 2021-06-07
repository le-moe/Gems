package com.example.fadi.networkinfoapi24;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import com.example.fadi.networkinfoapi24.tasks.IndividualSpeedTask;
import com.example.fadi.networkinfoapi24.tasks.StressTask;
import com.hypertrack.hyperlog.HyperLog;

import java.io.IOException;
import java.security.InvalidParameterException;

/**
 * Class that listens to Sms arrival. Used to start a scenario.
 */
public class SmsListener extends BroadcastReceiver {
    private static final String TAG = "SmsListener";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                String messageBody = smsMessage.getMessageBody();
                String sender = smsMessage.getDisplayOriginatingAddress();
                HyperLog.i(TAG, String.format("Received : %s\n Sent by %s", messageBody, sender));

                try {
                    parseSms(context, messageBody, sender);
                } catch (SmsParsingException e) {
                    HyperLog.e(TAG, "Error: " + e.getMessage());
                    Utilities.startNotification(context,
                            "Error while setting up a scenario",
                            "Got: " + e.getMessage());
                    sendSms(sender, "Error: " + e.getMessage());
                }


            }
        }
    }

    private void parseSms(Context context, String body, String phoneNumber) throws SmsParsingException {
        String[] s = body.split(",");

        if (s.length != 3 && s.length != 5) {
            throw new SmsParsingException("Body must contains 3 or 5 strings separated by a comma, " +
                    "found: " + s.length);
        }

        Scenario scenario;

        try {
            scenario = ScenarioImporter.importScenario(s[0] + ".json");
        } catch (IOException e) {
            throw new SmsParsingException("Scenario not found or invalid");
        }

        int hour, minute;

        try {
            hour = Integer.parseInt(s[1]);
            minute = Integer.parseInt(s[2]);
        } catch (NumberFormatException e) {
            throw new SmsParsingException("Can't parse hour and minute");
        }

        if (hour < 0 || hour > 23) {
            throw new SmsParsingException("Hour should be between 0 and 23");
        }

        if (minute < 0 || minute > 59) {
            throw new SmsParsingException("Minute should be between 0 and 59");
        }

        if (s.length == 5) {
            int order, total;

            try {
                order = Integer.parseInt(s[3]);
                total = Integer.parseInt(s[4]);
            } catch (NumberFormatException e) {
                throw new SmsParsingException("Can't parse order and total");
            }

            try {
                scenario.setOrderAndTotal(order, total);
            } catch (InvalidParameterException e) {
                throw new SmsParsingException(e.getMessage());
            }
        } else {
            if (scenario.getTasks().stream().anyMatch(t ->
                    t instanceof StressTask || t instanceof IndividualSpeedTask)) {
                throw new SmsParsingException("This scenario contains a stress task or an " +
                        "individual speed task, order and total number of phones cannot be empty.");
            }
        }

        // Start the scenario
        TaskIntentService.startAt(context, scenario, hour, minute);

        sendSms(phoneNumber, String.format("Scenario %s will start at %02d:%02d", scenario.getName(), hour, minute));

    }

    private void sendSms(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

    public static class SmsParsingException extends Exception {
        private SmsParsingException(String message) {
            super(message);
        }
    }
}