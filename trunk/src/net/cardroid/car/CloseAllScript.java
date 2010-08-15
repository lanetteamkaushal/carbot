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
public class CloseAllScript {
    private static String TAG = "CloseAllScript";
    private static final int TIMEOUT = 20000; // 20 seconds
    private static final SimpleEvent CLOSE_ALL_EVENT = new SimpleEvent(EventType.ROOF_CLOSE_STEP.getDestination(),
            EventType.ROOF_CLOSE_STEP.getIntData().or(EventType.WINDOW_LR_UP_FULL_NOPOWER.getIntData()),
            EventType.ROOF_CLOSE_STEP.nChars());

    public static void run(CarConnection car) {
        new CloseAllScript(car).run();
    }

    private final CarConnection mCarConnection;
    private final EventHandler<RoofPositionEvent> roofEventHandler;
    private final EventHandler<WindowPositionEvent> windowEventHandler;

    private boolean mWindowsClosed;
    private boolean mRoofClosed;

    @VisibleForTesting CloseAllScript(CarConnection carConnection) {
        mCarConnection = carConnection;
        roofEventHandler = new EventHandler<RoofPositionEvent>() {
            @Override public void handle(RoofPositionEvent e) {
                if (!e.isValid()) return;

                if (e.getAction() == RoofPositionEvent.Action.ROOF_SLIDE_COMPLETE
                    && e.getPercentOpen() == 0) {
                    mRoofClosed = true;
                    checkFinish();
                    Log.i(TAG, "Roof done " + e.getMessage());
                } else {
                    closeAll();
                }
            }
        };
        windowEventHandler = new EventHandler<WindowPositionEvent>() {
            @Override public void handle(WindowPositionEvent e) {
                if (!e.isValid()) return;

                if (e.getPercentOpen() == 0) {
                    mWindowsClosed = true;
                    checkFinish();
                    Log.i(TAG, "Windows done " + e.getMessage());
                } else {
                    closeAll();
                }
            }
        };
    }

    @VisibleForTesting void run() {
        mCarConnection.subscribe(RoofPositionEvent.EVENT_TYPE, roofEventHandler);
        mCarConnection.subscribe(WindowPositionEvent.EVENT_TYPE, windowEventHandler);
        closeAll();

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override public void run() {
                timer.cancel();
                finish();
            }
        }, TIMEOUT);
    }

    private void closeAll() {
        mCarConnection.send(CLOSE_ALL_EVENT);
    }

    private void checkFinish() {
        if (mWindowsClosed && mRoofClosed) {
            finish();
        } else {
            closeAll();
        }
    }

    private void finish() {
        mCarConnection.unsubscribe(RoofPositionEvent.EVENT_TYPE, roofEventHandler);
        mCarConnection.unsubscribe(WindowPositionEvent.EVENT_TYPE, windowEventHandler);
    }
}