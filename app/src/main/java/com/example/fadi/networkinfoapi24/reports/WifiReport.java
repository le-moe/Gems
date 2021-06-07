package com.example.fadi.networkinfoapi24.reports;

import com.example.fadi.networkinfoapi24.measurements.WifiMeasurement;
import com.example.fadi.networkinfoapi24.tasks.TaskType;

import java.util.Date;

/**
 * A report for a {@link com.example.fadi.networkinfoapi24.tasks.WifiTask}
 */
public class WifiReport extends MeasurementReport<WifiMeasurement> {
    @Override
    public FileNameFormat fileNameFormat() {
        Date date = getMeasurements().get(0).getTime();
        return new FileNameFormat(TaskType.WIFI, date);
    }
}
