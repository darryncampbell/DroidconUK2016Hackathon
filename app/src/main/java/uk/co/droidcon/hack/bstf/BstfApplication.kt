package uk.co.droidcon.hack.bstf

import android.content.Intent
import android.content.IntentFilter
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import com.google.firebase.database.FirebaseDatabase
import timber.log.Timber
import uk.co.droidcon.hack.bstf.reload.battery.BatteryStateReceiver
import uk.co.droidcon.hack.bstf.scan.ScanController
import uk.co.droidcon.hack.bstf.scan.ScanControllerImpl
import uk.co.droidcon.hack.bstf.sounds.SoundManager

class BstfApplication : MultiDexApplication() {

    lateinit var scanController: ScanController

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)

        // TODO init game manager by tap
        BstfComponent.setBstfGameManager(BstfGameManager(FirebaseDatabase.getInstance(), "a"))

        Timber.plant(Timber.DebugTree())

        // Loads all the sounds
        SoundManager.getInstance(this)

        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_OKAY)
        batteryFilter.addAction(Intent.ACTION_BATTERY_LOW)
        batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(BatteryStateReceiver(), batteryFilter)

        scanController = ScanControllerImpl.getInstance()
        scanController.onResume(this)
    }

    override fun onTerminate() {
        scanController.onPause()
        super.onTerminate()
    }

}
