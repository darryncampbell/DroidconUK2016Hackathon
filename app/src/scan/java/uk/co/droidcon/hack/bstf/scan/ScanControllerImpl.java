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

    private static final ScanControllerImpl INSTANCE = new ScanControllerImpl();

    private EMDKManager emdkManager;
    private BarcodeManager barcodeManager;
    private Scanner scanner;
    private Mode scannerMode = Mode.HIGH;

    private BehaviorSubject scannerTriggerSubject = BehaviorSubject.create();
    private BehaviorSubject<String> scanResultSubject = BehaviorSubject.create();

    public static ScanControllerImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public void onResume(Context context) {
        Timber.v("onResume: %s, %s", emdkManager, barcodeManager);
        if (emdkManager == null) {
            final EMDKResults results = EMDKManager.getEMDKManager(context, this);
            Timber.v("onResume: %s", results.statusCode.name());
            if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
                throw new RuntimeException();
            }
        } else {
            initBarcodeManager();
            initScanner();
            setMode(Mode.HIGH);
        }
    }

    @Override
    public void onPause() {
        Timber.v("onPause");
        destroyScanner();
        releaseEmdkManager(true);
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
    public void setMode(Mode mode) {
        this.scannerMode = mode;
        Timber.v("setMode: %s", mode);
        boolean turnedOn = mode != Mode.OFF;
        try {
            if (turnedOn && scanner != null && !scanner.isEnabled()) {
                scanner.enable();
                read();
            }
            configure();
        } catch (ScannerException e) {
            Timber.v(e, "cannot enable scanner");
        }
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
        Timber.v("onOpened");
        this.emdkManager = emdkManager;

        initBarcodeManager();
        initScanner();
        setMode(Mode.HIGH);
    }

    private void initBarcodeManager() {
        barcodeManager = (BarcodeManager) emdkManager.getInstance(FEATURE_TYPE.BARCODE);
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
        if (scanner == null) return;
        
        try {
            final ScannerConfig config = scanner.getConfig();

            // TODO disable scanner shots when Mode == OFF

            config.scanParams.decodeHapticFeedback = true;
            config.scanParams.decodeLEDTime = 1000;
            config.scanParams.audioStreamType = ScannerConfig.AudioStreamType.RINGER;
            config.scanParams.decodeAudioFeedbackUri = null;
            config.scanParams.decodeLEDFeedback = true;

            config.readerParams.readerSpecific.imagerSpecific.beamTimer = 300;
            config.readerParams.readerSpecific.laserSpecific.powerMode = ScannerConfig.PowerMode.HIGH;
            config.readerParams.readerSpecific.imagerSpecific.aimingPattern =
                    scannerMode == Mode.HIGH ? ScannerConfig.AimingPattern.ON : ScannerConfig.AimingPattern.OFF;
            config.readerParams.readerSpecific.imagerSpecific.illuminationMode =
                    scannerMode == Mode.HIGH ? ScannerConfig.IlluminationMode.ON : ScannerConfig.IlluminationMode.OFF;
            config.readerParams.readerSpecific.imagerSpecific.illuminationBrightness = 10;

            scanner.setConfig(config);
        } catch (ScannerException e) {
            Timber.v(e, "cannot configure");
        }
    }

    @Override
    public void onClosed() {
        Timber.v("onClosed");
        releaseEmdkManager(false);
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

    private void releaseEmdkManager(boolean complete) {
        Timber.v("releaseEmdkManager: %s", emdkManager);
        barcodeManager = null;

        if (emdkManager != null) {
            if (complete) {
                emdkManager.release();
                emdkManager = null;
            } else {
                emdkManager.release(FEATURE_TYPE.BARCODE);
            }
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
                if (scannerMode != Mode.OFF) {
                    scannerTriggerSubject.onNext(new Object());
                }
                break;
        }
    }

}
