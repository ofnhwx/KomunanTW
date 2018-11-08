package net.komunan.komunantw.ui.main.home.tab

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
import kotlinx.android.synthetic.main.item_tweet.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.ReleaseApplication
import net.komunan.komunantw.common.AppColor
import net.komunan.komunantw.repository.entity.TweetDetail
import net.komunan.komunantw.repository.entity.User
import net.komunan.komunantw.service.TwitterService
import net.komunan.komunantw.string
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class HomeTabAdapter: PagedListAdapter<TweetDetail, HomeTabAdapter.TweetViewHolder>(DIFF_CALLBACK) {
    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<TweetDetail> = object: DiffUtil.ItemCallback<TweetDetail>() {
            override fun areItemsTheSame(oldItem: TweetDetail, newItem: TweetDetail): Boolean = oldItem.isTheSame(newItem)
            override fun areContentsTheSame(oldItem: TweetDetail, newItem: TweetDetail): Boolean = oldItem.isContentsTheSame(newItem)
        }

        var current: Calendar = Calendar.getInstance(Locale.getDefault())
        var calendar: Calendar = Calendar.getInstance(Locale.getDefault())
    }

    override fun submitList(pagedList: PagedList<TweetDetail>?) {
        current = Calendar.getInstance(Locale.getDefault())
        calendar = Calendar.getInstance(Locale.getDefault())
        super.submitList(pagedList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TweetViewHolder(inflater.inflate(R.layout.item_tweet, parent, false))
    }

    override fun onBindViewHolder(holder: TweetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TweetViewHolder(view: View): RecyclerView.ViewHolder(view) {
        companion object {
            private val UTC = TimeZone.getTimeZone("UTC")

            @SuppressLint("ConstantLocale") private val format1 = SimpleDateFormat("HH:mm", Locale.getDefault())
            @SuppressLint("ConstantLocale") private val format2 = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
            @SuppressLint("ConstantLocale") private val format3 = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

            private val LINK_USERNAME = Link(Pattern.compile("@[^\\s]+"))
                    .setTextColor(AppColor.LINK)
                    .setTextColorOfHighlightedLink(AppColor.LINK_PRESSED)
            private val LINK_HASH_TAG = Link(Pattern.compile("#[^\\s]+"))
                    .setTextColor(AppColor.LINK)
                    .setTextColorOfHighlightedLink(AppColor.LINK_PRESSED)
            private val LINK_URL = Link(Pattern.compile("https?://[^\\s]+"))
                    .setTextColor(AppColor.LINK)
                    .setTextColorOfHighlightedLink(AppColor.LINK_PRESSED)
        }

        private var firstSetup: Boolean = true

        fun bind(tweet: TweetDetail?) {
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
            val user = withContext(Dispatchers.Default) { User.find(tweet.userId) ?: User.dummy() }
            itemView.tweet_user_icon.setImageURI(Uri.parse(user.imageUrl))
            itemView.tweet_user_name.text = user.name
            itemView.tweet_user_screen_name.text = R.string.format_screen_name.string(user.screenName)
            itemView.tweet_text.text = tweet.text
            itemView.tweet_date_time.text = TwitterService.makeStatusPermalink(formatTime(tweet.timestamp), user.screenName, tweet.id)
            itemView.tweet_via.text = Html.fromHtml(R.string.format_via.string(tweet.via))
            if (firstSetup) {
                itemView.tweet_action_reply.text = R.string.gmd_chat_bubble_outline.string()
            }
            itemView.tweet_action_retweet.text = R.string.format_gmd_repeat.string(tweet.retweetCount.toString())
            itemView.tweet_action_like.text = R.string.format_gmd_favorite_border.string(tweet.likeCount.toString())
        }

        private fun bindAppearance(tweet: TweetDetail) {
            if (firstSetup) {
                itemView.retweeted_by_mark.setTextColor(AppColor.GREEN)
                itemView.retweeted_by.setTextColor(AppColor.GRAY)
                itemView.tweet_user_screen_name.setTextColor(AppColor.GRAY)
                itemView.tweet_action_reply.setTextColor(AppColor.GRAY)
                itemView.tweet_action_retweet.setTextColor(AppColor.RETWEETED(tweet.retweeted))
                itemView.tweet_action_like.setTextColor(AppColor.LIKED(tweet.liked))
                itemView.tweet_date_time.setLinkTextColor(AppColor.LINK)
                itemView.tweet_via.setTextColor(AppColor.GRAY)
                itemView.tweet_via.setLinkTextColor(AppColor.LINK)
            }
        }

        private fun bindEvents(tweet: TweetDetail) {
            // テキスト(@〜, #〜, https://〜)
            tweet.urls.forEach { url ->
                val link = Link(url.display)
                        .setTextColor(AppColor.LINK)
                        .setTextColorOfHighlightedLink(AppColor.LINK_PRESSED)
                        .setOnClickListener {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url.expanded))
                            ReleaseApplication.context.startActivity(intent)
                        }
                itemView.tweet_text.applyLinks(link)
            }
            itemView.tweet_text.applyLinks(LINK_USERNAME, LINK_HASH_TAG, LINK_URL)
            // プロフィール画像 -> ユーザープロフィール
            itemView.tweet_user_icon.setOnClickListener {
                TwitterService.doOfficialProfile(tweet.userId)
            }
            // 返信
            itemView.tweet_action_reply.setOnClickListener {
                TwitterService.doOfficialTweet(tweet.id)
            }
            // リツイート
            itemView.tweet_action_retweet.setOnClickListener {
                TwitterService.doOfficialRetweet(tweet.id)
            }
            // お気に入り
            itemView.tweet_action_like.setOnClickListener {
                TwitterService.doOfficialLike(tweet.id)
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
                itemView.retweeted_by_mark.text = R.string.gmd_repeat.string()
            }
            if (tweet.isRetweet) {
                val retweetedBy = withContext(Dispatchers.Default) { User.find(tweet.retweetedBy)?: User.dummy() }
                itemView.retweeted_by.text = R.string.format_retweeted_by.string(retweetedBy.name)
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
