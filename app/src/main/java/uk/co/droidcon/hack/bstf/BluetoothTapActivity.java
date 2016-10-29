package uk.co.droidcon.hack.bstf;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import uk.co.droidcon.hack.bstf.bluetooth.BluetoothTapController;

public class BluetoothTapActivity extends AppCompatActivity {

    @BindView(R.id.start_game_qr)
    ImageView qrCodeView;
    private BluetoothTapController bluetoothController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_tap);
        ButterKnife.bind(this);

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
                        qrCodeView.setVisibility(tapped ? View.VISIBLE : View.INVISIBLE);
                    }
                });
    }

}
