package net.komunan.komunantw.ui.main.home.tab

import android.annotation.SuppressLint
import android.graphics.Paint
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.klinker.android.link_builder.Link
import com.klinker.android.link_builder.TouchableMovementMethod
import com.klinker.android.link_builder.applyLinks
import com.stfalcon.frescoimageviewer.ImageViewer
import kotlinx.android.synthetic.main.item_tweet.view.*
import kotlinx.android.synthetic.main.item_tweet_missing.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.common.AppColor
import net.komunan.komunantw.extension.intentActionView
import net.komunan.komunantw.extension.string
import net.komunan.komunantw.repository.entity.TweetDetail
import net.komunan.komunantw.repository.entity.User
import net.komunan.komunantw.service.TwitterService
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class HomeTabAdapter: PagedListAdapter<TweetDetail, HomeTabAdapter.TweetBaseViewHolder>(DIFF_CALLBACK) {
    companion object {
        private const val VIEW_TYPE_TWEET = 1
        private const val VIEW_TYPE_MISSING = 2

        val DIFF_CALLBACK: DiffUtil.ItemCallback<TweetDetail> = object: DiffUtil.ItemCallback<TweetDetail>() {
            override fun areItemsTheSame(oldItem: TweetDetail, newItem: TweetDetail): Boolean = oldItem.isTheSame(newItem)
            override fun areContentsTheSame(oldItem: TweetDetail, newItem: TweetDetail): Boolean = oldItem.isContentsTheSame(newItem)
        }

        var current: Calendar = Calendar.getInstance(Locale.getDefault())
        var calendar: Calendar = Calendar.getInstance(Locale.getDefault())
    }

    override fun submitList(pagedList: PagedList<TweetDetail>?) {
        throw RuntimeException()
    }

    override fun submitList(pagedList: PagedList<TweetDetail>?, commitCallback: Runnable?) {
        current = Calendar.getInstance(Locale.getDefault())
        calendar = Calendar.getInstance(Locale.getDefault())
        super.submitList(pagedList, commitCallback)
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
        abstract fun bind(tweet: TweetDetail?)
    }

    class TweetMissingViewHolder(itemView: View): TweetBaseViewHolder(itemView) {
        override fun bind(tweet: TweetDetail?) {
            if (tweet == null) {
                return
            }
            itemView.tweet_missing.run {
                setTextColor(AppColor.LINK)
                paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
                setOnClickListener { TwitterService.fetchTweets(tweet, true) }
            }
        }
    }

    class TweetViewHolder(itemView: View): TweetBaseViewHolder(itemView) {
        companion object {
            private val UTC = TimeZone.getTimeZone("UTC")

            @SuppressLint("ConstantLocale") private val format1 = SimpleDateFormat("HH:mm", Locale.getDefault())
            @SuppressLint("ConstantLocale") private val format2 = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
            @SuppressLint("ConstantLocale") private val format3 = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

            private val LINK_USERNAME = Link(Pattern.compile("@[a-zA-Z0-9_]+"))
                    .setTextColor(AppColor.LINK)
                    .setTextColorOfHighlightedLink(AppColor.LINK_PRESSED)
                    .setOnClickListener { TwitterService.Official.showProfile(it) }
            private val LINK_HASH_TAG = Link(Pattern.compile("#[^\\s]+"))
                    .setTextColor(AppColor.LINK)
                    .setTextColorOfHighlightedLink(AppColor.LINK_PRESSED)
                    .setOnClickListener { TwitterService.Official.showHashTag(it) }
            private val LINK_URL = Link(Pattern.compile("https?://[^\\s]+"))
                    .setTextColor(AppColor.LINK)
                    .setTextColorOfHighlightedLink(AppColor.LINK_PRESSED)
                    .setOnClickListener { it.intentActionView() }
        }

        private var firstSetup: Boolean = true
        private val mediaViews by lazy { listOf(itemView.tweet_media_1, itemView.tweet_media_2, itemView.tweet_media_3, itemView.tweet_media_4) }

        override fun bind(tweet: TweetDetail?) {
            if (tweet == null) {
                return
            }
            GlobalScope.launch(Dispatchers.Main) {
                bindValue(tweet)
                bindAppearance(tweet)
                bindEvents(tweet)
                bindRetweetedBy(tweet)
                firstSetup = false
            }
        }

        private suspend fun bindValue(tweet: TweetDetail) {
            val user = withContext(Dispatchers.Default) { User.find(if (tweet.isRetweet) tweet.rtUserId else tweet.userId) ?: User.dummy() }
            itemView.tweet_user_icon.setImageURI(user.imageUrl)
            itemView.tweet_user_name.text = user.name
            itemView.tweet_user_screen_name.text = string[R.string.format_screen_name](user.screenName)
            itemView.tweet_text.text = tweet.displayText
            when {
                tweet.ext.medias.any() -> {
                    for (i in IntRange(0, 3)) {
                        val media = tweet.ext.medias.elementAtOrNull(i)
                        if (media == null) {
                            mediaViews[i].visibility = View.GONE
                        } else {
                            mediaViews[i].visibility = View.VISIBLE
                            mediaViews[i].setImageURI(media.url)
                        }
                    }
                }
                tweet.ext.urls.count() == 1 -> {
                }
            }
            itemView.tweet_date_time.text = TwitterService.makeStatusPermalink(formatTime(tweet.timestamp), user.screenName, tweet.id)
            itemView.tweet_via.text = Html.fromHtml(string[R.string.format_via](tweet.via))
            if (firstSetup) {
                itemView.tweet_action_reply.text = string[R.string.gmd_chat_bubble_outline]()
            }
            itemView.tweet_action_retweet.text = string[R.string.format_gmd_repeat](tweet.retweetCount.toString())
            itemView.tweet_action_like.text = string[R.string.format_gmd_favorite_border](tweet.likeCount.toString())
        }

        private fun bindAppearance(tweet: TweetDetail) {
            if (firstSetup) {
                itemView.retweeted_by_mark.setTextColor(AppColor.GREEN)
                itemView.retweeted_by.setTextColor(AppColor.GRAY)
                itemView.tweet_user_name.setTextColor(AppColor.ORANGE)
                itemView.tweet_user_screen_name.setTextColor(AppColor.GRAY)
                itemView.tweet_text.setTextColor(AppColor.WHITE)
                itemView.tweet_action_reply.setTextColor(AppColor.GRAY)
                itemView.tweet_date_time.setLinkTextColor(AppColor.LINK)
                itemView.tweet_via.setTextColor(AppColor.GRAY)
                itemView.tweet_via.setLinkTextColor(AppColor.LINK)
            }
            when {
                tweet.ext.medias.any() -> {
                    itemView.media_container.visibility = View.VISIBLE
                }
                tweet.ext.urls.count() == 1 -> {
                    itemView.media_container.visibility = View.GONE
                }
                else -> {
                    itemView.media_container.visibility = View.GONE
                }
            }
            itemView.tweet_action_retweet.setTextColor(AppColor.RETWEETED(tweet.retweeted))
            itemView.tweet_action_like.setTextColor(AppColor.LIKED(tweet.liked))
        }

        private fun bindEvents(tweet: TweetDetail) {
            // テキスト(@〜, #〜, https://〜)
            tweet.ext.urls.forEach { url ->
                itemView.tweet_text.applyLinks(Link(url.display)
                        .setTextColor(AppColor.LINK)
                        .setTextColorOfHighlightedLink(AppColor.LINK_PRESSED)
                        .setOnClickListener { url.expanded.intentActionView() })
            }
            itemView.tweet_text.applyLinks(LINK_USERNAME, LINK_HASH_TAG, LINK_URL)
            // プロフィール画像 -> ユーザープロフィール
            itemView.tweet_user_icon.setOnClickListener {
                TwitterService.Official.showProfile(tweet.userId)
            }
            // 画像・OGP
            when {
                tweet.ext.medias.any() -> {
                    for (i in IntRange(0, 3)) {
                        val media = tweet.ext.medias.elementAtOrNull(i)
                        if (media != null) {
                            mediaViews[i].setOnClickListener {
                                ImageViewer.Builder(itemView.context, tweet.ext.medias)
                                        .setFormatter { media ->
                                            media.url
                                        }
                                        .setStartPosition(i)
                                        .show()
                            }
                            mediaViews[i].setOnLongClickListener {
                                if (media.isVideo) {
                                    media.videoVariants.first().url.intentActionView()
                                } else {
                                    media.url.intentActionView()
                                }
                                return@setOnLongClickListener true
                            }
                        }
                    }
                }
                tweet.ext.urls.count() == 1 -> {
                }
            }
            // 返信
            itemView.tweet_action_reply.setOnClickListener {
                TwitterService.Official.doTweet(tweet.id)
            }
            // リツイート
            itemView.tweet_action_retweet.setOnClickListener {
                TwitterService.Official.doRetweet(tweet.id)
            }
            // お気に入り
            itemView.tweet_action_like.setOnClickListener {
                TwitterService.Official.doLike(tweet.id)
            }
            // 各種リンクをタッチ可能に設定
            if (firstSetup) {
                itemView.tweet_text.movementMethod = TouchableMovementMethod.instance
                itemView.tweet_date_time.movementMethod = LinkMovementMethod.getInstance()
                itemView.tweet_via.movementMethod = LinkMovementMethod.getInstance()
            }
        }

        private suspend fun bindRetweetedBy(tweet: TweetDetail) {
            if (firstSetup) {
                itemView.retweeted_by_mark.text = string[R.string.gmd_repeat]()
            }
            if (tweet.isRetweet) {
                val user = withContext(Dispatchers.Default) { User.find(tweet.userId)?: User.dummy() }
                itemView.retweeted_by.text = string[R.string.format_retweeted_by](user.name)
                itemView.retweeted_by_container.visibility = View.VISIBLE
            } else {
                itemView.retweeted_by_container.visibility = View.GONE
            }
        }

        private fun formatTime(timestamp: Long?): String? {
            if (timestamp == null) {
                return null
            }
            calendar.timeZone = UTC
            calendar.timeInMillis = timestamp
            calendar.timeZone = TimeZone.getDefault()
            return when {
                calendar.get(Calendar.YEAR) != current.get(Calendar.YEAR) -> format3.format(calendar.time)
                calendar.get(Calendar.DAY_OF_YEAR) != current.get(Calendar.DAY_OF_YEAR) -> format2.format(calendar.time)
                else -> format1.format(calendar.time)
            }
        }
    }
}
