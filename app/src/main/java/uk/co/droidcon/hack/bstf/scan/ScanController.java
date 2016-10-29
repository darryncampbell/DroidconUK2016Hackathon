package uk.co.droidcon.hack.bstf.scan;

import android.content.Context;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;

import timber.log.Timber;

public class ScanController implements EMDKManager.EMDKListener {

    private EMDKManager emdkManager;

    public void onCreate(Context context) {
        Timber.v("onCreate");
        final EMDKResults results = EMDKManager.getEMDKManager(context, this);
        Timber.v("result: %s", results);
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            throw new RuntimeException();
        }
    }

    public void onDestroy() {
        Timber.v("onDestroy");
        releaseEmdkManager();
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
        Timber.v("onOpened: %s", emdkManager);
        this.emdkManager = emdkManager;
    }

    @Override
    public void onClosed() {
        Timber.v("onClosed");
        releaseEmdkManager();
    }

    private void releaseEmdkManager() {
        if (emdkManager != null) {
            emdkManager.release();
            emdkManager = null;
        }
    }

}
