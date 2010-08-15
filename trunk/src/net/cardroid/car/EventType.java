package net.cardroid.car;

import com.google.common.base.Preconditions;
import net.cardroid.can.CanMessage;

import java.math.BigInteger;

/**
* Date: Apr 11, 2010
* Time: 2:47:56 PM
*
* @author Lex Nikitin
*/
public interface EventType<T extends Event> {
    // fog lamps
    public SimpleEvent FOG_REAR_NOTIFY_ON = new SimpleEvent(0x21A, "C512FF");
    public SimpleEvent FOG_FRONT_NOTIFY_ON = new SimpleEvent(0x21A, "A512FF");
    public SimpleEvent FOG_FRONT_DOWN = new SimpleEvent(0x224, "FDFF");
    public SimpleEvent FOG_FRONT_UP = new SimpleEvent(0x224, "FEFF");
    public SimpleEvent FOG_REAR_DOWN = new SimpleEvent(0x22C, "FDFF");
    public SimpleEvent FOG_REAR_UP = new SimpleEvent(0x22C, "FEFF");

    // locks
    public SimpleEvent DOORS_CLOSE = new SimpleEvent(0x2A0, "222218012A430B03");
    public SimpleEvent DOORS_OPEN = new SimpleEvent(0x2A0, "111118010C400B00");
    // One message is enough for our needs, but actually 09 counts from 00 to 0E
    public SimpleEvent REMOTE_LOCK = new SimpleEvent(0x2A0, "2222180120630503");
    public SimpleEvent REMOTE_UNLOCK = new SimpleEvent(0x2A0, "1111180112400600");
    public SimpleEvent TRUNK_OPEN = new SimpleEvent(0x2A0, "8888800004400600");
    public SimpleEvent DOORS_AND_TRUNK_OPEN = new SimpleEvent(0x2A0, "8000000000000000");
    public SimpleEvent REMOTE_OPEN_ALL = new SimpleEvent(0x2A0, "1111180018400600");
    public SimpleEvent REMOTE_CLOSE_ALL = new SimpleEvent(0x2A0, "333338001A440504");

    // MFSW events
    public SimpleEvent DIAL = new SimpleEvent(0x1D6, "C1FC");
    public SimpleEvent TRACK_PREV = new SimpleEvent(0x1D6, "D0FC");
    public SimpleEvent TRACK_NEXT = new SimpleEvent(0x1D6, "E0FC");
    public SimpleEvent VOICE_COMMAND = new SimpleEvent(0x1D6, "C0FD");
    public SimpleEvent VOLUME_UP = new SimpleEvent(0x1D6, "C8FC");
    public SimpleEvent VOLUME_DOWN = new SimpleEvent(0x1D6, "C4FC");
    public SimpleEvent MFSW_BUTTONS_RELEASED = new SimpleEvent(0x1D6, "C0FC");

    // Seats
    public SimpleEvent SEAT_HEAT_L_PUSH = new SimpleEvent(0x1E7, "FDFF");
    public SimpleEvent SEAT_HEAT_L_RELEASE = new SimpleEvent(0x1E7, "FCFF");
    public SimpleEvent SEAT_HEAT_L_NOTIFY_1 = new SimpleEvent(0x232, "1FFFFF");
    public SimpleEvent SEAT_HEAT_L_NOTIFY_2 = new SimpleEvent(0x232, "2FFFFF");
    public SimpleEvent SEAT_HEAT_L_NOTIFY_3 = new SimpleEvent(0x232, "3FFFFF");

    public SimpleEvent SEAT_HEAT_R_NOTIFY_1 = new SimpleEvent(0x22A, "1FFFFF");
    public SimpleEvent SEAT_HEAT_R_NOTIFY_2 = new SimpleEvent(0x22A, "2FFFFF");
    public SimpleEvent SEAT_HEAT_R_NOTIFY_3 = new SimpleEvent(0x22A, "3FFFFF");

    // Windows
    public SimpleEvent WINDOW_LR_UP = new SimpleEvent(0x102, "DBFF");
    public SimpleEvent WINDOW_LR_DOWN = new SimpleEvent(0x102, "C9FF");
    public SimpleEvent WINDOW_R_DOWN = new SimpleEvent(0x102, "C8FF");
    public SimpleEvent WINDOW_LR_STOP = new SimpleEvent(0x102, "C0FF");
    public SimpleEvent WINDOW_L_DOWN = new SimpleEvent(0x102, "C1FF");
    public SimpleEvent WINDOW_R_UP = new SimpleEvent(0x102, "E0FF");
    public SimpleEvent WINDOW_L_UP = new SimpleEvent(0x102, "C3FF");

    public SimpleEvent REMOTE_WINDOWS_DOWN = new SimpleEvent(0x26E, "097F4001FFFFFFFF");
    public SimpleEvent WINDOW_L_DOWN_STEP_NOPOWER = new SimpleEvent(0x26E, "0100000000000000");
    public SimpleEvent WINDOW_L_DOWN_HALF_NOPOWER = new SimpleEvent(0x26E, "0200000000000000");
    public SimpleEvent WINDOW_LR_DOWN_STEP_NOPOWER = new SimpleEvent(0x26E, "0900000000000000");
    public SimpleEvent WINDOW_R_DOWN_STEP_NOPOWER = new SimpleEvent(0x26E, "0800000000000000");
    public SimpleEvent WINDOW_L_UP_STEP_NOPOWER = new SimpleEvent(0x26E, "0300000000000000");
    public SimpleEvent WINDOW_R_UP_FULL_NOPOWER = new SimpleEvent(0x26E, "2000000000000000");
    public SimpleEvent WINDOW_LR_UP_FULL_NOPOWER = new SimpleEvent(0x26E, "2400000000000000");
    public SimpleEvent WINDOW_R_UP_STEP_NOPOWER = new SimpleEvent(0x26E, "1800000000000000");
    public SimpleEvent WINDOW_L_UP_FULL_NOPOWER = new SimpleEvent(0x26E, "0400000000000000");

    //roof
    public SimpleEvent ROOF_OPEN_SLIGHT = new SimpleEvent(0x26E, "0000010000000000");
    public SimpleEvent ROOF_CLOSE_SLIGHT = new SimpleEvent(0x26E, "0000060000000000");
    public SimpleEvent ROOF_OPEN_FULL = new SimpleEvent(0x26E, "0000020000000000");
    public SimpleEvent REMOTE_ROOF_AND_WIDOWS = new SimpleEvent(0x26E, "097F4901FFFFFFFF");
    public SimpleEvent ROOF_CLOSE_STEP = new SimpleEvent(0x26E, "0000030000000000");
    public SimpleEvent REMOTE_ROOF_OPEN = new SimpleEvent(0x26E, "007F490000000000");


    // misc
    public SimpleEvent CRUISE_NOTIFY_OFF = new SimpleEvent(0x200, "FF8FFCFFFFFFFFFF");
    public SimpleEvent CRUISE_NOTIFY_ON = new SimpleEvent(0x200, "FF8FFDFFFFFFFFFF");
    public SimpleEvent DIMMER_MAX = new SimpleEvent(0x202, "E3FF");
    public SimpleEvent DIMMER_MIN = new SimpleEvent(0x202, "00FF");
    public SimpleEvent DTC_NOTIFY_OFF = new SimpleEvent(0x19E, "00E09FFCFF0C0029");
    public SimpleEvent DTC_NOTIFY_ON = new SimpleEvent(0x338, "B80002F000FFFFFF");
    public SimpleEvent INTERIOR_LIGHTS_DIM_ON = new SimpleEvent(0x23A, "11300161");

    T createEvent(CanMessage message);
    boolean matches(CanMessage message);
}
