package com.example.fadi.networkinfoapi24.tasks;

import com.example.fadi.networkinfoapi24.Duration;
import com.example.fadi.networkinfoapi24.reports.Report;

/**
 * Task that measures something periodically for a given duration.
 */
public abstract class PeriodicTask extends AbstractTask {
    private final Duration duration;
    private final Duration period;

    /**
     * @param duration The duration of the whole task in ms.
     * @param period   The period at which {@link #onNewPeriod()} is executed.
     */
    public PeriodicTask(Duration duration, Duration period) {
        this.duration = duration;
        this.period = period;
    }

    /**
     * This is executed once, at the start of the task.
     */
    public abstract void onStart();

    /**
     * This is executed each period until the task is over.
     */
    public abstract void onNewPeriod();

    /**
     * This is executed once, at the end of the task.
     *
     * @return a report that needs to be exported.
     */
    public abstract Report onStop();

    public Duration getDuration() {
        return duration;
    }

    public Duration getPeriod() {
        return period;
    }

    @Override
    public String toString() {
        return "Duration: " + duration + "\n" + "Period: " + period;
    }
}
