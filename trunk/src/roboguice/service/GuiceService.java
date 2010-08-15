package roboguice.service;

import android.app.Application;
import android.app.Service;
import com.google.inject.Injector;
import roboguice.application.GuiceApplication;
import roboguice.inject.ContextScope;

/**
 * Date: Apr 11, 2010
 * Time: 1:11:32 PM
 *
 * @author Lex Nikitin
 */
public abstract class GuiceService extends Service {
    protected ContextScope scope;

    @Override
    public void onCreate() {
        super.onCreate();

        final Injector injector = getInjector();
        scope = injector.getInstance(ContextScope.class);
        scope.enter(this);
        injector.injectMembers(this);
    }

    /**
     * @see roboguice.application.GuiceApplication#getInjector()
     */
    public Injector getInjector() {
        return ((GuiceApplication) getApplication()).getInjector();
    }

    /**
     * Use for classes that implement abstract system services, such as
     * {@link android.accessibilityservice.AccessibilityService}
     */
    public static void onCreate(Service service, Application application) {
        Injector injector = ((GuiceApplication) application).getInjector();

        ContextScope scope = injector.getInstance(ContextScope.class);
        scope.enter(service);
        injector.injectMembers(service);
    }
}
