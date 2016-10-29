package uk.co.droidcon.hack.bstf;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import uk.co.droidcon.hack.bstf.scan.ScanController;
import uk.co.droidcon.hack.bstf.scan.ScanControllerImpl;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "abc";
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.text_view)
    TextView textView;

    private ScanController scanController;
    private BeaconManager beaconManager;

    private Region region = new Region("monitored region", UUID.fromString("BBE1114A-0CDB-433D-B0C3-97AD44F9F639"), 1001, 1001);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        scanController = new ScanControllerImpl();
        scanController.onCreate(this);

        scanController.observeScanResults()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        textView.setText(s);
                    }
                });

        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Log.d(TAG, "onBeaconsDiscovered() called with: region = [" + region + "], list = [" + list + "]");
                    Beacon nearestBeacon = list.get(0);
                    Log.d(TAG, "nearest beacon: " + nearestBeacon);
                }
            }
        });
        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                Log.d(TAG, "onEnteredRegion() called with: region = [" + region + "], list = [" + list + "]");
            }

            @Override
            public void onExitedRegion(Region region) {
                Log.d(TAG, "onExitedRegion() called with: region = [" + region + "]");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        Log.d(TAG, "connect");
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                Log.d(TAG, "onServiceReady()");
                beaconManager.startMonitoring(region);
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "disconnect");
        beaconManager.stopMonitoring(region);
        beaconManager.stopRanging(region);
        beaconManager.disconnect();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        scanController.onDestroy();
        super.onDestroy();
    }

}
