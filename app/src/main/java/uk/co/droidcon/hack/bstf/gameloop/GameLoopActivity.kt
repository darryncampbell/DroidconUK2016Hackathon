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
    val recycler: RecyclerView by bindView(R.id.recycler)
    val gunName: View by bindView(R.id.gun_name)
    val gunImage: View by bindView(R.id.gun_image)
    val ammoCount: TextView by bindView(R.id.ammo_count)
    val ammoImage: View by bindView(R.id.ammo_image)

    var count = AMMO_COUNT
    var available = AMMO_COUNT * 5;
    var gunEmpty = false

    lateinit var localBroadcastManager: LocalBroadcastManager
    lateinit var soundManager: SoundManager
    lateinit var gameManager: BstfGameManager
    lateinit var adapter: PlayerStateAdapter
    lateinit var scanController: ScanController

    val reloadReceiver = ReloadReceiver()
    val subscriptions = CompositeSubscription()

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
        scanController = ScanControllerImpl.getInstance()

        updateTopUi()
    }

    override fun onResume() {
        super.onResume()
        val subscription = gameManager.observePlayerState()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    adapter.updateList(it)
                    if (!gameManager.amIAlive()) iAmKilled()
                }
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
        val profile = Profile.getProfileForId(tag) ?: return

        for (player in gameManager.otherPlayers()) {
            if (player.name == profile.superHeroName) {
                gameManager.shoot(player)
            }
        }
    }

    private fun shoot() {
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

        updateTopUi()
    }

    private fun iAmKilled() {
        // TODO:
        /*-Sound
        -UI
        -Timer => respawn
        -Block my scanning
        -After timeout => undo all stuff */
    }

    private fun gunReloaded() {
        if (available <= 0) return

        val deducted = Math.min(available, AMMO_COUNT)
        available -= deducted
        count += deducted

        gunEmpty = false
        scanController.setEnabled(true)
        updateTopUi()
    }

    private fun availableIncremented(inc: Int) {
        available += inc
    }

    fun updateTopUi() {
        ammoCount.text = "" + count
        text.visibility = if (gunEmpty) View.VISIBLE else View.GONE
        gunName.visibility = if (gunEmpty) View.GONE else View.VISIBLE
        gunImage.visibility = if (gunEmpty) View.GONE else View.VISIBLE
        ammoCount.visibility = if (gunEmpty) View.GONE else View.VISIBLE
        ammoImage.visibility = if (gunEmpty) View.GONE else View.VISIBLE
    }

    override fun onPause() {
        subscriptions.clear()
        super.onPause()
    }

    inner class ReloadReceiver() : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            gunReloaded()
        }
    }
}