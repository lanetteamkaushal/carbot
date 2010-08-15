package net.cardroid.car;

import net.cardroid.can.CanMessage;
import net.cardroid.car.PatternMatcher.ValuePatternProvider;
import net.cardroid.car.PatternMatcher.ValuePattern;

/**
* Date: Apr 22, 2010
* Time: 12:10:54 AM
*
* @author Lex Nikitin
*/
public class RoofPositionEvent extends ValueEvent<RoofPositionEvent.Action> {
    private static final int EVENT_TYPE_DESTINATION = 0x3BA;
    public static final RoofPositionEventType EVENT_TYPE = new RoofPositionEventType();

    public static enum Action implements ValuePatternProvider<Action> {
        //roof-notify-closed                                            =t3BA 7 0000F0FFFFF8F8
        //roof-notify-f0-f8-closed-to-slight                            =t3BA 7 0000F0FFFFFBF8
        //roof-notify-open-full-tilt                                    =t3BA 7 FF08FBFFFFF8F8
        //roof-notify-48-from-00-to-ff-slight-to-full-after-roof-stopped=t3BA 7 48FFFDFFFFF8F8
        //roof-notify-before-move-01-00-to-ff                           =t3BA 7 01FFFDFFFFF9F8
        ROOF_CLOSED(new ValuePattern<Action>(14, "0000F0FFFFF8F8", "", 0, 0)),
        ROOF_TILT_FULL(new ValuePattern<Action>(14, "FF08FBFFFFF8F8", "", 0, 0)),
        ROOF_TILT_START(new ValuePattern<Action>(14, "FF08FBFFFFF9F8", "", 0, 0)),
        ROOF_TILT_COMPLETE(new ValuePattern<Action>(14, "0000F", "FFFFFBF8", 0, 8)),
        ROOF_SLIDE_START(new ValuePattern<Action>(14, "", "FFFDFFFFF9F8", 0, 0x48)),
        ROOF_SLIDE_COMPLETE(new ValuePattern<Action>(14, "", "FFFDFFFFF8F8", 0, 0x48));

        private final ValuePattern<Action> pattenMatcher;

        Action(ValuePattern<Action> pattenMatcher) {
            this.pattenMatcher = pattenMatcher;
            pattenMatcher.setTag(this);
        }

        public ValuePattern<Action> get() {
            return pattenMatcher;
        }
    }

    public RoofPositionEvent(CanMessage message) {
        super(EVENT_TYPE, message);
    }

    public RoofPositionEvent(Action window, int percent) {
        super(EVENT_TYPE, new CanMessage(CanMessage.Type.CAN11, EVENT_TYPE_DESTINATION, 
            window.get().getDataForPercent(percent), -1));
    }

    public Action getAction() {
        return getMatch().getTag();
    }

    public int getPercentOpen() {
        return getMatch().getPrecent();
    }

    public static class RoofPositionEventType extends ValueEventType<Action> {
        public RoofPositionEventType() {
            super(RoofPositionEvent.EVENT_TYPE_DESTINATION, PatternMatcher.fromProviders(Action.values()));
        }

        @Override public RoofPositionEvent createEvent(CanMessage message) {
            return new RoofPositionEvent(message);
        }
    }
}
