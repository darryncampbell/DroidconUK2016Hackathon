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
import android.widget.ViewAnimator
import butterknife.bindView
import com.bumptech.glide.Glide
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import uk.co.droidcon.hack.bstf.BstfComponent
import uk.co.droidcon.hack.bstf.BstfGameManager
import uk.co.droidcon.hack.bstf.NfcItemController
import uk.co.droidcon.hack.bstf.R
import uk.co.droidcon.hack.bstf.game.HudActivity
import uk.co.droidcon.hack.bstf.models.Player
import uk.co.droidcon.hack.bstf.models.Profile
import uk.co.droidcon.hack.bstf.models.ShotEvent
import uk.co.droidcon.hack.bstf.models.Weapon
import uk.co.droidcon.hack.bstf.reload.battery.BatteryStateReceiver
import uk.co.droidcon.hack.bstf.scan.ScanController
import uk.co.droidcon.hack.bstf.sounds.SoundManager
import java.lang.Math.min
import java.util.*

class GameLoopActivity : AppCompatActivity() {

    companion object {
        var AMMO_COUNT = 15
    }

    val text: TextView by bindView(R.id.info)
    val recycler: RecyclerView by bindView(R.id.recycler)
    val gunName: TextView by bindView(R.id.gun_name)
    val gunImage: ImageView by bindView(R.id.gun_image)
    val ammoCount: TextView by bindView(R.id.ammo_count)
    val ammoImage: ImageView by bindView(R.id.ammo_image)

    val deathStateSwitcher: ViewAnimator by bindView(R.id.loop_death_switcher)
    val killedByView: TextView by bindView(R.id.killed_by) // TODO animate if time
    val killerWhoView: TextView by bindView(R.id.question_mark) // TODO animate if time
    val killerRevealSwitcher: ViewAnimator by bindView(R.id.killer_reveal)
    val killerImage: ImageView by bindView(R.id.killer_image)


    val reloadReceiver = ReloadReceiver()
    val subscriptions = CompositeSubscription()
    val respawnCommand = Runnable { respawn() }
    val revealKillerCommand = RevealCommand()

    var count = AMMO_COUNT
    var available = AMMO_COUNT * 5
    var gunEmpty = false
    var weapon = Weapon.GLOCK

    lateinit var localBroadcastManager: LocalBroadcastManager
    lateinit var soundManager: SoundManager

    lateinit var gameManager: BstfGameManager
    lateinit var adapter: PlayerStateAdapter
    lateinit var scanController: ScanController
    lateinit var nfcItemController: NfcItemController
    lateinit var random: Random

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_loop)

        soundManager = SoundManager.getInstance(this)
        scanController = ScanControllerImpl.getInstance()

        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager.registerReceiver(reloadReceiver, IntentFilter(BatteryStateReceiver.ACTION_RELOAD))

        gameManager = BstfComponent.getBstfGameManager()
        adapter = PlayerStateAdapter()
        adapter.updateList(gameManager.playerStateList())

        recycler.adapter = adapter
        recycler.itemAnimator = DefaultItemAnimator()

        gameManager.gameStarted()

        nfcItemController = NfcItemController()
        nfcItemController.setupNfcAdapter(this, this.javaClass)

        random = Random(1337)

        updateWeaponUi()
        updateTopUi()
    }

    override fun onResume() {
        super.onResume()
        scanController.onResume(this)
        nfcItemController.onResume(this)

        val subscription = gameManager.observePlayerState()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { adapter.updateList(it) }

        val scanSubscription = scanController.observeScanResults()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { parseHit(it) }

        val triggersSubscription = scanController.observeScanTrigger()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { shoot() }

        val nfcItemPickupSubscription = nfcItemController.observeItemResults().
                subscribeOn(Schedulers.computation()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe { parseItem(it) }

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
        subscriptions.add(nfcItemPickupSubscription)
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

    fun parseItem(item: NfcItemController.Item) {
        when (item) {
            NfcItemController.Item.LASER -> switchToWeapon(Weapon.LASER)
            NfcItemController.Item.GLOCK -> switchToWeapon(Weapon.GLOCK)
            NfcItemController.Item.AMMO -> {
                availableIncremented(AMMO_COUNT)
                updateWeaponUi()
                updateTopUi()
            }
        }
    }

    private fun switchToWeapon(newWeapon: Weapon) {
        weapon = newWeapon
        count = HudActivity.AMMO_COUNT
        gunEmpty = false
        updateWeaponUi()
        updateTopUi()
    }

    private fun shoot() {
        if (gunEmpty) {
            soundManager.playSound(weapon.emptySoundId)
            // TODO: animate ammo
            return
        }

        count--
        if (count <= 0) {
            gunEmpty = true
            scanController.setMode(ScanController.Mode.LOW)
        } else {
            soundManager.playSound(weapon.shootSoundId)
        }

        updateTopUi()
    }

    private fun iAmKilled() {
        var randomDeathSound = random.nextInt(9) % 2 == 0
        soundManager.playSound(if (randomDeathSound) soundManager.painSoundId else soundManager.deadSoundId)
        deathStateSwitcher.displayedChild = 1
    }

    protected fun updateWeaponUi() {
        gunImage.setImageResource(weapon.imageId)
        gunName.text = weapon.name
        ammoImage.setImageResource(weapon.ammoImageId)
    }

    private fun gunReloaded() {
        if (available <= 0) return
        soundManager.playSound(weapon.reloadSoundId)

        val deducted = min(min(available, AMMO_COUNT), AMMO_COUNT - count)
        available -= deducted
        count += deducted

        gunEmpty = false
        scanController.setMode(ScanController.Mode.HIGH)
        updateTopUi()
    }

    private fun availableIncremented(inc: Int) {
        available += inc
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
        ammoCount.text = count.toString() + " / " + available
        text.visibility = if (gunEmpty) View.VISIBLE else View.GONE
        gunName.visibility = if (gunEmpty) View.GONE else View.VISIBLE
        gunImage.visibility = if (gunEmpty) View.GONE else View.VISIBLE
        ammoCount.visibility = if (gunEmpty) View.GONE else View.VISIBLE
        ammoImage.visibility = if (gunEmpty) View.GONE else View.VISIBLE
    }

    override fun onPause() {
        nfcItemController.onPause(this)
        scanController.onPause()
        subscriptions.clear()
        super.onPause()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        nfcItemController.handleIntent(intent)
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