package net.cardroid.car;

import android.util.Log;
import com.google.common.annotations.VisibleForTesting;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Date: Apr 22, 2010
 * Time: 12:30:01 AM
 *
 * @author Lex Nikitin
 */
public class OpenAllScript {
    private static String TAG = "OpenAllScript";
    private static final int TIMEOUT = 20000; // 20 seconds
    private static final SimpleEvent OPEN_ALL_EVENT = new SimpleEvent(EventType.ROOF_OPEN_FULL.getDestination(), 
            EventType.ROOF_OPEN_FULL.getIntData().or(EventType.WINDOW_LR_DOWN_STEP_NOPOWER.getIntData()),
            EventType.ROOF_OPEN_FULL.nChars());

    public static void run(CarConnection car) {
        new OpenAllScript(car).run();
    }

    private final CarConnection mCarConnection;
    private final EventHandler<RoofPositionEvent> roofEventHandler;
    private final EventHandler<WindowPositionEvent> windowEventHandler;

    private boolean mWindowsOpen;
    private boolean mRoofOpen;

    @VisibleForTesting OpenAllScript(CarConnection carConnection) {
        mCarConnection = carConnection;
        roofEventHandler = new EventHandler<RoofPositionEvent>() {
            @Override public void handle(RoofPositionEvent e) {
                if (!e.isValid()) return;

                if (e.getAction() == RoofPositionEvent.Action.ROOF_SLIDE_COMPLETE
                    && e.getPercentOpen() == 100) {
                    mRoofOpen = true;
                    checkFinish();
                    Log.i(TAG, "Roof done " + e.getMessage());
                } else {
                    openAll();
                }
            }
        };
        windowEventHandler = new EventHandler<WindowPositionEvent>() {
            @Override public void handle(WindowPositionEvent e) {
                if (!e.isValid()) return;

                if (e.getPercentOpen() == 100) {
                    mWindowsOpen = true;
                    checkFinish();
                    Log.i(TAG, "Windows done " + e.getMessage());
                } else {
                    openAll();
                }
            }
        };
    }

    @VisibleForTesting void run() {
        mCarConnection.subscribe(RoofPositionEvent.EVENT_TYPE, roofEventHandler);
        mCarConnection.subscribe(WindowPositionEvent.EVENT_TYPE, windowEventHandler);
        openAll();
        
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override public void run() {
                timer.cancel();
                finish();
            }
        }, TIMEOUT);
    }

    private void openAll() {
        mCarConnection.send(OPEN_ALL_EVENT);
    }

    private void checkFinish() {
        if (mWindowsOpen && mRoofOpen) {
            finish();
        } else {
            openAll();
        }
    }

    private void finish() {
        mCarConnection.unsubscribe(RoofPositionEvent.EVENT_TYPE, roofEventHandler);
        mCarConnection.unsubscribe(WindowPositionEvent.EVENT_TYPE, windowEventHandler);
    }
}
