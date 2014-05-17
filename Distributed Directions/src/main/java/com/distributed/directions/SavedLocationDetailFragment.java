package com.distributed.directions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * A fragment representing a single SavedLocation detail screen.
 * This fragment is either contained in a {@link com.distributed.directions.SavedLocationListActivity}
 * in two-pane mode (on tablets) or a {@link com.distributed.directions.SavedLocationDetailActivity}
 * on handsets.
 */
public class SavedLocationDetailFragment extends Fragment implements View.OnClickListener{
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "location_name";

    /**
     * The location content this fragment is presenting.
     */
    private SavedLocation mLoc;

    private Button saveChangeButton, editLocButton, deleteButton, remoteButton;
    private EditText locName;
    private EditText ipAddress;
    private Spinner devChooser;
    private ArrayAdapter<String> adapter;
    private SavedLocDetailFragListener mListener;

    //private GoogleMap googleMap; // Might be null if Google Play services APK is not available.

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SavedLocationDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mLoc = SavedLocation.LOCATION_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof SavedLocDetailFragListener)) {
            throw new IllegalStateException("Activity must implement fragment's listener.");
        }

        mListener = (SavedLocDetailFragListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_savedlocation_detail, container, false);
        //setUpMapIfNeeded();

        // Show the content
        if (mLoc != null) {
            locName = (EditText) rootView.findViewById(R.id.det_location_name);
            locName.setText(mLoc.getName());
            locName.setEnabled(false);

            ipAddress = (EditText) rootView.findViewById(R.id.ip_address);
            ipAddress.setText(mLoc.getIp());
            ipAddress.setEnabled(false);

            ((TextView) rootView.findViewById(R.id.det_longitude)).setText(mLoc.getLongitude().toString());
            ((TextView) rootView.findViewById(R.id.det_latitude)).setText(mLoc.getLatitude().toString());
            String act = mLoc.getAutomatedActivity();
            if (act.length() == 0) { act = "None"; }
            //googleMap.addMarker(new MarkerOptions().position(
                   // new LatLng(mLoc.getLatitude(), mLoc.getLongitude())).title(mLoc.getName()));
            saveChangeButton = (Button) rootView.findViewById(R.id.save_changes_button);
            saveChangeButton.setEnabled(false);
            editLocButton = (Button) rootView.findViewById(R.id.edit_loc_button);
            deleteButton = (Button) rootView.findViewById(R.id.delete_button);
            remoteButton = (Button) rootView.findViewById(R.id.remote_start_button);

            saveChangeButton.setOnClickListener(this);
            editLocButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
            remoteButton.setOnClickListener(this);

            //Set up spinner for possible use
            List<String> spinnerList = new ArrayList<String>();
            for (AutomatedDevice a : AutomatedDevice.DEVICE_LIST){
                spinnerList.add(a.getName());
            }
            adapter = new ArrayAdapter<String>(container.getContext(), android.R.layout.simple_spinner_item, spinnerList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            devChooser = (Spinner) rootView.findViewById(R.id.det_activity_chooser);
            devChooser.setAdapter(adapter);
            devChooser.setEnabled(false);
        }

        return rootView;
    }

    public void onClick(View v){
        int clicked = v.getId();
        switch (clicked){
            case R.id.edit_loc_button:
                locName.setEnabled(true);
                ipAddress.setEnabled(true);
                editLocButton.setEnabled(false);
                saveChangeButton.setEnabled(true);
                devChooser.setSelection(adapter.getPosition(mLoc.getAutomatedActivity()));
                devChooser.setEnabled(true);
                break;
            case R.id.save_changes_button:
                String newName = locName.getText().toString();
                if (newName.length() > 0){
                    String newDevice = devChooser.getSelectedItem().toString();
                    devChooser.setEnabled(false);
                    SavedLocation oldLoc = mLoc;
                    mLoc = new SavedLocation(newName, oldLoc.getLatitude(), oldLoc.getLongitude(), newDevice);
                    mLoc.setIp(ipAddress.getText().toString());
                    editLocButton.setEnabled(true);
                    locName.setEnabled(false);
                    saveChangeButton.setEnabled(false);
                    boolean success = SavedLocation.updateMap(mLoc, oldLoc);
                    if (!success){
                        Toast.makeText(getActivity(), "New name cannot be the same as another location", Toast.LENGTH_SHORT).show();
                        mLoc = oldLoc;
                    } else {
                        mListener.onLocationChangeMade(mLoc, oldLoc);
                    }
                } else {
                    Toast.makeText(getActivity(), "New name must not be blank", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.delete_button:
                SharedPreferences prefs = this.getActivity().getSharedPreferences("distributed.directions.saved.locs", 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove(mLoc.getName());
                editor.commit();
                SavedLocation.removeLoc(mLoc);
                Intent goToList = new Intent(this.getActivity(), SavedLocationListActivity.class);
                startActivity(goToList);
                break;

            default:
                break;
        }

    }
    public interface SavedLocDetailFragListener{
        void onLocationChangeMade(SavedLocation newLoc, SavedLocation oldLoc);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #googleMap} is not null.
     * <p>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(android.os.Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     *//*
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (googleMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            googleMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.list_det_map))
                    .getMap();
            googleMap.setMyLocationEnabled(true);
            // Check if we were successful in obtaining the map.
            if (googleMap != null) {
                setUpMap();
            }
        }
    }

    *//**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #googleMap} is not null.
     *//*
    private void setUpMap() {
        googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("origin"));

    }*/
}
