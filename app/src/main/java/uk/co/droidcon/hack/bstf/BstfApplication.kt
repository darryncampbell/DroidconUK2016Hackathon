package uk.co.droidcon.hack.bstf

import android.app.Application
import android.content.Intent
import android.content.IntentFilter

import timber.log.Timber
import uk.co.droidcon.hack.bstf.reload.battery.BatteryStateReceiver

class BstfApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_OKAY)
        batteryFilter.addAction(Intent.ACTION_BATTERY_LOW)
        batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(BatteryStateReceiver(), batteryFilter)
    }

}
