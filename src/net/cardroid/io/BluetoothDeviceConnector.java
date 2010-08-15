package net.cardroid.io;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Date: Apr 8, 2010
 * Time: 10:50:08 PM
 *
 * @author Lex Nikitin
 */
public class BluetoothDeviceConnector implements DeviceConnector {
    // Serial port UUID
	private static final UUID UUID_SERIAL_PORT = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    public BluetoothDeviceConnector(BluetoothDevice device) throws IOException {
        this.mDevice = device;
    }

    @Override public InputStream getSocketInputStream() throws IOException {
        return mInputStream;
    }

    @Override public OutputStream getSocketOutputStream() throws IOException {
        return mOutputStream;
    }

    @Override
    public String getName() {
        return mDevice.getName();
    }

    @Override public void attemptToConnect() throws IOException {
        try {
            this.mSocket = mDevice.createRfcommSocketToServiceRecord(UUID_SERIAL_PORT);

            // Always cancel discovery because it will slow down a connection
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

            // This is a blocking call and will only return on a
            // successful connection or an exception
            mSocket.connect();
            mInputStream = mSocket.getInputStream();
            mOutputStream = mSocket.getOutputStream();

            if (mInputStream == null || mOutputStream == null) {
                throw new IOException("Failed to open socket");
            }
        } catch (IOException e) {
            mSocket.close();
            throw e;
        }
    }

    @Override public void close() throws IOException {
        mSocket.close();
    }
}
