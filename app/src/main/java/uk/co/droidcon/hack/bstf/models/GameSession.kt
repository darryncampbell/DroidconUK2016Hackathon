package uk.co.droidcon.hack.bstf.models

import java.util.*

class GameSession(var id: String? = null, var players: ArrayList<Player>? = null, var shotsFired: ArrayList<ShotEvent>? = null, var isStarted: Boolean = false)