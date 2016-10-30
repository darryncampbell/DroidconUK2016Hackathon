package uk.co.droidcon.hack.bstf.scan;

import android.content.Context;

import rx.Observable;

public class ScanControllerImpl implements ScanController {

    @Override
    public void onCreate(Context context) {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onDestroy() {
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
    public void setMode(Mode mode) {
    }

}
