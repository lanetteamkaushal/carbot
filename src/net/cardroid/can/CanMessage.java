/**
 * 
 */
package net.cardroid.can;

import java.math.BigInteger;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CanMessage {
	public enum Type {
		CAN11,
		CAN29
	}

	private final CanMessage.Type   type; 
	private final int destination;
	private final String data;
	private final long   timestampMillis; 
	private BigInteger dataBigInteger;
	private short [] bytes;

	public CanMessage(CanMessage.Type type, int destination, String data, long timestampMillis) {
		this.type = checkNotNull(type);
		this.destination = destination;
		this.data = checkNotNull(data);
		this.timestampMillis = timestampMillis;
	}
	
	public CanMessage.Type    getType() { return type; }
	public int     getDestination    () { return destination; }
	public String  getData           () { return data; }
	public long    getTimestampMillis() { return timestampMillis; }

    public short [] getDataBytes () {
        if (bytes == null) {
            int nBytes = getData().length() / 2;
            bytes = new short[nBytes];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = Short.parseShort(getData().substring(i * 2, i * 2 + 2), 16);
            }
        }
        return bytes;
    }

    public BigInteger getDataBigInteger () {
        if (dataBigInteger == null) {
            dataBigInteger = new BigInteger(data, 16);
        }
        return dataBigInteger;
    }

	public String toString() {
        String destination = Integer.toHexString(getDestination());
        destination = "000".substring(destination.length()) + destination;
        return "t" + destination.toUpperCase() + Integer.toHexString(getData().length() / 2).toUpperCase() + getData();
	}

	public boolean sameMessage(CanMessage message) {
		return getType().equals(message.getType())
			&& getDestination() == message.getDestination()
			&& getData().equals(message.getData());
	}

	@Override public boolean equals(Object o) {
		if (!(o instanceof CanMessage)) return false;
		
		CanMessage message = (CanMessage) o;

		return sameMessage(message) && getTimestampMillis() == message.getTimestampMillis(); 
	}

	@Override public int hashCode() {
		return type.hashCode() + destination + data.hashCode() + (int)timestampMillis;
	}
}