package com.cphandheld.spike_ble_location;

/**
 * Created by titan on 5/23/17.
 */

import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class RunningAverageRssiFilter  implements RssiFilter {

    private static final String TAG = "RARssiFilter";
    public static final long DEFAULT_SAMPLE_EXPIRATION_MILLISECONDS = 20000; /* 20 seconds */
    private static long sampleExpirationMilliseconds = DEFAULT_SAMPLE_EXPIRATION_MILLISECONDS;
    private ArrayList<Measurement> mMeasurements = new ArrayList<Measurement>();

    @Override
    public void addMeasurement(Integer rssi) {
        Measurement measurement = new Measurement();
        measurement.rssi = rssi;
        measurement.timestamp = SystemClock.elapsedRealtime();
        mMeasurements.add(measurement);
    }

    @Override
    public boolean noMeasurementsAvailable() {
        return mMeasurements.size() == 0;
    }

    @Override
    public double calculateRssi() {
        refreshMeasurements();
        int size = mMeasurements.size();
        int startIndex = 0;
        int endIndex = size -1;
        if (size > 2) {
            startIndex = size/10+1;
            endIndex = size-size/10-2;
        }

        double sum = 0;
        for (int i = startIndex; i <= endIndex; i++) {
            sum += mMeasurements.get(i).rssi;
        }
        double runningAverage = sum/(endIndex-startIndex+1);


        Log.v(TAG, String.format("Running average mRssi based on %s measurements: %s",
                size, runningAverage));
        return runningAverage;
    }

    private synchronized void refreshMeasurements() {
        ArrayList<Measurement> newMeasurements = new ArrayList<Measurement>();
        Iterator<Measurement> iterator = mMeasurements.iterator();
        while (iterator.hasNext()) {
            Measurement measurement = iterator.next();
            if (SystemClock.elapsedRealtime() - measurement.timestamp < sampleExpirationMilliseconds ) {
                newMeasurements.add(measurement);
            }
        }
        mMeasurements = newMeasurements;
        Collections.sort(mMeasurements);
    }

    private class Measurement implements Comparable<Measurement> {
        Integer rssi;
        long timestamp;
        @Override
        public int compareTo(Measurement arg0) {
            return rssi.compareTo(arg0.rssi);
        }
    }

    public static void setSampleExpirationMilliseconds(long newSampleExpirationMilliseconds) {
        sampleExpirationMilliseconds = newSampleExpirationMilliseconds;
    }

}
