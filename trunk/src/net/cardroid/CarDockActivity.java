package net.cardroid;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.inject.Inject;
import net.cardroid.android.BluetoothA2dpService;
import net.cardroid.io.DeviceConnectionService;
import roboguice.activity.GuiceActivity;

import java.io.IOException;

/**
 * Date: Apr 20, 2010
 * Time: 10:16:05 PM
 *
 * @author Lex Nikitin
 */
public class CarDockActivity extends GuiceActivity {
    private static String TAG = "CarDockActivity";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, CanAnalysisActivity.class));
        finish();
    }
}
