package uk.co.droidcon.hack.bstf.bluetooth;

import android.content.Context;

import com.robotsandpencils.bluetoothtap.BluetoothTapBeaconService;
import com.robotsandpencils.bluetoothtap.BluetoothTapDetector;
import com.robotsandpencils.bluetoothtap.models.Beacon;
import com.robotsandpencils.bluetoothtap.utils.Algorithm;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class BluetoothTapController implements BluetoothTapBeaconService.BeaconServiceListener, BluetoothTapDetector.TouchInListener {

    private Beacon beacon = new Beacon("BBE1114A-0CDB-433D-B0C3-97AD44F9F639", "C9:E7:D5:12:0B:42", 1000, 1000, -77);
    private PublishSubject<Beacon> beaconSubject = PublishSubject.create();
    private BehaviorSubject<Boolean> tapDetectionSubject = BehaviorSubject.create();
    private final BluetoothTapBeaconService service;
    private BluetoothTapDetector bluetoothTapDetector;
    private boolean touched;

    public BluetoothTapController(Context context) {
        service = BluetoothTapBeaconService.getInstance(context, true);
        service.setListener(this);

        bluetoothTapDetector = new BluetoothTapDetector(context, true);
        bluetoothTapDetector.setTouchInListener(this);
        bluetoothTapDetector.setAlgorithm(Algorithm.MAGNETIC_THRESHOLD);
        bluetoothTapDetector.setBeacon(beacon);
    }

    public Observable<Beacon> observeBeacon() {
        return beaconSubject.asObservable();
    }

    public Observable<Boolean> observeTapDetection() {
        return tapDetectionSubject;
    }

    public void start() {
        service.start();
        bluetoothTapDetector.onStart();
    }

    public void stop() {
        bluetoothTapDetector.onStop(false);
        service.stop();
    }

    @Override
    public void beaconDiscovered(Beacon beacon) {
        beaconSubject.onNext(beacon);
    }

    @Override
    public void touchInRegistered() {
        Timber.w("touchInRegistered");
        touched = !touched;
        tapDetectionSubject.onNext(touched);
    }

    @Override
    public void touchInNotRegistered() {
    }

    @Override
    public void updateTouchInProgress(int progress) {
//        Timber.w("updateTouchInProgress: %d", progress);
    }

}
