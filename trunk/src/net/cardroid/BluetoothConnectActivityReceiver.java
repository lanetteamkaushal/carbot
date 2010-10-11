package net.cardroid;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import net.cardroid.android.BluetoothDevicePicker;

/**
* Date: Oct 11, 2010
* Time: 12:39:01 AM
*
* @author Lex Nikitin
*/
public class BluetoothConnectActivityReceiver extends BroadcastReceiver {
    private BluetoothConnectActivity bluetoothConnectActivity;

    public BluetoothConnectActivityReceiver(BluetoothConnectActivity bluetoothConnectActivity) {
        this.bluetoothConnectActivity = bluetoothConnectActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent)  {
        if(BluetoothDevicePicker.ACTION_DEVICE_SELECTED.equals(intent.getAction())) {
            context.unregisterReceiver(this);
            BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            bluetoothConnectActivity.connectToService(device.getAddress());
        }
    }
}
