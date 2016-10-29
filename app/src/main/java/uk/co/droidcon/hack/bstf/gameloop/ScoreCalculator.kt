package uk.co.droidcon.hack.bstf.gameloop

import uk.co.droidcon.hack.bstf.BstfComponent
import uk.co.droidcon.hack.bstf.models.Player


private val gameManager = BstfComponent.getBstfGameManager()

fun Player.killCount(): Int =
        gameManager.gameSession.shotsFired!!.count { event -> event.source == this }

fun Player.deathCount(): Int =
        gameManager.gameSession.shotsFired!!.count { event -> event.target == this }

fun Player.timesKilledBy(killer: Player): Int =
        gameManager.gameSession.shotsFired!!.count { event ->
            event.target == this && event.source == killer
        }

fun Player.simpleScore(): Int = killCount() - deathCount()