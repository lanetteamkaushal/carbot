/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.cardroid.io;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.*;
import java.util.concurrent.Semaphore;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class FakeDeviceConnector implements DeviceConnector {
    // Debugging
    private static final String TAG = "FakeDeviceConnector";
    private static final boolean D = true;
    
    private static final int CONNECTION_REJECTS = 2;
    private static final char EOF_BYTE = 'q';
    private static int connectionRejects = 0;

    private final PipedInputStream mPipedInputStream;
    private final PipedOutputStream mPipedOutputStream;
	private OutputStream mOutputStream;
	private Handler mHandler;
        
    public FakeDeviceConnector() {
    	super();
    	
    	mPipedInputStream = new PipedInputStream() {

			@Override
			public synchronized int read() throws IOException {
				int ch = super.read();
				
				if (ch == EOF_BYTE) throw new IOException();
				
				return ch;
			}

			@Override
			public synchronized int read(byte[] bytes, int offset, int count)
					throws IOException {
				int res = super.read(bytes, offset, count);
				
				if (new String(bytes, offset, count).indexOf(EOF_BYTE) != -1) throw new IOException();
				
				return res;
			}

			@Override
			public int read(byte[] b) throws IOException {
				int res = super.read(b);
				
				if (new String(b).indexOf(EOF_BYTE) != -1) throw new IOException();

				return res;
			}			
    	};
    	mPipedOutputStream = new PipedOutputStream();
    	try {
			mPipedInputStream.connect(mPipedOutputStream);
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}

        final Semaphore semaphore = new Semaphore(0);
        Thread thread = new Thread() {
			@Override public void run() {
				Looper.prepare();
				mHandler = new Handler();
                semaphore.release();
				Looper.loop();
			}
		};
		thread.start();
        semaphore.acquireUninterruptibly();

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		mOutputStream = new OutputStream() {
			@Override public void write(int oneByte) throws IOException {
				baos.write(oneByte);
								
				if (oneByte == '\r') {
					final byte [] bytes = baos.toByteArray();
					baos.reset();

					if (new String(bytes).equals("qq\r")) {
						close();
					} else {
						// PipedOutputStream requires that all communication happen on one thread, 
						// so using handler
						mHandler.post(new Runnable() {
							@Override public void run() {
								try {								
									mPipedOutputStream.write(bytes);
									mPipedOutputStream.write("z\r".getBytes());
								} catch (IOException e) {
									Log.e(TAG, e.toString(), e);
								}
							}
						});						
					}
				}
			}
		};
    }

    @Override
    public String getName() {
        return "FakeDevice";
    }

    @Override public void attemptToConnect() throws IOException {
		while (connectionRejects++ < CONNECTION_REJECTS) {
			throw new IOException();
		}
		
		connectionRejects = 0;
	}

	@Override public InputStream getSocketInputStream() throws IOException {
		return mPipedInputStream;
	}
	
	@Override public OutputStream getSocketOutputStream() throws IOException {
		return mOutputStream; 
	}

	@Override public void close() throws IOException {
		mPipedInputStream.close();
		mPipedOutputStream.close();
		mHandler.getLooper().quit();
	}
}
