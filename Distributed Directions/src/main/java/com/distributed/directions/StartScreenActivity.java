package com.distributed.directions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

/**
 * Start screen activity. Consists of 2 buttons to determine if we want to get directions or start
 * the geofence app.
 */
public class StartScreenActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        Button directionsButton = (Button)findViewById(R.id.directions_button);
        Button geofenceButton = (Button)findViewById(R.id.geofence_button);

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
    }
}
