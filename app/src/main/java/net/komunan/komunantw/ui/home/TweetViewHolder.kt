package net.komunan.komunantw.ui.home

import android.support.v7.widget.RecyclerView
import android.view.View
import net.komunan.komunantw.repository.entity.TweetDetail

internal class TweetViewHolder(view: View): RecyclerView.ViewHolder(view) {
    fun bind(tweet: TweetDetail?) {
        if (tweet == null) {
            return
        }
        (itemView.tag as? TweetUI)?.run {
            bind(tweet)
        }
    }
}
