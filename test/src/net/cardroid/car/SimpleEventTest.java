package net.cardroid.car;

import junit.framework.TestCase;

import java.math.BigInteger;

/**
 * Date: Apr 11, 2010
 * Time: 2:50:16 PM
 *
 * @author Lex Nikitin
 */
public class SimpleEventTest extends TestCase {
    public void testHexConstructor() {
        SimpleEvent simpleEvent = new SimpleEvent(100, BigInteger.valueOf(0xD), 8);
        assertEquals("0000000D", simpleEvent.getCanMessage().getData());
    }

    public void testGetIntData() {
        SimpleEvent simpleEvent = new SimpleEvent(100, "1234");
        assertEquals(BigInteger.valueOf(0x1234), simpleEvent.getIntData());
        assertEquals(4, simpleEvent.nChars());

        simpleEvent = new SimpleEvent(100, "00001234");
        assertEquals(BigInteger.valueOf(0x1234), simpleEvent.getIntData());
        assertEquals(8, simpleEvent.nChars());
    }

    public void testIntegration() {
        SimpleEvent simpleEvent = new SimpleEvent(EventType.ROOF_OPEN_FULL.getDestination(),
            EventType.ROOF_OPEN_FULL.getIntData(), EventType.ROOF_OPEN_FULL.nChars());
        assertEquals(EventType.ROOF_OPEN_FULL.getCanMessage(), simpleEvent.getCanMessage());

        SimpleEvent compoundEvent = new SimpleEvent(EventType.ROOF_OPEN_FULL.getDestination(),
                EventType.ROOF_OPEN_FULL.getIntData().or(EventType.WINDOW_LR_DOWN_STEP_NOPOWER.getIntData()),
                EventType.ROOF_OPEN_FULL.nChars());
        assertEquals("0900020000000000", compoundEvent.getCanMessage().getData());
    }
}