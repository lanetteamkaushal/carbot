package net.cardroid;

import android.util.Log;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import net.cardroid.car.Event;
import net.cardroid.car.EventHandler;
import net.cardroid.car.EventHandlersGroup;
import net.cardroid.car.EventType;
import net.cardroid.util.Clock;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Date: Apr 18, 2010
 * Time: 3:32:44 PM
 *
 * @author Lex Nikitin
 */
public class ClickPatternEventHandler {
    private static String TAG = "ClickPatternEventHandler";

    @VisibleForTesting static int NCLICK_MAX_DELAY = 700;
    @VisibleForTesting static int LONG_CLICK_MIN_DELAY = 1000;
    // reset if key-up event was not received within RESET_DELAY
    @VisibleForTesting static int RESET_DELAY = 10000;

    private final Timer mTimer;
    private final Clock mClock;
    private final Map<ClickPattern, EventHandler> mEventHandlerMap = Maps.newHashMap();

    private TimerTask mTimerTask;
    private int mNClicks;
    private LongPressEventHandler mLongClickInProgressHandler;
    private long mClickTimeStamp;

    public ClickPatternEventHandler(Timer timer, Clock clock) {
        mTimer = timer;
        mClock = clock;
    }

    public interface LongPressEventHandler extends EventHandler {
        public void keyReleased(Event e);
    }

    public void attachTo(EventHandlersGroup.Builder groupBuilder, EventType pushEvent, EventType releaseEvent) {
        groupBuilder.subscribe(pushEvent, new EventHandler() {
            @Override public void handle(final Event e) {
                Log.i(TAG, "Event received " + e.toString());

                long currentTime = mClock.time();
                long dClickTime = mClickTimeStamp == 0 ? 0 : currentTime - mClickTimeStamp;
                if (dClickTime > RESET_DELAY) {
                    reset();
                }

                if (mLongClickInProgressHandler != null) {
                    Log.i(TAG, "Long click in progress. Event ignored " + e.toString());
                    return;
                }

                if (mClickTimeStamp == 0) {
                    mClickTimeStamp = currentTime;
                    mNClicks++;
                }

                if (mTimerTask != null) {
                    mTimerTask.cancel();
                    mTimerTask = null;
                }

                Log.i(TAG, "NClicks=" + mNClicks  + "; dClickTime = " + dClickTime);
                // button is pressed
                if (dClickTime >= LONG_CLICK_MIN_DELAY) {
                    Log.i(TAG, "Handle long click.");
                    EventHandler eventHandler = handleClickPattern(e, true);
                    if (eventHandler instanceof LongPressEventHandler) {
                        mLongClickInProgressHandler = (LongPressEventHandler) eventHandler;
                    }
                }
            }
        });
        groupBuilder.subscribe(releaseEvent, new EventHandler() {
            @Override public void handle(final Event e) {
                if (mLongClickInProgressHandler != null) {
                    EventHandler eventHandler = findMatch(mNClicks, true);
                    if (eventHandler instanceof LongPressEventHandler) {
                        ((LongPressEventHandler) eventHandler).keyReleased(e);
                    }
                    mLongClickInProgressHandler = null;
                }

                if (mClickTimeStamp != 0) {
                    if (mTimerTask != null) {
                        mTimerTask.cancel();
                    }

                    mTimerTask = new TimerTask() {
                        @Override public void run() {
                            Log.i(TAG, "Handle short click NClicks=" + mNClicks);
                            handleClickPattern(e, false);
                        }
                    };
                    mTimer.schedule(mTimerTask, NCLICK_MAX_DELAY);
                    mClickTimeStamp = 0;
                }
            }
        });
    }

    private void reset() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;            
        }
        mLongClickInProgressHandler = null;
        mNClicks = 0;
        mClickTimeStamp = 0;
    }

    public void subscribe(int nClicks, boolean isLastClickLong, EventHandler handler) {
        mEventHandlerMap.put(new ClickPattern(nClicks, isLastClickLong), handler);
    }

    private EventHandler handleClickPattern(Event e, boolean isLastClickLong) {
        EventHandler eventHandler = findMatch(mNClicks, isLastClickLong);
        if (eventHandler != null) eventHandler.handle(e);
        reset();
        return eventHandler;
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