package net.cardroid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import com.google.inject.Inject;
import com.google.inject.Module;
import net.cardroid.can.Can232Adapter;
import net.cardroid.car.CarConnection;
import net.cardroid.io.BluetoothDeviceConnector;
import net.cardroid.io.DeviceConnectionService;
import net.cardroid.io.DeviceConnector;
import net.cardroid.io.FakeDeviceConnector;
import roboguice.application.GuiceApplication;

import java.io.IOException;
import java.util.List;

public class Cardroid extends GuiceApplication {
    private static String TAG = "CardroidModule";
    private static final String PREFS_CARDROID = "CARDROID";
    private static final String IS_FAKE = "isFake";

    private static final String ADDRESS_MINILEX = "00:50:C2:7F:4D:EC";
	private static final String ADDRESS_LEX = "00:14:A4:D8:33:53";

    @Inject DeviceConnectionService mDeviceConnectionService;
	@Inject Can232Adapter mCanAdapter;
	@Inject CarConnection mCarConnection;

    private boolean mIsFake;
    
    @Override
    protected void addApplicationModules(List<Module> modules) {
        modules.add(new CardroidModule());
    }

    /** Called when application is first created. */
    @Override
    public void onCreate() {
        super.onCreate();
        getInjector().injectMembers(this);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_CARDROID, 0);
        mIsFake = sharedPreferences.getBoolean(IS_FAKE, false);
        
        mCanAdapter.attachTo(mDeviceConnectionService);
        mCarConnection.attachTo(mCanAdapter);

        startService(new Intent(this, CardroidService.class));
        setIsFake(mIsFake);
        mDeviceConnectionService.enableConnectionAttempts();
    }

    public void setIsFake(boolean fake) {
    	mIsFake = fake;
    	
        try {
	        mDeviceConnectionService.setDeviceConnector(createDeviceConnector());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_CARDROID, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_FAKE, fake);
        editor.commit();
    }

    public boolean isFake() {
        return mIsFake;
    }

    public DeviceConnector createDeviceConnector() throws IOException {
        // Get local Bluetooth adapter
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        DeviceConnector deviceConnector;

        // If the adapter is null, then Bluetooth is not supported
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available. Using fake adapter.", Toast.LENGTH_LONG).show();
            mIsFake = true;
        }

        if (mIsFake) {
            deviceConnector = new FakeDeviceConnector();
        } else {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(ADDRESS_MINILEX);
            deviceConnector = new BluetoothDeviceConnector(device);
        }
        
        return deviceConnector;
    }
}