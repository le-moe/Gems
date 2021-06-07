package com.example.fadi.networkinfoapi24.reports;

import com.example.fadi.networkinfoapi24.NetworkOperator;
import com.example.fadi.networkinfoapi24.NetworkTechnology;
import com.example.fadi.networkinfoapi24.measurements.CellMeasurement;
import com.example.fadi.networkinfoapi24.tasks.TaskType;

import java.util.Date;

/**
 * A report for a {@link com.example.fadi.networkinfoapi24.tasks.CellTask}
 */
public class CellReport extends MeasurementReport<CellMeasurement> {
    private final NetworkOperator operator;
    private final NetworkTechnology technology;

    public CellReport(NetworkOperator operator, NetworkTechnology technology) {
        this.operator = operator;
        this.technology = technology;
    }

    @Override
    public FileNameFormat fileNameFormat() {
        CellMeasurement measurement = getMeasurements().get(0);
        Date date = measurement.getTime();
        return new FileNameFormat(TaskType.CELL, date, operator.toString(), technology.toString());
    }
}
