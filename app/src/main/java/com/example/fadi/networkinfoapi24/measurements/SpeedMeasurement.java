package com.example.fadi.networkinfoapi24.measurements;

import java.math.BigDecimal;

/**
 * A measurement of the transfer rate during a {@link com.example.fadi.networkinfoapi24.tasks.SpeedTask}
 */
public class SpeedMeasurement extends Measurement {

    public final BigDecimal speed;

    /**
     * Creates a new {@link SpeedMeasurement}
     *
     * @param speed the transfer rate in bit/s
     */
    public SpeedMeasurement(BigDecimal speed) {
        this.speed = speed;
    }
}
