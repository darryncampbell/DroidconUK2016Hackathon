package uk.co.droidcon.hack.bstf

import android.util.Log
import com.google.firebase.database.*
import uk.co.droidcon.hack.bstf.models.GameSession
import uk.co.droidcon.hack.bstf.models.Player
import uk.co.droidcon.hack.bstf.models.ShotEvent
import java.util.*

class BstfGameManager(database: FirebaseDatabase, gameId: Int) : ValueEventListener {

    val databaseReference: DatabaseReference
    var gameSession: GameSession
    var me: Player? = null

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
        }
    }

    init {
        databaseReference = database.getReference(REFERENCE_GAME_SESSIONS + gameId.toString())
        databaseReference.addValueEventListener(this)
        gameSession = GameSession(gameId, ArrayList(), ArrayList())
    }

    fun startGame() {
        databaseReference.addListenerForSingleValueEvent(this)
    }

    fun setPlayer(player: Player) {
        if (gameSession.players == null) {
            gameSession.players = ArrayList()
        }

        gameSession.players?.add(player)
        databaseReference.setValue(gameSession)
        me = player
    }

    fun amIAlive(): Boolean = gameSession.shotsFired!!
            .filter { event -> event.target == me }
            .filter { event -> event.millis > System.currentTimeMillis().minus(20000) }
            .any()

    fun shoot(target: Player) {
        gameSession.shotsFired!!.add(ShotEvent(me!!, target, System.currentTimeMillis()))
        databaseReference.setValue(gameSession)
    }
}