Distributed-Final
=================

The watch apps are in the pebbleDirectionsApp and the pebbleGeofenceApp folders. Inside these folders, the json file is the
general information file for the watch app, so the name and so on. Inside, there is another folder src, and inside this 
folder is the actual C code for the app itself. To run the watch app code, first go to the directory of the app. Then type "pebble build" to build the code. Then type "pebble -install adressOfPhone" to install on the phone. The address of the phone is the address of your phone found in the pebble App on your phone. However, to do this, you need to first install the Pebble SDK on your computer.

The arduinoServer folder contains the code for our arduino.

The rest of the files are the setup and actual files for the android app. The java files for the android, are in the
DistributedDirections/src/main/java/com/distributed/directions folder. The xml files which are the layout and how the
android pages look are in the DistributedDirections/src/main/res/layout folder. We compiled the code and created the SDK using Android Studio, which then transfers the app SDK to the phone

