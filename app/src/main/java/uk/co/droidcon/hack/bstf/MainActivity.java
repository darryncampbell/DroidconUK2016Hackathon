package uk.co.droidcon.hack.bstf;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import uk.co.droidcon.hack.bstf.scan.ScanController;
import uk.co.droidcon.hack.bstf.scan.ScanControllerImpl;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView textView;

    private ScanController scanController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        textView = (TextView) findViewById(R.id.text_view);

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
    }

    @Override
    protected void onDestroy() {
        scanController.onDestroy();
        super.onDestroy();
    }

}
