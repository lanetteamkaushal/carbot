package net.cardroid;

import android.content.*;
import android.os.IBinder;
import com.google.inject.Inject;
import com.google.inject.Module;
import net.cardroid.can.Can232Adapter;
import net.cardroid.car.CarConnection;
import net.cardroid.io.DeviceConnectionService;
import roboguice.application.GuiceApplication;

import java.util.List;

public class Cardroid extends GuiceApplication {
    private static String TAG = "CardroidModule";

    private static final String ADDRESS_MINILEX = "00:50:C2:7F:4D:EC";

    @Inject DeviceConnectionService mDeviceConnectionService;
	@Inject Can232Adapter mCanAdapter;
	@Inject CarConnection mCarConnection;

    ICardroidService mCardroidService;

    @Override
    protected void addApplicationModules(List<Module> modules) {
        modules.add(new CardroidModule());
    }

    /** Called when application is first created. */
    @Override
    public void onCreate() {
        super.onCreate();
        getInjector().injectMembers(this);
    }

    public boolean isFake() {
        return mCardroidService.isFake();
    }

    public void setCardroidService(ICardroidService iCardroidService) {
        mCardroidService = iCardroidService;
    }

    public ICardroidService getCardroidService() {
        return mCardroidService;
    }

}