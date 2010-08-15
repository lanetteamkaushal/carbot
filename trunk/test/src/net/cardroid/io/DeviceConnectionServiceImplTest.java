package net.cardroid.io;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IMocksControl;

import java.io.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class DeviceConnectionServiceImplTest extends TestCase {
    private DeviceConnectionServiceImpl mDeviceConnectionService;

    private ConnectionListener mConnectionListener;
    private DeviceConnector mDeviceConnector;
    private ByteArrayInputStream mInputStream;
    private ByteArrayOutputStream mOutputStream;
    private IMocksControl mStrictControl;
    private Semaphore mDoneSemaphore;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mDoneSemaphore = new Semaphore(0);
        mStrictControl = EasyMock.createStrictControl();
        mDeviceConnectionService = new DeviceConnectionServiceImpl();
        mConnectionListener = mStrictControl.createMock(ConnectionListener.class);
        mDeviceConnectionService.addListener(mConnectionListener);
        mDeviceConnector = mStrictControl.createMock(DeviceConnector.class);
        mInputStream = new ByteArrayInputStream(new byte[0]);
        mOutputStream = new ByteArrayOutputStream();
    }

    public void testConnect() throws IOException, InterruptedException {
        final DeviceConnectionServiceImpl.ConnectionThread[] connectionThread = new DeviceConnectionServiceImpl.ConnectionThread[1];

        mConnectionListener.connecting(mDeviceConnector);
        // start connecting
        mDeviceConnector.attemptToConnect();
        EasyMock.expect(mDeviceConnector.getSocketInputStream())
            .andReturn(mInputStream);
        EasyMock.expect(mDeviceConnector.getSocketOutputStream())
            .andReturn(mOutputStream);
        mConnectionListener.connected(mDeviceConnector);
        mConnectionListener.connectionLost();
        EasyMock.expectLastCall().andAnswer(new IAnswer<Void>() {
            @Override public Void answer() throws Throwable {
                connectionThread[0] = mDeviceConnectionService.mConnectionThread;
                mDeviceConnectionService.stop();
                mDoneSemaphore.release();
                return null;
            }
        });
        mDeviceConnector.close();

        mStrictControl.replay();
        mDeviceConnectionService.setDeviceConnector(mDeviceConnector);
        mDeviceConnectionService.enableConnectionAttempts();

        if (!mDoneSemaphore.tryAcquire(1, TimeUnit.SECONDS)) {
            fail("mConnectionListener.connectionLost was never executed");
        }
        connectionThread[0].join(1000);
        if (connectionThread[0].isAlive()) {
            fail("Connection thread is still alive");
        }
        mStrictControl.verify();
    }
}