package uk.co.droidcon.hack.bstf

import android.util.Log
import com.google.firebase.database.*
import uk.co.droidcon.hack.bstf.models.GameSession
import uk.co.droidcon.hack.bstf.models.Player
import java.util.*

class BstfGameManager(database: FirebaseDatabase, gameId: Int) : ValueEventListener {

    val databaseReference: DatabaseReference
    var gameSession: GameSession

    companion object {
        val TAG = BstfGameManager::class.java.simpleName
        val REFERENCE_GAME_SESSIONS = "game_session_"
    }

    override fun onCancelled(databaseError: DatabaseError?) {
        if (databaseError == null) return
        Log.e(TAG, databaseError.toString())
    }

    override fun onDataChange(dataSnapshot: DataSnapshot?) {
        if (dataSnapshot == null) return
        Log.d(TAG, dataSnapshot.toString())
        if (dataSnapshot.exists()) {
            gameSession = dataSnapshot.getValue(GameSession::class.java)
        } else {
            databaseReference.setValue(gameSession)
            databaseReference.push()
        }
    }

    init {
        databaseReference = database.getReference(REFERENCE_GAME_SESSIONS + gameId.toString())
        databaseReference.addValueEventListener(this)
        gameSession = GameSession(gameId, ArrayList())
    }

    fun startGame() {
        databaseReference.addListenerForSingleValueEvent(this)
    }

    fun addPlayer(player: Player) {
        if (gameSession.players == null) {
            gameSession.players = ArrayList()
        }

        gameSession.players?.add(player)
        databaseReference.setValue(gameSession)
    }
}