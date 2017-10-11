package com.cphandheld.spike_ble_location;

import android.os.ParcelUuid;

/**
 * Created by titan on 10/10/17.
 */

public class Beacon {
    String Name;
    ParcelUuid Uuid;
    String Address;
    int Rssi;
    int PowerLevel;
    RunningAverageRssiFilter filter;

    Beacon(String name, ParcelUuid uuid, String address, int rssi, int powerLevel){
        filter = new RunningAverageRssiFilter();
        this.Name = name;
        this.Uuid = uuid;
        this.Address = address;
        this.Rssi = rssi;
        this.PowerLevel = powerLevel;
        filter.addMeasurement(rssi);
    }

    public ParcelUuid getUuid() {
        return Uuid;
    }

    public String getName() {
        return Name;
    }

    public String getAddress() {
        return Address;
    }

    public int getRssi() {
        return Rssi;
    }

    public void setRssi(int rssi) {
        Rssi = rssi;
        filter.addMeasurement(rssi);
    }

    public int getPowerLevel() {
        return PowerLevel;
    }
}
