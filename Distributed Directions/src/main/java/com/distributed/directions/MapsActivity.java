package com.distributed.directions;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * Main Activity class for the maps. Shows the map and does all of the
 * computations of distance, and geofencing.
 */
public class MapsActivity extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        GoogleMap.OnMapLongClickListener,
        LocationListener{

    // Ids for the two apps on the watch app.
    private final static UUID PEBBLE_APP_UUID_DIRECTIONS = UUID.fromString("ab457116-2823-4189-9b18-980eae57f301");
    private final static UUID PEBBLE_APP_UUID_GEOFENCE = UUID.fromString("fd214ce8-7f74-4747-b1e0-d5e1932f1d47");

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 10;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    // Geofence radius, set to .05 miles
    private static final double GEOFENCE_RADIUS = 0.05;

    private LatLng startPos;
    private GoogleMap googleMap;
    LocationRequest mLocationRequest;
    LocationClient mLocationClient;
    boolean mLocationUpdateBool;
    boolean directionsApp;

    /**
     * OnCreate method. This is called when activity is first started. This
     * initializes everything, the map, the locations, and the variables.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Gets data from start screen and determines whether it is a directions or geofence.
        Intent intentFromStart = getIntent();
        directionsApp = intentFromStart.getBooleanExtra("isDirections", true);

        setUpMapIfNeeded();

        // Sets up the map, and location change parameters
        googleMap.setOnMapLongClickListener(this);
        mLocationUpdateBool = false;
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);

        // Creates the location client, that handles current location. Waits till it is connected
        mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();
        try {
            mLocationClient.wait();
        }catch(Exception e){
            Log.e("Error:", "Could not connect");
        }

        // Opens up the app on the watch depending on functionality. Also loads map with appropriate
        // markers, destination for directions, and geofence points for geofence
        if(!directionsApp) {
            loadLocations();
            setUpMarks();
            PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID_GEOFENCE);
        }else{
            PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID_DIRECTIONS);
            SharedPreferences prefs = getPreferences(MODE_PRIVATE);
            double lat = Double.longBitsToDouble(prefs.getLong("destinationLat", 200));
            double lon = Double.longBitsToDouble(prefs.getLong("destinationLong", 200));
            if(!(lat == 200 && lon == 200)){
                googleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title("Destination"));

            }
        }

        // If geofence, this starts the listener from watch, message to turn on/off appliance.
        // If it receives a message, then sends the corresponding message to appliance.
        if(!directionsApp) {
            PebbleKit.PebbleDataReceiver pebbleListener = new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID_GEOFENCE) {
                @Override
                public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
                    String name = data.getString(1);
                    int onOff = data.getInteger(2).intValue();
                    CharSequence text = "turn on app in " + name;
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(MapsActivity.this, text, duration);
                    toast.show();

                    Iterator<SavedLocation> it = SavedLocation.LOCATION_MAP.values().iterator();
                    while(it.hasNext()){
                        SavedLocation tempLoc = it.next();
                        if(tempLoc.getName().equals(name)) {
                            AutomatedDevice mDev = AutomatedDevice.DEVICE_MAP.get(tempLoc.getAutomatedActivity());
                            DeviceRequest request = new DeviceRequest();
                            String serverResponse = "";
                            try {
                                serverResponse = request.execute(mDev.getAddress(), String.valueOf(mDev.getPortNum()), onOff+"").get(3000, TimeUnit.MILLISECONDS);
                            } catch (Exception e){
                                Log.e("onClick", "Error getting server response: " + e.toString());
                            }
                            if (!serverResponse.startsWith("OK")){
                                Toast.makeText(MapsActivity.this, "Error setting device", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                }
            };
            PebbleKit.registerReceivedDataHandler(this, pebbleListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!directionsApp) {
            loadLocations();
            setUpMarks();
        }
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
     * Sets up the location manager and finds the current position. Using the current position,
     * it zooms the camera so it is zoomed to your location
     */
    private void setUpMap() {
        LocationManager locationManager = (LocationManager)getSystemService(this.LOCATION_SERVICE);

        Location currentPos = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
        if (currentPos == null) {
            currentPos = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
        }

        if(currentPos != null) {
            startPos = new LatLng(currentPos.getLatitude(), currentPos.getLongitude());
            /**googleMap.addMarker(new MarkerOptions().position(
                    startPos).title("Current Position"));*/
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(startPos));
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(12));

        }else{
            CharSequence text = "GPS not running, can't determine position";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
        }
    }

    /**
     * If we are trying to do geofencing, this finds all the locations, and puts a mark down in the map
     */
    public void setUpMarks(){
        if(!directionsApp){
            Iterator<SavedLocation> it = SavedLocation.LOCATION_MAP.values().iterator();
            while(it.hasNext()){
                SavedLocation tempLoc = it.next();
                googleMap.addMarker(new MarkerOptions().position(new LatLng(tempLoc.getLatitude(), tempLoc.getLongitude())).
                        title(tempLoc.getName()));
            }
        }
    }

    /**
     * On click listener. If a point is long clicked in the map,
     * If we are doing directions, this clears the map of markers (previous destination)
     * and sets the new destination. Also turns on updates so we get updates if location changes
     * If we are doing geofencing, this adds a new location to the list and goes to the
     * list activity.
     * @param latLng: location of point that is long clicked
     */
    @Override
    public void onMapLongClick(LatLng latLng) {
        if(directionsApp) {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Destination"));

            SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
            editor.putLong("destinationLat", Double.doubleToLongBits(latLng.latitude));
            editor.putLong("destinationLong", Double.doubleToLongBits(latLng.longitude));
            editor.commit();

            CharSequence text = "Directing to this location";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(this, text, duration);
            toast.show();

            mLocationUpdateBool = true;
            if (mLocationUpdateBool) {
                mLocationClient.requestLocationUpdates(mLocationRequest, this);
            }
        }else{
            SavedLocation.addLoc(new SavedLocation("New location", latLng.latitude, latLng.longitude, "137.165.9.105"));
            Intent goToList = new Intent(this, SavedLocationListActivity.class);
            startActivity(goToList);
        }
    }

    /**
     * Gets distance between two points in miles
     * @param p1
     * @param p2
     * @return
     */
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

    /**
     * returns radians from degree
     * @param deg
     * @return
     */
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /**
     * Returns degree from radians
     * @param rad
     * @return
     */
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    /**
     * Gets the angle in degrees of two locations in the map.
     * @param p1
     * @param p2
     * @return degree with repect to north of the two points
     */
    private double getAngleOfLineBetweenTwoPoints(LatLng p1, LatLng p2) {
        double xDiff = p2.latitude - p1.latitude;
        double yDiff = p2.longitude - p1.longitude;
        double latlangAngle = (Math.toDegrees(Math.atan2(yDiff, xDiff)) + 360.0)%360;
        return (-1.0 * (latlangAngle - 90.0) + 360)%360;

    }

    /**
     * Sends data to pebble watch
     * @param dist
     * @param angle
     */
    public void sendAlertToPebble(String dist, int angle) {
        try {
            PebbleDictionary data = new PebbleDictionary();
            data.addUint8(0, (byte) 42);
            data.addString(1, "Distance: " + dist + " miles");
            if(angle != -1) {
                data.addUint32(2, angle);
            }
            PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID_DIRECTIONS, data);

        }catch (Exception e){
            CharSequence text = "Couldn't send message to Pebble";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
        }
    }

    /**
     * Finds the distance from current to destination and returns the
     * distance in string format with only 3 decimal points
     * @param current
     * @return distance to destination
     */
    public String getDistMessage(LatLng current){
        DecimalFormat myFormatter = new DecimalFormat("##.###");
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        double lat = Double.longBitsToDouble(prefs.getLong("destinationLat", 0));
        double lon = Double.longBitsToDouble(prefs.getLong("destinationLong", 0));
        return myFormatter.format(getDistance(current, new LatLng(lat, lon))) + "";
    }

    /**
     * Finds the direction of current location to destination and returns the
     * degree with respect to north rounded to integers
     * @param current
     * @return
     */
    public int getDirMessage(LatLng current){
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        double lat = Double.longBitsToDouble(prefs.getLong("destinationLat", 0));
        double lon = Double.longBitsToDouble(prefs.getLong("destinationLong", 0));
        double angleToDest = getAngleOfLineBetweenTwoPoints(current, new LatLng(lat, lon));
        return (int) angleToDest;
    }

    /**
     * On location listener. If we move and location is changed, we first check which app we are running.
     * If finding direction, we find distance and direction to destination and sends message to pebble.
     * If geofence, then we check all locations and finds distance to current location. If it is near,
     * we send message
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
        if(directionsApp) {
            sendAlertToPebble(getDistMessage(current), getDirMessage(current));
        }else{
            Iterator<SavedLocation> it = SavedLocation.LOCATION_MAP.values().iterator();
            while(it.hasNext()) {
                SavedLocation tempLoc = it.next();
                if (!tempLoc.isVisited()) {
                    if (getDistance(current, new LatLng(tempLoc.getLatitude(), tempLoc.getLongitude())) < GEOFENCE_RADIUS) {
                        CharSequence text = tempLoc.getName() + ", activity: " + tempLoc.getAutomatedActivity();
                        int duration = Toast.LENGTH_LONG;
                        Toast toast = Toast.makeText(this, text, duration);
                        toast.show();
                        tempLoc.setIsVisited(true);

                        PebbleDictionary data = new PebbleDictionary();
                        data.addUint8(0, (byte) 42);
                        data.addString(1, tempLoc.getName());
                        data.addString(2, tempLoc.getAutomatedActivity());
                        PebbleKit.sendDataToPebble(getApplicationContext(), PEBBLE_APP_UUID_GEOFENCE, data);

                    }
                }
            }
        }
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
        if(mLocationUpdateBool || !directionsApp) {
            mLocationClient.requestLocationUpdates(mLocationRequest, this);
        }

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

    /**
     * Loads the location for geofencing from SavedLocations.
     */
    private void loadLocations() {
        SharedPreferences prefs = this.getSharedPreferences("distributed.directions.saved.locs", 0);
        Map<String, ?> locMap = prefs.getAll();
        Set<String> keys = locMap.keySet();
        String next;
        for (String s : keys) {
            next = prefs.getString(s, "");
            if (next.length() > 0) {
                SavedLocation loc = new SavedLocation(next);
                loc.setIsVisited(false);
                SavedLocation.addLoc(loc);
            }
        }
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

