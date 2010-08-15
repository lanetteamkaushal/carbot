package net.cardroid.can;


public class CanListenerAdapter implements CanListener {

	@Override public void connected() {}
	@Override public void connectionLost() {}
	@Override public void errorReceived() {}
	@Override public void messageReceived(CanMessage message) {}
	@Override public void beforeMessageSent(String message) {}
	@Override public void unknownDataReceived(String data) {}
	@Override public void commandExecuted() {}
}
