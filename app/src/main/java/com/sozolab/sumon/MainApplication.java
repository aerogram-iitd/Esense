package com.sozolab.sumon;

import android.app.Application;
import android.content.Context;
import android.location.LocationManager;

/**
 * @author rishabh-goel on 25-09-2020
 * @project Esense
 */
public class MainApplication extends Application {

    private static MainApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }


    public static MainApplication getApplication() {
        return application;
    }

    public static boolean isGPSEnabled(final boolean isLocationPermissionGranted) {
        boolean providerEnabled = false;
        if (isLocationPermissionGranted) {
            final LocationManager manager = (LocationManager) getApplication().getSystemService(Context.LOCATION_SERVICE);
            providerEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        return providerEnabled;
    }

}
