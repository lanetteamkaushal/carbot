package net.cardroid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.ComponentName;
import android.os.Parcelable;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.widget.ListView;
import android.widget.TextView;
import com.google.inject.Inject;
import net.cardroid.car.CarConnection;
import net.cardroid.car.CloseAllScript;
import net.cardroid.car.OpenAllScript;
import roboguice.activity.GuiceActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;

import com.google.common.collect.Maps;
import roboguice.inject.InjectView;

/**
* Date: Apr 11, 2010
* Time: 10:12:58 PM
*
* @author Lex Nikitin
*/
public class VoiceRecognitionActivity extends GuiceActivity {
    private static String TAG = "VoiceRecognitionActivity";
    
    private static final String START_APP_COMMAND = "start ";
    private static final String OPEN_ALL_COMMAND = "open windows";
    private static final String CLOSE_ALL_COMMAND = "close windows";
    private static final String LIST_STATIONS_COMMAND = "list stations";
    private static final String LIST_STATIONS_PACKAGE = "com.pandora.android";
    private static final String LIST_STATIONS_CLASS = ".activity.StationListActivity";
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    @Inject private CarConnection mCarConnection;
    @InjectView(R.id.status_text) private TextView mStatusText;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice_recognition);
    }

    @Override protected void onResume() {
        super.onResume();
        
        SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle bundle) {
            	Log.i(TAG, "onReadyForSpeech");
                mStatusText.setText("Ready for speech");
            }

            @Override public void onBeginningOfSpeech() {
                Log.i(TAG, "onBeginningOfSpeech");
            }

            @Override public void onRmsChanged(float v) {
                Log.i(TAG, "onRmsChanged");
            }

            @Override public void onBufferReceived(byte[] bytes) {
                Log.i(TAG, "onBufferReceived");
            }

            @Override public void onEndOfSpeech() {
                Log.i(TAG, "onEndOfSpeech");
            }

            @Override public void onError(int i) {
                Log.i(TAG, "onError");
                mStatusText.setText("Error");
                finish();
            }

            @Override public void onResults(Bundle bundle) {
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                Log.i(TAG, "onResults " + matches);
                mStatusText.setText("Results : " + matches);
                if (matches.size() > 0) {
                    for (String match : matches) {
                        if (match.startsWith(START_APP_COMMAND)) {
                            String appName = match.substring(START_APP_COMMAND.length());

                            ResolveInfo applicationToStart = buildAllAppsMap(getPackageManager()).get(appName);

                            if (applicationToStart != null) {
                                startApplication(applicationToStart);
                            }
                        } else if (match.equals(LIST_STATIONS_COMMAND)) {
                            startActivity(new Intent().setClassName(LIST_STATIONS_PACKAGE, LIST_STATIONS_CLASS));
                        } else if (match.equals(CLOSE_ALL_COMMAND)) {
                            CloseAllScript.run(mCarConnection);
                        } else if (match.equals(OPEN_ALL_COMMAND)) {
                            OpenAllScript.run(mCarConnection);
                        }
                    }
                }
                finish();
            }

            @Override public void onPartialResults(Bundle bundle) {
                ArrayList<String> recognitionResults = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                mStatusText.setText("Partial results : " + recognitionResults);
                Log.i(TAG, "onPartialResults " + recognitionResults);
                finish();
            }

            @Override public void onEvent(int i, Bundle bundle) {
                Log.i(TAG, "onEvent");
                finish();
            }
        });
        Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, "true");
        intent.putExtra("car_dock", true);
        intent.putExtra("fullRecognitionResults", true);
        speechRecognizer.startListening(intent);
    }

    @Override protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    private Map<String, ResolveInfo> buildAllAppsMap(PackageManager packageManager) {
        Map<String, ResolveInfo> allAppsMap = Maps.newHashMap();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> allApps = packageManager.queryIntentActivities(mainIntent, 0);
        for (ResolveInfo resolveInfo : allApps) {
            String appName = resolveInfo.loadLabel(packageManager).toString();
            if (appName.length() == 0) {
                appName = resolveInfo.activityInfo.name;
            }
            allAppsMap.put(appName.toLowerCase(), resolveInfo);
        }
        return allAppsMap;
    }

    private void startApplication(ResolveInfo resolveInfo) {
        Intent intent = new Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            .setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
        startActivity(intent);
    }
}
