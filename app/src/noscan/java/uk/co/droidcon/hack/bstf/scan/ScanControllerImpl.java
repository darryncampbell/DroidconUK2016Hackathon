package uk.co.droidcon.hack.bstf.scan;

import android.content.Context;

import rx.Observable;
import timber.log.Timber;

public class ScanControllerImpl implements ScanController {

    private static final ScanControllerImpl INSTANCE = new ScanControllerImpl();

    public static ScanControllerImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public void onResume(Context context) {
        Timber.v("onResume");
    }

    @Override
    public void onPause() {
        Timber.v("onPause");
    }

    @Override
    public Observable observeScanTrigger() {
        return Observable.empty();
    }

    @Override
    public Observable<String> observeScanResults() {
        return Observable.just("no-scan");
    }

    @Override
    public void setEnabled(boolean enabled) {
    }

}
