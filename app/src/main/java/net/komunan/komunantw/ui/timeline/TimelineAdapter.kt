package net.komunan.komunantw.ui.timeline

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.view.ViewGroup
import net.komunan.komunantw.repository.entity.TweetDetail
import org.jetbrains.anko.AnkoContext

internal class TimelineAdapter: PagedListAdapter<TweetDetail, TweetViewHolder>(DIFF_CALLBACK) {
    companion object {
        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<TweetDetail>() {
            override fun areItemsTheSame(oldItem: TweetDetail, newItem: TweetDetail) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: TweetDetail, newItem: TweetDetail) = oldItem.id == newItem.id
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            = TweetViewHolder(TweetUI().createView(AnkoContext.create(parent.context, parent)))
    override fun onBindViewHolder(holder: TweetViewHolder, position: Int) = holder.bindTo(getItem(position))
}
