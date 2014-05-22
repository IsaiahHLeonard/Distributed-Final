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

    private Button saveChangeButton, editLocButton, deleteButton;
    private EditText locName;
    private Spinner devChooser;
    private ArrayAdapter<String> adapter;
    private SavedLocDetailFragListener mListener;


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

        // Show the content by getting appropriate UI elements and filling them in
        if (mLoc != null) {
            locName = (EditText) rootView.findViewById(R.id.det_location_name);
            locName.setText(mLoc.getName());
            locName.setEnabled(false);

            ((TextView) rootView.findViewById(R.id.det_longitude)).setText(mLoc.getLongitude().toString());
            ((TextView) rootView.findViewById(R.id.det_latitude)).setText(mLoc.getLatitude().toString());
            String act = mLoc.getAutomatedActivity();
            if (act.length() == 0) { act = "None"; }
            saveChangeButton = (Button) rootView.findViewById(R.id.save_changes_button);
            saveChangeButton.setEnabled(false);
            editLocButton = (Button) rootView.findViewById(R.id.edit_loc_button);
            deleteButton = (Button) rootView.findViewById(R.id.delete_button);

            saveChangeButton.setOnClickListener(this);
            editLocButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);

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

            //If user wants to edit location, enable the appropriate fields
            case R.id.edit_loc_button:
                locName.setEnabled(true);
                editLocButton.setEnabled(false);
                saveChangeButton.setEnabled(true);
                devChooser.setSelection(adapter.getPosition(mLoc.getAutomatedActivity()));
                devChooser.setEnabled(true);
                break;

            //When user tries to save changes, make sure that they have entered data appropriately, then try to update
            //our sets of saved locations
            case R.id.save_changes_button:
                String newName = locName.getText().toString();
                if (newName.length() > 0){
                    String newDevice = devChooser.getSelectedItem().toString();
                    devChooser.setEnabled(false);
                    SavedLocation oldLoc = mLoc;
                    mLoc = new SavedLocation(newName, oldLoc.getLatitude(), oldLoc.getLongitude(), newDevice);
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

            //Delete the saved location
            case R.id.delete_button:
                SharedPreferences prefs = this.getActivity().getSharedPreferences("distributed.directions.saved.locs", 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove(mLoc.getName());
                editor.commit();
                SavedLocation.removeLoc(mLoc);
                Intent goToStart= new Intent(this.getActivity(), StartScreenActivity.class);
                startActivity(goToStart);
                break;

            default:
                break;
        }

    }
    public interface SavedLocDetailFragListener{
        void onLocationChangeMade(SavedLocation newLoc, SavedLocation oldLoc);
    }

}
