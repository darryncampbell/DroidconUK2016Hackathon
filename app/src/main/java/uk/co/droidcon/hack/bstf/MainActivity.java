package uk.co.droidcon.hack.bstf;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.droidcon.hack.bstf.scan.ScanController;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.edit_text) EditText editText;

    private ScanController scanController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        scanController = new ScanController();
        scanController.onCreate(this);

        editText.setText("yo");
    }

    @Override
    protected void onDestroy() {
        scanController.onDestroy();
        super.onDestroy();
    }

}
