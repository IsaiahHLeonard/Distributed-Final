package com.distributed.directions;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by isaiahleonard on 5/4/14.
 * Custom array adapter for SavedLocations. Used tutorial at
 * http://www.javacodegeeks.com/2013/09/android-listview-with-adapter-example.html
 */
public class DeviceArrayAdapter extends ArrayAdapter<AutomatedDevice> {
    Context mContext;
    int layoutResourceId;
    List<AutomatedDevice> devs = null;

    public DeviceArrayAdapter(Context mContext, int layoutResourceId, List<AutomatedDevice> l){
        super(mContext, layoutResourceId, l);

        this.mContext = mContext;
        this.layoutResourceId = layoutResourceId;
        this.devs = l;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            //inflate layout
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }

        //Location based on position
        AutomatedDevice dev = devs.get(position);

        //Get TextViews and fill them in
        TextView locName = (TextView) convertView.findViewById(R.id.device_name);
        TextView address = (TextView) convertView.findViewById(R.id.device_address);
        TextView portNum = (TextView) convertView.findViewById(R.id.device_port);


        locName.setText(dev.getName());
        address.setText(dev.getAddress());
        portNum.setText(String.valueOf(dev.getPortNum()));



        return convertView;


    }

}
