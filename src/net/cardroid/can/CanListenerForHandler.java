package net.cardroid.can;

import android.os.Handler;


public class CanListenerForHandler implements CanListener {
    private final Handler mHandler;
    private final CanListener mListener;

    public CanListenerForHandler(CanListener listener, Handler handler) {
        mHandler = handler;
        mListener = listener;
    }

    @Override public void connectionLost() {
        mHandler.post(new Runnable() {
            @Override public void run() {
                mListener.connectionLost();
            }
        });
    }

    @Override public void connected() {
        mHandler.post(new Runnable() {
            @Override public void run() {
                mListener.errorReceived();
            }
        });
    }

    @Override public void errorReceived() {
        mHandler.post(new Runnable() {
            @Override public void run() {
                mListener.errorReceived();
            }
        });
    }

	@Override public void messageReceived(final CanMessage message) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                mListener.messageReceived(message);
            }
        });
    }

	@Override public void beforeMessageSent(final String message) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                mListener.beforeMessageSent(message);
            }
        });
    }

	@Override public void unknownDataReceived(final String data) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                mListener.unknownDataReceived(data);
            }
        });
    }

	@Override public void commandExecuted() {
        mHandler.post(new Runnable() {
            @Override public void run() {
                mListener.commandExecuted();
            }
        });
    }
}