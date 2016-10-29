package uk.co.droidcon.hack.bstf.game

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import butterknife.bindView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import uk.co.droidcon.hack.bstf.R
import uk.co.droidcon.hack.bstf.reload.battery.BatteryStateReceiver
import uk.co.droidcon.hack.bstf.scan.ScanController
import uk.co.droidcon.hack.bstf.scan.ScanControllerImpl
import uk.co.droidcon.hack.bstf.sounds.SoundManager

open class HudActivity : AppCompatActivity() {

    companion object {
        var AMMO_COUNT = 15
    }

    protected var count = AMMO_COUNT
    protected var gunEmpty = false

    protected var localBroadcastManager: LocalBroadcastManager? = null
    protected var soundManager: SoundManager? = null
    protected val reloadReceiver = ReloadReceiver()

    internal var scanController: ScanController? = null

    val text: TextView by bindView(R.id.info)
    val ammoCount: TextView by bindView(R.id.ammo_count)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hud)

        soundManager = SoundManager.getInstance(this)

        setupScanController()

        setupShooting()

        // TODO: go fullscreen

        updateUi()
    }

    open fun setupScanController() {
        scanController = ScanControllerImpl()
        scanController!!.onCreate(this)

        scanController!!.observeScanTrigger().
                subscribeOn(Schedulers.computation()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe { shoot() }
    }


    override fun onResume() {
        scanController.onResume()
        super.onResume()
    }

    override fun onPause() {
        scanController.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        scanController!!.onDestroy()
        super.onDestroy()
    }

    protected open fun setupShooting() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager!!.registerReceiver(reloadReceiver, IntentFilter(BatteryStateReceiver.ACTION_RELOAD))
    }

    protected fun shoot() {
        if (gunEmpty) {
            // TODO: empty sound
            // TODO: animate ammo
            return
        }

        count--
        if (count <= 0) {
            gunEmpty = true
            scanController!!.setEnabled(false)
        } else {
            // TODO: improve with soundPool
//            MediaPlayer.create(this, R.raw.pistol).start()
            soundManager!!.playSound(SoundManager.PISTOL)
        }

        updateUi()
    }

    protected fun updateUi() {
        ammoCount.text = "" + count
        text.visibility = if (gunEmpty) View.VISIBLE else View.GONE
    }

    private fun gunReloaded() {
        soundManager!!.playSound(SoundManager.RELOAD)

        count = AMMO_COUNT
        gunEmpty = false
        if (scanController != null) {
            scanController!!.setEnabled(true)
        }
        updateUi()
    }

    inner class ReloadReceiver() : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            gunReloaded()
        }
    }
}
