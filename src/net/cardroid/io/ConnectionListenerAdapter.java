/**
 * 
 */
package net.cardroid.io;

public class ConnectionListenerAdapter implements ConnectionListener {
    @Override public void connecting(DeviceConnector deviceConnector) {}
    @Override public void connected(DeviceConnector deviceConnector) {}
    @Override public void idle(int idleDelay) {}
	@Override public void messageReceived(byte[] readBuf, long timestampMillis) {}
	@Override public void messageSent(byte[] writeBuf) {}
	@Override public void connectionLost() {}
}