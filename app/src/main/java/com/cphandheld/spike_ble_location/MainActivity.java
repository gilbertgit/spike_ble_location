package com.cphandheld.spike_ble_location;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;


public class MainActivity extends Activity {

    private final static String TAG = MainActivity.class.getSimpleName();
    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    Button startScanningButton;
    Button stopScanningButton;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    RunningAverageRssiFilter filter;
    ArrayList<Beacon> listBeacons;
    BeaconListAdapter listAdapter;
    ListView listViewBeacons;
    TextView textViewClosestTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        peripheralTextView = (TextView) findViewById(R.id.PeripheralTextView);
//        peripheralTextView.setMovementMethod(new ScrollingMovementMethod());
        textViewClosestTo = (TextView) findViewById(R.id.textViewClosestTo);

        startScanningButton = (Button) findViewById(R.id.StartScanButton);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();
            }
        });

        stopScanningButton = (Button) findViewById(R.id.StopScanButton);
        stopScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();
            }
        });
        stopScanningButton.setVisibility(View.INVISIBLE);

        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        listViewBeacons = (ListView)findViewById(R.id.listViewBeacons);


        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }

        listBeacons = new ArrayList<Beacon>();
        listAdapter = new BeaconListAdapter(this, listBeacons);
        listViewBeacons.setAdapter(listAdapter);

        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }

        filter = new RunningAverageRssiFilter();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopScanning();
    }

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            if(result.getDevice().getName() != null && result.getDevice().getName().startsWith("Kontakt")) {
                String name = result.getDevice().getName();
                String address = result.getDevice().getAddress();
                int powerLevel = result.getScanRecord().getTxPowerLevel();
                ParcelUuid uuid = result.getScanRecord().getServiceUuids().get(0);
                DecimalFormat numberFormat = new DecimalFormat("#.0");

                int rssi = result.getRssi();
                boolean devicePreviouslyAdded = false;

                for (Beacon beacon : listBeacons) {
                    if (beacon.Address.equals(address)) {
                        beacon.setRssi(rssi);
                        devicePreviouslyAdded = true;
                        listAdapter.notifyDataSetChanged();
                        break;
                    }
                }

                if (!devicePreviouslyAdded) {
                    Beacon beacon = new Beacon(name, uuid, address, rssi, powerLevel);
                    listBeacons.add(beacon);
                    listAdapter.notifyDataSetChanged();
                }

                Collections.sort(listBeacons, new Comparator<Beacon>() {
                    @Override
                    public int compare(Beacon o1, Beacon o2) {
                        return Integer.compare((int) o2.filter.calculateRssi(), (int) o1.filter.calculateRssi());
                    }
                });

                Beacon b = listBeacons.get(0);
                String closestDevice = b.getName() + " : " +
                        b.getAddress() + " : " +
                        numberFormat.format(b.filter.calculateRssi());
                Log.v(TAG, closestDevice);
                textViewClosestTo.setText(closestDevice);
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    public void startScanning() {
        System.out.println("start scanning");
        //peripheralTextView.setText("");
        startScanningButton.setVisibility(View.INVISIBLE);
        stopScanningButton.setVisibility(View.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });
    }

    public void stopScanning() {
        System.out.println("stopping scanning");
        //peripheralTextView.append("Stopped Scanning");
        startScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setVisibility(View.INVISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }
}