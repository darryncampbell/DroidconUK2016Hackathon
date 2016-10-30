package uk.co.droidcon.hack.bstf.game

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.bindView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import uk.co.droidcon.hack.bstf.NfcItemController
import uk.co.droidcon.hack.bstf.R
import uk.co.droidcon.hack.bstf.gameloop.GameLoopActivity
import uk.co.droidcon.hack.bstf.models.Weapon
import uk.co.droidcon.hack.bstf.reload.battery.BatteryStateReceiver
import uk.co.droidcon.hack.bstf.scan.ScanController
import uk.co.droidcon.hack.bstf.scan.ScanControllerImpl
import uk.co.droidcon.hack.bstf.sounds.SoundManager
import java.lang.Math.min

open class HudActivity : AppCompatActivity() {

    companion object {
        var AMMO_COUNT = 15
    }

    protected var count = AMMO_COUNT
    protected var available = AMMO_COUNT * 5
    protected var gunEmpty = false
    protected var weapon = Weapon.LASER

    protected var localBroadcastManager: LocalBroadcastManager? = null
    protected var soundManager: SoundManager? = null
    protected val reloadReceiver = ReloadReceiver()

    internal var scanController: ScanController? = null
    internal var nfcItemController: NfcItemController? = null

    val text: TextView by bindView(R.id.info)
    val gunName: TextView by bindView(R.id.gun_name)
    val gunImage: ImageView by bindView(R.id.gun_image)
    val ammoCount: TextView by bindView(R.id.ammo_count)
    val ammoImage: ImageView by bindView(R.id.ammo_image)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hud)

        soundManager = SoundManager.getInstance(this)

        setupScanController()
        setupNfcItemController()

        setupShooting()

        updateWeaponUi()
        updateTopUi()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        nfcItemController?.handleIntent(intent)
    }

    open fun setupScanController() {
        scanController = ScanControllerImpl.getInstance()
        scanController!!.observeScanTrigger().
                subscribeOn(Schedulers.computation()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe { shoot() }
    }

    fun parseItem(item: NfcItemController.Item) {
        when(item) {
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
        count = AMMO_COUNT
        gunEmpty = false
        updateWeaponUi()
        updateTopUi()
    }

    fun setupNfcItemController() {
        nfcItemController = NfcItemController()
        nfcItemController!!.setupNfcAdapter(this, this.javaClass)

        nfcItemController!!.observeItemResults().
                subscribeOn(Schedulers.computation()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe { parseItem(it) }
    }

    override fun onResume() {
        nfcItemController?.onResume(this)
        super.onResume()
    }

    override fun onPause() {
        nfcItemController?.onPause(this)
        super.onPause()
    }

    protected fun switchWeapon() {
        if (weapon == Weapon.GLOCK) weapon = Weapon.LASER else weapon = Weapon.GLOCK
        count = AMMO_COUNT
        updateWeaponUi()
        updateTopUi()
    }

    protected open fun setupShooting() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager?.registerReceiver(reloadReceiver, IntentFilter(BatteryStateReceiver.ACTION_RELOAD))

        text.setOnLongClickListener {
            gunReloaded()
            true
        }
    }

    protected fun shoot() {
        if (gunEmpty) {
            soundManager?.playSound(weapon.emptySoundId)
            // TODO: animate ammo
            return
        }

        count--
        if (count <= 0) {
            gunEmpty = true
            scanController!!.setMode(ScanController.Mode.OFF)
        } else {
            soundManager?.playSound(weapon.shootSoundId)
        }

        updateTopUi()
    }

    protected fun updateWeaponUi() {
        gunImage.setImageResource(weapon.imageId)
        gunName.text = weapon.name
        ammoImage.setImageResource(weapon.ammoImageId)
    }

    private fun availableIncremented(inc: Int) {
        available += inc
    }

    protected fun updateTopUi() {
        ammoCount.text = count.toString() + " / " + available
        text.visibility = if (gunEmpty) View.VISIBLE else View.GONE
        gunName.visibility = if (gunEmpty) View.GONE else View.VISIBLE
        gunImage.visibility = if (gunEmpty) View.GONE else View.VISIBLE
        ammoCount.visibility = if (gunEmpty) View.GONE else View.VISIBLE
        ammoImage.visibility = if (gunEmpty) View.GONE else View.VISIBLE
    }

    private fun gunReloaded() {
        if (available <= 0) return
        soundManager?.playSound(weapon.reloadSoundId)

        val deducted = min(min(available, GameLoopActivity.AMMO_COUNT), GameLoopActivity.AMMO_COUNT - count)
        available -= deducted
        count += deducted

        gunEmpty = false
        scanController?.setMode(ScanController.Mode.HIGH)
        updateTopUi()
    }

    inner class ReloadReceiver() : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            gunReloaded()
        }
    }
}
