package uk.co.droidcon.hack.bstf;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.robotsandpencils.bluetoothtap.models.Beacon;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import uk.co.droidcon.hack.bstf.bluetooth.BluetoothTapController;
import uk.co.droidcon.hack.bstf.scan.ScanController;
import uk.co.droidcon.hack.bstf.scan.ScanControllerImpl;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.text_view) TextView textView;

    private ScanController scanController;
    private BluetoothTapController bluetoothController;

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

        bluetoothController = new BluetoothTapController(this);
    }

    @Override
    protected void onDestroy() {
        scanController.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupBluetoothTapController();
        bluetoothController.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bluetoothController.stop();
    }

    private void setupBluetoothTapController() {
        bluetoothController.observe()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Beacon>() {
                    @Override
                    public void call(Beacon beacon) {
                        Log.d(TAG, "call() called with: beacon = [" + beacon + "]");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG, "call() called with: throwable = [" + throwable + "]");
                    }
                });
    }


}
