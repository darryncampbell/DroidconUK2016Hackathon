package uk.co.droidcon.hack.bstf.gameloop

import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.util.SortedListAdapterCallback
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import uk.co.droidcon.hack.bstf.BstfComponent
import uk.co.droidcon.hack.bstf.R
import uk.co.droidcon.hack.bstf.gameloop.PlayerStateAdapter.RowViewHolder
import uk.co.droidcon.hack.bstf.models.PlayerState
import uk.co.droidcon.hack.bstf.models.Profile

class PlayerStateAdapter : RecyclerView.Adapter<RowViewHolder>() {

    private val playerMe = BstfComponent.getBstfGameManager().me!!
    private val playerStateList = SortedList<PlayerState>(PlayerState::class.java,
            object : SortedListAdapterCallback<PlayerState>(this) {
                override fun areContentsTheSame(oldItem: PlayerState, newItem: PlayerState): Boolean {
                    return oldItem == newItem
                }

                override fun areItemsTheSame(item1: PlayerState, item2: PlayerState): Boolean {
                    return item1.player.name == item2.player.name
                }

                override fun compare(mine: PlayerState, their: PlayerState): Int {
                    if (mine.player.name == their.player.name) {
                        return 0

                    } else {
                        // Always place us on top
                        if (mine.player.name == playerMe.name) return 1
                        if (their.player.name == playerMe.name) return -1

                        // Otherwise sort by score
                        else return mine.player.simpleScore().compareTo(their.player.simpleScore())
                    }
                }
            })


    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {

        val state = playerStateList.get(position)
        val profile = Profile.getProfileForName(state.player.name)
        val summaryText = holder.summaryView.context.getString(R.string.text__player_state_summary,
                state.player.killCount(), state.player.deathCount())
        val relativeSummaryText = holder.summaryView.context.getString(R.string.text__player_state_relative_summary,
                playerMe.timesKilledBy(state.player),
                state.player.timesKilledBy(playerMe))

        holder.nameView.text = state.player.name
        Glide.with(holder.avatarView.context).load(profile.avatarId).into(holder.avatarView)
        holder.summaryView.text = summaryText
        holder.relativeSummaryView.text = relativeSummaryText

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_player_state, parent, false)
        return RowViewHolder(view)
    }

    override fun getItemCount(): Int {
        return playerStateList.size()
    }

    fun updateList(stateList: List<PlayerState>) {
        playerStateList.beginBatchedUpdates()
        try {
            playerStateList.clear()
            playerStateList.addAll(stateList)
        } finally {
            playerStateList.endBatchedUpdates()
        }
    }


    inner class RowViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val nameView: TextView
        val avatarView: ImageView
        val summaryView: TextView
        val relativeSummaryView: TextView

        init {
            nameView = view.findViewById(R.id.player_name) as TextView
            avatarView = view.findViewById(R.id.player_avatar) as ImageView
            summaryView = view.findViewById(R.id.summary) as TextView
            relativeSummaryView = view.findViewById(R.id.relative_summary) as TextView
        }
    }
}