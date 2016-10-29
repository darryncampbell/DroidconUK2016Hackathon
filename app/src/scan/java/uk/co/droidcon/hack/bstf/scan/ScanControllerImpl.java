package uk.co.droidcon.hack.bstf.scan;

import android.content.Context;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.FEATURE_TYPE;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.ScannerConfig;
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

public class ScanControllerImpl implements ScanController, EMDKManager.EMDKListener, BarcodeManager.ScannerConnectionListener, Scanner.DataListener, Scanner.StatusListener {

    private EMDKManager emdkManager;
    private BarcodeManager barcodeManager;
    private Scanner scanner;
    private boolean scannerEnabled = true;

    private BehaviorSubject scannerTriggerSubject = BehaviorSubject.create();
    private BehaviorSubject<String> scanResultSubject = BehaviorSubject.create();

    @Override
    public void onCreate(Context context) {
        Timber.v("onCreate");
        final EMDKResults results = EMDKManager.getEMDKManager(context, this);
        Timber.v("onCreate: %s", results.statusCode.name());
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            throw new RuntimeException();
        }
    }

    @Override
    public void onResume() {
        Timber.v("onResume: %s, %s", emdkManager, barcodeManager);
        if (emdkManager != null) {
            initBarcodeManager();
            initScanner();
            setEnabled(scannerEnabled);
        }
    }

    @Override
    public void onPause() {
        Timber.v("onPause");
        destroyScanner();
        releaseEmdkManager();
    }

    @Override
    public void onDestroy() {
        Timber.v("onDestroy");
        destroyScanner();
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
        this.scannerEnabled = enabled;
        Timber.v("setEnabled: %b", enabled);
        try {
            if (enabled) {
                scanner.enable();
                read();
            }
            configure();
        } catch (ScannerException e) {
            Timber.v(e, "cannot enable: %b", enabled);
        }
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
        Timber.v("onOpened");
        this.emdkManager = emdkManager;

        initBarcodeManager();
        initScanner();
        setEnabled(true);
    }

    private void initBarcodeManager() {
        barcodeManager = (BarcodeManager) emdkManager.getInstance(FEATURE_TYPE.BARCODE);
        barcodeManager.addConnectionListener(this);
    }

    private void initScanner() {
        this.scanner = getScanner();
        this.scanner.addDataListener(this);
        this.scanner.addStatusListener(this);
    }

    private Scanner getScanner() {
        for (ScannerInfo devicesInfo : barcodeManager.getSupportedDevicesInfo()) {
            if (devicesInfo.getConnectionType() == ConnectionType.INTERNAL
                    && devicesInfo.getDecoderType() == DecoderType.TWO_DIMENSIONAL
                    && devicesInfo.getDeviceType() == DeviceType.IMAGER) {
                return barcodeManager.getDevice(devicesInfo);
            }
        }
        throw new RuntimeException("cannot get scanner");
    }

    private void read() {
        try {
            scanner.read();
        } catch (ScannerException e) {
            Timber.e(e, "cannot read");
        }
    }

    private void configure() {
        try {
            final ScannerConfig config = scanner.getConfig();

            config.scanParams.decodeHapticFeedback = true;
            config.scanParams.decodeLEDTime = 1000;
            config.readerParams.readerSpecific.imagerSpecific.beamTimer = 300;
            config.readerParams.readerSpecific.laserSpecific.powerMode = ScannerConfig.PowerMode.HIGH;
            config.readerParams.readerSpecific.imagerSpecific.aimingPattern = scannerEnabled ? ScannerConfig.AimingPattern.ON : ScannerConfig.AimingPattern.OFF;
            config.readerParams.readerSpecific.imagerSpecific.illuminationMode = scannerEnabled ? ScannerConfig.IlluminationMode.ON : ScannerConfig.IlluminationMode.OFF;
            config.readerParams.readerSpecific.imagerSpecific.illuminationBrightness = 10;

            scanner.setConfig(config);
        } catch (ScannerException e) {
            Timber.v(e, "cannot configure");
        }
    }

    @Override
    public void onClosed() {
        Timber.v("onClosed");
        releaseEmdkManager();
    }

    private void destroyScanner() {
        Timber.v("destroyScanner: %s", scanner);
        if (scanner != null) {
            try {
                scanner.cancelRead();
                scanner.disable();
            } catch (ScannerException e) {
                Timber.v(e, "cannot disable scanner");
            }
            scanner.removeDataListener(this);
            scanner.removeStatusListener(this);
            try {
                scanner.release();
            } catch (ScannerException e) {
                Timber.v(e, "cannot release scanner");
            }

            scanner = null;
        }
    }

    private void releaseEmdkManager() {
        Timber.v("releaseEmdkManager: %s", emdkManager);
        if (barcodeManager != null) {
            barcodeManager.removeConnectionListener(this);
            barcodeManager = null;
        }

        if (emdkManager != null) {
            emdkManager.release();
            emdkManager = null;
        }
    }

    @Override
    public void onConnectionChange(ScannerInfo scannerInfo, BarcodeManager.ConnectionState connectionState) {
        Timber.v("onConnectionChange: %s", connectionState);
    }

    @Override
    public void onData(ScanDataCollection scanDataCollection) {
        Timber.v("onData: %s", scanDataCollection.getFriendlyName());
        ArrayList<ScanDataCollection.ScanData> scanData = scanDataCollection.getScanData();
        for (ScanDataCollection.ScanData data : scanData) {
            scanResultSubject.onNext(data.getData());
        }
    }

    @Override
    public void onStatus(StatusData statusData) {
        Timber.v("onStatus: %s %s", statusData.getFriendlyName(), statusData.getState());
        switch (statusData.getState()) {
            case IDLE:
                configure();
                read();
                break;
            case SCANNING:
                scannerTriggerSubject.onNext(new Object());
                break;
        }
    }
}
