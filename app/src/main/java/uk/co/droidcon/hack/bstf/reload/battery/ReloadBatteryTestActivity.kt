package uk.co.droidcon.hack.bstf.reload.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.BatteryManager
import android.os.Bundle
import android.support.annotation.RawRes
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import uk.co.droidcon.hack.bstf.R

class ReloadBatteryTestActivity : AppCompatActivity() {

    internal val batteryStateReceiver = BatteryStateReceiver(R.raw.reload)

    lateinit internal var tvText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tvText = TextView(this)
        setContentView(tvText)

        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_OKAY)
        batteryFilter.addAction(Intent.ACTION_BATTERY_LOW)
        batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryStateReceiver, batteryFilter)
    }

    override fun onDestroy() {
        unregisterReceiver(batteryStateReceiver)
        super.onDestroy()
    }

    private fun addText(text: String) {
        tvText.text = tvText.text.toString() + "\n\n" + text
    }

    inner class BatteryStateReceiver(@RawRes internal val soundToPlay: Int) : BroadcastReceiver() {
        internal var mediaPlayer: MediaPlayer? = null
        internal var batteryWasLow: Boolean = false

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (Intent.ACTION_BATTERY_LOW == action) {
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(context, soundToPlay)
                }
                batteryWasLow = true
                addText("BATTERY WAS LOW")
                return
            }

            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)

            if (batteryWasLow && level != 0) {
                mediaPlayer!!.start()
                addText("RELOAD")
                batteryWasLow = false
            }

            addText("nothing happening...(level = $level)")
        }
    }
}