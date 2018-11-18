package net.komunan.komunantw.ui.main.home.tab

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.klinker.android.link_builder.Link
import com.klinker.android.link_builder.TouchableMovementMethod
import com.klinker.android.link_builder.applyLinks
import kotlinx.android.synthetic.main.item_tweet.view.*
import kotlinx.android.synthetic.main.item_tweet_missing.view.*
import kotlinx.coroutines.*
import net.komunan.komunantw.R
import net.komunan.komunantw.common.AppColor
import net.komunan.komunantw.common.extension.intentActionView
import net.komunan.komunantw.common.extension.string
import net.komunan.komunantw.common.service.TwitterService
import net.komunan.komunantw.repository.entity.Credential
import net.komunan.komunantw.repository.entity.Tweet
import net.komunan.komunantw.repository.entity.User
import net.komunan.komunantw.repository.entity.ext.TweetSourceExt

class HomeTabAdapter: PagedListAdapter<TweetSourceExt, HomeTabAdapter.TweetBaseViewHolder>(DIFF_CALLBACK) {
    companion object {
        private const val VIEW_TYPE_TWEET = 1
        private const val VIEW_TYPE_MISSING = 2

        val DIFF_CALLBACK: DiffUtil.ItemCallback<TweetSourceExt> = object: DiffUtil.ItemCallback<TweetSourceExt>() {
            override fun areItemsTheSame(oldItem: TweetSourceExt, newItem: TweetSourceExt): Boolean = oldItem.isTheSame(newItem)
            override fun areContentsTheSame(oldItem: TweetSourceExt, newItem: TweetSourceExt): Boolean = oldItem.isContentsTheSame(newItem)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetBaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_TWEET -> TweetViewHolder(inflater.inflate(R.layout.item_tweet, parent, false))
            VIEW_TYPE_MISSING -> TweetMissingViewHolder(inflater.inflate(R.layout.item_tweet_missing, parent, false))
            else -> throw IllegalStateException()
        }
    }

    override fun onBindViewHolder(holder: TweetBaseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)?.isMissing != false) VIEW_TYPE_MISSING else VIEW_TYPE_TWEET
    }

    abstract class TweetBaseViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun bind(tweetSource: TweetSourceExt?)
    }

    class TweetMissingViewHolder(itemView: View): TweetBaseViewHolder(itemView) {
        override fun bind(tweetSource: TweetSourceExt?) {
            if (tweetSource == null) {
                return
            }
            itemView.tweet_missing.run {
                setTextColor(AppColor.LINK)
                paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
                setOnClickListener { TwitterService.fetchTweets(tweetSource, true) }
            }
        }
    }

    class TweetViewHolder(itemView: View): TweetBaseViewHolder(itemView) {
        private var firstSetup: Boolean = true

        override fun bind(tweetSource: TweetSourceExt?) {
            if (tweetSource == null) {
                return
            }
            GlobalScope.launch(Dispatchers.Main) {
                if (firstSetup) {
                    firstSetup()
                }
                val tweet       = withContext(Dispatchers.Default) { Tweet.dao.find(tweetSource.tweetId)!! }
                val credentials = withContext(Dispatchers.Default) { tweetSource.credentials() }
                bindValue(tweet, credentials).join()
                firstSetup = false
            }
        }

        private fun firstSetup() {
            itemView.header_container.apply {
                replyToMarkColor = AppColor.RED
                retweetedMarkColor = AppColor.GREEN
                textColor = AppColor.GRAY
            }
            itemView.user_container.apply {
                nameColor = AppColor.ORANGE
                screenNameColor = AppColor.GRAY
            }
            itemView.tweet_text.apply {
                setTextColor(AppColor.WHITE)
                movementMethod = TouchableMovementMethod.instance
            }
            itemView.media_container.apply {
                // do nothing
            }
            itemView.ogp_container.apply {
                titleColor = AppColor.WHITE
                descriptionColor = AppColor.GRAY
            }
            itemView.action_container.apply {
                textColor = AppColor.GRAY
                retweetedColor = AppColor.RETWEETED
                likedColor = AppColor.LIKED
            }
            itemView.footer_container.apply {
                textColor = AppColor.GRAY
                linkColor = AppColor.LINK
            }
        }

        private fun bindValue(tweet: Tweet, credentials: List<Credential>) = GlobalScope.launch(Dispatchers.Main) {
            val user = async(Dispatchers.Default) { User.dao.find(tweet.mainUserId, credentials) ?: User.dummy() }

            itemView.header_container.bind(tweet, credentials)
            itemView.action_container.bind(tweet)

            itemView.tweet_text.apply {
                text = tweet.displayText
                val links = tweet.ext.mentions.map { mention ->
                    makeLink(string[R.string.format_screen_name](mention)) {
                        TwitterService.Official.showProfile(it)
                    }
                }.plus(tweet.ext.hashtags.map { hashtag ->
                    makeLink(string[R.string.format_hashtag](hashtag)) {
                        TwitterService.Official.showHashtag(it)
                    }
                }).plus(tweet.ext.urls.map { url ->
                    makeLink(url.display) {
                        url.expanded.intentActionView()
                    }
                })
                if (links.any()) {
                    applyLinks(links)
                }
            }

            when {
                tweet.ext.medias.any() -> {
                    // 写真・ビデオ表示
                    itemView.media_container.visibility = View.VISIBLE
                    itemView.ogp_container.visibility = View.GONE
                    itemView.media_container.bind(tweet.ext.medias)
                }
                tweet.hasQuote -> {
                    itemView.media_container.visibility = View.GONE
                    itemView.ogp_container.visibility = View.GONE
                    // TODO: 引用リツイートを検討
                }
                tweet.ext.urls.size == 1 -> {
                    // OGP表示
                    itemView.media_container.visibility = View.GONE
                    itemView.ogp_container.visibility = View.VISIBLE
                    itemView.ogp_container.bind(tweet.ext.urls.first().expanded)
                }
                else -> {
                    // 表示なし
                    itemView.media_container.visibility = View.GONE
                    itemView.ogp_container.visibility = View.GONE
                }
            }

            itemView.tweet_user_icon.setImageURI(user.await().imageUrl)
            itemView.tweet_user_icon.setOnClickListener { TwitterService.Official.showProfile(tweet.mainUserId) }
            itemView.user_container.bind(user.await())
            itemView.footer_container.bind(tweet, user.await())
        }

        private fun makeLink(text: String, listener: (String) -> Unit): Link {
            return Link(text).apply {
                setTextColor(AppColor.LINK)
                setTextColorOfHighlightedLink(AppColor.LINK_PRESSED)
                setOnClickListener { listener.invoke(it) }
            }
        }
    }
}
