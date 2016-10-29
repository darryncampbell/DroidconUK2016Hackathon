package uk.co.droidcon.hack.bstf.models

data class PlayerState(val player: Player, val shotsFired: List<ShotEvent>, val shotsReceived: List<ShotEvent>) {
}