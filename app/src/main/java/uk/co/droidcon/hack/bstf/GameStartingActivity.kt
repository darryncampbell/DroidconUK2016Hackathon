package uk.co.droidcon.hack.bstf

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.TextView
import butterknife.bindView

class GameStartingActivity : AppCompatActivity() {

    val toolbar: Toolbar by bindView(R.id.toolbar)
    val gameIdTextView: TextView by bindView(R.id.game_starting_game_id)

    lateinit var gameManager: BstfGameManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_starting)
        setSupportActionBar(toolbar)
        gameManager = BstfComponent.getBstfGameManager()

        gameIdTextView.text = "Game Session " + gameManager.gameSession.id
    }

    fun assignProfile() {

    }
}