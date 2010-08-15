package net.cardroid.car;

import net.cardroid.can.Can232Adapter;
import net.cardroid.can.CanMessage;
import net.cardroid.car.PatternMatcher.ValuePattern;
import net.cardroid.car.PatternMatcher.ValuePatternProvider;

/**
* Date: Apr 22, 2010
* Time: 12:10:54 AM
*
* @author Lex Nikitin
*/
public class WindowPositionEvent extends ValueEvent<WindowPositionEvent.Window> {
    private static final int EVENT_TYPE_DESTINATION = 0x3B6;
    public static final WindowPositionEventType EVENT_TYPE = new WindowPositionEventType();

    public static enum Window implements ValuePatternProvider<Window> {
        //window-l-position-full-down =t3B6 3 4FFE E0
        //window-left-position-full-up=t3B6 3 00FC E0
        //roof-notify-before-move-01-00-to-ff                           =t3BA 7 01FFFDFFFFF9F8
        LEFT(new ValuePattern<Window>(6, "", "E0", 0xFC, 0x50FE));
        //RIGHT;

        private final ValuePattern<Window> pattern;

        Window(ValuePattern<Window> pattern) {
            this.pattern = pattern;
            pattern.setTag(this);
        }


        @Override public ValuePattern<Window> get() {
            return pattern;
        }
    }

    public WindowPositionEvent(CanMessage message) {
        super(EVENT_TYPE, message);
    }

    public WindowPositionEvent(Window window, int percent) {
        super(EVENT_TYPE, new CanMessage(CanMessage.Type.CAN11, EVENT_TYPE_DESTINATION,
            window.get().getDataForPercent(percent), -1));
    }

    public Window getWindow() {
        return getMatch().getTag();
    }

    public int getPercentOpen() {
        return getMatch().getPrecent();
    }

    public static class WindowPositionEventType extends ValueEventType<Window> {
        public WindowPositionEventType() {
            super(WindowPositionEvent.EVENT_TYPE_DESTINATION, PatternMatcher.fromProviders(Window.values()));
        }

        @Override public WindowPositionEvent createEvent(CanMessage message) {
            return new WindowPositionEvent(message);
        }
    }
}