package net.cardroid;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;
import net.cardroid.analysis.CanLog;
import net.cardroid.android.IWindowManager;
import net.cardroid.can.Can232Adapter;
import net.cardroid.can.Can232AdapterImpl;
import net.cardroid.car.CarConnection;
import net.cardroid.car.CarConnectionImpl;
import net.cardroid.io.DeviceConnectionService;
import net.cardroid.io.DeviceConnectionServiceImpl;
import net.cardroid.util.Clock;
import net.cardroid.util.RealClock;

/**
 * Date: Apr 10, 2010
 * Time: 6:55:48 PM
 *
 * @author Lex Nikitin
 */
public class CardroidModule implements Module {
    @Override
    public void configure(Binder binder) {
        binder.bind(DeviceConnectionService.class).to(DeviceConnectionServiceImpl.class);
        binder.bind(DeviceConnectionServiceImpl.class).in(Singleton.class);

        binder.bind(Can232Adapter.class).to(Can232AdapterImpl.class);
        binder.bind(Can232AdapterImpl.class).in(Singleton.class);

        binder.bind(CarConnection.class).to(CarConnectionImpl.class);
        binder.bind(CarConnectionImpl.class).in(Singleton.class);

        binder.bind(CanLog.class).toInstance(new CanLog());

        binder.bind(IWindowManager.class).toInstance(new IWindowManager());
        
        binder.bind(Clock.class).to(RealClock.class);
    }
}
