package uk.co.droidcon.hack.bstf

import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import com.google.firebase.database.FirebaseDatabase

class BstfApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)

        // TODO init game manager by tap
        BstfComponent.setBstfGameManager(BstfGameManager(FirebaseDatabase.getInstance(), 1))
    }
}