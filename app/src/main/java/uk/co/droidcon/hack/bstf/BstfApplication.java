package uk.co.droidcon.hack.bstf;

import android.app.Application;

import timber.log.Timber;

public class BstfApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
    }

}
