package com.example.fadi.networkinfoapi24.tasks;

import com.example.fadi.networkinfoapi24.Duration;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A tasks that performs a speedtest with multiple phones,
 * each one starting a bit after the other.
 */
public class StressTask extends SpeedTask {

    private boolean isLast = false;

    /**
     * Creates a new task that performs a speed test.
     *
     * @param mode     Either DOWNLOAD or UPLOAD.
     * @param duration The duration of the speed test.
     * @param period   The time interval between two measurements.
     * @param wifi
     */
    @JsonCreator
    public StressTask(@JsonProperty("mode") Mode mode,
                      @JsonProperty("duration") Duration duration,
                      @JsonProperty("period") Duration period,
                      @JsonProperty("wifi") boolean wifi) {
        super(mode, duration, period, wifi);
    }

    public static StressTask fromStressTask(StressTask other, Duration delay) {
        StressTask task = new StressTask(
                other.getMode(),
                other.getDuration(),
                other.getPeriod(),
                other.isWifi()
        );
        task.setDelay(delay);
        return task;
    }

    @Override
    public String toString() {
        String tech = (isWifi() ? "wifi" : "cellular");
        return "StressTest: " + getMode().toString() + " " + tech + "\n" +
                "Duration: " + getDuration() + "\n" +
                "Period: " + getPeriod();
    }

    @JsonIgnore
    public void setLast(boolean last) {
        isLast = last;
    }

    @Override
    public boolean shouldDisableWifi() {
        return isWifi() && isLast;
    }
}
