package com.distributed.directions;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements
        GoogleMap.OnMapLongClickListener {

    private LatLng destination;
    private LatLng startPos;
    private GoogleMap googleMap; // Might be null if Google Play services APK is not available.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        googleMap.setOnMapLongClickListener(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        googleMap.setOnMapLongClickListener(this);

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
        LocationManager locationManager = (LocationManager)
                getSystemService(this.LOCATION_SERVICE);

        Location currentPos = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
        startPos = new LatLng(currentPos.getLatitude(), currentPos.getLongitude());

        googleMap.addMarker(new MarkerOptions().position(
                startPos).title("Current Position"));

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(startPos));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(12));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(latLng).title("Destination"));
        destination = latLng;

        CharSequence text = "Distance: " + getDistance(startPos, destination) +
                " miles, Degree: " + getAngleOfLineBetweenTwoPoints(startPos, destination);
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
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

}
