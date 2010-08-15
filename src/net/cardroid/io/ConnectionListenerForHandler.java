/**
 * 
 */
package net.cardroid.io;

import android.os.Handler;

public class ConnectionListenerForHandler implements ConnectionListener {
    private final ConnectionListener mConnectionListener;
    private final Handler mHandler;

    public ConnectionListenerForHandler(ConnectionListener connectionListener, Handler handler) {
        mConnectionListener = connectionListener;
        mHandler = handler;
    }

    @Override public void connecting(final DeviceConnector deviceConnector) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                mConnectionListener.connecting(deviceConnector);
            }
        });
    }

    @Override public void connected(final DeviceConnector deviceConnector) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                mConnectionListener.connected(deviceConnector);
            }
        });
    }

    @Override public void idle(final int idleDelay) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                mConnectionListener.idle(idleDelay);
            }
        });
    }

	@Override public void messageReceived(final byte[] readBuf, final long timestampMillis) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                mConnectionListener.messageReceived(readBuf, timestampMillis);
            }
        });
    }

	@Override public void messageSent(final byte[] writeBuf) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                mConnectionListener.messageSent(writeBuf);
            }
        });
    }

	@Override public void connectionLost() {
        mHandler.post(new Runnable() {
            @Override public void run() {
                mConnectionListener.connectionLost();
            }
        });
    }
}