package com.example.fadi.networkinfoapi24.measurements;

import android.net.wifi.ScanResult;

/**
 * A measurement of Wifi data.
 *
 * @see com.example.fadi.networkinfoapi24.tasks.WifiTask
 */
public class WifiMeasurement extends Measurement {
    private final String BSSID;
    private final String SSID;
    private final int frequency;
    private final int level;
    private final int channelWidth;

    /**
     * @param BSSID        The address of the access point.
     * @param SSID         The network name.
     * @param frequency    The primary 20 MHz frequency (in MHz) of the channel over which
     *                     the client is communicating with the access point.
     * @param level        The detected signal level in dBm, also known as the RSSI.
     * @param channelWidth
     */
    public WifiMeasurement(String BSSID, String SSID, int frequency, int level, int channelWidth) {
        this.BSSID = BSSID;
        this.SSID = SSID;
        this.frequency = frequency;
        this.level = level;
        this.channelWidth = channelWidth;
    }

    /**
     * Create a {@link WifiMeasurement} from a {@link ScanResult}
     *
     * @param result The {@link ScanResult}
     * @return an instance of {@link WifiMeasurement}
     */
    public static WifiMeasurement fromScanResult(ScanResult result) {
        return new WifiMeasurement(
                result.BSSID,
                result.SSID,
                result.frequency,
                result.level,
                result.channelWidth);
    }

    public String getBSSID() {
        return BSSID;
    }

    public String getSSID() {
        return SSID;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getLevel() {
        return level;
    }

    public String getChannelWidth() {
        switch (channelWidth) {
            case ScanResult.CHANNEL_WIDTH_20MHZ:
                return "20MHz";
            case ScanResult.CHANNEL_WIDTH_40MHZ:
                return "40MHz";
            case ScanResult.CHANNEL_WIDTH_80MHZ:
                return "80MHz";
            case ScanResult.CHANNEL_WIDTH_160MHZ:
                return "160MHz";
            case ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ:
                return "80+80MHz";
        }
        return null;
    }

    @Override
    public String toString() {
        return "BSSID : " + BSSID + "\n" +
                "SSID : " + SSID + "\n" +
                "Frequency : " + frequency + "\n" +
                "Level : " + level + "\n" +
                "Channel width : " + getChannelWidth();
    }
}
