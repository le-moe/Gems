package com.example.fadi.networkinfoapi24.tasks;

import android.content.Context;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.fadi.networkinfoapi24.Duration;
import com.example.fadi.networkinfoapi24.NetworkOperator;
import com.example.fadi.networkinfoapi24.NetworkTechnology;
import com.example.fadi.networkinfoapi24.measurements.CellMeasurement;
import com.example.fadi.networkinfoapi24.reports.CellReport;
import com.example.fadi.networkinfoapi24.reports.Report;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Task that measures various data from the cellular network given a technology (GSM, WCDMA, LTE)
 * and a operator
 */
public class CellTask extends PeriodicTask {

    private static final String TAG = "CellTask";
    private CellReport report;
    private TelephonyManager telephonyManager;

    @JsonCreator
    public CellTask(@JsonProperty("duration") Duration duration,
                    @JsonProperty("period") Duration period,
                    @JsonProperty("technology") NetworkTechnology technology,
                    @JsonProperty("operator") NetworkOperator operator) {
        super(duration, period);
        setTechnology(technology);
        setOperator(operator);
    }

    @Override
    public void onStart() {
        report = new CellReport(getOperator(), getTechnology());
        telephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);

    }

    @Override
    public void onNewPeriod() {
        // TODO: Check permissions but normally it should be fine
        List<CellInfo> allCellInfo = telephonyManager.getAllCellInfo();

        for (CellInfo cellInfo : allCellInfo) {
            CellMeasurement measurement = CellMeasurement.fromCellInfo(cellInfo, getOperator());
            report.add(measurement);
            Log.i(TAG, measurement.toString());
        }
    }

    @Override
    public Report onStop() {
        return report;
    }

    @Override
    public String toString() {
        return String.format("%s using %s\n", getOperator().toString(), getTechnology().toString()) + super.toString();
    }
}
