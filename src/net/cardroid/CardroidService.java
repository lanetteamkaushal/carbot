package net.cardroid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;

import android.util.Log;
import com.google.inject.Inject;
import net.cardroid.android.BluetoothA2dpService;
import net.cardroid.io.ConnectionListener;
import net.cardroid.io.ConnectionListenerAdapter;
import net.cardroid.io.DeviceConnectionService;
import net.cardroid.io.DeviceConnector;
import roboguice.service.GuiceService;

import java.util.Iterator;

/**
 * Date: Apr 11, 2010
 * Time: 1:27:03 PM
 *
 * @author Lex Nikitin
 */
public class CardroidService extends GuiceService {
    private static final String TAG = "CardroidService";

    @Inject private DeviceConnectionService mDeviceConnectionService;
    @Inject private KeyDispatcher mKeyDispatcher;
    @Inject private BluetoothA2dpService mBluetoothA2dpService;

    private ConnectionListener mConnectionListener;

    @Override public IBinder onBind(Intent intent) {
        return null;
    }

    @Override public void onCreate() {
        super.onCreate();

        mKeyDispatcher.attach();

        mConnectionListener = new ConnectionListenerAdapter() {
            @Override public void connected(DeviceConnector deviceConnector) {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

                mBluetoothA2dpService.connectSink(BluetoothAdapter.getDefaultAdapter().getRemoteDevice("00:17:53:12:2F:96"));
            }
        };
        mDeviceConnectionService.addListener(mConnectionListener);

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

    @Override public void onDestroy() {
        super.onDestroy();
        mDeviceConnectionService.removeListener(mConnectionListener);
    }
}
