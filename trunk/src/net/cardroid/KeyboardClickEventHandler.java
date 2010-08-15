package net.cardroid;

import android.util.Log;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import net.cardroid.car.Event;
import net.cardroid.car.EventHandler;
import net.cardroid.car.EventHandlersGroup;
import net.cardroid.car.EventType;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Date: Apr 18, 2010
 * Time: 3:32:44 PM
 *
 * @author Lex Nikitin
 */
public class KeyboardClickEventHandler {
    private static String TAG = "KeyboardClickEventHandler";

    @VisibleForTesting static int BEFORE_REPEAT_DELAY = 700;

    private long mClickTimeStamp;

    public void attachTo(EventHandlersGroup.Builder groupBuilder, EventType pushEvent, EventType releaseEvent,
        final EventHandler eventHandler) {
        groupBuilder.subscribe(pushEvent, new EventHandler() {
            @Override public void handle(final Event e) {
                Log.i(TAG, "Event received " + e.toString());

                long dClickTime;

                if (mClickTimeStamp == 0) {
                    mClickTimeStamp = System.currentTimeMillis();
                    eventHandler.handle(e);
                } else {
                    dClickTime = System.currentTimeMillis() - mClickTimeStamp;

                    if (dClickTime > BEFORE_REPEAT_DELAY) {
                        eventHandler.handle(e);
                    }
                }
            }
        });
        groupBuilder.subscribe(releaseEvent, new EventHandler() {
            @Override public void handle(final Event e) {
                mClickTimeStamp = 0;
            }
        });
    }
}