package uk.co.droidcon.hack.bstf

import android.util.Log
import com.google.firebase.database.*
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import uk.co.droidcon.hack.bstf.models.GameSession
import uk.co.droidcon.hack.bstf.models.Player
import uk.co.droidcon.hack.bstf.models.PlayerState
import uk.co.droidcon.hack.bstf.models.ShotEvent
import java.util.*


class BstfGameManager(database: FirebaseDatabase, gameId: Int) : ValueEventListener {

    val databaseReference: DatabaseReference
    var gameSession: GameSession
    var me: Player? = null

    var isSynced: Boolean = false
    private val readySubject: BehaviorSubject<Boolean> = BehaviorSubject.create()
    private val playersSubject: PublishSubject<ArrayList<Player>> = PublishSubject.create()
    private val shotsFiredSubject: PublishSubject<ArrayList<ShotEvent>> = PublishSubject.create();

    companion object {
        val TAG = BstfGameManager::class.java.simpleName
        val REFERENCE_GAME_SESSIONS = "game_session_"
        val RESPAWN_DURATION_MILLIS = 20000
    }

    override fun onCancelled(databaseError: DatabaseError?) {
        if (databaseError == null) return
        Log.e(TAG, databaseError.toString())
    }

    override fun onDataChange(dataSnapshot: DataSnapshot?) {
        if (dataSnapshot == null) return
        if (!isSynced) {
            isSynced = true
            readySubject.onNext(isSynced)
        }

        Log.d(TAG, dataSnapshot.toString())
        if (dataSnapshot.exists()) {
            gameSession = dataSnapshot.getValue(GameSession::class.java)
            gameSession.players = if (gameSession.players != null) ArrayList(gameSession.players!!.filter { it != null }) else ArrayList()
            gameSession.shotsFired = if (gameSession.shotsFired != null) ArrayList(gameSession.shotsFired!!.filter { it != null }) else ArrayList()
            playersSubject.onNext(gameSession.players)
            shotsFiredSubject.onNext(gameSession.shotsFired)
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
            val players = ArrayList<Player>()
            players.addAll(gameSession.players!!.filter { it.name != me?.name })
            gameSession.players = players
            databaseReference.setValue(gameSession)
        }
    }

    fun otherPlayers(): List<Player> {
        return gameSession.players!!.filter { p -> p != me }
    }

    // TODO Using System.currentTimeMillis will cause sync issues
    // Use /.info/serverTimeOffset to sync the game clock

    fun amIAlive(): Boolean = gameSession.shotsFired!!
            .filter { event -> event.target == me }
            .filter { event -> event.millis > System.currentTimeMillis().minus(RESPAWN_DURATION_MILLIS) }
            .any()

    fun observePlayers(): Observable<ArrayList<Player>> {
        return playersSubject.asObservable()
    }

    fun observeSyncedState(): Observable<Boolean> {
        return readySubject.asObservable()
    }

    fun observePlayerState(): Observable<List<PlayerState>> {
        return observePlayers().map { playerList -> makePlayerStateList(playerList) }
    }

    fun observeShotsFired(): Observable<ArrayList<ShotEvent>> {
        return shotsFiredSubject.asObservable()
    }

    fun toggleReadyState() {
        if (me == null) return
        me!!.isReady = !me!!.isReady

        for (player in gameSession.players!!) {
            if (player.name == me!!.name) {
                player.isReady = me!!.isReady
            }
        }

        databaseReference.setValue(gameSession)
    }

    fun playerStateList(): List<PlayerState> {
        return makePlayerStateList(gameSession.players!!)
    }

    private fun makePlayerStateList(playerList: ArrayList<Player>): List<PlayerState> {
        return playerList.map { player ->
            PlayerState(player, gameSession.shotsFired!!.filter { event ->
                event.source == player
            }, gameSession.shotsFired!!.filter { event ->
                event.target == player
            })
        }
    }

    fun gameStarted() {
        gameSession.isStarted = true
        databaseReference.setValue(gameSession)
    }
}