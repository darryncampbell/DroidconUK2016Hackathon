package uk.co.droidcon.hack.bstf

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.ImageView
import android.widget.TextView
import butterknife.bindView
import uk.co.droidcon.hack.bstf.models.Player
import uk.co.droidcon.hack.bstf.models.Profile
import java.util.*

class GameStartingActivity : AppCompatActivity() {

    val toolbar: Toolbar by bindView(R.id.toolbar)
    val gameIdTextView: TextView by bindView(R.id.game_starting_game_id)
    val meNameTextView: TextView by bindView(R.id.game_starting_me_name)
    val meAvatarImageView: ImageView by bindView(R.id.game_starting_me_avatar)

    lateinit var gameManager: BstfGameManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_starting)
        setSupportActionBar(toolbar)
        gameManager = BstfComponent.getBstfGameManager()

        gameIdTextView.text = "Game Session " + gameManager.gameSession.id

        if (gameManager.me == null) {
            assignProfile()
        }

        showMe()
    }

    private fun showMe() {
        if (gameManager.me == null) return
        val profile = Profile.getProfileForId(gameManager.me?.name.toString())
        meNameTextView.text = profile.id
        meAvatarImageView.setImageResource(profile.avatarId)
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