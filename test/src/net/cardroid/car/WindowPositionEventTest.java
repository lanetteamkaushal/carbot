package net.cardroid.car;

import junit.framework.TestCase;
import net.cardroid.can.CanMessage;
import net.cardroid.car.PatternMatcher.ValuePattern;
import net.cardroid.car.PatternMatcher.ValuePatternProvider;

/**
* Date: Apr 22, 2010
* Time: 12:10:54 AM
*
* @author Lex Nikitin
*/
public class WindowPositionEventTest extends TestCase {
    //window-l-position-full-down =t3B6 3 4FFE E0
    //window-left-position-full-up=t3B6 3 00FC E0
    public void testEventLWindow() {
        WindowPositionEvent positionEvent = WindowPositionEvent.EVENT_TYPE.createEvent(new CanMessage(CanMessage.Type.CAN11, 0x3B6, "4FFEE0", -1));
        assertEquals(WindowPositionEvent.Window.LEFT, positionEvent.getWindow());
        assertEquals(100, positionEvent.getPercentOpen());

        positionEvent = WindowPositionEvent.EVENT_TYPE.createEvent(new CanMessage(CanMessage.Type.CAN11, 0x3B6, "00FCE0", -1));
        assertEquals(WindowPositionEvent.Window.LEFT, positionEvent.getWindow());
        assertEquals(0, positionEvent.getPercentOpen());
    }
}