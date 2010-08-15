package net.cardroid.car;

import net.cardroid.can.CanMessage;

/**
 * Date: Apr 11, 2010
 * Time: 3:51:20 PM
 *
 * @author Lex Nikitin
 */
public interface Event {
    EventType getEventType();

    CanMessage getMessage();
}
