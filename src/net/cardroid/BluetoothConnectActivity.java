package net.cardroid;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.google.inject.Inject;
import net.cardroid.android.BluetoothDevicePicker;
import net.cardroid.can.Can232Adapter;
import net.cardroid.io.*;
import roboguice.activity.GuiceActivity;
import roboguice.inject.InjectView;

public class BluetoothConnectActivity extends GuiceActivity {
    // Debugging
    private static final String TAG = "BluetoothConnectActivity";
    private static final boolean D = true;

    // Intent request codes
    private static final int REQUEST_ENABLE_BT = 2;

    @Inject private Cardroid mCardroid;
	@Inject private DeviceConnectionService mDeviceConnectionService;
	@InjectView(R.id.status_text_view) TextView mStatusTextView;

    private ConnectionListener mBluetoothListener;
    private final BroadcastReceiver mBluetoothPickerReceiver = new BluetoothConnectActivityReceiver(this);

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.bluetooth_connect);
        
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        mBluetoothListener = new ConnectionListenerForHandler(createBluetoothListener(), new Handler());
        mDeviceConnectionService.addListener(mBluetoothListener);

        if (mCardroid.getCardroidService() == null) {
            bindService(new Intent(this, CardroidService.class), new ServiceConnection() {
                @Override public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    mCardroid.setCardroidService((ICardroidService) iBinder);
                    connectToService(BluetoothConnectActivity.this.mCardroid.getCardroidService().getDefaultAdapter());
                }

                @Override public void onServiceDisconnected(ComponentName componentName) {
                    mCardroid.setCardroidService(null);
                    Log.e(TAG, "Unexpectedly disconnected from CardroidService.");
                }
            }, Context.BIND_AUTO_CREATE);
        } else {
            connectToService(mCardroid.getCardroidService().getDefaultAdapter());
        }        
    }
    	
    @Override public void onStart() {
        super.onStart();
        if(D) Log.i(TAG, "++ ON START ++");
    }

    void connectToService(String defaultAdapter) {
        if (defaultAdapter == null) {
            registerReceiver(mBluetoothPickerReceiver, new IntentFilter(BluetoothDevicePicker.ACTION_DEVICE_SELECTED));
            startActivity(new Intent(BluetoothDevicePicker.ACTION_LAUNCH)
                .putExtra(BluetoothDevicePicker.EXTRA_NEED_AUTH, false)
                .putExtra(BluetoothDevicePicker.EXTRA_FILTER_TYPE, BluetoothDevicePicker.FILTER_TYPE_ALL)
                .setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS));
        } else {
            mCardroid.getCardroidService().connectTo(defaultAdapter);
        }
    }

    @Override protected void onDestroy() {
    	super.onDestroy();
        mDeviceConnectionService.removeListener(mBluetoothListener);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.bluetooth_connect_menu, menu);
	    return true;
	}	
	
    @Override public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.fake_on_menu).setChecked(mCardroid.isFake());
        return true;
    }

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
        case R.id.exit_menu:
            finish();
            break;
        case R.id.pick_device_menu:
            connectToService(null);
            break;
        case R.id.fake_on_menu:
            mCardroid.getCardroidService().setIsFake(!item.isChecked());
            break;
		}
		return true;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
/*        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to setDeviceConnector
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to setDeviceConnector to the device
                mChatService.setDeviceConnector(device);
            }
            break;*/
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode != Activity.RESULT_OK) {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private ConnectionListener createBluetoothListener() {
        return new ConnectionListenerAdapter() {
            @Override public void connecting(DeviceConnector deviceConnector) {
                setStatusText("Try to connect to "  + deviceConnector.getName() + "...");
            }

            @Override public void connected(DeviceConnector deviceConnector) {
                onConnected(deviceConnector);
            }

            @Override public void idle(int idleDelay) {
                setStatusText("Waiting for " + (idleDelay / 1000) + " sec");
            }
        };
    }

    private void onConnected(DeviceConnector deviceConnector) {
        setStatusText("Connected to " + deviceConnector.getName());
        finish();
    }

    private void setStatusText(String text) {
    	mStatusTextView.setText(text);
	}

    public static void verifyConnected(Context context, Can232Adapter can232Adapter) {
        if (!can232Adapter.isConnected()) {
            start(context);
        }
    }

    public static void start(Context context) {
        context.startActivity(new Intent(context, BluetoothConnectActivity.class));
    }

}
