package net.cardroid.can;

import net.cardroid.io.DeviceConnectionService;

public interface Can232Adapter {
    boolean isConnected();

    public enum Baud {
		B10K(0),
		B20K(1),
		B50K(2),
		B100K(3),
		B125K(4),
		B250K(5),
		B500K(6),
		B800K(7),
		B1M(8);
		private final int sMode;

		private Baud(int sMode) { this.sMode = sMode; }

		public int getMode() {
			return sMode;
		}
	}

    void attachTo(DeviceConnectionService deviceConnectionService);

    void stop();

    void addListener(CanListener listener);

    void removeListener(CanListener listener);

    void sendMessageSync(CanMessage message);

    void sendCommandSync(String command);

    void scheduleCommand(String command);

    void scheduleMessage(CanMessage message);

    void runInCommandThread(Runnable runnable);
}
