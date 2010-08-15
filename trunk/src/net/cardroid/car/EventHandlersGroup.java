package net.cardroid.car;

import com.google.common.collect.Sets;

import java.util.Iterator;
import java.util.Set;

/**
 * Date: Apr 17, 2010
 * Time: 9:54:19 PM
 *
 * @author Lex Nikitin
 */
public class EventHandlersGroup {
    private final Set<EventTypeToHandler> mSubscriptions;

    private EventHandlersGroup(Set<EventTypeToHandler> subscriptions) {
        mSubscriptions = subscriptions;
    }

    public void acivate(CarConnection car) {
        CarConnectionImpl carConnection = (CarConnectionImpl) car;
        Builder builder = carConnection.handlersGroup.fromPrototype();
        for (EventTypeToHandler eventTypeToHandler : mSubscriptions) {
            builder.subscribe(eventTypeToHandler.getEventType(), eventTypeToHandler.getHandler());
        }
        carConnection.handlersGroup = builder.build();
    }

    public void cancel(CarConnection car) {
        CarConnectionImpl carConnection = (CarConnectionImpl) car;
        Builder builder = carConnection.handlersGroup.fromPrototype();
        for (EventTypeToHandler eventTypeToHandler : mSubscriptions) {
            builder.unsubscribe(eventTypeToHandler.getEventType(), eventTypeToHandler.getHandler());
        }
        carConnection.handlersGroup = builder.build();
    }

    public Iterable<EventTypeToHandler> getSubscriptions() {
        return mSubscriptions;
    }

    public Builder fromPrototype () {
        return new Builder(mSubscriptions);
    }

    public static class Builder {
        private final Set<EventTypeToHandler> mSubscriptions;

        public Builder() {
            mSubscriptions = Sets.newHashSet();
        }

        public Builder(Set<EventTypeToHandler> subscriptions) {
            mSubscriptions = Sets.newHashSet(subscriptions);
        }

        public <T extends Event> Builder subscribe(EventType<T> e, EventHandler<T> handler) {
            mSubscriptions.add(new EventTypeToHandler<T>(e, handler));
            return this;
        }

        public <T extends Event> Builder unsubscribe(EventType<T> e, EventHandler<T> handler) {
            for (Iterator<EventTypeToHandler> iterator = mSubscriptions.iterator(); iterator.hasNext();) {
                EventTypeToHandler eventTypeToHandler = iterator.next();
                if (e.equals(eventTypeToHandler.getEventType()) && handler.equals(eventTypeToHandler.getHandler())) {
                    iterator.remove();
                }
            }
            return this;
        }

        public EventHandlersGroup build() {
            return new EventHandlersGroup(mSubscriptions);
        }
    }

    public static final class EventTypeToHandler<T extends Event> {
        final EventType<T> mmEventType;
        final EventHandler<T> mmHandler;

        EventTypeToHandler(EventType<T> eventType, EventHandler<T> mHandler) {
            mmEventType = eventType;
            mmHandler = mHandler;
        }

        public EventType<T> getEventType() {
            return mmEventType;
        }

        public EventHandler<T> getHandler() {
            return mmHandler;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EventTypeToHandler that = (EventTypeToHandler) o;

            if (!mmEventType.equals(that.mmEventType)) return false;
            if (!mmHandler.equals(that.mmHandler)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = mmEventType.hashCode();
            result = 31 * result + mmHandler.hashCode();
            return result;
        }
    }
}
