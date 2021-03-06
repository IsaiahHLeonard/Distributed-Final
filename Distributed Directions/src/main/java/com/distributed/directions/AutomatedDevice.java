package com.distributed.directions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by isaiahleonard on 5/4/14.
 * Simple abstraction for an automated device that stores necessary information to communicate with the Arduino.
 */
public class AutomatedDevice {

    //Variables to store data for each Automated Device
    String name;
    String address;
    ArrayList<String> possibleStates;
    int portNum;

    //Structures to contain the set of automated devices that have been configured for the app
    public static ArrayList<AutomatedDevice> DEVICE_LIST = new ArrayList<AutomatedDevice>();
    public static Map<String, AutomatedDevice> DEVICE_MAP = new HashMap<String, AutomatedDevice>();

    //Some sample devices - fake ip
    static{

        addDev(new AutomatedDevice("Light Switch", "cat.com", 999, "On", "Off"));
        addDev(new AutomatedDevice("Toaster", "dog.com", 12, "High", "Medium", "Low"));

    }

    //Simple constructor. Handles variable number of possible states
    public AutomatedDevice(String n, String a, int port, String... possible){
        name = n;
        address = a;
        portNum = port;
        possibleStates = new ArrayList<String>();
        if (possible.length > 0) {
            for (String poss : possible) {
                possibleStates.add(poss);
            }
        }
    }
    //Constructor that takes semi-colon delimited representation as input.
    public AutomatedDevice(String delimitedRep){
        StringTokenizer tok = new StringTokenizer(delimitedRep, ";");
        name = tok.nextToken();
        address = tok.nextToken();
        portNum = Integer.parseInt(tok.nextToken());
        possibleStates = new ArrayList<String>();
        while (tok.hasMoreTokens()){
            possibleStates.add(tok.nextToken());
        }
    }

    //Helper/accessor methods to add or remove devices to/from the list & map
    private static void addDev(AutomatedDevice toAdd){
        if(!exists(toAdd.getName())) {
            DEVICE_LIST.add(toAdd);
            DEVICE_MAP.put(toAdd.getName(), toAdd);
        }
    }
    private static void addDev(AutomatedDevice toAdd, int index){
        if(!exists(toAdd.getName())) {
            DEVICE_LIST.add(index, toAdd);
            DEVICE_MAP.put(toAdd.getName(), toAdd);
        }
    }
    private static void removeDevice(AutomatedDevice toRemove){
        DEVICE_LIST.remove(toRemove);
        DEVICE_MAP.remove(toRemove.getName());
    }
    public static void updateMap(AutomatedDevice newDev, AutomatedDevice oldDev) {
        int index = DEVICE_LIST.indexOf(oldDev);
        removeDevice(oldDev);
        addDev(newDev, index);
    }


    public static boolean exists(String name){
        if (DEVICE_MAP.containsKey(name)) {return true;}
        else {return false;}
    }

    //public method to add new device
    public static void addDevice(AutomatedDevice newDev) {addDev(newDev);}

    //Simple accessor methods
    public String getName(){
        return name;
    }
    public String getAddress(){
        return address;
    }
    public int getPortNum() { return portNum; }
    public String getStates(){
        StringBuilder result = new StringBuilder();
        for (String s : possibleStates){
            result.append(s + ";");
        }
        return result.substring(0,result.length()-1);
    }
    public ArrayList<String> getStateList(){
        return possibleStates;
    }
    public void setPossibleStates(ArrayList<String> states){
        possibleStates.addAll(states);
    }
    public String delimitedRep(){ return (name + ";" + address + ";" + portNum + ";" + getStates());}
}
