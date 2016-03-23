### Project goal ###
Provide an extendable library and utility applications that communicate with car equipment.
This at least includes APIs to read speed, rpms, positions of windows, doors, etc; as well as to open/close windows, lock/unlock doors, trunk, sunroof, etc from an android phone.

### Current state ###
Carbot is a relatively unstable application that integrates with Mini Cooper S [R56](https://code.google.com/p/carbot/source/detail?r=56) and provides following functions.
  * Voice commands like "open windows", "close windows" (**car windows**)
  * Use steering wheel buttons to navigate within applications, switch applications, control media player and Pandora
  * Steering wheel activated voice search
  * Extra functionality for existing buttons, such as triple click to open both windows and roof altogether

### Basic idea ###
Carbot uses [CAN232 adapter](http://www.can232.com/) and a [Bluetooth serial adapter](http://www.google.com/search?hl=en&q=bluetooth+serial+adapter&aq=f&aqi=g6g-m3&aql=&oq=&gs_rfai=) to connect to KCAN bus of a Mini Cooper. Phone uses [RFCOMM Bluetooth API](http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createRfcommSocketToServiceRecord(java.util.UUID)) to connect to CAN 232, then it initializes CAN 232 adapter and continuously reads events on the CAN bus.


### Setup ###
First you will need to [install CAN 232 adapter](CAN232attach.md)

Start the application. **Note** application will start automatically when you put your phone into a car dock.
During the first run it will open bluetooth device picker. Select you bluetooth adapter.

Once connected
  * prev/next track buttons on MFSW change tracks
  * single click on the phone button (a button labelled with phone on MFSW) allows to invoke voice commands "windows open/close"
  * double click on the phone button starts voice search. I mostly use it for navigation. You can say "navigate to 1600 Amphiteathre parkway, Mountain View"
  * triple click on the open/close driver's window switch will open/close both windows and roof
  * triple click on the phone button will switch into "d-pad mode", next/prev track and volume buttons will now work as cursor buttons on the phone. Single press on the phone button - 'enter'. Long press - 'home'.

**Important** For "d-pad mode" you will need to re-flash your phone to give INJECT\_EVENTS permission to Cardroid application. This permission is for internal use only and default system never gives the permission to regular applications.

TODO: Explain how to reflash a phone.

[CAN codes for Mini Cooper S](CANcodesMiniCooperS.md)