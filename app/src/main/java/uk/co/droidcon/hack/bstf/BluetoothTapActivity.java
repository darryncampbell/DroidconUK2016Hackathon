package uk.co.droidcon.hack.bstf;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.robotsandpencils.bluetoothtap.models.Beacon;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;
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
        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText("Ready to detect BluetoothTap Beacon touches");
                qrCodeView.setVisibility(View.INVISIBLE);
            }
        });

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
                .map(new Func1<Beacon, Pair<Beacon, Bitmap>>() {
                    @Override
                    public Pair<Beacon, Bitmap> call(Beacon beacon) {
                        try {
                            Timber.w("onNext: %s", beacon);
                            BitMatrix bitMatrix = new QRCodeWriter().encode(beacon.getUuid(), BarcodeFormat.QR_CODE, 400, 400);
                            int width = bitMatrix.getWidth();
                            int height = bitMatrix.getHeight();
                            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                            for (int x = 0; x < width; x++) {
                                for (int y = 0; y < height; y++) {
                                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                                }
                            }
                            return new Pair<>(beacon, bitmap);
                        } catch (Throwable t) {
                            Timber.e(t, "failed generating qr code");
                            return null;
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Pair<Beacon, Bitmap>>() {
                    @Override
                    public void call(Pair<Beacon, Bitmap> result) {
                        textView.setText(result.second != null ? "BEACON TOUCHED\nUUID: " + result.first.getUuid() : "Ready to detect BluetoothTap Beacon touches");
                        qrCodeView.setVisibility(result.second != null ? View.VISIBLE : View.INVISIBLE);
                        if (result.second != null) {
                            qrCodeView.setImageBitmap(result.second);
                        }

                    }
                });
    }


}
