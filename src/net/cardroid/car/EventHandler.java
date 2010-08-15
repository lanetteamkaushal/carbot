package net.cardroid.car;

/**
* Date: Apr 11, 2010
* Time: 2:48:04 PM
*
* @author Lex Nikitin
*/
public interface EventHandler<T extends Event> {
    public void handle(T e);
}
