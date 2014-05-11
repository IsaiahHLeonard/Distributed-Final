package com.distributed.directions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.distributed.directions.SavedLocation;
import com.distributed.directions.SavedLocationDetailFragment;

/**
 * An activity representing a list of SavedLocations. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link com.distributed.directions.SavedLocationDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link SavedLocationListFragment} and the item details
 * (if present) is a {@link com.distributed.directions.SavedLocationDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link SavedLocationListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class SavedLocationListActivity extends FragmentActivity
        implements SavedLocationListFragment.Callbacks, SavedLocationDetailFragment.SavedLocDetailFragListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    public ArrayList<SavedLocation> savedLocs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savedlocation_list);
        //Read in SharedPreferences to get SavedLocations. Then load into ArrayList
        this.loadLocations();

        //Test
        testSharedPrefsLocations();
        logSharedPrefsLocations();


        if (findViewById(R.id.savedlocation_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((SavedLocationListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.savedlocation_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link SavedLocationListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(SavedLocationDetailFragment.ARG_ITEM_ID, id);
            SavedLocationDetailFragment fragment = new SavedLocationDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.savedlocation_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, SavedLocationDetailActivity.class);
            detailIntent.putExtra(SavedLocationDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    private void loadLocations() {
        SharedPreferences prefs = this.getSharedPreferences("distributed.directions.saved.locs", 0);
        Map<String, ?> locMap = prefs.getAll();
        Set<String> keys = locMap.keySet();
        String next;
        for (String s : keys) {
            next = prefs.getString(s, "");
            if (next.length() > 0) {

                SavedLocation.addLoc(new SavedLocation(next));
            }
        }
    }

    private void testSharedPrefsLocations(){
        SharedPreferences prefs = this.getSharedPreferences("distributed.directions.saved.locs", 0);
        SharedPreferences.Editor editor = prefs.edit();
        for (SavedLocation loc : SavedLocation.LOCS){
            editor.putString(loc.getName(), loc.delimitedRep());
        }
        if (! editor.commit()){
            Log.i("ERROR:  ", "Failed in testSharedPrefsLocations");
        }
    }

    private void logSharedPrefsLocations(){
        Log.i("BLAC", "BLAH");
        SharedPreferences prefs = this.getSharedPreferences("distributed.directions.saved.locs", 0);
        Map<String, ?> locMap = prefs.getAll();
        Set<String> keys = locMap.keySet();
        String next;
        for (String s : keys) {
            next = prefs.getString(s, "");
            if (next.length() > 0) {

                Log.i("Location in SharedPreferences: ", next);
            }
        }

    }
    public void onLocationChangeMade(SavedLocation newLoc, SavedLocation oldLoc){
        Log.i("Cat", "cat");
        testSharedPrefsLocations();
        logSharedPrefsLocations();
        SharedPreferences prefs = this.getSharedPreferences("distributed.directions.saved.locs", 0);
        if (prefs.contains(oldLoc.getName())){
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(oldLoc.getName());
            editor.putString(newLoc.getName(), newLoc.delimitedRep());
            boolean success = editor.commit();
            if (!success){
                Log.i("ERROR:  ", "Failed to update shared preferences");
            }
            

        } else {
            Log.i("ERROR: ", "Shared Prefs did not have " + oldLoc.getName());
        }
        SavedLocationListFragment frag = (SavedLocationListFragment) getSupportFragmentManager().findFragmentById(R.id.savedlocation_list);
        frag.updateLocAdapter();

    }
}
