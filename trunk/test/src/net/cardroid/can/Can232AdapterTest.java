package net.cardroid.can;

import android.test.MoreAsserts;

import com.google.common.base.Preconditions;
import net.cardroid.io.ConnectionListener;
import net.cardroid.io.DeviceConnectionService;
import net.cardroid.io.DeviceConnector;
import net.cardroid.testing.Constants;
import net.cardroid.testing.MultithreadedTestCase;
import org.easymock.EasyMock;

import java.text.ParseException;
import java.util.concurrent.Semaphore;

public class Can232AdapterTest extends MultithreadedTestCase {
    private static final String COMMAND = "COMMAND";

    private CanMessage canMessage;
    private Can232AdapterImpl mCanAdapter;
    private MockCanListener mCanListener;
    private MockDeviceConnectionService mDeviceConnectionService;
    private ConnectionListener mConnectionListener;

    @Override protected void setUp() throws Exception {
        super.setUp();

        canMessage = CanMessageParser.parseMesage("t1230", -1);
        mCanAdapter = new Can232AdapterImpl();
        mCanAdapter.mSendMessageTimeoutMillis = Constants.MAX_WAIT_TIME_MILLIS * 3;
        mCanListener = new MockCanListener();
        mDeviceConnectionService = new MockDeviceConnectionService();
        mCanAdapter.addListener(mCanListener);
        mCanAdapter.attachTo(mDeviceConnectionService);
    }

    @Override protected void tearDown() throws Exception {
        try {
            Thread commandThread = mCanAdapter.mCommandThread;

            mCanListener.unblockAllNotifications();
            mDeviceConnectionService.unblockWrites();
            mCanAdapter.stop();

            if (commandThread != null) {
                waitForThread(commandThread);
            }
        } catch (Throwable e) {
            mThrowables.add(e);                
        }

        super.tearDown();
    }

    public void testScheduleCommand() throws ParseException {
        mCanListener.easy.beforeMessageSent(COMMAND);
        EasyMock.replay(mCanListener.easy);

        mCanAdapter.scheduleCommand(COMMAND);
        mCanListener.waitForNotificaiton();
        EasyMock.verify(mCanListener.easy);

        mCanListener.resumeNotificaiton();
        mDeviceConnectionService.waitForWrite();
        MoreAsserts.assertEquals((COMMAND + "\r").getBytes(), mDeviceConnectionService.mWriteOut);
        mDeviceConnectionService.resumeWrite();
    }

    public void testSendCommand() throws ParseException {
        // try in a wrong thread
        try {
            mCanAdapter.sendCommandSync(COMMAND);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }

        // now in command thread
        mCanListener.easy.beforeMessageSent(COMMAND);
        EasyMock.replay(mCanListener.easy);

        mCanAdapter.runInCommandThread(new Runnable() {
            @Override public void run() {
                mCanAdapter.sendCommandSync(COMMAND);
            }
        });

        mCanListener.waitForNotificaiton();
        EasyMock.verify(mCanListener.easy);

        mCanListener.resumeNotificaiton();
        mDeviceConnectionService.waitForWrite();
        MoreAsserts.assertEquals((COMMAND + "\r").getBytes(), mDeviceConnectionService.mWriteOut);
        mDeviceConnectionService.resumeWrite();
    }

    public void testSendMessage() throws ParseException {
        // try in a wrong thread
        try {
            mCanAdapter.sendMessageSync(canMessage);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }

        // now in command thread
        for(int i = 0; i < Can232AdapterImpl.MESSAGE_QUEUE_SIZE; i++) {
            mCanListener.easy.beforeMessageSent(EasyMock.eq(canMessage.toString()));
        }
        EasyMock.replay(mCanListener.easy);

        mCanAdapter.runInCommandThread(new Runnable() {
            @Override public void run() {
                for(int i = 0; i < Can232AdapterImpl.MESSAGE_QUEUE_SIZE + 1; i++) {
                    mCanAdapter.sendMessageSync(canMessage);
                }
            }
        });

        for(int i = 0; i < Can232AdapterImpl.MESSAGE_QUEUE_SIZE; i++) {
            mCanListener.waitForNotificaiton();
            mCanListener.resumeNotificaiton();

            mDeviceConnectionService.waitForWrite();
            MoreAsserts.assertEquals((canMessage.toString() + "\r").getBytes(), mDeviceConnectionService.mWriteOut);
            mDeviceConnectionService.resumeWrite();
        }
        // wait and verify that there was no calls scheduled
        waitForAsyncProcess();
        EasyMock.verify(mCanListener.easy);

        // schedule 'command executed' message, which should unblock 9-th command 
        EasyMock.reset(mCanListener.easy);
        mCanListener.easy.beforeMessageSent(EasyMock.eq(canMessage.toString()));
        mCanListener.easy.commandExecuted();
        EasyMock.replay(mCanListener.easy);
        // need new thread to process because mDeviceConnectionService and mCanListener
        // method invocations block execution
        new Thread() {
            @Override public void run() {
                mConnectionListener.messageReceived("z\r".getBytes(), -1);
            }
        }.start();

        // message is sent simultaneously with commandExecuted notification
        // TODO: can this result in ConcurrentModificationException in EasyMock?
        mCanListener.waitForNotificaiton();
        mCanListener.waitForNotificaiton();
        EasyMock.verify(mCanListener.easy);
        mCanListener.resumeNotificaiton();
        mCanListener.resumeNotificaiton();

        // now command is executed
        mDeviceConnectionService.waitForWrite();
        MoreAsserts.assertEquals((canMessage.toString() + "\r").getBytes(), mDeviceConnectionService.mWriteOut);
        mDeviceConnectionService.resumeWrite();
    }

    private class MockCanListener implements CanListener {
        private final Semaphore listenerIn = new Semaphore(0);
        private final Semaphore listenerOut = new Semaphore(0);
        final CanListener easy = EasyMock.createMock(CanListener.class);

        @Override
        public void messageReceived(CanMessage message) {
            easy.messageReceived(message);
            listenerIn.release();
            waitForSemaphore(listenerOut);
        }

        @Override
        public void beforeMessageSent(String message) {
            easy.beforeMessageSent(message);
            listenerIn.release();
            waitForSemaphore(listenerOut);
        }

        @Override
        public void errorReceived() {
            easy.errorReceived();
            listenerIn.release();
            waitForSemaphore(listenerOut);
        }

        @Override
        public void unknownDataReceived(String data) {
            easy.unknownDataReceived(data);
            listenerIn.release();
            waitForSemaphore(listenerOut);
        }

        @Override
        public void connectionLost() {
            easy.connectionLost();
            listenerIn.release();
            waitForSemaphore(listenerOut);
        }

        @Override public void connected() {
            easy.connected();
            listenerIn.release();
            waitForSemaphore(listenerOut);
        }

        @Override
        public void commandExecuted() {
            easy.commandExecuted();
            listenerIn.release();
            waitForSemaphore(listenerOut);
        }

        public void waitForNotificaiton() {
        	waitForSemaphore(listenerIn);
        }

        public void resumeNotificaiton() {
            listenerOut.release();
        }

        public void unblockAllNotifications() {
            listenerOut.release(100);
        }
    }

    private class MockDeviceConnectionService implements DeviceConnectionService {
        private final Semaphore writeIn = new Semaphore(0);
        private final Semaphore writeOut = new Semaphore(0);

        private ConnectionListener mRemoveListenerListener;
        private DeviceConnector mConnectDeviceConnector;
        private byte [] mWriteOut;
        private boolean stopInvoked;

        @Override public void disableConnectionAttempts() {
            throw new UnsupportedOperationException();
        }

        @Override public void enableConnectionAttempts() {
            throw new UnsupportedOperationException();
        }

        @Override public boolean isConnected() {
            throw new UnsupportedOperationException();
        }

        @Override public void addListener(ConnectionListener listener) {
            Preconditions.checkState(mConnectionListener == null);
            mConnectionListener = listener;
        }

        @Override public void removeListener(ConnectionListener listener) {
            mRemoveListenerListener = listener;
        }

        @Override public void setDeviceConnector(DeviceConnector deviceConnector) {
            mConnectDeviceConnector = deviceConnector;
        }

        @Override public void stop() {
            stopInvoked = true;
        }

        @Override public void write(byte[] out) {
            mWriteOut = out;
            writeIn.release();
            waitForSemaphore(writeOut);
        }

        @Override public DeviceConnector getDeviceConnector() {
            throw new UnsupportedOperationException();
        }

        public void waitForWrite() {
        	waitForSemaphore(writeIn);
        }

        public void resumeWrite() {
            writeOut.release();
        }

        public void unblockWrites() {
            writeOut.release(100);
        }
    }
}
