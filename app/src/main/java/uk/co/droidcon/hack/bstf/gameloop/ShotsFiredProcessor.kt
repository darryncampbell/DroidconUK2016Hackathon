package uk.co.droidcon.hack.bstf.gameloop

import rx.Observable
import uk.co.droidcon.hack.bstf.BstfGameManager
import uk.co.droidcon.hack.bstf.models.Player
import uk.co.droidcon.hack.bstf.models.ShotEvent


fun BstfGameManager.observeDeathState(): Observable<ShotEvent>
//        = observeShotsFired()
//                .map { it.lastOrNull() { event -> event.target == me!! } }
//                .filter { it?.isRespawning() ?: false }
//                .map { it!! }
        = Observable.never()

fun BstfGameManager.observeDeathEvent(): Observable<ShotEvent>
//        = observeDeathState().distinctUntilChanged()
        = Observable.never()


fun BstfGameManager.observeRespawnTime(): Observable<Pair<ShotEvent, Long>>
//        = observeDeathState().map { Pair(it, it.remainingRespawnTime()) }
        = Observable.never()


fun BstfGameManager.canShoot(target: Player): Boolean =
        gameSession.shotsFired!!
                .lastOrNull { it.target == target }
                ?.isRespawning() ?: true


fun BstfGameManager.shoot(target: Player): Boolean {
    val willShoot = true
    if (willShoot) {
        gameSession.shotsFired!!.add(ShotEvent(me!!, target, System.currentTimeMillis()))
        databaseReference.setValue(gameSession)
    }
    return willShoot
}


private fun ShotEvent.isRespawning()
        = remainingRespawnTime() > 0

private fun ShotEvent.remainingRespawnTime()
        = BstfGameManager.RESPAWN_DURATION_MILLIS - System.currentTimeMillis() + millis
