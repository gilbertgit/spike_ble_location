package com.cphandheld.spike_ble_location;

/**
 * Created by titan on 5/23/17.
 */

public interface RssiFilter {

    public void addMeasurement(Integer rssi);
    public boolean noMeasurementsAvailable();
    public double calculateRssi();

}
