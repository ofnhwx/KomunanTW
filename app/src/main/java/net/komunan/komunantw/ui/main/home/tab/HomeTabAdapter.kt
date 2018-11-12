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
import kotlinx.coroutines.*
import net.komunan.komunantw.R
import net.komunan.komunantw.common.AppColor
import net.komunan.komunantw.extension.intentActionView
import net.komunan.komunantw.extension.string
import net.komunan.komunantw.repository.entity.*
import net.komunan.komunantw.repository.entity.ext.TweetSourceExt
import net.komunan.komunantw.service.TwitterService
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class HomeTabAdapter: PagedListAdapter<TweetSourceExt, HomeTabAdapter.TweetBaseViewHolder>(DIFF_CALLBACK) {
    companion object {
        private const val VIEW_TYPE_TWEET = 1
        private const val VIEW_TYPE_MISSING = 2

        val DIFF_CALLBACK: DiffUtil.ItemCallback<TweetSourceExt> = object: DiffUtil.ItemCallback<TweetSourceExt>() {
            override fun areItemsTheSame(oldItem: TweetSourceExt, newItem: TweetSourceExt): Boolean = oldItem.isTheSame(newItem)
            override fun areContentsTheSame(oldItem: TweetSourceExt, newItem: TweetSourceExt): Boolean = oldItem.isContentsTheSame(newItem)
        }

        var current: Calendar = Calendar.getInstance(Locale.getDefault())
        var calendar: Calendar = Calendar.getInstance(Locale.getDefault())
    }

    override fun submitList(pagedList: PagedList<TweetSourceExt>?) {
        throw RuntimeException()
    }

    override fun submitList(pagedList: PagedList<TweetSourceExt>?, commitCallback: Runnable?) {
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

        override fun bind(tweetSource: TweetSourceExt?) {
            if (tweetSource == null) {
                return
            }
            GlobalScope.launch(Dispatchers.Default) {
                val dTweet       = async { Tweet.dao.find(tweetSource.tweetId)!! }
                val dCredentials = async { tweetSource.credentials() }
                joinAll(
                        bindValue(dTweet, dCredentials),
                        bindAppearance(dTweet),
                        bindEvents(dTweet),
                        bindRetweetedBy(dTweet, dCredentials),
                        bindMedias(dTweet)
                )
                firstSetup = false
            }
        }

        private fun bindValue(dTweet: Deferred<Tweet>, dCredentials: Deferred<List<Credential>>) = GlobalScope.launch(Dispatchers.Main) {
            itemView.apply {
                if (firstSetup) {
                    tweet_action_reply.text = string[R.string.gmd_chat_bubble_outline]()
                }
                dTweet.await().also { tweet ->
                    val dUser = async(Dispatchers.Default) { User.dao.find(tweet.mainUserId, dCredentials.await()) ?: User.dummy() }
                    tweet_text.text = tweet.displayText
                    tweet_via.text = Html.fromHtml(string[R.string.format_via](tweet.via))
                    tweet_action_retweet.text = string[R.string.format_gmd_repeat]("${tweet.retweetCount}")
                    tweet_action_like.text = string[R.string.format_gmd_favorite_border]("${tweet.likeCount}")
                    dUser.await().also { user ->
                        tweet_user_icon.setImageURI(user.imageUrl)
                        tweet_user_name.text = user.name
                        tweet_user_screen_name.text = string[R.string.format_screen_name](user.screenName)
                        if (user.dummy) {
                            tweet_date_time.text = formatTime(tweet.timestamp)
                        } else {
                            tweet_date_time.text = TwitterService.makeStatusPermalink(formatTime(tweet.timestamp), user.screenName, tweet.id)
                        }
                    }
                }
            }
        }

        private fun bindAppearance(dTweet: Deferred<Tweet>) = GlobalScope.launch(Dispatchers.Main) {
            itemView.apply {
                if (firstSetup) {
                    retweeted_by_mark.setTextColor(AppColor.GREEN)
                    retweeted_by.setTextColor(AppColor.GRAY)
                    tweet_user_name.setTextColor(AppColor.ORANGE)
                    tweet_user_screen_name.setTextColor(AppColor.GRAY)
                    tweet_text.setTextColor(AppColor.WHITE)
                    tweet_action_reply.setTextColor(AppColor.GRAY)
                    tweet_date_time.setLinkTextColor(AppColor.LINK)
                    tweet_via.setTextColor(AppColor.GRAY)
                    tweet_via.setLinkTextColor(AppColor.LINK)
                }
                val tweet = dTweet.await()
                val tweetAccounts = withContext(Dispatchers.Default) { Tweet.accountDao.find(tweet.id) }
                val retweeted = tweetAccounts.map(TweetAccount::retweeted).any { it }
                val liked     = tweetAccounts.map(TweetAccount::liked    ).any { it }
                itemView.tweet_action_retweet.setTextColor(AppColor.RETWEETED(retweeted))
                itemView.tweet_action_like.setTextColor(AppColor.LIKED(liked))
            }
        }

        private fun bindEvents(dTweet: Deferred<Tweet>) = GlobalScope.launch(Dispatchers.Main) {
            itemView.apply {
                // 各種リンクをタッチ可能に設定
                if (firstSetup) {
                    tweet_text.movementMethod      = TouchableMovementMethod.instance
                    tweet_date_time.movementMethod = LinkMovementMethod.getInstance()
                    tweet_via.movementMethod       = LinkMovementMethod.getInstance()
                }
                // イベント設定
                dTweet.await().also { tweet ->
                    // テキスト(@〜, #〜, https://〜)
                    tweet.ext.urls.forEach { url ->
                        tweet_text.applyLinks(Link(url.display)
                                .setTextColor(AppColor.LINK)
                                .setTextColorOfHighlightedLink(AppColor.LINK_PRESSED)
                                .setOnClickListener { url.expanded.intentActionView() })
                    }
                    tweet_text.applyLinks(LINK_USERNAME, LINK_HASH_TAG, LINK_URL)
                    // プロフィール画像 -> ユーザープロフィール
                    val userId = tweet.mainUserId
                    tweet_user_icon.setOnClickListener { TwitterService.Official.showProfile(userId) }
                    // 返信・リツイート・お気に入り
                    val tweetId = tweet.id
                    tweet_action_reply.setOnClickListener   { TwitterService.Official.doTweet(tweetId)   }
                    tweet_action_retweet.setOnClickListener { TwitterService.Official.doRetweet(tweetId) }
                    tweet_action_like.setOnClickListener    { TwitterService.Official.doLike(tweetId)    }
                }
            }
        }

        private fun bindMedias(dTweet: Deferred<Tweet>) = GlobalScope.launch(Dispatchers.Main) {
            val tweet = dTweet.await()
            when {
                tweet.ext.medias.count() == 1 && tweet.ext.medias.first().isVideo -> {
                    // ビデオ表示(最大1枚)
                    itemView.media_container.visibility = View.VISIBLE
                    bindVideo(tweet.ext.medias.first())
                }
                tweet.ext.medias.any() -> {
                    // 写真表示(最大4枚)
                    itemView.media_container.visibility = View.VISIBLE
                    bindPhotos(tweet.ext.medias)
                }
                tweet.ext.urls.count() == 1 -> {
                    // OGP表示
                    itemView.media_container.visibility = View.GONE
                }
                else -> {
                    // 表示なし
                    itemView.media_container.visibility = View.GONE
                }
            }
        }

        private fun bindVideo(media: TweetExtension.TweetMedia) {
            itemView.media_container.visibility = View.VISIBLE
            mediaViews.first().apply {
                visibility = View.VISIBLE
                setImageURI(media.url)
                setOnClickListener {
                    // TODO: 内部での動画再生
                    media.videoVariants.first().url.intentActionView()
                }
                setOnLongClickListener {
                    media.videoVariants.first().url.intentActionView()
                    return@setOnLongClickListener true
                }
            }
            mediaViews.drop(1).forEach { it.visibility = View.GONE }
        }

        private fun bindPhotos(medias: List<TweetExtension.TweetMedia>) {
            mediaViews.forEachIndexed { index, mediaView ->
                val media = medias.elementAtOrNull(index)
                if (media == null) {
                    mediaView.visibility = View.GONE
                } else {
                    mediaView.visibility = View.VISIBLE
                    mediaView.setImageURI(media.url)
                    mediaView.setOnClickListener {
                        ImageViewer.Builder(itemView.context, medias)
                                .setFormatter { media -> media.url }
                                .setStartPosition(index)
                                .show()
                    }
                    mediaView.setOnLongClickListener {
                        media.url.intentActionView()
                        return@setOnLongClickListener true
                    }
                }
            }
        }

        private fun bindRetweetedBy(dTweet: Deferred<Tweet>, dCredentials: Deferred<List<Credential>>) = GlobalScope.launch(Dispatchers.Main) {
            itemView.apply {
                if (firstSetup) {
                    retweeted_by_mark.text = string[R.string.gmd_repeat]()
                }
                dTweet.await().also { tweet ->
                    if (tweet.isRetweet) {
                        val credentials = dCredentials.await()
                        val user = withContext(Dispatchers.Default) { User.dao.find(tweet.userId, credentials) ?: User.dummy() }
                        itemView.retweeted_by.text = string[R.string.format_retweeted_by](user.name)
                        itemView.retweeted_by_container.visibility = View.VISIBLE
                    } else {
                        itemView.retweeted_by_container.visibility = View.GONE
                    }
                }
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
