package uk.co.droidcon.hack.bstf.game

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import butterknife.bindView
import uk.co.droidcon.hack.bstf.R
import uk.co.droidcon.hack.bstf.reload.battery.BatteryStateReceiver
import uk.co.droidcon.hack.bstf.scan.ScanController

open class HudActivity : AppCompatActivity() {

    companion object {
        var AMMO_COUNT = 15
    }

    protected var count = AMMO_COUNT
    protected var gunEmpty = false

    protected var localBroadcastManager: LocalBroadcastManager? = null
    internal var mediaPlayer: MediaPlayer? = null
    protected val reloadReceiver = ReloadReceiver()

    lateinit internal var scanController: ScanController

    val text: TextView by bindView(R.id.info)
    val ammoCount: TextView by bindView(R.id.ammo_count)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hud)

        setupScanController()

        setupShooting()

        // TODO: go fullscreen

        updateUi()
    }

    open fun setupScanController() {
        scanController = ScanController()
        scanController.onCreate(this)
    }

    override fun onDestroy() {
        scanController.onDestroy()
        super.onDestroy()
    }

    protected open fun setupShooting() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager!!.registerReceiver(reloadReceiver, IntentFilter(BatteryStateReceiver.ACTION_RELOAD))
    }

    protected fun shoot() {
        MediaPlayer.create(this, R.raw.pistol).start()
    }

    protected fun updateUi() {
        ammoCount.text = "" + count
        text.visibility = if(gunEmpty) View.VISIBLE else View.GONE
    }

    private fun gunReloaded() {
        count = AMMO_COUNT
        gunEmpty = false
        updateUi()
    }

    inner class ReloadReceiver() : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            gunReloaded()
        }
    }
}
