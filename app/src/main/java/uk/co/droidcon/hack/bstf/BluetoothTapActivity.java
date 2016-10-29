package uk.co.droidcon.hack.bstf;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import uk.co.droidcon.hack.bstf.bluetooth.BluetoothTapController;

public class BluetoothTapActivity extends AppCompatActivity {

    ImageView qrCodeView;
    private BluetoothTapController bluetoothController;
    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_tap);
        textView = (TextView) findViewById(R.id.text_view);
        qrCodeView = (ImageView) findViewById(R.id.start_game_qr);

        bluetoothController = new BluetoothTapController(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        susbcribeToBluetoothTapController();
        bluetoothController.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bluetoothController.stop();
    }

    private void susbcribeToBluetoothTapController() {
        bluetoothController.observeTapDetection()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean tapped) {
                        textView.setText(tapped ? "BEACON TOUCHED" : "Ready to detect BluetoothTap Beacon touches");
                        qrCodeView.setVisibility(tapped ? View.VISIBLE : View.INVISIBLE);
                    }
                });
    }

}
