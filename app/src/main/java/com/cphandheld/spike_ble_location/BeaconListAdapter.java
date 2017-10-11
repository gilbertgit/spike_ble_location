package com.cphandheld.spike_ble_location;

/**
 * Created by titan on 10/10/17.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class BeaconListAdapter extends BaseAdapter {
    private Activity activity;
    private List<Beacon> beacons;

    private static LayoutInflater inflater = null;

    public BeaconListAdapter(Activity _activity, ArrayList<Beacon> _beacons) {
        this.activity = _activity;
        this.beacons = _beacons;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return beacons.size();
    }

    @Override
    public Object getItem(int position) {
        return beacons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(activity);
            view = vi.inflate(R.layout.listitem_beacon, null);
        }

        TextView uuid = (TextView)view.findViewById(R.id.textViewDeviceUuid);
        TextView rssi = (TextView)view.findViewById(R.id.textViewRssi);
        TextView txPower = (TextView)view.findViewById(R.id.textViewPower);
        TextView majorMinor = (TextView)view.findViewById(R.id.textViewMajorMinor);
        TextView key = (TextView)view.findViewById(R.id.textViewKey);
        TextView mac = (TextView)view.findViewById(R.id.textViewMac);

        Beacon beacon = beacons.get(position);

        if(beacon != null) {
            DecimalFormat numberFormat = new DecimalFormat("#.0");
//            uuid.setText(beacon.getProximityUUID().toString());
            rssi.setText(numberFormat.format(beacon.filter.calculateRssi()));
            txPower.setText(String.valueOf(beacon.getPowerLevel()));
//            majorMinor.setText(String.valueOf(beacon.getMajor()) + " : " + String.valueOf(beacon.getMinor()));
            key.setText(beacon.getName());
            mac.setText(beacon.getAddress());
        }
        return view;
    }
}
