package com.example.fadi.networkinfoapi24.measurements;

import java.util.Date;

/**
 * A measurement is basically data with a timestamp indicating when it was measured.
 */
public abstract class Measurement {
    private Date time = new Date();

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
