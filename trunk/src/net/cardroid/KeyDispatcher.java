package net.cardroid;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import com.google.inject.Inject;
import net.cardroid.android.IWindowManager;
import net.cardroid.car.*;
import net.cardroid.util.Clock;

import java.util.Timer;

/**
 * Date: Apr 17, 2010
 * Time: 10:22:48 PM
 *
 * @author Lex Nikitin
 */
public class KeyDispatcher {
    private static String TAG = "KeyDispatcher";
    private static final int NOTIFICATION_KEYBOARD_MODE = 1;

    private final CarConnection mCarConnection;
    private final IWindowManager mWindowManager;
    private final NotificationManager mNotificationManager;
    private final Context mContext;
    private final Timer mTimer;
    private final Clock mClock;

    private final EventHandlersGroup mCursorKeygroup;
    private final EventHandlersGroup mMusicControlKeygroup;
    private final EventHandlersGroup mUtilityKeygroup;

    enum State { MEDIA_CONTROL, DPAD_MODE }

    private State mState = State.MEDIA_CONTROL;

    @Inject
    public KeyDispatcher(CarConnection carConnection, NotificationManager notificationManager, IWindowManager windowManager,
            Context context, Timer timer, Clock clock) {
        mNotificationManager = notificationManager;
        mCarConnection = carConnection;
        mWindowManager = windowManager;
        mContext = context;
        mTimer = timer;
        mClock = clock;

        EventHandlersGroup.Builder cursorKeyGroupBuilder = new EventHandlersGroup.Builder();
        initCursorKeyHandlers(cursorKeyGroupBuilder);
        mCursorKeygroup = cursorKeyGroupBuilder.build();

        EventHandlersGroup.Builder musicControlKeyGroupBuilder = new EventHandlersGroup.Builder();
        initMusicControlKeyHandlers(musicControlKeyGroupBuilder);
        mMusicControlKeygroup = musicControlKeyGroupBuilder.build();

        EventHandlersGroup.Builder utilityKeyGroupBuilder = new EventHandlersGroup.Builder();
        initUtilityKeyHandlers(utilityKeyGroupBuilder);
        mUtilityKeygroup = utilityKeyGroupBuilder.build();
    }

    public void attach() {
        mMusicControlKeygroup.acivate(mCarConnection);
        mUtilityKeygroup.acivate(mCarConnection);
        
        ClickPatternEventHandler dialPatternHandler = new ClickPatternEventHandler(mTimer, mClock);
        dialPatternHandler.subscribe(3, false, new EventHandler() {
            @Override public void handle(Event e) {
                if (mState == State.MEDIA_CONTROL) {
                    mMusicControlKeygroup.cancel(mCarConnection);
                    mCursorKeygroup.acivate(mCarConnection);

                    Notification notification = new Notification(R.drawable.dpad, "Dpad mode", System.currentTimeMillis());
                    notification.setLatestEventInfo(mContext, "Dpad mode", "Steering wheel control switched to dpad mode",
                        PendingIntent.getService(mContext, 0, new Intent(mContext, CardroidService.class), 0));
                    notification.flags = Notification.FLAG_ONGOING_EVENT;
                    mNotificationManager.notify(NOTIFICATION_KEYBOARD_MODE, notification);
                    mState = State.DPAD_MODE;
                } else {
                    mCursorKeygroup.cancel(mCarConnection);
                    mMusicControlKeygroup.acivate(mCarConnection);

                    mNotificationManager.cancel(NOTIFICATION_KEYBOARD_MODE);
                    mState = State.MEDIA_CONTROL;
                }
            }
        });
        EventHandlersGroup.Builder builder = new EventHandlersGroup.Builder();
        dialPatternHandler.attachTo(builder, EventType.DIAL, EventType.MFSW_BUTTONS_RELEASED);
        builder.build().acivate(mCarConnection);
    }

    private void initCursorKeyHandlers(EventHandlersGroup.Builder cursorKeyGroupBuilder) {
        ClickPatternEventHandler dialPatternHandler = new ClickPatternEventHandler(mTimer, mClock);
        dialPatternHandler.subscribe(2, false, new EventHandler() {
            @Override public void handle(Event e) {
                sendKeyPress(KeyEvent.KEYCODE_HOME);
            }
        });
        dialPatternHandler.subscribe(2, true, new ClickPatternEventHandler.LongPressEventHandler() {
            @Override public void handle(Event e) {
                mWindowManager.injectKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HOME), true);
            }

            @Override public void keyReleased(Event e) {
                mWindowManager.injectKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HOME), true);
            }
        });
        dialPatternHandler.subscribe(1, false, new EventHandler() {
            @Override public void handle(Event e) {
                sendKeyPress(KeyEvent.KEYCODE_ENTER);
            }
        });
        dialPatternHandler.attachTo(cursorKeyGroupBuilder, EventType.DIAL, EventType.MFSW_BUTTONS_RELEASED);

        subscribeSingleClickHandler(cursorKeyGroupBuilder, EventType.VOLUME_UP, EventType.MFSW_BUTTONS_RELEASED, new EventHandler() {
            @Override public void handle(Event e) {
                sendKeyPress(KeyEvent.KEYCODE_DPAD_UP);
                mCarConnection.send(EventType.VOLUME_DOWN);
            }
        });
        subscribeSingleClickHandler(cursorKeyGroupBuilder, EventType.TRACK_NEXT, EventType.MFSW_BUTTONS_RELEASED, new EventHandler() {
            @Override public void handle(Event e) {
                sendKeyPress(KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        subscribeSingleClickHandler(cursorKeyGroupBuilder, EventType.VOLUME_DOWN, EventType.MFSW_BUTTONS_RELEASED, new EventHandler() {
            @Override public void handle(Event e) {
                sendKeyPress(KeyEvent.KEYCODE_DPAD_DOWN);
                mCarConnection.send(EventType.VOLUME_UP);
            }
        });
        subscribeSingleClickHandler(cursorKeyGroupBuilder, EventType.TRACK_PREV, EventType.MFSW_BUTTONS_RELEASED, new EventHandler() {
            @Override public void handle(Event e) {
                sendKeyPress(KeyEvent.KEYCODE_DPAD_LEFT);
            }
        });
    }

    // event handler will receive process events similar to how keyboard does it
    // first event will be fired right away, then after a delay repeated events will be
    // triggered until key is released
    private void subscribeSingleClickHandler(EventHandlersGroup.Builder cursorKeyGroupBuilder, SimpleEvent pushEvent, SimpleEvent releaseEvent, EventHandler handler) {
        KeyboardClickEventHandler keyboardClickEventHandler = new KeyboardClickEventHandler();
        keyboardClickEventHandler.attachTo(cursorKeyGroupBuilder, pushEvent, releaseEvent, handler);
    }

    private void sendKeyPress(int keyCode) {
        mWindowManager.injectKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode), true);
        mWindowManager.injectKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode), true);
    }

    private void initMusicControlKeyHandlers(EventHandlersGroup.Builder musicKeyGroupBuilder) {
        ClickPatternEventHandler dialPatternHandler = new ClickPatternEventHandler(mTimer, mClock);
        dialPatternHandler.subscribe(1, false, new EventHandler() {
            @Override public void handle(Event e) {
                Intent intent = new Intent(mContext, VoiceRecognitionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
        dialPatternHandler.subscribe(2, false, new EventHandler() {
            @Override public void handle(Event e) {
                Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH)
                    .putExtra("car_dock", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
        dialPatternHandler.attachTo(musicKeyGroupBuilder, EventType.DIAL, EventType.MFSW_BUTTONS_RELEASED);

        ClickPatternEventHandler volumeUpHandler = new ClickPatternEventHandler(mTimer, mClock);
        volumeUpHandler.subscribe(2, false, new MediaButtonPressReleaseEventHandler(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
        volumeUpHandler.attachTo(musicKeyGroupBuilder, EventType.VOLUME_UP, EventType.MFSW_BUTTONS_RELEASED);

        ClickPatternEventHandler volumeDownHandler = new ClickPatternEventHandler(mTimer, mClock);
        volumeDownHandler.subscribe(2, false, new MediaButtonPressReleaseEventHandler(KeyEvent.KEYCODE_MEDIA_STOP));
        volumeDownHandler.attachTo(musicKeyGroupBuilder, EventType.VOLUME_DOWN, EventType.MFSW_BUTTONS_RELEASED);

        ClickPatternEventHandler nextTrackHandler = new ClickPatternEventHandler(mTimer, mClock);
        nextTrackHandler.subscribe(1, false, new MediaButtonPressReleaseEventHandler(KeyEvent.KEYCODE_MEDIA_NEXT));
        nextTrackHandler.subscribe(1, true, new MediaButtonHoldEventHandler(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD));
        nextTrackHandler.attachTo(musicKeyGroupBuilder, EventType.TRACK_NEXT, EventType.MFSW_BUTTONS_RELEASED);

        ClickPatternEventHandler previousTrackHandler = new ClickPatternEventHandler(mTimer, mClock);
        previousTrackHandler.subscribe(1, false, new MediaButtonPressReleaseEventHandler(KeyEvent.KEYCODE_MEDIA_PREVIOUS));
        previousTrackHandler.subscribe(1, true, new MediaButtonHoldEventHandler(KeyEvent.KEYCODE_MEDIA_REWIND));
        previousTrackHandler.attachTo(musicKeyGroupBuilder, EventType.TRACK_PREV, EventType.MFSW_BUTTONS_RELEASED);
    }

    private void initUtilityKeyHandlers(EventHandlersGroup.Builder utilityKeyGroupBuilder) {
        ClickPatternEventHandler openWindowsMultiHandler = new ClickPatternEventHandler(mTimer, mClock);
        openWindowsMultiHandler.subscribe(3, false, new EventHandler() {
            @Override public void handle(Event e) {
                OpenAllScript.run(mCarConnection);
            }
        });
        openWindowsMultiHandler.attachTo(utilityKeyGroupBuilder, EventType.WINDOW_L_DOWN, EventType.WINDOW_LR_STOP);

        ClickPatternEventHandler closeWindowsPatternHandler = new ClickPatternEventHandler(mTimer, mClock);
        closeWindowsPatternHandler.subscribe(3, false, new EventHandler() {
            @Override public void handle(Event e) {
                CloseAllScript.run(mCarConnection);
            }
        });
        closeWindowsPatternHandler.attachTo(utilityKeyGroupBuilder, EventType.WINDOW_L_UP, EventType.WINDOW_LR_STOP);
    }

    private class MediaButtonPressReleaseEventHandler implements EventHandler {
        private final int mmKeyEvent;

        public MediaButtonPressReleaseEventHandler(int keycode) {
            mmKeyEvent = keycode;
        }

        @Override public void handle(Event e) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
            intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, mmKeyEvent));
            mContext.sendOrderedBroadcast(intent, null);

            intent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
            intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, mmKeyEvent));
            mContext.sendOrderedBroadcast(intent, null);
        }
    }

    private class MediaButtonHoldEventHandler implements ClickPatternEventHandler.LongPressEventHandler {
        private final int mmKeyEvent;

        public MediaButtonHoldEventHandler(int keycode) {
            mmKeyEvent = keycode;
        }

        @Override public void handle(Event e) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
            intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, mmKeyEvent));
            mContext.sendOrderedBroadcast(intent, null);
        }

        @Override public void keyReleased(Event e) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
            intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, mmKeyEvent));
            mContext.sendOrderedBroadcast(intent, null);
        }
    }
}
