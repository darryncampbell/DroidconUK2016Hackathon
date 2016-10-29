package uk.co.droidcon.hack.bstf.gamestarting

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.widget.ImageView
import android.widget.TextView
import butterknife.bindView
import uk.co.droidcon.hack.bstf.BstfComponent
import uk.co.droidcon.hack.bstf.BstfGameManager
import uk.co.droidcon.hack.bstf.R
import uk.co.droidcon.hack.bstf.models.Player
import uk.co.droidcon.hack.bstf.models.Profile
import java.util.*

class GameStartingActivity : AppCompatActivity() {

    val toolbar: Toolbar by bindView(R.id.toolbar)
    val gameIdTextView: TextView by bindView(R.id.game_starting_game_id)
    val playersRecyclerView: RecyclerView by bindView(R.id.game_starting_players)

    lateinit var gameManager: BstfGameManager
    lateinit var adapter: GameStartingPlayersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_starting)
        setSupportActionBar(toolbar)
        gameManager = BstfComponent.getBstfGameManager()

        gameIdTextView.text = "Game Session " + gameManager.gameSession.id

        if (gameManager.me == null) {
            assignProfile()
        }

        adapter = GameStartingPlayersAdapter()
        playersRecyclerView.adapter = adapter
        playersRecyclerView.layoutManager = LinearLayoutManager(this)

        adapter.setPlayers(gameManager.gameSession.players)
    }

    fun assignProfile() {
        val availableProfiles = ArrayList<Profile>()
        availableProfiles.addAll(Profile.values())

        if (gameManager.gameSession.players != null) {
            for (player in gameManager.gameSession.players!!) {
                val profile = Profile.getProfileForId(player.name)
                availableProfiles.remove(profile)
            }
        }

        val random = Random()
        val randomIndex = random.nextInt(availableProfiles.size)
        val profile = availableProfiles[randomIndex]

        gameManager.setPlayer(Player(profile.id))
    }
}