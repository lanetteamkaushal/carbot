package net.cardroid;

import com.google.common.collect.Iterables;
import junit.framework.TestCase;
import net.cardroid.car.*;
import net.cardroid.util.MockClock;
import net.cardroid.util.MockTimer;
import org.easymock.EasyMock;

/**
 * Date: Apr 18, 2010
 * Time: 5:46:43 PM
 *
 * @author Lex Nikitin
 */
public class ClickPatternEventHandlerTest extends TestCase {
    private EventHandlersGroup.Builder groupBuilder;
    private ClickPatternEventHandler clickPatternHandler;
    private EventHandlersGroup handlersGroup;
    private static final long KEY_REPEAT_MAX_DELAY = 300;
    private MockTimer mMockTimer;
    private MockClock mMockClock;
    private EventHandler mMockEventHandler;

    @Override protected void setUp() throws Exception {
        super.setUp();

        groupBuilder = new EventHandlersGroup.Builder();
        mMockTimer = new MockTimer();
        mMockClock = new MockClock();
        clickPatternHandler = new ClickPatternEventHandler(mMockTimer, mMockClock);
        clickPatternHandler.attachTo(groupBuilder, EventType.DIAL, EventType.MFSW_BUTTONS_RELEASED);
        handlersGroup = groupBuilder.build();
        mMockEventHandler = EasyMock.createStrictMock(EventHandler.class);
    }

    public void testClick() throws InterruptedException {
        clickPatternHandler.subscribe(1, false, mMockEventHandler);
        doShortClick(false);
        mMockEventHandler.handle(EventType.MFSW_BUTTONS_RELEASED);
        EasyMock.replay(mMockEventHandler);
        runNClickTimerTask();
        assertEquals(0, mMockTimer.timerTasks.size());
        EasyMock.verify(mMockEventHandler);
    }

    public void testLongClick() throws InterruptedException {
        clickPatternHandler.subscribe(1, true, mMockEventHandler);
        doLongClick(false);
    }

    public void testClickClick() throws InterruptedException {
        clickPatternHandler.subscribe(1, false, null);
        clickPatternHandler.subscribe(2, false, mMockEventHandler);
        doShortClick(false);
        doShortClick(true);
        mMockEventHandler.handle(EventType.MFSW_BUTTONS_RELEASED);
        EasyMock.replay(mMockEventHandler);
        runNClickTimerTask();
        assertEquals(0, mMockTimer.timerTasks.size());
        EasyMock.verify(mMockEventHandler);
    }

    public void testClickClickClick() throws InterruptedException {
        clickPatternHandler.subscribe(1, false, null);
        clickPatternHandler.subscribe(2, false, null);
        clickPatternHandler.subscribe(3, false, mMockEventHandler);
        doShortClick(false);
        doShortClick(true);
        doShortClick(true);
        mMockEventHandler.handle(EventType.MFSW_BUTTONS_RELEASED);
        EasyMock.replay(mMockEventHandler);
        runNClickTimerTask();
        assertEquals(0, mMockTimer.timerTasks.size());
        EasyMock.verify(mMockEventHandler);
    }

    public void testClickLongClick() throws InterruptedException {
        clickPatternHandler.subscribe(1, false, null);
        clickPatternHandler.subscribe(1, true, null);
        clickPatternHandler.subscribe(2, true, mMockEventHandler);
        doShortClick(false);
        doLongClick(true);
    }

    public void testTwoClicksShortLong() throws InterruptedException {
        clickPatternHandler.subscribe(1, true, mMockEventHandler);
        doShortClick(false);
        runNClickTimerTask();
        doLongClick(false);
    }

    public void testTwoClicksLongShort() throws InterruptedException {
        EventHandler shortClickHandler = EasyMock.createStrictMock(EventHandler.class);
        clickPatternHandler.subscribe(1, true, mMockEventHandler);
        clickPatternHandler.subscribe(1, false, shortClickHandler);
        doLongClick(false);

        doShortClick(false);
        shortClickHandler.handle(EventType.MFSW_BUTTONS_RELEASED);
        EasyMock.replay(shortClickHandler);
        runNClickTimerTask();
        assertEquals(0, mMockTimer.timerTasks.size());
        EasyMock.verify(shortClickHandler);
    }

    private void doLongClick(boolean hasNClickTask) throws InterruptedException {
        doFirstClickEvent(EventType.DIAL, hasNClickTask);
        handleEvent(EventType.DIAL);
        assertEquals(0, mMockTimer.timerTasks.size());
        handleEvent(EventType.DIAL);
        assertEquals(0, mMockTimer.timerTasks.size());
        handleEvent(EventType.DIAL);
        assertEquals(0, mMockTimer.timerTasks.size());

        mMockClock.time += ClickPatternEventHandler.LONG_CLICK_MIN_DELAY;
        mMockEventHandler.handle(EventType.DIAL);
        EasyMock.replay(mMockEventHandler);
        handleEvent(EventType.DIAL);
        EasyMock.verify(mMockEventHandler);

        assertEquals(0, mMockTimer.timerTasks.size());
        handleEvent(EventType.MFSW_BUTTONS_RELEASED);
        assertEquals(0, mMockTimer.timerTasks.size());
        handleEvent(EventType.MFSW_BUTTONS_RELEASED);
        assertEquals(0, mMockTimer.timerTasks.size());
        handleEvent(EventType.MFSW_BUTTONS_RELEASED);
        assertEquals(0, mMockTimer.timerTasks.size());
        handleEvent(EventType.MFSW_BUTTONS_RELEASED);
        assertEquals(0, mMockTimer.timerTasks.size());
    }

    private void doShortClick(boolean hasNClickTask) throws InterruptedException {
        doFirstClickEvent(EventType.DIAL, hasNClickTask);
        handleEvent(EventType.DIAL);
        assertEquals(0, mMockTimer.timerTasks.size());
        handleEvent(EventType.DIAL);
        assertEquals(0, mMockTimer.timerTasks.size());
        handleEvent(EventType.DIAL);
        assertEquals(0, mMockTimer.timerTasks.size());

        handleEvent(EventType.MFSW_BUTTONS_RELEASED);
        assertEquals(ClickPatternEventHandler.NCLICK_MAX_DELAY, Iterables.getOnlyElement(mMockTimer.timerTasks).sheduledDelay);
        handleEvent(EventType.MFSW_BUTTONS_RELEASED);
        assertEquals(ClickPatternEventHandler.NCLICK_MAX_DELAY, Iterables.getOnlyElement(mMockTimer.timerTasks).sheduledDelay);
        handleEvent(EventType.MFSW_BUTTONS_RELEASED);
        assertEquals(ClickPatternEventHandler.NCLICK_MAX_DELAY, Iterables.getOnlyElement(mMockTimer.timerTasks).sheduledDelay);
        handleEvent(EventType.MFSW_BUTTONS_RELEASED);
        assertEquals(ClickPatternEventHandler.NCLICK_MAX_DELAY, Iterables.getOnlyElement(mMockTimer.timerTasks).sheduledDelay);
    }

    private void doFirstClickEvent(SimpleEvent event, boolean hasNClickTask) {
        if (hasNClickTask) {
            assertEquals(ClickPatternEventHandler.NCLICK_MAX_DELAY, Iterables.getOnlyElement(mMockTimer.timerTasks).sheduledDelay);
        }
        handleEvent(event);
        if (hasNClickTask) {
            assertTrue(Iterables.getOnlyElement(mMockTimer.timerTasks).isCancelled());
            mMockTimer.timerTasks.remove(0);
        } else {
            assertEquals(0, mMockTimer.timerTasks.size());
        }
    }

    private void handleEvent(Event e) {
        for (EventHandlersGroup.EventTypeToHandler eventTypeToHandler : handlersGroup.getSubscriptions()) {
            if (eventTypeToHandler.getEventType().equals(e.getEventType())) {
                eventTypeToHandler.getHandler().handle(e);
            }
        }
    }

    public void runNClickTimerTask() throws InterruptedException {
        MockTimer.MockTimerTask timerTask = Iterables.getOnlyElement(mMockTimer.timerTasks);
        assertEquals(ClickPatternEventHandler.NCLICK_MAX_DELAY, timerTask.sheduledDelay);
        mMockTimer.timerTasks.remove(timerTask);
        timerTask.timerTask.run();
    }
}