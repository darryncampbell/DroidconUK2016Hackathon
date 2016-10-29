package uk.co.droidcon.hack.bstf

import android.content.Intent
import android.content.IntentFilter
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import com.google.firebase.database.FirebaseDatabase
import timber.log.Timber
import uk.co.droidcon.hack.bstf.reload.battery.BatteryStateReceiver

class BstfApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)

        // TODO init game manager by tap
        BstfComponent.setBstfGameManager(BstfGameManager(FirebaseDatabase.getInstance(), 1))

        Timber.plant(Timber.DebugTree())

        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_OKAY)
        batteryFilter.addAction(Intent.ACTION_BATTERY_LOW)
        batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(BatteryStateReceiver(), batteryFilter)

    }
}