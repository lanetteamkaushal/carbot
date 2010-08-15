package net.cardroid.util;

/**
 * Date: Apr 23, 2010
 * Time: 1:51:44 AM
 *
 * @author Lex Nikitin
 */
public class RealClock implements Clock {
    @Override public long time() {
        return System.currentTimeMillis();
    }
}
