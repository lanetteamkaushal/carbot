package net.cardroid.io;

import android.util.Log;
import com.google.common.annotations.VisibleForTesting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Date: Apr 10, 2010
 * Time: 6:33:26 PM
 *
 * @author Lex Nikitin
 */
public class DeviceConnectionServiceImpl implements DeviceConnectionService {
    // Debugging
    private static final String TAG = "DeviceConnectionService";
    private static final boolean D = true;

    // non-final for testing
    public static int WAIT_BEFORE_RETRY_MILLIS = 2000;

    private final ConnectionListenerInvocator mConnectionListenerInvocator;

    // Member fields
    @VisibleForTesting ConnectionThread mConnectionThread;

    public DeviceConnectionServiceImpl() {
        mConnectionListenerInvocator = new ConnectionListenerInvocator();
    }

    @Override public boolean isConnected() {
        ConnectionThread connectionThread = mConnectionThread;
        return connectionThread != null && connectionThread.isConnected();
    }

    @Override
    public void addListener(ConnectionListener listener) {
		mConnectionListenerInvocator.addListener(listener);
	}

	@Override
    public void removeListener(ConnectionListener listener) {
		mConnectionListenerInvocator.removeListener(listener);
	}

    /**
     * Start the ConnectionThread to initiate a connection to a remote device.
     * @param deviceConnector device to setDeviceConnector
     */
    @Override
    public synchronized void setDeviceConnector(DeviceConnector deviceConnector) {
        if (mConnectionThread == null) {
            // Start the thread to setDeviceConnector with the given device
            mConnectionThread = new ConnectionThread(deviceConnector);
            mConnectionThread.start();
        } else {
            // close existing connections if any
            try {
                mConnectionThread.mmDeviceConnector.close();
            } catch (IOException e) {
                Log.e(TAG, e.toString(), e);
            }

            mConnectionThread.mmDeviceConnector = deviceConnector;
        }
    }

    @Override public synchronized void disableConnectionAttempts() {
        mConnectionThread.disableConnectionAttempts();
    }

    @Override public void enableConnectionAttempts() {
        mConnectionThread.enableConnectionAttempts();
    }

    /**
     * Stop all threads
     */
    @Override
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");

        try {
            if (mConnectionThread != null) {
                mConnectionThread.cancel();
                mConnectionThread = null;
            }
		} catch (IOException e) {
			Log.e(TAG, e.toString(), e);
		}
    }

    /**
     * Write to OutputStream
     * @param out The bytes to write
     * @see ConnectionThread#write(byte[])
     */
    @Override
    public void write(byte[] out) {
        try {
            DeviceConnectionServiceImpl.ConnectionThread connectionThread = mConnectionThread;
            if (connectionThread != null) {
                connectionThread.write(out);
            } else {
                throw new IOException("Output stream is closed");
            }
        } catch (IOException e) {
            stop();
        }
    }

    @Override public DeviceConnector getDeviceConnector() {
        DeviceConnectionServiceImpl.ConnectionThread connectionThread = mConnectionThread;
        return connectionThread == null ? null : connectionThread.mmDeviceConnector;
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device.
     */
    @VisibleForTesting class ConnectionThread extends Thread {
        private DeviceConnector mmDeviceConnector;
        private boolean mmIsAllowedToConnect = false;
        private boolean mmIsStopped = false;
        private boolean mmIsConnected = false;
        private InputStream mmSocketInputStream;
        private OutputStream mmSocketOutputStream;

        public ConnectionThread(DeviceConnector deviceConnector) {
            mmDeviceConnector = deviceConnector;
        }

        public void run() {
            Log.i(TAG, "BEGIN ConnectionThread");
            setName("ConnectionThread");

            while(!mmIsStopped) {
                if (connect()) {
                    try {
                        mmIsConnected = true;
                        read();
                    } finally {
                        mConnectionListenerInvocator.connectionLost();
                        mmIsConnected = false;
                        mmSocketInputStream = null;
                        mmSocketOutputStream = null;
                    }
                }
            }

            synchronized (DeviceConnectionServiceImpl.this) {
                mConnectionThread = null;
            }
        }

        private boolean connect() {
            for (;;) {
                try {
                    synchronized (this) {
                        try {
                            if (!mmIsAllowedToConnect) wait();
                        } catch (InterruptedException e) {
                            Log.e(TAG, e.toString(), e);
                        }
                    }

                    if (mmIsStopped) {
                        return false;
                    }

                    // Make a connection
                    mConnectionListenerInvocator.connecting(mmDeviceConnector);

                    mmDeviceConnector.attemptToConnect();

                    mmSocketInputStream = mmDeviceConnector.getSocketInputStream();
                    mmSocketOutputStream = mmDeviceConnector.getSocketOutputStream();

                    mConnectionListenerInvocator.connected(mmDeviceConnector);

                    return true;
                } catch (IOException e) {
                    Log.i(TAG, e.toString(), e);
                    mmSocketInputStream = null;
                    mmSocketOutputStream = null;
                    mConnectionListenerInvocator.idle(WAIT_BEFORE_RETRY_MILLIS);

                    try {
                        Thread.sleep(WAIT_BEFORE_RETRY_MILLIS);
                    } catch (InterruptedException ie) {
                        Log.e(TAG, ie.toString());
                    }
                }
            }
        }

        private void read() {
            Log.i(TAG, "BEGIN reading");
            setName("ReaderThread");

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int ch;

            // Keep listening to the InputStream while connected
            try {
                while (!mmIsStopped) {
                    // Read from the InputStream
                    ch = mmSocketInputStream.read();
                    if (ch == -1) {
                    	throw new IOException("End of file");
                    }
                    buffer.write(ch);

                    if (ch == 0x7 || ch == 0xc || ch == 0xd) {
                        // Send the obtained bytes to the UI Activity
                        mConnectionListenerInvocator.messageReceived(buffer.toByteArray(), System.currentTimeMillis());
                        buffer.reset();
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "disconnected", e);
            }
        }

        public void cancel() throws IOException {
            mmIsStopped = true;
            mmDeviceConnector.close();
            enableConnectionAttempts();
        }

        public void write(byte[] out) throws IOException {
            OutputStream outputStream = mmSocketOutputStream;
            if (outputStream != null) {
                outputStream.write(out);
            } else {
                throw new IOException("Socket is closed");
            }
        }

        public boolean isConnected() {
            return mmIsConnected;
        }

        private synchronized void enableConnectionAttempts() {
            mmIsAllowedToConnect = true;
            notify();
        }

        private synchronized void disableConnectionAttempts() {
            mmIsAllowedToConnect = false;
        }
    }
}
