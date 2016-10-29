package uk.co.droidcon.hack.bstf.gamestarting

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import butterknife.bindView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import uk.co.droidcon.hack.bstf.BstfComponent
import uk.co.droidcon.hack.bstf.BstfGameManager
import uk.co.droidcon.hack.bstf.R
import uk.co.droidcon.hack.bstf.gameloop.GameLoopActivity
import uk.co.droidcon.hack.bstf.models.Player
import uk.co.droidcon.hack.bstf.models.Profile
import java.util.*

class GameStartingActivity : AppCompatActivity() {

    val gameIdTextView: TextView by bindView(R.id.game_starting_game_id)
    val playersRecyclerView: RecyclerView by bindView(R.id.game_starting_players)
    val isReadyButton: Button by bindView(R.id.game_starting_ready)

    val subscriptions = CompositeSubscription()

    lateinit var gameManager: BstfGameManager
    lateinit var adapter: GameStartingPlayersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_starting)
        gameManager = BstfComponent.getBstfGameManager()

        gameIdTextView.text = "Game Session " + gameManager.gameSession.id

        adapter = GameStartingPlayersAdapter()
        playersRecyclerView.adapter = adapter
        playersRecyclerView.layoutManager = LinearLayoutManager(this)

        isReadyButton.setOnClickListener {
            gameManager.toggleReadyState()
            isReadyButton.text = if (gameManager.me!!.isReady) "not ready" else "ready"
        }
    }

    override fun onResume() {
        super.onResume()

        adapter.setPlayers(gameManager.gameSession.players)

        if (gameManager.isSynced && gameManager.gameSession.isStarted) {
            Toast.makeText(this, "Game already started, sorry :(", Toast.LENGTH_SHORT).show()
            return
        }

        val playersSubscription = gameManager.observePlayers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    adapter.setPlayers(it)
                    if (!gameManager.gameSession.isStarted && it.size > 1 && it.all { it.isReady }) {
                        openActiveGame()
                    }
                })

        val isReadySubscription = gameManager.observeSyncedState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (gameManager.me == null && !gameManager.gameSession.isStarted) {
                        assignProfile()
                        adapter.me = gameManager.me
                        isReadyButton.visibility = View.VISIBLE
                        isReadyButton.text = if (gameManager.me != null && gameManager.me!!.isReady) "not ready" else "ready"
                    }
                })

        subscriptions.add(playersSubscription)
        subscriptions.add(isReadySubscription)
    }

    override fun onPause() {
        super.onPause()
        subscriptions.clear()
    }

    private fun openActiveGame() {
        val intent = Intent(this, GameLoopActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    fun assignProfile() {
        val availableProfiles = ArrayList<Profile>()
        availableProfiles.addAll(Profile.values())

        if (gameManager.gameSession.players != null) {
            for (player in gameManager.gameSession.players!!) {
                val profile = Profile.getProfileForName(player.name)
                availableProfiles.remove(profile)
            }
        }

        if (availableProfiles.isEmpty()) {
            Toast.makeText(this, "Party is full, sorry :(", Toast.LENGTH_SHORT).show()
            isReadyButton.visibility = View.GONE
            return
        }

        val random = Random()
        val randomIndex = random.nextInt(availableProfiles.size)
        val profile = availableProfiles[randomIndex]

        gameManager.signUp(Player(profile.superHeroName))
    }

    override fun onBackPressed() {
        gameManager.signOff()
        super.onBackPressed()
    }
}