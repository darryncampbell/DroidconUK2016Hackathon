package uk.co.droidcon.hack.bstf.gamestarting

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import uk.co.droidcon.hack.bstf.BstfComponent
import uk.co.droidcon.hack.bstf.BstfGameManager
import uk.co.droidcon.hack.bstf.R
import uk.co.droidcon.hack.bstf.scan.ScanController
import uk.co.droidcon.hack.bstf.scan.ScanControllerImpl

class GameStarterActivity : AppCompatActivity() {

    internal var scanController: ScanController = ScanControllerImpl.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_starter)

        scanController.observeScanResults().subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe { code ->
            Timber.d("scan result: %s", code)
            BstfComponent.setBstfGameManager(BstfGameManager(FirebaseDatabase.getInstance(), code))
            startActivity(Intent(this@GameStarterActivity, GameStartingActivity::class.java))
            finish()
        }

    }

}
