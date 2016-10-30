package uk.co.droidcon.hack.bstf.scan;

import android.content.Context;

import rx.Observable;

public interface ScanController {

    void onCreate(Context context);

    void onResume();

    void onPause();

    void onDestroy();

    Observable observeScanTrigger();

    Observable<String> observeScanResults();

    void setMode(Mode mode);

    enum Mode {
        HIGH,
        LOW,
        OFF
    }
}
