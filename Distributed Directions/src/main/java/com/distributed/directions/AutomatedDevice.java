package com.distributed.directions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple abstraction for an automated device that stores necessary information to communicate with the Arduino.
 */
public class AutomatedDevice {
    String name;
    String address;
    ArrayList<String> possibleStates;

    public static ArrayList<AutomatedDevice> DEVICE_LIST = new ArrayList<AutomatedDevice>();
    public static Map<String, AutomatedDevice> DEVICE_MAP = new HashMap<String, AutomatedDevice>();

    static{
        addDev(new AutomatedDevice("None", "", ""));
        addDev(new AutomatedDevice("Light Switch", "cat.com", "On", "Off"));
        addDev(new AutomatedDevice("Toaster", "dog.com", "High", "Medium", "Low"));

    }

    public AutomatedDevice(String n, String a, String... possible){
        name = n;
        address = a;
        possibleStates = new ArrayList<String>();
        if (possible.length > 0) {
            for (String poss : possible) {
                possibleStates.add(poss);
            }
        }
    }

    private static void addDev(AutomatedDevice toAdd){
        DEVICE_LIST.add(toAdd);
        DEVICE_MAP.put(toAdd.getName(), toAdd);
    }

    private static void addDev(AutomatedDevice toAdd, int index){
        DEVICE_LIST.add(index, toAdd);
        DEVICE_MAP.put(toAdd.getName(), toAdd);
    }
    private static void removeDevice(AutomatedDevice toRemove){
        DEVICE_LIST.remove(toRemove);
        DEVICE_MAP.remove(toRemove.getName());
    }
    public static void updateMap(AutomatedDevice newDev, AutomatedDevice oldDev){
        int index = DEVICE_LIST.indexOf(oldDev);
        removeDevice(oldDev);
        addDev(newDev, index);
    }
    public static void addDevice(AutomatedDevice newDev) {addDev(newDev);}
    public String getName(){
        return name;
    }
    public String getAddress(){
        return address;
    }
    public String getStates(){
        StringBuilder result = new StringBuilder();
        for (String s : possibleStates){
            result.append(s + "-");
        }
        return result.substring(0,result.length()-1);
    }
}
