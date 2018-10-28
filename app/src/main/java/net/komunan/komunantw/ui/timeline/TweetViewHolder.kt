package net.komunan.komunantw.ui.timeline

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import net.komunan.komunantw.R
import net.komunan.komunantw.repository.entity.TweetDetail
import net.komunan.komunantw.repository.entity.User
import org.jetbrains.anko.find

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
