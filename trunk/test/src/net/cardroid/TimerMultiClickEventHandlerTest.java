package net.cardroid;

import junit.framework.TestCase;
import net.cardroid.car.Event;
import net.cardroid.car.EventHandler;
import net.cardroid.car.EventType;

/**
 * Date: Apr 18, 2010
 * Time: 5:46:43 PM
 *
 * @author Lex Nikitin
 */
public class TimerMultiClickEventHandlerTest extends TestCase {
    @Override protected void setUp() throws Exception {
        super.setUp();
        Thread.sleep(2000);
    }

    public void testClick () throws InterruptedException {
        MockEventHandler eventHandler = new MockEventHandler();
        TimerMultiClickEventHandler multiClickHandler = new TimerMultiClickEventHandler();
        multiClickHandler.subscribe(1, false, eventHandler);
        long expectedTimestamp = doShortClick(multiClickHandler);
        waitClickNotifiedDelay();
        assertTimestamp(eventHandler, expectedTimestamp);
    }

    public void testLongClick() throws InterruptedException {
        MockEventHandler eventHandler = new MockEventHandler();
        TimerMultiClickEventHandler multiClickHandler = new TimerMultiClickEventHandler();
        multiClickHandler.subscribe(1, true, eventHandler);
        long expectedTimestamp = doLongClick(multiClickHandler);
        assertTimestamp(eventHandler, expectedTimestamp);
    }

    public void testClickClick() throws InterruptedException {
        MockEventHandler eventHandler = new MockEventHandler();
        TimerMultiClickEventHandler multiClickHandler = new TimerMultiClickEventHandler();
        multiClickHandler.subscribe(1, false, null);
        multiClickHandler.subscribe(2, false, eventHandler);
        doShortClick(multiClickHandler);
        long expectedTimestamp = doShortClick(multiClickHandler);
        waitClickNotifiedDelay();
        assertTimestamp(eventHandler, expectedTimestamp);
    }

    public void testClickClickClick() throws InterruptedException {
        MockEventHandler eventHandler = new MockEventHandler();
        TimerMultiClickEventHandler multiClickHandler = new TimerMultiClickEventHandler();
        multiClickHandler.subscribe(1, false, null);
        multiClickHandler.subscribe(2, false, null);
        multiClickHandler.subscribe(3, false, eventHandler);
        doShortClick(multiClickHandler);
        doShortClick(multiClickHandler);
        long expectedTimestamp = doShortClick(multiClickHandler);
        waitClickNotifiedDelay();
        assertTimestamp(eventHandler, expectedTimestamp);
    }

    public void testClickLongClick() throws InterruptedException {
        MockEventHandler eventHandler = new MockEventHandler();
        TimerMultiClickEventHandler multiClickHandler = new TimerMultiClickEventHandler();
        multiClickHandler.subscribe(1, false, null);
        multiClickHandler.subscribe(1, true, null);
        multiClickHandler.subscribe(2, true, eventHandler);
        doShortClick(multiClickHandler);
        long expectedTimestamp = doLongClick(multiClickHandler);
        assertTimestamp(eventHandler, expectedTimestamp);
    }

    private void assertTimestamp(MockEventHandler eventHandler, long expectedTimestamp) {
        assertTrue("Expected greater than " + expectedTimestamp + ", but was " + eventHandler.timeStamp,
            eventHandler.timeStamp > expectedTimestamp);
    }

    private long doShortClick(TimerMultiClickEventHandler multiClickHandler) throws InterruptedException {
        long startedAt = System.currentTimeMillis();
        multiClickHandler.handle(EventType.DIAL);
        waitWheelKeyEventDelay();
        multiClickHandler.handle(EventType.DIAL);
        waitWheelKeyEventDelay();
        multiClickHandler.handle(EventType.DIAL);
        Thread.sleep(adjustDelay(TimerMultiClickEventHandler.KEY_REPEAT_MAX_DELAY, 1.1f));
        return startedAt + TimerMultiClickEventHandler.KEY_REPEAT_MAX_DELAY * 3;
    }

    private long doLongClick(TimerMultiClickEventHandler multiClickHandler) throws InterruptedException {
        long startedAt = System.currentTimeMillis();
        long longClickTimeStamp = startedAt + adjustDelay(TimerMultiClickEventHandler.LONG_CLICK_MIN_DELAY, 1.1f);
        while (System.currentTimeMillis() <= longClickTimeStamp) {
            multiClickHandler.handle(EventType.DIAL);
            waitWheelKeyEventDelay();
            multiClickHandler.handle(EventType.DIAL);
        }
        return startedAt + TimerMultiClickEventHandler.LONG_CLICK_MIN_DELAY;
    }

    public void waitWheelKeyEventDelay() throws InterruptedException {
        Thread.sleep(adjustDelay(TimerMultiClickEventHandler.KEY_REPEAT_MAX_DELAY, .5f));
    }

    private long adjustDelay(int delay, float adjustment) {
        return (long)((float) delay * adjustment);
    }

    public void waitClickNotifiedDelay() throws InterruptedException {
        Thread.sleep(adjustDelay(TimerMultiClickEventHandler.NCLICK_MAX_DELAY, 1.1f));
    }

    private static class MockEventHandler implements EventHandler {
        long timeStamp;

        @Override public void handle(Event e) {
            assertEquals(timeStamp, 0);
            timeStamp = System.currentTimeMillis();
        }
    }
}
