package net.cardroid;

import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;

public class BluetoothScanController {
	private final Logger log = Logger.getInstance(BluetoothScanController.class.getName());
	
	public BluetoothScanController() {
	}

	public void startScan() {
	    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
	    log.i("Adapter : " + adapter);
	    
	    // Check for Bluetooth support in the first place 
	    // Emulator doesn't support Bluetooth and will return null
	    if(adapter == null) { 
	      log.i("Bluetooth NOT supported. Aborting.");
	      return;
	    }
	    
	    // Starting the device discovery
	    log.i("Starting discovery...");
	    // adapter.startDiscovery();
	    // Booo. I should listen for events instead. 
	    log.i("Done with discovery...");
	
	    // Listing paired devices
	    log.i("Looking up paired RFCOM devices");
	    Set<BluetoothDevice> devices = adapter.getBondedDevices();
	    for (BluetoothDevice device : devices) {
	      if (isRfcomm(device)) {
	          log.i("RFCOM: " + device.getName() + " " + device.getAddress());        	  
	      }
	    } 
	    if (devices.size() == 0) {
	    	log.i("No paired devices");
	    }
	}	
	
    private boolean isRfcomm(BluetoothDevice device) {
    	BluetoothClass bluetoothClass = device.getBluetoothClass();

    	return bluetoothClass != null && bluetoothClass.hasService(BluetoothClass.Service.TELEPHONY); 
    }	
}
