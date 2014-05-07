package com.distributed.directions;

import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        GoogleMap.OnMapLongClickListener,
        LocationListener{

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;


    private LatLng destination;
    private LatLng startPos;
    private GoogleMap googleMap; // Might be null if Google Play services APK is not available.
    LocationRequest mLocationRequest;
    LocationClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        googleMap.setOnMapLongClickListener(this);

        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);

        mLocationClient = new LocationClient(this, this, this);

        mLocationClient.connect();


    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

    }



    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #googleMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (googleMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            googleMap.setMyLocationEnabled(true);
            // Check if we were successful in obtaining the map.
            if (googleMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #googleMap} is not null.
     */
    private void setUpMap() {
        LocationManager locationManager = (LocationManager)getSystemService(this.LOCATION_SERVICE);

        Location currentPos = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
        if (currentPos == null) {
            currentPos = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
        }

        if(currentPos != null) {
            startPos = new LatLng(currentPos.getLatitude(), currentPos.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(
                    startPos).title("Current Position"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(startPos));
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(12));

        }else{
            CharSequence text = "GPS not running, can't determine position";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(latLng).title("Destination"));
        destination = latLng;

        String message = getDistDirMessage();
        CharSequence text = message;
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(this, text, duration);
        toast.show();

        sendAlertToPebble(message);
    }

    private double getDistance(LatLng p1, LatLng p2){
        double lat1 = p1.latitude;
        double lon1 = p1.longitude;
        double lat2 = p2.latitude;
        double lon2 = p2.longitude;
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private double getAngleOfLineBetweenTwoPoints(LatLng p1, LatLng p2) {
        double xDiff = p2.latitude - p1.latitude;
        double yDiff = p2.longitude - p1.longitude;
        return Math.toDegrees(Math.atan2(yDiff, xDiff));
    }

    public void sendAlertToPebble(String message) {
        try {
            final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");

            final Map data = new HashMap();
            data.put("title", "Test Message");
            data.put("body", message);
            final JSONObject jsonData = new JSONObject(data);
            final String notificationData = new JSONArray().put(jsonData).toString();

            i.putExtra("messageType", "PEBBLE_ALERT");
            i.putExtra("sender", "MyAndroidApp");
            i.putExtra("notificationData", notificationData);

            sendBroadcast(i);
        }catch (Exception e){
            CharSequence text = "Couldn't send message to Pebble";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
        }
    }

    public String getDistDirMessage(){
        DecimalFormat myFormatter = new DecimalFormat("##.##");
        return "Distance: " + myFormatter.format(getDistance(startPos, destination)) +
                " miles, Degree: " + myFormatter.format(getAngleOfLineBetweenTwoPoints(startPos, destination));

    }


    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    /*
 * Called by Location Services when the request to connect the
 * client finishes successfully. At this point, you can
 * request the current location or start periodic updates
 */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        // If already requested, start periodic updates
        mLocationClient.requestLocationUpdates(mLocationRequest, this);

    }

    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    /*
 * Called by Location Services if the attempt to
 * Location Services fails.
 */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {


    }
}
