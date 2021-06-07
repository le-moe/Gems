package com.example.fadi.networkinfoapi24.reports;

import com.example.fadi.networkinfoapi24.measurements.Measurement;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a collection of measurements to be exported.
 */
public abstract class MeasurementReport<T extends Measurement> extends Report {
    private final List<T> measurements = new ArrayList<>();

    /**
     * Add a measurement to the report
     * @param measurement the measurement that needs to be added.
     */
    public void add(T measurement) {
        measurements.add(measurement);
    }

    public List<T> getMeasurements() {
        return measurements;
    }


}
