package uk.co.droidcon.hack.bstf.gamestarting

import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
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
        Log.d("Adapter", "onBindViewHolder")
        val player = players[position]
        val profile = Profile.getProfileForName(player.name)

        if (player.name == me?.name) {
            holder.nameView.text = player.name + " (me)"
        } else {
            holder.nameView.text = player.name
        }

        Glide.with(holder.itemView.context).load(profile.avatarId).into(holder.avatarView)
        holder.isReadyView.setImageResource(if (player.isReady) R.drawable.checked else R.drawable.unchecked)
        val tintColor = if (player.isReady) R.color.colorPrimary else android.R.color.darker_gray
        DrawableCompat.setTint(holder.isReadyView.drawable, ContextCompat.getColor(holder.itemView.context, tintColor))
    }

    override fun getItemCount(): Int {
        return players.size
    }

    fun setPlayers(newPlayers: ArrayList<Player>?) {
        if (newPlayers == null) return
        players = newPlayers
        notifyDataSetChanged()
    }

    inner class GameStartingPlayersViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val nameView: TextView
        val avatarView: ImageView
        val isReadyView: ImageView

        init {
            nameView = view.findViewById(R.id.player_name) as TextView
            avatarView = view.findViewById(R.id.player_avatar) as ImageView
            isReadyView = view.findViewById(R.id.player_is_ready) as ImageView
        }
    }
}