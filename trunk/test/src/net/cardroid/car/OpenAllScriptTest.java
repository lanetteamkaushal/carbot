package net.cardroid.car;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import junit.framework.TestCase;
import net.cardroid.can.Can232Adapter;
import net.cardroid.can.CanMessage;

import java.util.List;
import java.util.Map;

/**
 * Date: Apr 22, 2010
 * Time: 12:30:01 AM
 *
 * @author Lex Nikitin
 */
public class OpenAllScriptTest extends TestCase {
    private static final int TIMEOUT = 60000; // 1 minute

    public void testOpen() {
        MockCarConnection carConnection = new MockCarConnection();
        OpenAllScript script = new OpenAllScript(carConnection);
        script.run();
        assertEquals(2, carConnection.eventToHandler.size());
        assertEquals(1, carConnection.sentEvents.size());

        carConnection.eventToHandler.get(WindowPositionEvent.EVENT_TYPE).handle(
            new WindowPositionEvent(WindowPositionEvent.Window.LEFT, 10));
        assertEquals(2, carConnection.sentEvents.size());

        carConnection.eventToHandler.get(WindowPositionEvent.EVENT_TYPE).handle(
            new WindowPositionEvent(WindowPositionEvent.Window.LEFT, 100));
        assertEquals(2, carConnection.sentEvents.size());

        // try bad window event
        carConnection.eventToHandler.get(WindowPositionEvent.EVENT_TYPE).handle(
            new WindowPositionEvent(new CanMessage(CanMessage.Type.CAN11, 0x3B6, "AA", -1)));
        assertEquals(2, carConnection.sentEvents.size());

        // try bad roof event
        carConnection.eventToHandler.get(RoofPositionEvent.EVENT_TYPE).handle(
            new RoofPositionEvent(new CanMessage(CanMessage.Type.CAN11, 0x3B6, "AA", -1)));
        assertEquals(2, carConnection.sentEvents.size());

        carConnection.eventToHandler.get(RoofPositionEvent.EVENT_TYPE).handle(
            new RoofPositionEvent(RoofPositionEvent.Action.ROOF_SLIDE_COMPLETE, 10));
        assertEquals(3, carConnection.sentEvents.size());

        carConnection.eventToHandler.get(RoofPositionEvent.EVENT_TYPE).handle(
            new RoofPositionEvent(RoofPositionEvent.Action.ROOF_SLIDE_COMPLETE, 100));
        assertEquals(3, carConnection.sentEvents.size());
        assertEquals(0, carConnection.eventToHandler.size());
    }

    private class MockCarConnection implements CarConnection {
        private Map<EventType, EventHandler> eventToHandler = Maps.newHashMap();
        private List<Event> sentEvents = Lists.newArrayList();

        @Override public void attachTo(Can232Adapter adapter) {
        }

        @Override public void subscribe(EventType e, EventHandler handler) {
            eventToHandler.put(e, handler);
        }

        @Override public void unsubscribe(EventType e, EventHandler handler) {
            eventToHandler.remove(e);
        }

        @Override public void send(Event event) {
            sentEvents.add(event);
        }
    }
}