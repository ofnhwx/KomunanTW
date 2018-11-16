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
import net.komunan.komunantw.common.extension.intentActionView
import net.komunan.komunantw.common.extension.string
import net.komunan.komunantw.repository.entity.*
import net.komunan.komunantw.repository.entity.ext.TweetSourceExt
import net.komunan.komunantw.common.service.TwitterService
import net.komunan.komunantw.repository.entity.ext.TweetExtension
import net.komunan.komunantw.ui.common.view.TweetActionContainer
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
            val tweet = dTweet.await()
            val dUser = async(Dispatchers.Default) { User.dao.find(tweet.mainUserId, dCredentials.await()) ?: User.dummy() }
            itemView.tweet_text.text = tweet.displayText
            itemView.tweet_via.text = Html.fromHtml(string[R.string.format_via](tweet.via))
            itemView.action_container.apply {
                retweetCount = tweet.retweetCount
                likeCount = tweet.likeCount
            }

            val user = dUser.await()
            itemView.tweet_user_icon.setImageURI(user.imageUrl)
            itemView.tweet_user_name.text = user.name
            itemView.tweet_user_screen_name.text = string[R.string.format_screen_name](user.screenName)
            if (user.dummy) {
                itemView.tweet_date_time.text = formatTime(tweet.timestamp)
            } else {
                itemView.tweet_date_time.text = TwitterService.makeStatusPermalink(formatTime(tweet.timestamp), user.screenName, tweet.id)
            }
        }

        private fun bindAppearance(dTweet: Deferred<Tweet>) = GlobalScope.launch(Dispatchers.Main) {
            if (firstSetup) {
                itemView.retweeted_by_mark.setTextColor(AppColor.GREEN)
                itemView.retweeted_by.setTextColor(AppColor.GRAY)
                itemView.tweet_user_name.setTextColor(AppColor.ORANGE)
                itemView.tweet_user_screen_name.setTextColor(AppColor.GRAY)
                itemView.tweet_text.setTextColor(AppColor.WHITE)
                itemView.tweet_date_time.setLinkTextColor(AppColor.LINK)
                itemView.tweet_via.setTextColor(AppColor.GRAY)
                itemView.tweet_via.setLinkTextColor(AppColor.LINK)
            }
            val tweet = dTweet.await()
            val tweetAccounts = withContext(Dispatchers.Default) { Tweet.accountDao.find(tweet.id) }
            itemView.action_container.isRetweeted = tweetAccounts.map(TweetAccount::retweeted).any { it }
            itemView.action_container.isLiked     = tweetAccounts.map(TweetAccount::liked    ).any { it }
        }

        private fun bindEvents(dTweet: Deferred<Tweet>) = GlobalScope.launch(Dispatchers.Main) {
            // 各種リンクをタッチ可能に設定
            if (firstSetup) {
                itemView.tweet_text.movementMethod      = TouchableMovementMethod.instance
                itemView.tweet_date_time.movementMethod = LinkMovementMethod.getInstance()
                itemView.tweet_via.movementMethod       = LinkMovementMethod.getInstance()
            }
            // テキスト(@〜, #〜, https://〜)
            val tweet = dTweet.await()
            tweet.ext.urls.forEach { url ->
                itemView.tweet_text.applyLinks(Link(url.display)
                        .setTextColor(AppColor.LINK)
                        .setTextColorOfHighlightedLink(AppColor.LINK_PRESSED)
                        .setOnClickListener { url.expanded.intentActionView() })
            }
            itemView.tweet_text.applyLinks(LINK_USERNAME, LINK_HASH_TAG, LINK_URL)
            // プロフィール画像 -> ユーザープロフィール
            itemView.tweet_user_icon.setOnClickListener { TwitterService.Official.showProfile(tweet.mainUserId) }
            // 返信・リツイート・お気に入り
            itemView.action_container.setOnClickListener { action ->
                when (action) {
                    TweetActionContainer.Action.REPLY   -> TwitterService.Official.doTweet(tweet.id)
                    TweetActionContainer.Action.RETWEET -> TwitterService.Official.doRetweet(tweet.id)
                    TweetActionContainer.Action.LIKE    -> TwitterService.Official.doLike(tweet.id)
                }
            }
        }

        private fun bindMedias(dTweet: Deferred<Tweet>) = GlobalScope.launch(Dispatchers.Main) {
            val tweet = dTweet.await()
            when {
                tweet.ext.medias.count() == 1 && tweet.ext.medias.first().isVideo -> {
                    // ビデオ表示(最大1枚)
                    itemView.media_container.visibility = View.VISIBLE
                    itemView.ogp_container.visibility = View.GONE
                    bindVideo(tweet.ext.medias.first())
                }
                tweet.ext.medias.any() -> {
                    // 写真表示(最大4枚)
                    itemView.media_container.visibility = View.VISIBLE
                    itemView.ogp_container.visibility = View.GONE
                    bindPhotos(tweet.ext.medias)
                }
                !tweet.hasQuote && tweet.ext.urls.count() == 1 -> {
                    // OGP表示
                    itemView.media_container.visibility = View.GONE
                    itemView.ogp_container.visibility = View.VISIBLE
                    bindOGP(tweet.ext.urls.first())
                }
                else -> {
                    // 表示なし
                    itemView.media_container.visibility = View.GONE
                    itemView.ogp_container.visibility = View.GONE
                }
            }
        }

        private fun bindVideo(media: TweetExtension.Media) {
            itemView.media_container.apply {
                bindVideo(media)
                setOnClickListener { index, medias ->
                    medias[index].videoVariants.first().url.intentActionView()
                }
                setOnLongClickListener { index, medias ->
                    medias[index].videoVariants.first().url.intentActionView()
                }
            }
        }

        private fun bindPhotos(medias: List<TweetExtension.Media>) {
            itemView.media_container.apply {
                bindPhotos(medias)
                setOnClickListener { index, medias ->
                    ImageViewer.Builder(itemView.context, medias)
                            .setFormatter { media -> media.url }
                            .setStartPosition(index)
                            .show()
                }
                setOnLongClickListener { index, medias ->
                    medias[index].url.intentActionView()
                }
            }
        }

        private fun bindOGP(tweetUrl: TweetExtension.Url) {
            itemView.ogp_container.apply {
                bind(tweetUrl.expanded)
                setOnClickListener { tweetUrl.expanded.intentActionView() }
            }
        }

        private fun bindRetweetedBy(dTweet: Deferred<Tweet>, dCredentials: Deferred<List<Credential>>) = GlobalScope.launch(Dispatchers.Main) {
            if (firstSetup) {
                itemView.retweeted_by_mark.text = string[R.string.gmd_repeat]()
            }
            val tweet = dTweet.await()
            if (tweet.isRetweet) {
                val credentials = dCredentials.await()
                val user = withContext(Dispatchers.Default) { User.dao.find(tweet.userId, credentials) ?: User.dummy() }
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
                calendar.get(Calendar.YEAR)        != current.get(Calendar.YEAR)        -> format3.format(calendar.time)
                calendar.get(Calendar.DAY_OF_YEAR) != current.get(Calendar.DAY_OF_YEAR) -> format2.format(calendar.time)
                else -> format1.format(calendar.time)
            }
        }
    }
}
