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
import android.widget.ImageView
import android.widget.TextView
import android.widget.ViewSwitcher
import butterknife.bindView
import com.bumptech.glide.Glide
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import uk.co.droidcon.hack.bstf.BstfComponent
import uk.co.droidcon.hack.bstf.BstfGameManager
import uk.co.droidcon.hack.bstf.R
import uk.co.droidcon.hack.bstf.models.Player
import uk.co.droidcon.hack.bstf.models.Profile
import uk.co.droidcon.hack.bstf.models.ShotEvent
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

    val deathStateSwitcher: ViewSwitcher by bindView(R.id.loop_death_switcher)
    val killedByView: TextView by bindView(R.id.killed_by)
    val killerWhoView: TextView by bindView(R.id.question_mark)
    val killerRevealSwitcher: ViewSwitcher by bindView(R.id.killer_reveal)
    val killerImage: ImageView by bindView(R.id.killer_image)


    val reloadReceiver = ReloadReceiver()
    val subscriptions = CompositeSubscription()
    val respawnCommand = Runnable { respawn() }
    val revealKillerCommand = RevealCommand()

    var count = AMMO_COUNT
    var gunEmpty = false

    lateinit var localBroadcastManager: LocalBroadcastManager
    lateinit var soundManager: SoundManager

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

        val respawnScheduler = gameManager.observeRespawnTime()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { scheduleRespawn(it.second, it.first) }

        val deathEventHandler = gameManager.observeDeathEvent()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { iAmKilled() }

        subscriptions.add(subscription)
        subscriptions.add(scanSubscription)
        subscriptions.add(triggersSubscription)
        subscriptions.add(respawnScheduler)
        subscriptions.add(deathEventHandler)
    }

    private fun scheduleRespawn(timeRemaining: Long, event: ShotEvent) {
        ensureRespawning(timeRemaining, event)

        recycler.removeCallbacks(respawnCommand)
        scanController.setMode(ScanController.Mode.OFF)
        recycler.postDelayed(respawnCommand, timeRemaining)
    }

    private fun respawn() {
        count = AMMO_COUNT
        scanController.setMode(ScanController.Mode.HIGH)
        deathStateSwitcher.displayedChild = 0
        updateTopUi()
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
            scanController.setMode(ScanController.Mode.LOW)
        } else {
            soundManager.playSound(SoundManager.PISTOL)
        }

        updateTopUi()
    }

    private fun iAmKilled() {
        soundManager.playSound(SoundManager.DEATH)
        deathStateSwitcher.displayedChild = 1
    }

    private fun gunReloaded() {
        count = AMMO_COUNT
        gunEmpty = false
        scanController.setMode(ScanController.Mode.HIGH)
        updateTopUi()
    }

    private fun ensureRespawning(timeRemaining: Long, event: ShotEvent) {
        revealKillerCommand.killer = event.source

        val timeUntilReveal = timeRemaining - BstfGameManager.RESPAWN_DURATION_MILLIS / 4
        if (timeUntilReveal < 0) {
            // Already revealed
            killerRevealSwitcher.displayedChild = 1
        } else {
            killerRevealSwitcher.postDelayed(revealKillerCommand, timeUntilReveal)
        }
    }

    private fun revealKiller(killer: Player) {
        killerRevealSwitcher.displayedChild = 1
        val profile = Profile.getProfileForName(killer.name)
        Glide.with(this).load(profile.avatarId).into(killerImage)
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

    inner class RevealCommand(var killer: Player? = null) : Runnable {
        override fun run() {
            revealKiller(killer!!)
        }

    }
}