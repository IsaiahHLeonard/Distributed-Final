package com.distributed.directions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import com.distributed.directions.SavedLocation;

/**
 * An activity representing a single SavedLocation detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link com.distributed.directions.SavedLocationListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link com.distributed.directions.SavedLocationDetailFragment}.
 */
public class SavedLocationDetailActivity extends ActionBarActivity implements SavedLocationDetailFragment.SavedLocDetailFragListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savedlocation_detail);

        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(SavedLocationDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(SavedLocationDetailFragment.ARG_ITEM_ID));
            SavedLocationDetailFragment fragment = new SavedLocationDetailFragment();

            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.savedlocation_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, SavedLocationListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onLocationChangeMade(SavedLocation newLoc, SavedLocation oldLoc){
        Log.i("Dog", "cat");
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
        //SavedLocationListFragment frag = (SavedLocationListFragment) getSupportFragmentManager().findFragmentById(R.id.savedlocation_list);
        //frag.updateLocAdapter();
    }
}
