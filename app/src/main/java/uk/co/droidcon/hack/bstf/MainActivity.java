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
import timber.log.Timber;
import uk.co.droidcon.hack.bstf.scan.ScanController;
import uk.co.droidcon.hack.bstf.scan.ScanControllerImpl;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.text_view) TextView textView;

    private ScanController scanController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        Timber.v("init");

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        scanController.onResume();
    }

    @Override
    protected void onPause() {
        scanController.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Timber.v("onDestroy");
        scanController.onDestroy();
        super.onDestroy();
    }

}
