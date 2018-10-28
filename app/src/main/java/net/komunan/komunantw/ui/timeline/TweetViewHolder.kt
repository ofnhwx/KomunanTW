package net.komunan.komunantw.ui.timeline

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import net.komunan.komunantw.repository.entity.TweetDetail
import org.jetbrains.anko.find

internal class TweetViewHolder(view: View): RecyclerView.ViewHolder(view) {
    fun bindTo(tweet: TweetDetail?) {
        if (tweet == null) {
            return
        }
        itemView.find<TextView>(1).text = "${tweet.sourceIds}:${tweet.id}:${tweet.text}"
    }
}
