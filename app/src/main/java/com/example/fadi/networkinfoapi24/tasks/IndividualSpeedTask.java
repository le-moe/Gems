package com.example.fadi.networkinfoapi24.tasks;

import com.example.fadi.networkinfoapi24.Duration;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IndividualSpeedTask extends SpeedTask {
    /**
     * Creates a new task that performs a speed test.
     *
     * @param mode     Either DOWNLOAD or UPLOAD.
     * @param duration The duration of the speed test.
     * @param period   The time interval between two measurements.
     * @param wifi
     */
    @JsonCreator
    public IndividualSpeedTask(@JsonProperty("mode") Mode mode,
                               @JsonProperty("duration") Duration duration,
                               @JsonProperty("period") Duration period,
                               @JsonProperty("wifi") boolean wifi) {
        super(mode, duration, period, wifi);
    }

    @Override
    public String toString() {
        return "Individual " + super.toString();
    }
}
