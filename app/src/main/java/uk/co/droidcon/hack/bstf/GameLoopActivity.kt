package uk.co.droidcon.hack.bstf

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.widget.Button
import butterknife.bindView
import uk.co.droidcon.hack.bstf.models.ShotEvent
import java.util.*

class GameLoopActivity : AppCompatActivity() {

    val recycler: RecyclerView by bindView(R.id.recycler)
    val buttonShoot: Button by bindView(R.id.dev_shoot)
    val buttonGetShot: Button by bindView(R.id.dev_shot)

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_game_loop)

        buttonShoot.setOnClickListener { view ->
            val manager = BstfComponent.getBstfGameManager()
            val otherPlayers = manager.otherPlayers()
            val victim = otherPlayers[Random().nextInt(otherPlayers.size)]
            manager.shoot(victim)
        }

        buttonGetShot.setOnClickListener { view ->
            // Kind of hacky -- but for testing only
            val manager = BstfComponent.getBstfGameManager()
            val session = manager.gameSession
            val otherPlayers = manager.otherPlayers()
            val criminal = otherPlayers[Random().nextInt(otherPlayers.size)]
            session.shotsFired!!.add(ShotEvent(criminal, manager.me))
            manager.databaseReference.setValue(session)
        }
    }


}