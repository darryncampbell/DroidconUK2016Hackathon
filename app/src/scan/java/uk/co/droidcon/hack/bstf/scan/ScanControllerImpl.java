package uk.co.droidcon.hack.bstf.scan;

import android.content.Context;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerInfo;
import com.symbol.emdk.barcode.ScannerInfo.ConnectionType;
import com.symbol.emdk.barcode.ScannerInfo.DecoderType;
import com.symbol.emdk.barcode.ScannerInfo.DeviceType;
import com.symbol.emdk.barcode.StatusData;

import java.util.ArrayList;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

public class ScanControllerImpl implements ScanController, EMDKManager.EMDKListener {

    private EMDKManager emdkManager;
    private Scanner scanner;

    private BehaviorSubject scannerTriggerSubject = BehaviorSubject.create();
    private BehaviorSubject<String> scanResultSubject = BehaviorSubject.create();

    @Override
    public void onCreate(Context context) {
        final EMDKResults results = EMDKManager.getEMDKManager(context, this);
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            throw new RuntimeException();
        }
    }

    @Override
    public void onDestroy() {
        releaseEmdkManager();
    }

    @Override
    public Observable observeScanTrigger() {
        return scannerTriggerSubject.asObservable();
    }

    @Override
    public Observable<String> observeScanResults() {
        return scanResultSubject.asObservable();
    }

    @Override
    public void setEnabled(boolean enabled) {
        try {
            if (enabled) {
                scanner.enable();
                read();
            } else {
                scanner.disable();
            }
        } catch (ScannerException e) {
            Timber.v(e, "cannot enable: %b", enabled);
        }
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
        this.emdkManager = emdkManager;

        final BarcodeManager barcodeManager = (BarcodeManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);
        barcodeManager.addConnectionListener(new BarcodeManager.ScannerConnectionListener() {
            @Override
            public void onConnectionChange(ScannerInfo scannerInfo, BarcodeManager.ConnectionState connectionState) {
                Timber.v("onConnectionChange: %s", scannerInfo.getFriendlyName());
            }
        });

        initScanner();
        setEnabled(true);
    }

    private void initScanner() {
        this.scanner = getScanner();
        this.scanner.addDataListener(new Scanner.DataListener() {
            @Override
            public void onData(ScanDataCollection scanDataCollection) {
                Timber.v("onData: %s", scanDataCollection.getFriendlyName());
                ArrayList<ScanDataCollection.ScanData> scanData = scanDataCollection.getScanData();
                for (ScanDataCollection.ScanData data : scanData) {
                    scanResultSubject.onNext(data.getData());
                }
            }
        });

        this.scanner.addStatusListener(new Scanner.StatusListener() {
            @Override
            public void onStatus(StatusData statusData) {
                switch (statusData.getState()) {
                    case IDLE:
                        read();
                        break;
                    case SCANNING:
                        scannerTriggerSubject.onNext(new Object());
                        break;
                }
                Timber.v("onStatus: %s %s", statusData.getFriendlyName(), statusData.getState());
            }
        });
    }

    private Scanner getScanner() {
        final BarcodeManager barcodeManager = (BarcodeManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);
        for (ScannerInfo devicesInfo : barcodeManager.getSupportedDevicesInfo()) {
            if (devicesInfo.getConnectionType() == ConnectionType.INTERNAL
                    && devicesInfo.getDecoderType() == DecoderType.TWO_DIMENSIONAL
                    && devicesInfo.getDeviceType() == DeviceType.IMAGER) {
                return barcodeManager.getDevice(devicesInfo);
            }
        }
        return null;
    }

    private void enable() {
        try {
            scanner.enable();
        } catch (ScannerException e) {
            Timber.e(e, "cannot enable");
        }
    }

    private void read() {
        try {
            scanner.read();
        } catch (ScannerException e) {
            Timber.e(e, "cannot read");
        }
    }

    @Override
    public void onClosed() {
        releaseEmdkManager();
    }

    private void releaseEmdkManager() {
        if (emdkManager != null) {
            emdkManager.release();
            emdkManager = null;
        }
    }

}
