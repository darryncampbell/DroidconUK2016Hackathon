package uk.co.droidcon.hack.bstf.reload.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.BatteryManager
import android.support.annotation.RawRes
import android.util.Log
import uk.co.droidcon.hack.bstf.BuildConfig
import uk.co.droidcon.hack.bstf.R

class BatteryStateReceiver(@RawRes internal val soundToPlay: Int = R.raw.reload) : BroadcastReceiver() {

    internal var mediaPlayer: MediaPlayer? = null
    internal var batteryWasLow: Boolean = false

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (Intent.ACTION_BATTERY_LOW == action) {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, soundToPlay)
            }
            batteryWasLow = true
            log("BATTERY WAS LOW")
            return
        }

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)

        if (batteryWasLow && level != 0) {
            mediaPlayer!!.start()
            log("RELOAD!")
            batteryWasLow = false
        }

        log("nothing happening...(level = $level)")
    }

    private fun log(text: String) {
        Log.d(BuildConfig.TAG, text)
    }
}