package uk.co.droidcon.hack.bstf.bluetooth;

import android.content.Context;
import android.util.Log;

import com.robotsandpencils.bluetoothtap.BluetoothTapBeaconService;
import com.robotsandpencils.bluetoothtap.models.Beacon;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public class BluetoothTapController implements BluetoothTapBeaconService.BeaconServiceListener {

    private static final String TAG = BluetoothTapController.class.getSimpleName();
    private final BluetoothTapBeaconService service;
    private BehaviorSubject<Beacon> subject = BehaviorSubject.create();

    public BluetoothTapController(Context context) {
        service = BluetoothTapBeaconService.getInstance(context, true);
        service.setListener(this);
    }

    public Observable<Beacon> observe() {
        return subject.asObservable();
    }

    public void start() {
        Log.d(TAG, "start() called");
        service.start();
    }

    public void stop() {
        Log.d(TAG, "stop() called");
        service.stop();
    }

    @Override
    public void beaconDiscovered(Beacon beacon) {
        Log.d(TAG, "beaconDiscovered() called with: beacon = [" + beacon + "]");
        subject.onNext(beacon);
    }
}
