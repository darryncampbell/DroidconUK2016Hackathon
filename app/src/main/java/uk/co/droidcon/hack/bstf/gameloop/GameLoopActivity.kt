package uk.co.droidcon.hack.bstf.gameloop

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.RecyclerView
import android.widget.Button
import butterknife.bindView
import rx.subscriptions.CompositeSubscription
import uk.co.droidcon.hack.bstf.BstfComponent
import uk.co.droidcon.hack.bstf.BstfGameManager
import uk.co.droidcon.hack.bstf.R
import uk.co.droidcon.hack.bstf.models.ShotEvent
import java.util.*

class GameLoopActivity : AppCompatActivity() {

    val recycler: RecyclerView by bindView(R.id.recycler)
    val buttonShoot: Button by bindView(R.id.dev_shoot)
    val buttonGetShot: Button by bindView(R.id.dev_shot)

    val subscriptions = CompositeSubscription()

    lateinit var gameManager: BstfGameManager
    lateinit var adapter: PlayerStateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        adapter = PlayerStateAdapter()

        val subscription = gameManager.observePlayerState().subscribe { adapter.updateList(it) }
        subscriptions.add(subscription)

        recycler.adapter = adapter
        recycler.itemAnimator = DefaultItemAnimator()
    }

    override fun onDestroy() {
        subscriptions.clear()
        super.onDestroy()
    }
}