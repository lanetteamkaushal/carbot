package net.cardroid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.google.inject.Inject;
import net.cardroid.can.Can232Adapter;
import net.cardroid.car.CarConnection;
import net.cardroid.io.*;
import roboguice.service.GuiceService;

import java.io.IOException;

/**
 * Date: Apr 11, 2010
 * Time: 1:27:03 PM
 *
 * @author Lex Nikitin
 */
public class CardroidService extends GuiceService {
    private static final String TAG = "CardroidService";
    private static final String PREFS_CARDROID = "CARDROID";
    private static final String IS_FAKE = "isFake";
    private static final String DEFAULT_ADAPTER = "defaultAdapter";

    @Inject private DeviceConnectionService mDeviceConnectionService;
    @Inject private KeyDispatcher mKeyDispatcher;
    @Inject Can232Adapter mCanAdapter;
    @Inject CarConnection mCarConnection;

    private ConnectionListener mConnectionListener;
    private boolean mIsFake;

    @Override public IBinder onBind(Intent intent) {
        return new CardroidServiceBinder();
    }

    @Override public void onCreate() {
        super.onCreate();

        mCanAdapter.attachTo(mDeviceConnectionService);
        mCarConnection.attachTo(mCanAdapter);

        mKeyDispatcher.attach();

        mConnectionListener = new ConnectionListenerAdapter() {
            @Override public void connected(DeviceConnector deviceConnector) {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

                //mBluetoothA2dpService.connectSink(BluetoothAdapter.getDefaultAdapter().getRemoteDevice("00:17:53:12:2F:96"));
            }
        };
        mDeviceConnectionService.addListener(mConnectionListener);

        mIsFake = getIsFake();
//        PackageManager packageManager = getPackageManager();
//        List<ResolveInfo> infoList = packageManager.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
//        if (infoList.size() > 0) {
//            mCarConnection.subscribe(EventType.DIAL, new EventHandler() {
//                @Override public void handle(Event e) {
//                    mUiHandler.post(new Runnable() {
//                        @Override public void run() {
//                            Intent intent = new Intent(CardroidService.this, VoiceRecognitionActivity.class);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);
//                        }
//                    });
//                }
//            });
//        }
//        Intent intent = new Intent(this, AppLauncherDialog.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
    }

    private boolean getIsFake() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_CARDROID, 0);
        return sharedPreferences.getBoolean(IS_FAKE, false);
    }

    private String getDefaultAdapter() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_CARDROID, 0);
        return sharedPreferences.getString(DEFAULT_ADAPTER, null);
    }

    @Override public void onDestroy() {
        super.onDestroy();
        mDeviceConnectionService.removeListener(mConnectionListener);
    }

    private DeviceConnector createDeviceConnector(String deviceAddress) throws IOException {
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
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            deviceConnector = new BluetoothDeviceConnector(device);
        }

        return deviceConnector;
    }

    private void startConnection(String deviceAddress) {
        try {
	        mDeviceConnectionService.setDeviceConnector(createDeviceConnector(deviceAddress));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        mDeviceConnectionService.enableConnectionAttempts();
    }

    public void setIsFake(boolean fake) {
    	mIsFake = fake;

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_CARDROID, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_FAKE, fake);
        editor.commit();
    }

    public void setDefaultAdapter(String defaultAdapter) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_CARDROID, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DEFAULT_ADAPTER, defaultAdapter);
        editor.commit();
    }

    public class CardroidServiceBinder extends Binder implements ICardroidService {
        @Override public void connectTo(String deviceAddress) {
            setDefaultAdapter(deviceAddress);
            startConnection(deviceAddress);
        }

        @Override public void setIsFake(boolean isFake) {
            CardroidService.this.setIsFake(isFake);
        }

        @Override public String getDefaultAdapter() {
            return CardroidService.this.getDefaultAdapter();
        }

        @Override public boolean isFake() {
            return mIsFake;
        }
    }
}
