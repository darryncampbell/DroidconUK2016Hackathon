package uk.co.droidcon.hack.bstf.gameloop

import rx.Observable
import uk.co.droidcon.hack.bstf.BstfGameManager
import uk.co.droidcon.hack.bstf.models.Player
import uk.co.droidcon.hack.bstf.models.ShotEvent


fun BstfGameManager.observeDeathState(): Observable<ShotEvent>
        = observeShotsFired()
                .map { it.lastOrNull() { event -> event.target == me!! } }
                .filter { it?.isRespawning() ?: false }
                .map { it!! }

fun BstfGameManager.observeDeathEvent(): Observable<ShotEvent>
        = observeDeathState().distinctUntilChanged()


fun BstfGameManager.observeRespawnTime(): Observable<Pair<ShotEvent, Long>>
        = observeDeathState().map { Pair(it, it.remainingRespawnTime()) }


fun BstfGameManager.isRespawning(target: Player): Boolean =
        gameSession.shotsFired!!
                .lastOrNull { it.target == target }
                ?.isRespawning() ?: false


fun BstfGameManager.shoot(target: Player): Boolean {
    val willShoot = !isRespawning(target)
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
