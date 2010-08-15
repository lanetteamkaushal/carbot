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

    public BigInteger getDataBigInteger () {
        if (dataBigInteger == null) {
            dataBigInteger = new BigInteger(data, 16);
        }
        return dataBigInteger;
    }

	public String toString() {
		return "t" + Integer.toHexString(getDestination()).toUpperCase() + Integer.toHexString(getData().length() / 2).toUpperCase() + getData();
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