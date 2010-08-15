package net.cardroid.io;

import com.google.common.collect.Lists;

import java.util.List;

/**
* Date: Apr 6, 2010
* Time: 11:00:09 PM
*
* @author Lex Nikitin
*/
class ConnectionListenerInvocator implements ConnectionListener {
    private final List<ConnectionListener> mConnectionListeners;
    private List<ConnectionListener> mCopyOfConnectionListeners;

    public ConnectionListenerInvocator() {
        this.mConnectionListeners = Lists.newArrayList();
    }

    @Override public void messageSent(byte [] writeBuf) {
        for (ConnectionListener listener : getCopyOfConnectionListeners()) {
            listener.messageSent(writeBuf);
        }
    }

    @Override public void messageReceived(byte [] readBuf, long timestampMillis) {
        for (ConnectionListener listener : getCopyOfConnectionListeners()) {
            listener.messageReceived(readBuf, timestampMillis);
        }
    }

    @Override public void idle(int idleDelay) {
        for (ConnectionListener listener : getCopyOfConnectionListeners()) {
            listener.idle(idleDelay);
        }
    }

    @Override public void connectionLost() {
        for (ConnectionListener listener : getCopyOfConnectionListeners()) {
            listener.connectionLost();
        }
    }

    @Override public void connecting(DeviceConnector deviceConnector) {
        for (ConnectionListener listener : getCopyOfConnectionListeners()) {
            listener.connecting(deviceConnector);
        }
    }

    @Override public void connected(DeviceConnector deviceConnector) {
        for (ConnectionListener listener : getCopyOfConnectionListeners()) {
            listener.connected(deviceConnector);
        }
    }

    public synchronized void addListener(ConnectionListener listener) {
        mConnectionListeners.add(listener);
        mCopyOfConnectionListeners = null;
    }

    public synchronized void removeListener(ConnectionListener listener) {
        mConnectionListeners.remove(listener);
        mCopyOfConnectionListeners = null;
    }

    private synchronized List<ConnectionListener> getCopyOfConnectionListeners() {
        if (mCopyOfConnectionListeners == null) {
            mCopyOfConnectionListeners = Lists.newArrayList(mConnectionListeners);
        }
        return mCopyOfConnectionListeners;
    }
}
