package com.example.fadi.networkinfoapi24.reports;

import com.example.fadi.networkinfoapi24.tasks.TaskType;

import java.util.Date;

/**
 * A report for a {@link com.example.fadi.networkinfoapi24.tasks.CallTask}
 */
public class CallReport extends Report {
    private final Date time = new Date();
    private final String operator;
    private final String phoneNumber;
    private final int callDuration;
    private final boolean timeout;

    public CallReport(String operator, String phoneNumber, int callDuration, boolean timeout) {
        this.operator = operator;
        this.phoneNumber = phoneNumber;
        this.callDuration = callDuration;
        this.timeout = timeout;
    }


    @Override
    public FileNameFormat fileNameFormat() {
        return new FileNameFormat(TaskType.CALL, time, operator);
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

    public int getCallDuration() {
        return callDuration;
    }

    public boolean isTimeout() {
        return timeout;
    }
}
