package uk.co.droidcon.hack.bstf.scan;

import android.content.Context;

import rx.Observable;

public class ScanControllerImpl implements ScanController {

    @Override
    public void onCreate(Context context) {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public Observable<String> observeScanResults() {
        return Observable.just("no-scan");
    }

    @Override
    public void setEnabled(boolean enabled) {
    }

}
