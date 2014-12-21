package com.glasstowerstudios.garrulo.app;

import android.app.Application;
import android.util.Log;

/**
 * Main Application class for Garrulo. Static instance is held and used for situations where a
 * UI-independent context is required.
 */
public class GarruloApplication extends Application {
    private static final String LOGTAG = GarruloApplication.class.getSimpleName();

    private static GarruloApplication sInstance;

    @Override
    public void onCreate() {
        sInstance = this;
    }

    public static GarruloApplication getInstance() {
        if (sInstance == null) {
            Log.e(LOGTAG, "Attempted to retrieve GarruloApplication instance before onCreate!");
        }

        return sInstance;
    }
}
