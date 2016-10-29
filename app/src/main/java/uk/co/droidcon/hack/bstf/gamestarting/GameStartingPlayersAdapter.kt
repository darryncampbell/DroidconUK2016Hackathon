package uk.co.droidcon.hack.bstf.gamestarting

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import uk.co.droidcon.hack.bstf.R
import uk.co.droidcon.hack.bstf.models.Player
import uk.co.droidcon.hack.bstf.models.Profile
import java.util.*

class GameStartingPlayersAdapter : RecyclerView.Adapter<GameStartingPlayersAdapter.GameStartingPlayersViewHolder>() {

    private var players: ArrayList<Player> = ArrayList()
    var me: Player? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameStartingPlayersViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_game_starting_player, parent, false)
        return GameStartingPlayersViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameStartingPlayersViewHolder, position: Int) {
        val player = players[position]
        val profile = Profile.getProfileForId(player.name)

        if (player.name == me?.name) {
            holder.nameView.text = player.name + " (me)"
        } else {
            holder.nameView.text = player.name
        }

        holder.avatarView.setImageResource(profile.avatarId)
    }

    override fun getItemCount(): Int {
        return players.size
    }

    fun setPlayers(newPlayers: ArrayList<Player>?) {
        if (newPlayers == null) return
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return players.size
            }

            override fun getNewListSize(): Int {
                return newPlayers.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val old = players[oldItemPosition]
                val new = newPlayers[newItemPosition]
                return old.name == new.name
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val old = players[oldItemPosition]
                val new = newPlayers[newItemPosition]
                return old == new
            }
        })

        this.players = newPlayers
        result.dispatchUpdatesTo(this)
    }

    inner class GameStartingPlayersViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val nameView: TextView
        val avatarView: ImageView

        init {
            nameView = view.findViewById(R.id.player_name) as TextView
            avatarView = view.findViewById(R.id.player_avatar) as ImageView
        }
    }
}