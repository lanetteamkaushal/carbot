/**
 * 
 */
package net.cardroid.io;

public interface ConnectionListener {
    void connecting(DeviceConnector deviceConnector);
    void connected(DeviceConnector deviceConnector);
	void idle(int idleDelay);
    void connectionLost();
	void messageReceived(byte[] readBuf, long timestampMillis);
	void messageSent(byte[] writeBuf);
}