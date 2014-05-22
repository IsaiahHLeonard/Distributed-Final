package com.distributed.directions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/* This class represents an activity which allows the user to create a new device by giving an ip address
* and port number. A setup message is sent and the reply contains possible device states. The new device is saved.
 */
public class CreateAutomatedDevice extends ActionBarActivity implements View.OnClickListener{
    private Button submitButton;
    private String serverResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_automated);
        submitButton = (Button) findViewById(R.id.submit_device_button);
        submitButton.setOnClickListener(this);

    }

    //When the user clicks submit new device, we must first make sure that the parameters they have given are appropriate. If they are
    //then communicate with device to set it up/get possible device states
    public void onClick(View v) {
        String address = ((EditText) findViewById(R.id.new_address)).getText().toString();
        String name = ((EditText) findViewById(R.id.new_activity_name)).getText().toString();
        String portNum = ((EditText) findViewById(R.id.port_number)).getText().toString();
        serverResponse = "";
        if (address.length() == 0) {
            Toast.makeText(this, "Address must not be blank", Toast.LENGTH_LONG).show();
        } else if (name.length() == 0) {
            Toast.makeText(this, "Name must not be blank", Toast.LENGTH_LONG).show();
        } else if (portNum.length() == 0){
            Toast.makeText(this, "Port number must not be blank", Toast.LENGTH_LONG).show();
        } else if (! portNum.matches("[0-9]+")){
            Toast.makeText(this, "Port number must be an integer", Toast.LENGTH_LONG).show();
        } else if (AutomatedDevice.exists(name)) {
            Toast.makeText(this, "Name must not be the same as another device", Toast.LENGTH_LONG).show();
        } else {

                //new background request task to communicate with arduino
                DeviceRequest request = new DeviceRequest();
                try {
                    //Limit time to 3 seconds. If longer, likely an error of some sort
                    serverResponse = request.execute(address, portNum, "setup").get(3000, TimeUnit.MILLISECONDS);
                } catch (Exception e){
                    Log.e("onClick", "Error getting server response: " + e.toString());
                }

                if (serverResponse.length() > 0){
                    //We got server response, valid server --> create automated device and save to sharedprefs
                    AutomatedDevice newDevice = new AutomatedDevice(name+";"+address+";"+portNum+";"+serverResponse);
                    Log.i("New Device", newDevice.delimitedRep());
                    AutomatedDevice.addDevice(newDevice);
                    if(!writeDevToSharedPrefs(newDevice)){

                        Toast.makeText(this, "Error writing new device to shared prefs", Toast.LENGTH_LONG).show();
                    } else {

                    }
                    //Knock us back to list of devices
                    Intent deviceListIntent = new Intent(this, AutomatedDeviceListActivity.class);
                    startActivity(deviceListIntent);
                } else {
                    Toast.makeText(this, "Server setup failed", Toast.LENGTH_LONG).show();
                    Log.i("onClick", "Server setup failed");
                }

            }
        }

    //Write new device to shared preferences
    private Boolean writeDevToSharedPrefs(AutomatedDevice device) {
        SharedPreferences prefs = this.getSharedPreferences("distributed.directions.saved.devices", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(device.getName(), device.delimitedRep());
        if (! editor.commit()){
            return false;
        }
        return true;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_automated, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
