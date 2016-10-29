package uk.co.droidcon.hack.bstf.scan;

import android.content.Context;

import rx.Observable;

public interface ScanController {

    void onCreate(Context context);

    void onDestroy();

    Observable<String> observeScanResults();

    void setEnabled(boolean enabled);

}
