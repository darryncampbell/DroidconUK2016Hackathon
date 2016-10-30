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
import butterknife.bindView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import uk.co.droidcon.hack.bstf.BstfComponent
import uk.co.droidcon.hack.bstf.BstfGameManager
import uk.co.droidcon.hack.bstf.NfcItemController
import uk.co.droidcon.hack.bstf.R
import uk.co.droidcon.hack.bstf.game.HudActivity
import uk.co.droidcon.hack.bstf.models.Profile
import uk.co.droidcon.hack.bstf.models.Weapon
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
    val gunName: TextView by bindView(R.id.gun_name)
    val gunImage: ImageView by bindView(R.id.gun_image)
    val ammoCount: TextView by bindView(R.id.ammo_count)
    val ammoImage: ImageView by bindView(R.id.ammo_image)

    var count = AMMO_COUNT
    var gunEmpty = false
    var weapon = Weapon.GLOCK

    lateinit var localBroadcastManager: LocalBroadcastManager
    lateinit var soundManager: SoundManager
    lateinit var gameManager: BstfGameManager
    lateinit var adapter: PlayerStateAdapter
    lateinit var scanController: ScanController
    lateinit var nfcItemController: NfcItemController

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

        nfcItemController = NfcItemController()
        nfcItemController.setupNfcAdapter(this, this.javaClass)

        updateWeaponUi()
        updateTopUi()
    }

    override fun onResume() {
        super.onResume()
        nfcItemController.onResume(this)
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

        val nfcItemPickupSubscription = nfcItemController.observeItemResults().
                subscribeOn(Schedulers.computation()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe { parseItem(it) }

        subscriptions.add(subscription)
        subscriptions.add(scanSubscription)
        subscriptions.add(triggersSubscription)
        subscriptions.add(nfcItemPickupSubscription)
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
        when(item) {
            NfcItemController.Item.LASER -> switchToWeapon(Weapon.LASER)
            NfcItemController.Item.GLOCK -> switchToWeapon(Weapon.GLOCK)
            NfcItemController.Item.AMMO -> {
                count = AMMO_COUNT
                gunEmpty = false
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
            scanController.setEnabled(false)
        } else {
            soundManager.playSound(weapon.shootSoundId)
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

    protected fun updateWeaponUi() {
        gunImage.setImageResource(weapon.imageId)
        gunName.text = weapon.name
        ammoImage.setImageResource(weapon.ammoImageId)
    }

    private fun gunReloaded() {
        soundManager.playSound(weapon.reloadSoundId)
        count = AMMO_COUNT
        gunEmpty = false
        scanController.setEnabled(true)
        updateTopUi()
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
        nfcItemController.onPause(this)
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
}