package uk.co.droidcon.hack.bstf.reload.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.support.annotation.RawRes
import android.support.v4.content.LocalBroadcastManager
import timber.log.Timber
import uk.co.droidcon.hack.bstf.R

class BatteryStateReceiver(@RawRes internal val soundToPlay: Int = R.raw.reload) : BroadcastReceiver() {

    companion object {
        val ACTION_RELOAD = "ACTION_RELOADED"
    }

    internal var localBroadcastManager: LocalBroadcastManager? = null
    internal var batteryWasLow: Boolean = false

    override fun onReceive(context: Context, intent: Intent) {
        if (localBroadcastManager == null) {
            localBroadcastManager = LocalBroadcastManager.getInstance(context)
        }

        val action = intent.action
        if (Intent.ACTION_BATTERY_LOW == action) {
            batteryWasLow = true
            Timber.d("BATTERY WAS LOW")
            return
        }

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)

        if (batteryWasLow && level != 0) {
            localBroadcastManager!!.sendBroadcast(Intent(ACTION_RELOAD))
            Timber.d("RELOAD!")
            batteryWasLow = false
        }

        Timber.d("nothing happening...(level = $level)")
    }
}