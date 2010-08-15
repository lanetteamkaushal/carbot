package net.cardroid.car;

import net.cardroid.can.Can232Adapter;
import net.cardroid.can.CanMessage;
import net.cardroid.util.FormatUtil;

import java.math.BigInteger;

/**
 * Date: Apr 11, 2010
 * Time: 2:50:16 PM
 *
 * @author Lex Nikitin
 */
public class SimpleEvent implements EventType, Event {
    private final CanMessage mCanMessage;

    public SimpleEvent(int destination, String data) {
        mCanMessage = new CanMessage(CanMessage.Type.CAN11, destination, data, -1);
    }

    public SimpleEvent(int destination, BigInteger data, int length) {
        String value = FormatUtil.formatAsCanData(data, length);
        mCanMessage = new CanMessage(CanMessage.Type.CAN11, destination, value, -1);
    }

    public int getDestination() {
        return mCanMessage.getDestination();
    }

    public CanMessage getCanMessage() {
        return mCanMessage;
    }

    public BigInteger getIntData() {
        String data = mCanMessage.getData();
        byte [] values = new byte[data.length() / 2];
        for (int i = 0; i < values.length; i++) {
            values[i] = Byte.parseByte(data.substring(i * 2, i * 2 + 2), 16);
        }
        return new BigInteger(values);
    }

    public int nChars() {
        return mCanMessage.getData().length();
    }

    @Override public SimpleEvent getEventType() {
        return this;
    }

    @Override public CanMessage getMessage() {
        return mCanMessage;
    }

    @Override public SimpleEvent createEvent(CanMessage message) {
        return this;
    }

    @Override public boolean matches(CanMessage message) {
        return message.sameMessage(mCanMessage);
    }
}
