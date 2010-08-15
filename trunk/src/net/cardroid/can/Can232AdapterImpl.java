package net.cardroid.can;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.cardroid.io.ConnectionListener;
import net.cardroid.io.ConnectionListenerAdapter;
import net.cardroid.io.DeviceConnectionService;
import net.cardroid.io.DeviceConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Date: Apr 10, 2010
 * Time: 7:16:13 PM
 *
 * @author Lex Nikitin
 */
public class Can232AdapterImpl implements Can232Adapter {
    // Debugging
    private static final String TAG = "Can232Adapter";
    private static final boolean D = true;
    private static final int CONNECTION_SETUP_DELAY_MS = 500;
    private static final int CONNECTION_SETUP_TIMEOUT_MS = 20000;

    @VisibleForTesting static final int MESSAGE_QUEUE_SIZE = 8;

    // should depend on the baud rate
    @VisibleForTesting int mSendMessageTimeoutMillis = 100;

	private DeviceConnectionService mDeviceConnectionService;
	private List<CanListener> mListeners = Lists.newArrayList();
    private Handler mCommandHandler;
    private Semaphore mMessageSemaphore = new Semaphore(MESSAGE_QUEUE_SIZE);
    @VisibleForTesting Thread mCommandThread;
	
	@Override
    public void attachTo(DeviceConnectionService deviceConnectionService) {
        checkArgument(deviceConnectionService != null, "deviceConnectionService is null");
        checkArgument(!deviceConnectionService.isConnected(), "Can attach only to a disconnected service");
        checkState(mDeviceConnectionService == null,
            "Adapter is already attached to a service");

        mDeviceConnectionService = deviceConnectionService;
        mDeviceConnectionService.addListener(mConnectionListener);

        // start command thread and create handler
        final Semaphore semaphore = new Semaphore(0);
        mCommandThread = new Thread() {
            @Override public void run() {
                Looper.prepare();
                mCommandHandler = new Handler();
                semaphore.release();
                Looper.loop();
                mCommandThread = null;
            }
        };
        mCommandThread.setName("CommandThread");
        mCommandThread.start();
        semaphore.acquireUninterruptibly();
	}

    @Override public boolean isConnected() {
        return mDeviceConnectionService.isConnected();
    }

	@Override
    public void stop() {
        mCommandHandler.getLooper().quit();
		mDeviceConnectionService.stop();
        mDeviceConnectionService = null;
	}

	@Override
    public synchronized void addListener(CanListener listener) {
		ArrayList<CanListener> newListeners = Lists.newArrayList(mListeners);
		newListeners.add(listener);
		mListeners = newListeners;
	}

	@Override
    public synchronized void removeListener(CanListener listener) {
		ArrayList<CanListener> newListeners = Lists.newArrayList(mListeners);
		newListeners.remove(listener);
		mListeners = newListeners;
	}

	@Override
    public void sendMessageSync(CanMessage message) {
        Preconditions.checkState(Thread.currentThread() == mCommandThread,
            "Cannot send synchronous messages outside command thread");
        try {
            mMessageSemaphore.tryAcquire(mSendMessageTimeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, e.toString(), e);
        }
        writeCommand(message.toString());
	}

	@Override
    public void sendCommandSync(String command) {
        Preconditions.checkState(Thread.currentThread() == mCommandThread,
            "Cannot execute synchronous commands outside command thread");
        writeCommand(command);
    }

    @Override
    public void scheduleCommand(final String command) {
        Preconditions.checkState(mCommandThread != null, "Not attached");
		mCommandHandler.post(new Runnable() {
            @Override
            public void run() {
                sendCommandSync(command);
            }
        });
	}

	@Override
    public void scheduleMessage(final CanMessage message) {
        Preconditions.checkState(mCommandThread != null, "Not attached");
		mCommandHandler.post(new Runnable() {
            @Override
            public void run() {
                sendMessageSync(message);
            }
        });
	}

    @Override
    public void runInCommandThread(Runnable runnable) {
        mCommandHandler.post(runnable);
    }

    private void writeCommand(String command) {
        allListeners.beforeMessageSent(command);

        // adapter can be closed while we're sending a command
        DeviceConnectionService service = mDeviceConnectionService;
        if (service != null) {
            Log.i(TAG, "Executing command '" + command + "'");            
            service.write((command + "\r").getBytes());
        }
    }

    private void commandExecuted() {
        // semaphore can get out of sync command response is received
        // later then in {@link #mSendMessageTimeoutMillis}
        if (mMessageSemaphore.availablePermits() < MESSAGE_QUEUE_SIZE) {
            // considering the speed of connection vs android system,
            // is is very unlikely that number of permits will increase
            // in between check and release
            mMessageSemaphore.release();
        }

        // it is important that we release blocked command first, because that will
        // give a bit better throughput
        allListeners.commandExecuted();
    }

    private final ConnectionListener mConnectionListener = new ConnectionListenerAdapter() {
        private Timer mmInitializerThread = new Timer();

        @Override public void connected(DeviceConnector deviceConnector) {
            Log.i(TAG, "Connected to adapter");
            mmInitializerThread.cancel();
            mmInitializerThread = new Timer();
            mmInitializerThread.schedule(new TimerTask() {
                @Override public void run() {
                    Log.i(TAG, "Initializing adapter for 100K");
                    mDeviceConnectionService.write("C\r".getBytes());
                    mDeviceConnectionService.write("V\r".getBytes());
                    try {
                        Log.i(TAG, "Initializing adapter for 100K");
                        Thread.sleep(CONNECTION_SETUP_DELAY_MS);
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.toString(), e);
                    }
                }
            }, CONNECTION_SETUP_DELAY_MS, CONNECTION_SETUP_TIMEOUT_MS);
        }

        @Override
		public void messageReceived(byte[] readBuf, long timestampMillis) {
            String message = new String(readBuf);
            if(message.startsWith("V") && message.length() <= 6 && message.endsWith("\r")) {
                mmInitializerThread.cancel();
                mDeviceConnectionService.removeListener(this);
                mDeviceConnectionService.write("S3\r".getBytes());
                mDeviceConnectionService.write("O\r".getBytes());
                mDeviceConnectionService.addListener(mDeviceListener);
                allListeners.connected();
            }
		}

        @Override public void connectionLost() {
            mmInitializerThread.cancel();
        }

        @Override public void idle(int idleDelay) {
            mmInitializerThread.cancel();
        }

        @Override public void connecting(DeviceConnector deviceConnector) {
            mmInitializerThread.cancel();
        }
    };


    private final ConnectionListener mDeviceListener = new ConnectionListenerAdapter() {
        @Override public void connected(DeviceConnector deviceConnector) {
            allListeners.connected();
        }

        @Override
		public void messageReceived(byte[] readBuf, long timestampMillis) {
            if (readBuf.length == 1) {
            	if (readBuf[0] == 0x7) {
            		allListeners.errorReceived();
            	} else {
            		allListeners.unknownDataReceived(new String(readBuf, 0, readBuf.length - 1));
            	}
            } else {
            	if (readBuf.length == 2 && new String(readBuf).equals("z\r")) {
                    commandExecuted();
                } else {
                	CanMessage message;
                	try {
                		message = CanMessageParser.parseMesage(readBuf, 0, readBuf.length - 1, timestampMillis);
    				} catch (java.text.ParseException e) {
                		allListeners.unknownDataReceived(new String(readBuf, 0, readBuf.length - 1));
                		return;
    				}
    				allListeners.messageReceived(message);
            	}
            }
		}

		@Override
		public void messageSent(byte[] writeBuf) {
            allListeners.beforeMessageSent(new String(writeBuf, 0, writeBuf.length - 1));
		}

		@Override
		public void connectionLost() {
            mDeviceConnectionService.removeListener(this);
            mDeviceConnectionService.addListener(mConnectionListener);
			allListeners.connectionLost();
		}
    };

    private final CanListener allListeners = new CanListener() {

		@Override public void beforeMessageSent(String message) {
			List<CanListener> listeners;
			synchronized (this) {
				listeners = mListeners;
			}
			for (CanListener listener : listeners) listener.beforeMessageSent(message);
		}

		@Override public void messageReceived(CanMessage message) {
			List<CanListener> listeners;
			synchronized (this) {
				listeners = mListeners;
			}
			for (CanListener listener : listeners) listener.messageReceived(message);
		}

		@Override public void unknownDataReceived(String data) {
			List<CanListener> listeners;
			synchronized (this) {
				listeners = mListeners;
			}
			for (CanListener listener : listeners) listener.unknownDataReceived(data);
		}

		@Override public void errorReceived() {
			List<CanListener> listeners;
			synchronized (this) {
				listeners = mListeners;
			}
			for (CanListener listener : listeners) listener.errorReceived();
		}

		@Override public void connectionLost() {
			List<CanListener> listeners;
			synchronized (this) {
				listeners = mListeners;
			}
			for (CanListener listener : listeners) listener.connectionLost();
		}

        @Override public void connected() {
            List<CanListener> listeners;
            synchronized (this) {
                listeners = mListeners;
            }
            for (CanListener listener : listeners) listener.connected();
        }

        @Override public void commandExecuted() {
			List<CanListener> listeners;
			synchronized (this) {
				listeners = mListeners;
			}
			for (CanListener listener : listeners) listener.commandExecuted();
		}
    };
}
