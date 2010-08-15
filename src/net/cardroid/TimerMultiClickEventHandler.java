package net.cardroid;

import android.util.Log;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import net.cardroid.car.Event;
import net.cardroid.car.EventHandler;

import java.util.*;

/**
 * Date: Apr 18, 2010
 * Time: 3:32:44 PM
 *
 * @author Lex Nikitin
 */
public class TimerMultiClickEventHandler implements EventHandler {
    private static String TAG = "TimerMultiClickEventHandler";
    
    @VisibleForTesting static int KEY_REPEAT_MAX_DELAY = 100;
    @VisibleForTesting static int NCLICK_MAX_DELAY = 700;
    @VisibleForTesting static int LONG_CLICK_MIN_DELAY = 1000;

    private Map<ClickPattern, EventHandler> mEventHandlerMap = Maps.newHashMap();
    private int mNClicks;
    
    private long mEventTimeStamp;
    private long mClickTimeStamp;
    private Timer mTimer = new Timer();

    public void subscribe(int nClicks, boolean isLastClickLong, EventHandler handler) {
        mEventHandlerMap.put(new ClickPattern(nClicks, isLastClickLong), handler);
    }

    @Override public void handle(final Event e) {
        Log.i(TAG, "Event received " + e.toString());
        long newTimeStamp = System.currentTimeMillis();
        long dEventTime = newTimeStamp - mEventTimeStamp;
        long dClickTime = newTimeStamp - mClickTimeStamp;
        mEventTimeStamp = newTimeStamp;

        mTimer.cancel();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override public void run() {
                Log.i(TAG, "Handle short click. NClicks=" + mNClicks + "; dEventTime=" + (System.currentTimeMillis() - mEventTimeStamp));
                handleClickPattern(e, false);
            }
        }, Math.max(KEY_REPEAT_MAX_DELAY, NCLICK_MAX_DELAY));

        if (dEventTime <= KEY_REPEAT_MAX_DELAY) {
            Log.i(TAG, "Button is still pressed " + e.toString() + " dClickTime=" + dClickTime);
            // button is pressed
            if (dClickTime > LONG_CLICK_MIN_DELAY) {
                Log.i(TAG, "Handle long click. NClicks=" + mNClicks + "; dEventTime=" + (System.currentTimeMillis() - mEventTimeStamp));
                handleClickPattern(e, true);
            }
            return;
        }

        if (dClickTime < NCLICK_MAX_DELAY) {
            // n-click
            mNClicks++;
        } else {
            mNClicks = 1;
        }
        Log.i(TAG, "This is a new click. NClicks=" + mNClicks  + "; dEventTime = " + dEventTime);
        mClickTimeStamp = newTimeStamp;
    }

    private void handleClickPattern(Event e, boolean isLastClickLong) {
        EventHandler eventHandler = findMatch(mNClicks, isLastClickLong);
        if (eventHandler != null) eventHandler.handle(e);
        mTimer.cancel();
        mNClicks = 0;
        mClickTimeStamp = 0;
    }

    private EventHandler findMatch (int nClicks, boolean isLastClickLong) {
        return mEventHandlerMap.get(new ClickPattern(nClicks, isLastClickLong));
    }

    private static class ClickPattern {
        int nClicks;
        boolean isLastClickLong;

        private ClickPattern(int nClicks, boolean lastClickLong) {
            this.nClicks = nClicks;
            isLastClickLong = lastClickLong;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ClickPattern that = (ClickPattern) o;

            if (isLastClickLong != that.isLastClickLong) return false;
            if (nClicks != that.nClicks) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = nClicks;
            result = 31 * result + (isLastClickLong ? 1 : 0);
            return result;
        }
    }
}
