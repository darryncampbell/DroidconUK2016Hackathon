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
import uk.co.droidcon.hack.bstf.R
import uk.co.droidcon.hack.bstf.models.Weapon
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
    protected var weapon = Weapon.LASER

    protected var localBroadcastManager: LocalBroadcastManager? = null
    protected var soundManager: SoundManager? = null
    protected val reloadReceiver = ReloadReceiver()

    internal var  scanController: ScanController? = null

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

        setupShooting()

        updateWeaponUi()
        updateTopUi()
    }

    open fun setupScanController() {
        scanController = ScanControllerImpl()
        scanController?.onCreate(this)

        scanController!!.observeScanTrigger().
                subscribeOn(Schedulers.computation()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe { shoot() }
    }


    override fun onResume() {
        scanController?.onResume()
        super.onResume()
    }

    override fun onPause() {
        scanController?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        scanController?.onDestroy()
        super.onDestroy()
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
            scanController?.setEnabled(false)
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

    protected fun updateTopUi() {
        ammoCount.text = "" + count
        text.visibility = if (gunEmpty) View.VISIBLE else View.GONE
        gunName.visibility = if (gunEmpty) View.GONE else View.VISIBLE
        gunImage.visibility = if (gunEmpty) View.GONE else View.VISIBLE
        ammoCount.visibility = if (gunEmpty) View.GONE else View.VISIBLE
        ammoImage.visibility = if (gunEmpty) View.GONE else View.VISIBLE
    }

    private fun gunReloaded() {
        soundManager?.playSound(weapon.reloadSoundId)
        count = AMMO_COUNT
        gunEmpty = false
        scanController?.setEnabled(true)
        updateTopUi()
    }

    inner class ReloadReceiver() : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            gunReloaded()
        }
    }
}
