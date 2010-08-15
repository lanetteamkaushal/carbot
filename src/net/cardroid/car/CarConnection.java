package net.cardroid.car;

import net.cardroid.can.Can232Adapter;

/**
 * Date: Apr 11, 2010
 * Time: 2:13:15 PM
 *
 * @author Lex Nikitin
 */
public interface CarConnection {

    void attachTo(Can232Adapter adapter);

    <T extends Event> void subscribe(EventType<T> e, EventHandler<T> handler);
    <T extends Event> void unsubscribe(EventType<T> e, EventHandler<T> handler);

    void send(Event event);
}
