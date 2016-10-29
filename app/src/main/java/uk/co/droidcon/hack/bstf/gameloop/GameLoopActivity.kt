package uk.co.droidcon.hack.bstf.gameloop

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import butterknife.bindView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import uk.co.droidcon.hack.bstf.BstfComponent
import uk.co.droidcon.hack.bstf.BstfGameManager
import uk.co.droidcon.hack.bstf.R
import uk.co.droidcon.hack.bstf.models.Profile
import uk.co.droidcon.hack.bstf.reload.battery.BatteryStateReceiver
import uk.co.droidcon.hack.bstf.scan.ScanController
import uk.co.droidcon.hack.bstf.scan.ScanControllerImpl
import uk.co.droidcon.hack.bstf.sounds.SoundManager

class GameLoopActivity : AppCompatActivity() {

    companion object {
        var AMMO_COUNT = 15
    }

    val text: TextView by bindView(R.id.info)
    val ammoCount: TextView by bindView(R.id.ammo_count)

    var count = AMMO_COUNT
    var gunEmpty = false

    lateinit var localBroadcastManager: LocalBroadcastManager
    lateinit var soundManager: SoundManager
    val reloadReceiver = ReloadReceiver()

    val recycler: RecyclerView by bindView(R.id.recycler)

    val subscriptions = CompositeSubscription()

    lateinit var gameManager: BstfGameManager
    lateinit var adapter: PlayerStateAdapter
    lateinit var scanController: ScanController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_loop)

        soundManager = SoundManager.getInstance(this)

        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager.registerReceiver(reloadReceiver, IntentFilter(BatteryStateReceiver.ACTION_RELOAD))

        gameManager = BstfComponent.getBstfGameManager()
        adapter = PlayerStateAdapter()
        adapter.updateList(gameManager.playerStateList())

        recycler.adapter = adapter
        recycler.itemAnimator = DefaultItemAnimator()

        gameManager.gameStarted()
        scanController = ScanControllerImpl()
        scanController.onCreate(this)

        updateAmmoCount()
    }

    override fun onResume() {
        super.onResume()
        scanController.onResume()
        val subscription = gameManager.observePlayerState().subscribe { adapter.updateList(it) }
        val scanSubscription = scanController.observeScanResults()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { parseHit(it) }

        val triggersSubscription = scanController.observeScanTrigger()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { shoot() }

        subscriptions.add(subscription)
        subscriptions.add(scanSubscription)
        subscriptions.add(triggersSubscription)
    }

    private fun parseHit(tag: String) {
        val profile = Profile.getProfileForId(tag)
        if (profile == null) return

        for (player in gameManager.otherPlayers()) {
            if (player.name == profile.superHeroName) {
                gameManager.shoot(player)
            }
        }
    }

    fun shoot() {
        if (gunEmpty) {
            soundManager.playSound(SoundManager.EMPTY_POP)
            // TODO: animate ammo
            return
        }

        count--
        if (count <= 0) {
            gunEmpty = true
            scanController.setEnabled(false)
        } else {
            soundManager.playSound(SoundManager.PISTOL)
        }

        updateAmmoCount()
    }

    private fun gunReloaded() {
        count = AMMO_COUNT
        gunEmpty = false
        scanController.setEnabled(true)
        updateAmmoCount()
    }

    fun updateAmmoCount() {
        ammoCount.text = "" + count
        text.visibility = if (gunEmpty) View.VISIBLE else View.GONE
    }

    override fun onPause() {
        scanController.onPause()
        subscriptions.clear()
        super.onPause()
    }

    override fun onDestroy() {
        scanController.onDestroy()
        super.onDestroy()
    }

    inner class ReloadReceiver() : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            gunReloaded()
        }
    }
}