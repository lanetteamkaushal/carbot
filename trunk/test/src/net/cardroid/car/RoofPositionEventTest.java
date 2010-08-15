package net.cardroid.car;

import junit.framework.TestCase;
import net.cardroid.can.CanMessage;

/**
* Date: Apr 22, 2010
* Time: 12:10:54 AM
*
* @author Lex Nikitin
*/
public class RoofPositionEventTest extends TestCase {
    //roof-notify-closed                                            =t3BA 7 0000F0FFFFF8F8
    //roof-notify-f0-f8-closed-to-slight                            =t3BA 7 0000F0FFFFFBF8
    //roof-notify-open-full-tilt                                    =t3BA 7 FF08FBFFFFF8F8
    //roof-notify-48-from-00-to-ff-slight-to-full-after-roof-stopped=t3BA 7 48FFFDFFFFF8F8
    //roof-notify-before-move-01-00-to-ff                           =t3BA 7 01FFFDFFFFF9F8

	public void testRoofClosed() {
        RoofPositionEvent roofEvent = createRoofEvent("0000F0FFFFF8F8");
        assertEquals(RoofPositionEvent.Action.ROOF_CLOSED, roofEvent.getAction());
    }

    public void testTiltFull() {
        RoofPositionEvent roofEvent = createRoofEvent("FF08FBFFFFF8F8");
        assertEquals(RoofPositionEvent.Action.ROOF_TILT_FULL, roofEvent.getAction());
    }

    public void testSlideComplete() {
        RoofPositionEvent roofEvent = createRoofEvent("48FFFDFFFFF8F8");
        assertEquals(RoofPositionEvent.Action.ROOF_SLIDE_COMPLETE, roofEvent.getAction());
        assertEquals(100, roofEvent.getPercentOpen());

        roofEvent = createRoofEvent("24FFFDFFFFF8F8");
        assertEquals(RoofPositionEvent.Action.ROOF_SLIDE_COMPLETE, roofEvent.getAction());
        assertEquals(50, roofEvent.getPercentOpen());
    }

    public void testSlideStart() {
        RoofPositionEvent roofEvent = createRoofEvent("24FFFDFFFFF9F8");
        assertEquals(RoofPositionEvent.Action.ROOF_SLIDE_START, roofEvent.getAction());
        assertEquals(50, roofEvent.getPercentOpen());
    }

    public void testTiltComplete() {
        RoofPositionEvent roofEvent = createRoofEvent("0000F4FFFFFBF8");
        assertEquals(RoofPositionEvent.Action.ROOF_TILT_COMPLETE, roofEvent.getAction());
        assertEquals(50, roofEvent.getPercentOpen());
    }

    private RoofPositionEvent createRoofEvent(String data) {
        return RoofPositionEvent.EVENT_TYPE.createEvent(new CanMessage(CanMessage.Type.CAN11, 0x3BA, data, -1));
    }
}