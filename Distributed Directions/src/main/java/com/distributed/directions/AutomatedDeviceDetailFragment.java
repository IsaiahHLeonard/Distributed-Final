package com.distributed.directions;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


/**
 * A fragment representing a single AutomatedDevice detail screen.
 * This fragment is either contained in a {@link AutomatedDeviceListActivity}
 * in two-pane mode (on tablets) or a {@link AutomatedDeviceDetailActivity}
 * on handsets.
 */
public class AutomatedDeviceDetailFragment extends Fragment implements View.OnClickListener{
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "device_name";

    /**
     * The device this fragment is presenting.
     */
    private AutomatedDevice mDev;

    //variables for the UI elements
    private Button setDeviceStateButton;
    private TextView devName;
    private Spinner stateChooser;
    private ArrayAdapter<String> adapter;
    private AutomatedDevDetailFragListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AutomatedDeviceDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mDev = AutomatedDevice.DEVICE_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof AutomatedDevDetailFragListener)) {
            throw new IllegalStateException("Activity must implement fragment's listener.");
        }

        mListener = (AutomatedDevDetailFragListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_automateddevice_detail, container, false);

        // Show the content by filling in appropriate values from the device being displayed in this fragment
        if (mDev != null) {
            devName = (TextView) rootView.findViewById(R.id.device_name);
            devName.setText(mDev.getName());
            ((TextView) rootView.findViewById(R.id.device_address)).setText(mDev.getAddress());
            ((TextView) rootView.findViewById(R.id.device_port)).setText(String.valueOf(mDev.getPortNum()));

            setDeviceStateButton = (Button) rootView.findViewById(R.id.set_device_state_button);
            setDeviceStateButton.setOnClickListener(this);

            //Fill spinner with possible states for this device
            ArrayList<String> spinnerList = mDev.getStateList();
            adapter = new ArrayAdapter<String>(container.getContext(), android.R.layout.simple_spinner_item, spinnerList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            stateChooser = (Spinner) rootView.findViewById(R.id.device_state_spinner);
            stateChooser.setAdapter(adapter);
        }

        return rootView;
    }

    //When the user clicks the set-state button, send message to device to set its state appropriately
    public void onClick(View v){
        int clicked = v.getId();
        String selectedState = stateChooser.getSelectedItem().toString();
        Log.i("onClick", "Set state to " + selectedState);
        DeviceRequest request = new DeviceRequest();
        String serverResponse = "";
        try {
            serverResponse = request.execute(mDev.getAddress(), String.valueOf(mDev.getPortNum()), selectedState).get(3000, TimeUnit.MILLISECONDS);
        } catch (Exception e){
            Log.e("onClick", "Error getting server response: " + e.toString());
        }

        //Our protocol has the server responding with 'OK' when it correctly processes a message and sets the state appropriately
        if (!serverResponse.startsWith("OK")){
            Toast.makeText(getActivity(), "Error setting device", Toast.LENGTH_SHORT).show();
        }
    }
    public interface AutomatedDevDetailFragListener{
        void onDeviceChangeMade(AutomatedDevice newDev, AutomatedDevice oldDev);
    }


}
