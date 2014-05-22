package com.distributed.directions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import java.util.Map;
import java.util.Set;

/**
 * Start screen activity. Consists of 5 buttons which give user access to the different parts of the app.
 * 1. Directions 2. GeoFence tracking 3. List of SavedLocations 4. List of AutomatedDevices 5. Create new device
 */
public class StartScreenActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        loadDevices();
        Button directionsButton = (Button)findViewById(R.id.directions_button);
        Button geofenceButton = (Button)findViewById(R.id.geofence_button);
        Button locationButton = (Button)findViewById(R.id.location_list_button);
        Button deviceButton = (Button)findViewById(R.id.device_list_button);
        Button createDeviceButton = (Button)findViewById(R.id.create_device_button);

        directionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Goes to the map activity
                Intent goToMap = new Intent(StartScreenActivity.this, MapsActivity.class);
                goToMap.putExtra("isDirections", true);
                startActivity(goToMap);
            }
        });

        geofenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Goes to the map activity
                Intent goToMap = new Intent(StartScreenActivity.this, MapsActivity.class);
                goToMap.putExtra("isDirections", false);
                startActivity(goToMap);
            }
        });

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Goes to location list
                Intent goTolist = new Intent(StartScreenActivity.this, SavedLocationListActivity.class);
                startActivity(goTolist);
            }
        });

        deviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Goes to device list
                Intent goTolist = new Intent(StartScreenActivity.this, AutomatedDeviceListActivity.class);
                startActivity(goTolist);
            }
        });

        createDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Goes to create device page
                Intent goToCreate = new Intent(StartScreenActivity.this, CreateAutomatedDevice.class);
                startActivity(goToCreate);
            }
        });
    }

    /**
     * Loads the devices from the shared prefrences
     */
    private void loadDevices() {
        SharedPreferences prefs = this.getSharedPreferences("distributed.directions.saved.devices", 0);
        Map<String, ?> devMap = prefs.getAll();
        Set<String> keys = devMap.keySet();
        String next;
        for (String s : keys) {
            next = prefs.getString(s, "");
            if (next.length() > 0) {
                com.distributed.directions.AutomatedDevice.addDevice(new AutomatedDevice(next));
            }
        }
    }
}
