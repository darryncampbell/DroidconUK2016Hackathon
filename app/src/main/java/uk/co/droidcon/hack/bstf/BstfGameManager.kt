package uk.co.droidcon.hack.bstf

import android.util.Log
import com.google.firebase.database.*
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import uk.co.droidcon.hack.bstf.models.GameSession
import uk.co.droidcon.hack.bstf.models.Player
import uk.co.droidcon.hack.bstf.models.ShotEvent
import java.util.*

class BstfGameManager(database: FirebaseDatabase, gameId: Int) : ValueEventListener {

    val databaseReference: DatabaseReference
    var gameSession: GameSession
    var me: Player? = null

    private var isSynced: Boolean = false
    private val readyObservable: BehaviorSubject<Boolean> = BehaviorSubject.create()
    private val playersSubject: PublishSubject<ArrayList<Player>> = PublishSubject.create()

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
        if (!isSynced) {
            isSynced = true
            readyObservable.onNext(isSynced)
        }

        Log.d(TAG, dataSnapshot.toString())
        if (dataSnapshot.exists()) {
            gameSession = dataSnapshot.getValue(GameSession::class.java)
            playersSubject.onNext(gameSession.players)
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

    fun signUp(player: Player) {
        if (gameSession.players == null) {
            gameSession.players = ArrayList()
        }

        gameSession.players?.add(player)
        databaseReference.setValue(gameSession)
        me = player
    }

    fun signOff() {
        if (gameSession.players != null) {
            gameSession.players!!.filter { it.name != me?.name }
            databaseReference.setValue(gameSession)
        }
    }

    fun amIAlive(): Boolean = gameSession.shotsFired!!
            .filter { event -> event.target == me }
            .filter { event -> event.millis > System.currentTimeMillis().minus(20000) }
            .any()

    fun shoot(target: Player) {
        gameSession.shotsFired!!.add(ShotEvent(me!!, target, System.currentTimeMillis()))
        databaseReference.setValue(gameSession)
    }

    fun observePlayers(): Observable<ArrayList<Player>> {
        return playersSubject.asObservable()
    }

    fun observeSyncedState(): Observable<Boolean> {
        return readyObservable.asObservable()
    }
}