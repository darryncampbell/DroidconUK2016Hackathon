package uk.co.droidcon.hack.bstf;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import uk.co.droidcon.hack.bstf.bluetooth.BluetoothTapController;
import uk.co.droidcon.hack.bstf.scan.ScanController;
import uk.co.droidcon.hack.bstf.scan.ScanControllerImpl;

public class MainActivity extends AppCompatActivity {

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


}
