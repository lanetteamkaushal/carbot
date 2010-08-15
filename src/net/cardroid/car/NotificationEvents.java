package net.cardroid.car;

import com.google.common.base.Preconditions;

/**
 * Date: Apr 21, 2010
 * Time: 11:50:23 PM
 *
 * @author Lex Nikitin
 */
public class NotificationEvents {
    //roof-notify-closed                                            =t3BA 7 0000F0FFFFF8F8
    //roof-notify-f0-f8-closed-to-slight                            =t3BA 7 0000F0FFFFFBF8
    //roof-notify-open-full-tilt                                    =t3BA 7 FF08FBFFFFF8F8
    //roof-notify-48-from-00-to-ff-slight-to-full-after-roof-stopped=t3BA 7 48FFFDFFFFF8F8
    //roof-notify-before-move-01-00-to-ff                           =t3BA 7 01FFFDFFFFF9F8

    public static SimpleEvent roofTiltComplete(int openPercent) {
        Preconditions.checkArgument(0 <= openPercent && openPercent <= 100);
        return new SimpleEvent(0x3BA, "0000F" + Integer.toHexString(openPercent * 0x8 / 100) + "FFFFFBF8");
    }

    public static SimpleEvent ROOF_CLOSED = new SimpleEvent(0x3BA, "0000F0FFFFF8F8");

    public static SimpleEvent roofSlideComplete(int openPercent) {
        Preconditions.checkArgument(0 <= openPercent && openPercent <= 100);
        return new SimpleEvent(0x3BA, toHexLeadZeros(openPercent * 0x48 / 100, 2) + "FFFDFFFFF8F8");
    }

    public static SimpleEvent roofSlideStart(int openPercent) {
        Preconditions.checkArgument(0 <= openPercent && openPercent <= 100);
        return new SimpleEvent(0x3BA, toHexLeadZeros(openPercent * 0x48 / 100, 2) + "FFFDFFFFF9F8");
    }

    //window-l-position-full-down =t3B6 3 4FFE E0
    //window-left-position-full-up=t3B6 3 00FC E0
    public static SimpleEvent windowLMoveComplete(int openPercent) {
        Preconditions.checkArgument(0 <= openPercent && openPercent <= 100);
        return new SimpleEvent(0x3B6, toHexLeadZeros(0xFC + openPercent * 0x4F02 / 100, 4) + "E0");
    }

    private static String toHexLeadZeros(int value, int nDigits) {
        String result = Integer.toHexString(value);
        Preconditions.checkArgument(result.length() <= value, "Match is too big");
        return "0000000000000000".substring(0, result.length() - nDigits) + result;
    }
}
