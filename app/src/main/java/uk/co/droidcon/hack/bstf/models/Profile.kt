package uk.co.droidcon.hack.bstf.models

import uk.co.droidcon.hack.bstf.R

enum class Profile(val id: String, val superHeroName: String, val avatarId: Int) {
    SPIDERMAN("u1", "Spiderman", R.drawable.avatar_spiderman),
    GOKU("u2", "Goku", R.drawable.avatar_goku),
    DOGE("u3", "Doge", R.drawable.avatar_doge),
    ZEBRA("u4", "Zebra", R.drawable.avatar_zebra),
    TERMINATOR("u5", "Terminator", R.drawable.avatar_terminator);

    companion object {

        fun getProfileForId(id: String): Profile? {
            return when (id) {
                SPIDERMAN.id -> SPIDERMAN
                GOKU.id -> GOKU
                DOGE.id -> DOGE
                TERMINATOR.id -> TERMINATOR
                ZEBRA.id -> ZEBRA
                else -> null
            }
        }

        fun getProfileForName(name: String): Profile {
            return when (name) {
                SPIDERMAN.superHeroName -> SPIDERMAN
                GOKU.superHeroName -> GOKU
                DOGE.superHeroName -> DOGE
                TERMINATOR.superHeroName -> TERMINATOR
                ZEBRA.superHeroName -> ZEBRA
                else -> throw IllegalArgumentException("No profile with superHeroName " + name)
            }
        }
    }
}