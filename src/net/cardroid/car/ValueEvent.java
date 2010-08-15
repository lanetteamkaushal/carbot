package net.cardroid.car;

import net.cardroid.can.Can232Adapter;
import net.cardroid.can.CanMessage;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Date: Apr 22, 2010
 * Time: 2:22:45 AM
 *
 * @author Lex Nikitin
 */
public class ValueEvent<T> implements Event {
    private final ValueEventType<T> eventType;
    private final CanMessage mMessage;

    PatternMatcher.Match<T> match;
    private int percent;

    protected ValueEvent(ValueEventType<T> eventType, CanMessage message) {
        this.eventType = checkNotNull(eventType);
        mMessage = checkNotNull(message);
    }

    protected PatternMatcher.Match<T> getMatch() {
        if (match == null) {
            match = eventType.mPatternMatcher.findPattern(mMessage.getData());
        }

        return match;
    }

    public boolean isValid() {
        return getMatch() != null;
    }

    @Override public ValueEventType getEventType() {
        return eventType;
    }

    @Override public CanMessage getMessage() {
        return mMessage;
    }

    protected abstract static class ValueEventType<T> implements EventType {
        private final PatternMatcher<T> mPatternMatcher;
        private final int mDestination;

        public ValueEventType(int destination, PatternMatcher<T> patternMatcher) {
            this.mDestination = destination;
            this.mPatternMatcher = patternMatcher;
        }

        @Override public boolean matches(CanMessage message) {
            return message.getDestination() == mDestination;
        }
    }
}
