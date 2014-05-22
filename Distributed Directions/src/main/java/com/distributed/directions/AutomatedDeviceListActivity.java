package com.distributed.directions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * An activity representing a list of AutomatedDevices. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link AutomatedDeviceDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link AutomatedDeviceListFragment} and the item details
 * (if present) is a {@link AutomatedDeviceDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link AutomatedDeviceListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class AutomatedDeviceListActivity extends FragmentActivity
        implements AutomatedDeviceListFragment.Callbacks, AutomatedDeviceDetailFragment.AutomatedDevDetailFragListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    public ArrayList<AutomatedDevice> savedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automateddevice_list);
        //Read in SharedPreferences to get AutomatedDevices. Then load into ArrayList
        SharedPreferences prefs = this.getSharedPreferences("distributed.directions.saved.devices", 0);
        SharedPreferences.Editor eraser = prefs.edit();
        eraser.clear();
        eraser.commit();

        this.loadDevices();

        //Test
        testSharedPrefsDevices();
        logSharedPrefsDevices();


        if (findViewById(R.id.automateddevice_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((AutomatedDeviceListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.automateddevice_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    @Override
    public void onResume(){
        super.onResume();
        AutomatedDeviceListFragment frag = (AutomatedDeviceListFragment) getSupportFragmentManager().findFragmentById(R.id.automateddevice_list);
        frag.updateDevAdapter();
    }

    /**
     * Callback method from {@link AutomatedDeviceListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(AutomatedDeviceDetailFragment.ARG_ITEM_ID, id);
            AutomatedDeviceDetailFragment fragment = new AutomatedDeviceDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.automateddevice_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, AutomatedDeviceDetailActivity.class);
            detailIntent.putExtra(AutomatedDeviceDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

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

    private void testSharedPrefsDevices(){
        SharedPreferences prefs = this.getSharedPreferences("distributed.directions.saved.devices", 0);
        SharedPreferences.Editor editor = prefs.edit();
        for (AutomatedDevice dev : AutomatedDevice.DEVICE_LIST){
            editor.putString(dev.getName(), dev.delimitedRep());
        }
        if (! editor.commit()){
            Log.i("ERROR:  ", "Failed in testSharedPrefsDevices");
        }
    }

    private void logSharedPrefsDevices(){
        SharedPreferences prefs = this.getSharedPreferences("distributed.directions.saved.devices", 0);
        Map<String, ?> devMap = prefs.getAll();
        Set<String> keys = devMap.keySet();
        String next;
        for (String s : keys) {
            next = prefs.getString(s, "");
            if (next.length() > 0) {

                Log.i("Device in SharedPreferences: ", next);
            }
        }

    }
    public void onDeviceChangeMade(AutomatedDevice newDev, AutomatedDevice oldDev){
        testSharedPrefsDevices();
        logSharedPrefsDevices();
        SharedPreferences prefs = this.getSharedPreferences("distributed.directions.saved.devices", 0);
        if (prefs.contains(oldDev.getName())){
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(oldDev.getName());
            editor.putString(newDev.getName(), newDev.delimitedRep());
            boolean success = editor.commit();
            if (!success){
                Log.i("ERROR:  ", "Failed to update shared preferences");
            }


        } else {
            Log.i("ERROR: ", "Shared Prefs did not have " + oldDev.getName());
        }
        AutomatedDeviceListFragment frag = (AutomatedDeviceListFragment) getSupportFragmentManager().findFragmentById(R.id.automateddevice_list);
        frag.updateDevAdapter();

    }

}
