package uk.co.droidcon.hack.bstf.models

data class ShotEvent(var source: Player? = null, var target: Player? = null, var millis: Long = 0)