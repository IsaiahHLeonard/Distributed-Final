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
    private class AsyncRequest extends AsyncTask<String, Void, String> {

        private static final int SERVERPORT = 9999;
        private static final String SERVER_IP = "137.165.9.105";

        @Override
        protected String doInBackground(String... input) {
            Log.i("AsyncRequest", "in Background");

            return executeRequest(input[0], Integer.parseInt(input[1]), input[2]);
            //return null;
        }

        //executes background request to arduino server
        //in this case, sending message to setup, trying to get possible device states
        private String executeRequest(String ipAddress, int portNumber, String command) {

            String response = "";
            try {
                InetAddress serverAddr = InetAddress.getByName(ipAddress);
                Socket socket = new Socket(serverAddr, portNumber);
                Log.e("OnOff: ", command);
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
                out.println(command);

                //Wait between sending message and reading input
                SystemClock.sleep(1000);

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                response = in.readLine();

                if (command.equals("setup")){
                    Log.i("AsyncRequest", "Setting up new Device");
                    if(response.startsWith("OK:")){
                        //format of response is: "OK: <semi-colon delimited device states"
                        response = response.substring(response.indexOf(" ")+1);


                    } else {
                        //Toast.makeText(this, "Bad setup connection", Toast.LENGTH_LONG).show();
                        Log.i("AsyncRequest", "Setup connection error");
                    }
                }
                in.close();
                out.close();
                socket.close();
            } catch (Exception e) {
                Log.e("Socket error: ", e.toString());
            }
            return response;

        }

    }

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

                DeviceRequest request = new DeviceRequest();
                try {
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
                    //Todo: Knock us back to StartScreenActivity
                    Intent deviceListIntent = new Intent(this, AutomatedDeviceListActivity.class);
                    startActivity(deviceListIntent);
                } else {
                    Toast.makeText(this, "Server setup failed", Toast.LENGTH_LONG).show();
                    Log.i("onClick", "Server setup failed");
                }

            }
        }

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
