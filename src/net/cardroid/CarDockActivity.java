package net.cardroid;

import android.content.Intent;
import android.os.Bundle;
import roboguice.activity.GuiceActivity;

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
