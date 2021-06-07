package com.example.fadi.networkinfoapi24;

import java.util.Locale;

/**
 * A mutable data structure that stores a duration.
 * <p>
 * It is mutable so that any field could be set without needing to set the other ones.
 * This is useful in {@link ScenarioImporter} where in the JSON configuration file, you only need
 * to provides values for non-zero fields.
 */
public class Duration {
    private int hours = 0;
    private int minutes = 0;
    private int seconds = 0;
    private int milliseconds = 0;

    public Duration() {

    }

    public Duration(int hours, int minutes, int seconds, int milliseconds) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.milliseconds = milliseconds;
    }

    @Override
    public String toString() {
        if (milliseconds != 0)
            return String.format(Locale.getDefault(), "%02d:%02d:%02d:%03d", hours, minutes, seconds, milliseconds);
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    public int toSeconds() {
        return seconds + 60 * (minutes + 60 * hours);
    }

    public int toMilliseconds() {
        return milliseconds + 1000 * toSeconds();
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(int milliseconds) {
        this.milliseconds = milliseconds;
    }
}
