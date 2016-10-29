package uk.co.droidcon.hack.bstf;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;

import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.droidcon.hack.bstf.models.Player;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.player_name)
    EditText playerNameEditText;

    private BstfGameManager bstfGameManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        bstfGameManager = new BstfGameManager(FirebaseDatabase.getInstance(), 1);
        bstfGameManager.startGame();
    }

    @OnClick(R.id.player_add)
    public void onAddPlayerClick() {
        String name = playerNameEditText.getText().toString();
        if (name.equals("")) {
            return;
        }

        bstfGameManager.addPlayer(new Player(name));
    }
}
