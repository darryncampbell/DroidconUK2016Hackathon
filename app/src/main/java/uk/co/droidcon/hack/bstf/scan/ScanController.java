package uk.co.droidcon.hack.bstf.scan;

import android.content.Context;

import rx.Observable;

public interface ScanController {

    void onResume(Context context);

    void onPause();

    Observable observeScanTrigger();

    Observable<String> observeScanResults();

    void setMode(Mode mode);

    enum Mode {
        HIGH,
        LOW,
        OFF
    }
}
