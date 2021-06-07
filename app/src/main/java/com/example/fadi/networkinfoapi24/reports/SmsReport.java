package com.example.fadi.networkinfoapi24.reports;

import android.app.Activity;
import android.provider.Telephony;
import android.telephony.SmsManager;

import com.example.fadi.networkinfoapi24.tasks.TaskType;

import java.util.Date;

/**
 * A report for a {@link com.example.fadi.networkinfoapi24.tasks.SmsTask}
 */
public class SmsReport extends Report {
    private final Date time = new Date();
    private final String operator;
    private final String phoneNumber;
    private int statusCode;
    private int resultCode;
    private int sendDuration;
    private int deliveryDuration;

    public SmsReport(String operator, String phoneNumber) {
        this.operator = operator;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public FileNameFormat fileNameFormat() {
        return new FileNameFormat(TaskType.SMS, time, operator);
    }

    public String getSendMessage() {
        switch (resultCode) {
            case 0:
                return null;
            case Activity.RESULT_OK:
                return "success: sms sent";
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                return "error: generic failure";
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                return "error: no service";
            case SmsManager.RESULT_ERROR_NULL_PDU:
                return "error: null pdu";
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                return "error: radio off";
            default:
                return "error: unknown";

        }
    }

    public String getDeliveryMessage() {
        switch (statusCode) {
            case Telephony.Sms.STATUS_COMPLETE:
                return "success: SMS delivered";
            case Telephony.Sms.STATUS_FAILED:
                return "error: SMS not delivered";
            case Telephony.Sms.STATUS_PENDING:
                return "error: delivery status pending";
            default:
                return "error: unknown";
        }

    }

    public Date getTime() {
        return time;
    }

    public String getOperator() {
        return operator;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public int getDeliveryDuration() {
        return deliveryDuration;
    }

    public void setDeliveryDuration(int deliveryDuration) {
        this.deliveryDuration = deliveryDuration;
    }

    public int getSendDuration() {
        return sendDuration;
    }

    public void setSendDuration(int sendDuration) {
        this.sendDuration = sendDuration;
    }
}
