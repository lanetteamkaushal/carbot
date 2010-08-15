package net.cardroid.io;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Date: Apr 8, 2010
 * Time: 10:50:29 PM
 *
 * @author Lex Nikitin
 */
public interface DeviceConnector {
    void attemptToConnect() throws IOException;

    void close() throws IOException;

    InputStream getSocketInputStream() throws IOException;

    OutputStream getSocketOutputStream() throws IOException;

    String getName();
}
