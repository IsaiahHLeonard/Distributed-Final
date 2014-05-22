package com.distributed.directions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Simple class to store information for saved locations to give data persistence.
 */


public class SavedLocation {
    //Simple class variables/data
    String name;
    Double lat, lon;
    String automatedActivity;
    boolean isVisited;

    /**
     * An array of SavedLocations
     */
    public static List<SavedLocation> LOCS = new ArrayList<SavedLocation>();

    /**
     * A map of Saved Locations, key is name, value is semi-colon delimited representation
     */
    public static Map<String, SavedLocation> LOCATION_MAP = new HashMap<String, SavedLocation>();


    //Sample locations
    static {
        addItem(new SavedLocation("Home", 42.709461, -73.193278));

        addItem(new SavedLocation("Work", 42.722100, -73.196669));

        addItem(new SavedLocation("Williams", 42.710849, -73.206668, "Light Switch"));

        addItem(new SavedLocation("Morgan", 42.712051, -73.204737, "Microwave"));
    }

    //Simple methods to add,remove,or edit saved locations
    private static void addItem(SavedLocation loc) {
        LOCS.add(loc);
        LOCATION_MAP.put(loc.getName(), loc);
    }
    private static void addItem(SavedLocation loc, int index){
        LOCS.add(index, loc);
        LOCATION_MAP.put(loc.getName(), loc);
    }

    public static void removeLoc(SavedLocation toRemove){
        LOCS.remove(toRemove);
        LOCATION_MAP.remove(toRemove.getName());
    }
    public static boolean updateMap(SavedLocation newLoc, SavedLocation oldLoc){
        if (!newLoc.getName().equals(oldLoc.getName()) && LOCATION_MAP.containsKey(newLoc.getName())){
            return false;
        }
        int index = LOCS.indexOf(oldLoc);
        removeLoc(oldLoc);
        addItem(newLoc, index);
        return true;
    }
    public static boolean addLoc(SavedLocation newLoc){
        if (LOCATION_MAP.containsKey(newLoc.getName())){
            return false;
        }

        addItem(newLoc);
        return true;
    }

    //Multiple constructors for variable number of parameters
    public SavedLocation(String n, Double la, Double lo){
        name = n;
        lat = la;
        lon = lo;
        automatedActivity = "None";
    }

    public SavedLocation(String n, Double la, Double lo, String activity){
        name = n;
        lat = la;
        lon = lo;
        automatedActivity = activity;
    }
    //make SavedLocation from delimited version
    public SavedLocation(String delimitedLoc){
        StringTokenizer tok = new StringTokenizer(delimitedLoc, ";");
        name = tok.nextToken();
        lat = Double.parseDouble(tok.nextToken());
        lon = Double.parseDouble(tok.nextToken());
        if (tok.hasMoreTokens()){
            automatedActivity = tok.nextToken();
        } else {
            automatedActivity = "None";
        }
    }

    public String getName(){
        return name;
    }
    public Double getLongitude(){
        return lon;
    }
    public Double getLatitude(){
        return lat;
    }
    public String getAutomatedActivity() { return automatedActivity; }
    public void setName(String newName){ name = newName; }
    public void setAutomatedActivity(String newActivity){ automatedActivity = newActivity; }
    public String delimitedRep() { return (name + ";" + lat.toString() + ";" + lon.toString() + ";" + automatedActivity ); }
    //These methods are used so we know whether or not to trigger a geofence
    public boolean isVisited(){return isVisited;}
    public void setIsVisited(boolean isVisited){
        this.isVisited = isVisited;
    }
}
