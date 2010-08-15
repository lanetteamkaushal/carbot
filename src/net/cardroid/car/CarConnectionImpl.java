package net.cardroid.car;

import net.cardroid.can.Can232Adapter;
import net.cardroid.can.CanListenerAdapter;
import net.cardroid.can.CanMessage;

/**
 * Date: Apr 11, 2010
 * Time: 2:13:27 PM
 *
 * @author Lex Nikitin
 */
public class CarConnectionImpl implements CarConnection {
    EventHandlersGroup handlersGroup = new EventHandlersGroup.Builder().build();
    private Can232Adapter mCanAdapter;

    @Override public void attachTo(Can232Adapter adapter) {
        mCanAdapter = adapter;
        adapter.addListener(new CanListenerAdapter() {
            @Override public void messageReceived(CanMessage message) {
                for (EventHandlersGroup.EventTypeToHandler eventTypeToHandler : handlersGroup.getSubscriptions()) {
                    EventType eventType = eventTypeToHandler.getEventType();
                    if (eventType.matches(message)) {
                        eventTypeToHandler.getHandler().handle(eventType.createEvent(message));
                    }                    
                }
            }
        });
    }

    @Override public <T extends Event> void subscribe(EventType<T> e, EventHandler<T> handler) {
        handlersGroup = handlersGroup
            .fromPrototype()
            .subscribe(e, handler)
            .build();
    }

    @Override public <T extends Event> void unsubscribe(EventType<T> e, EventHandler<T> handler) {
        handlersGroup = handlersGroup
            .fromPrototype()
            .unsubscribe(e, handler)
            .build();
    }

    @Override public void send(Event event) {
        mCanAdapter.scheduleMessage(event.getMessage());
    }
}
