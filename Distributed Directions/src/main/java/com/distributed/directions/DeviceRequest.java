package com.distributed.directions;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class DeviceRequest extends AsyncTask<String, Void, String> {

    private static final int SERVERPORT = 9999;
    private static final String SERVER_IP = "137.165.9.105";

    @Override
    protected String doInBackground(String... input) {
        Log.i("DeviceRequest", "in Background: " + input[0] + " " + input[1] + " " +input[2]);

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
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())), true);
            out.println(command);

            //Wait between sending message and reading input
            SystemClock.sleep(1000);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            response = in.readLine();

            if (command.equals("setup")){
                Log.i("DeviceRequest", "Setting up new Device");
                if(response.startsWith("OK:")){
                    //format of response is: "OK: <semi-colon delimited device states"
                    response = response.substring(response.indexOf(" ")+1);


                } else {
                    //Toast.makeText(this, "Bad setup connection", Toast.LENGTH_LONG).show();
                    Log.i("DeviceRequest", "Setup connection error");
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