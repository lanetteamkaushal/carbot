/**
 * 
 */
package net.cardroid.can;


public interface CanListener {
	void messageReceived(CanMessage message);
	void beforeMessageSent(String message);
	void errorReceived();	
	void unknownDataReceived(String data);
	void connectionLost();
	void connected();
	void commandExecuted();
}