package net.cardroid.android;

import android.bluetooth.BluetoothDevice;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;

import java.lang.reflect.Method;

/**
 * Date: May 15, 2010
 * Time: 5:31:30 PM
 *
 * @author Lex Nikitin
 */
public class BluetoothA2dpService {
    private static String TAG = "BluetoothA2dpService";
    private final Object mBluetoothA2dpService;

    public BluetoothA2dpService() {
        Object bluetoothA2dpService = null;
        try {
            Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
            Method getService = serviceManagerClass.getDeclaredMethod("getService", String.class);
            bluetoothA2dpService = getService.invoke(null, "bluetooth_a2dp");

            Class<?> serviceClassStub = Class.forName("android.bluetooth.IBluetoothA2dp$Stub");
            Method asInterface = serviceClassStub.getDeclaredMethod("asInterface", IBinder.class);
            bluetoothA2dpService = asInterface.invoke(null, (IBinder)bluetoothA2dpService);
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        mBluetoothA2dpService = bluetoothA2dpService;        
    }

    public boolean connectSink(BluetoothDevice device) {
        try {
            Method declaredMethod = mBluetoothA2dpService.getClass().getDeclaredMethod("connectSink", BluetoothDevice.class);
            return (Boolean)declaredMethod.invoke(mBluetoothA2dpService, device);
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
            return false;
        }
    }
}
