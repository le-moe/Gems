package com.example.fadi.networkinfoapi24.reports;

import com.example.fadi.networkinfoapi24.NetworkOperator;
import com.example.fadi.networkinfoapi24.NetworkTechnology;
import com.example.fadi.networkinfoapi24.measurements.SpeedMeasurement;
import com.example.fadi.networkinfoapi24.tasks.SpeedTask;
import com.example.fadi.networkinfoapi24.tasks.TaskType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A report for a {@link com.example.fadi.networkinfoapi24.tasks.SpeedTask}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpeedReport extends Report {
    public final NetworkTechnology type;
    public final NetworkOperator operator;
    public final boolean usesWifi;
    public final SpeedTask.Mode mode;
    private String BSSID;
    private String SSID;

    private final List<SpeedMeasurement> measurements = new ArrayList<>();

    public SpeedReport(SpeedTask.Mode mode, NetworkTechnology type, NetworkOperator operator) {
        this.type = type;
        this.operator = operator;
        this.mode = mode;
        usesWifi = false;
    }

    public SpeedReport(SpeedTask.Mode mode) {
        this.mode = mode;
        this.type = null;
        this.operator = null;
        usesWifi = true;
    }

    public void add(SpeedMeasurement measurement) {
        measurements.add(measurement);
    }

    public List<SpeedMeasurement> getMeasurements() {
        return measurements;
    }

    @Override
    public FileNameFormat fileNameFormat() {
        String[] others = createOthers();
        Date date = measurements.isEmpty() ? new Date() : measurements.get(0).getTime();
        return new FileNameFormat(TaskType.SPEED, date, others);
    }

    private String[] createOthers() {
        if (usesWifi)
            return new String[]{"WIFI"};
        if (operator == null || type == null)
            return new String[]{"CELL"};
        return new String[]{operator.toString(), type.toString()};
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public String getSSID() {
        return SSID;
    }
}
