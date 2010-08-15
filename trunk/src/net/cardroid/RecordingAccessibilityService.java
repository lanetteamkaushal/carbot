package net.cardroid;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import roboguice.service.GuiceService;

/**
 * Date: Apr 12, 2010
 * Time: 11:00:07 PM
 *
 * @author Lex Nikitin
 */
public class RecordingAccessibilityService extends AccessibilityService {
    private static String TAG = "RecordingAccessibilityService";
    
    private AccessibilityManager mAccessibilityManager;
    private AccessibilityEvent mAccessibilityEvent;

    @Override public void onCreate() {
        super.onCreate();
        GuiceService.onCreate(this, getApplication());

        mAccessibilityManager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
    }

    @Override protected void onServiceConnected() {
        super.onServiceConnected();

        AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();
        serviceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL;
        serviceInfo.notificationTimeout = 0;
        serviceInfo.flags = AccessibilityServiceInfo.DEFAULT;
        setServiceInfo(serviceInfo);
    }

    @Override public void onAccessibilityEvent(final AccessibilityEvent accessibilityEvent) {
        Log.d(TAG, accessibilityEvent.toString());
        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
        	if (mAccessibilityEvent == null) {
            	mAccessibilityEvent = accessibilityEvent;
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        mAccessibilityManager.sendAccessibilityEvent(mAccessibilityEvent);
                        mAccessibilityEvent = null;
                    }
                }, 2000);        		
        	}
        }
    }

    @Override public void onInterrupt() {
    }
}
