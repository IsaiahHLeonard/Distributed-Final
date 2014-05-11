package com.distributed.directions;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.distributed.directions.SavedLocation;

import java.util.List;

/**
 * Custom array adapter for SavedLocations. Used tutorial at
 * http://www.javacodegeeks.com/2013/09/android-listview-with-adapter-example.html
 */
public class LocationArrayAdapter extends ArrayAdapter<SavedLocation> {
    Context mContext;
    int layoutResourceId;
    List<SavedLocation> locs = null;

    public LocationArrayAdapter(Context mContext, int layoutResourceId, List<SavedLocation> l){
        super(mContext, layoutResourceId, l);

        this.mContext = mContext;
        this.layoutResourceId = layoutResourceId;
        this.locs = l;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            //inflate layout
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }

        //Location based on position
        SavedLocation loc = locs.get(position);

        //Get TextViews and fill them in
        TextView locName = (TextView) convertView.findViewById(R.id.location_name);
        TextView lat = (TextView) convertView.findViewById(R.id.latitude);
        TextView lon = (TextView) convertView.findViewById(R.id.longitude);
        TextView act = (TextView) convertView.findViewById(R.id.associated_activity);

        locName.setText(loc.getName());
        lat.setText(loc.getLatitude().toString());
        lon.setText(loc.getLongitude().toString());
        act.setText(loc.getAutomatedActivity());

        return convertView;


    }

}
