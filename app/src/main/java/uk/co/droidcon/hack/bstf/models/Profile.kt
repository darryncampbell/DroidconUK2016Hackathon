package uk.co.droidcon.hack.bstf.models

import uk.co.droidcon.hack.bstf.R

enum class Profile(val id: String, val avatarId: Int) {
    SPIDERMAN("Spiderman", R.drawable.avatar_spiderman),
    GOKU("Goku", R.drawable.avatar_goku),
    DOGE("Doge", R.drawable.avatar_doge),
    TERMINATOR("Terminator", R.drawable.avatar_terminator);

    companion object {

        fun getProfileForId(id: String) : Profile {
            return when(id) {
                SPIDERMAN.id -> SPIDERMAN
                GOKU.id -> GOKU
                DOGE.id -> DOGE
                TERMINATOR.id -> TERMINATOR
                else -> throw IllegalArgumentException("No profile with id " + id)
            }
        }
    }
}