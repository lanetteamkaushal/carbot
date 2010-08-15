package net.cardroid.android;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;

import java.lang.reflect.Method;

/**
 * Date: Apr 13, 2010
 * Time: 2:22:18 AM
 *
 * @author Lex Nikitin
 */
public class IWindowManager {
    private static String TAG = "IWindowManager";

    private final Object mIWindowManager;

    public IWindowManager() {
        Object iWindowManager = null;
        try {
            Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
            Method getService = serviceManagerClass.getDeclaredMethod("getService", String.class);
            iWindowManager = getService.invoke(null, "window");

            Class<?> wmClassStub = Class.forName("android.view.IWindowManager$Stub");
            Method asInterface = wmClassStub.getDeclaredMethod("asInterface", IBinder.class);
            iWindowManager = asInterface.invoke(null, (IBinder)iWindowManager);
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        mIWindowManager = iWindowManager;
    }

    public boolean injectPointerEvent(MotionEvent me, boolean b) {
        try {
            Method declaredMethod = mIWindowManager.getClass().getDeclaredMethod("injectPointerEvent", MotionEvent.class, boolean.class);
            return (Boolean)declaredMethod.invoke(mIWindowManager, me, b);
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        return false;
    }

    public boolean injectTrackballEvent(MotionEvent me, boolean b) throws RemoteException {
        return false;
    }

    public boolean injectKeyEvent(KeyEvent ke, boolean sync) {
        try {
            Method declaredMethod = mIWindowManager.getClass().getDeclaredMethod("injectKeyEvent", KeyEvent.class, boolean.class);
            return (Boolean)declaredMethod.invoke(mIWindowManager, ke, sync);
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        return false;
    }

    /**
     * Retrieve the current screen orientation, constants as per
     * {@link android.view.Surface}.
     */
    public int getRotation() {
        try {
            Method declaredMethod = mIWindowManager.getClass().getDeclaredMethod("getRotation");
            return (Integer)declaredMethod.invoke(mIWindowManager);
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        return -1;
    }
}
