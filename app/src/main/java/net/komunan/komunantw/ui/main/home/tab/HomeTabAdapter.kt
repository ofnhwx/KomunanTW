package net.komunan.komunantw.ui.main.home.tab

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
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
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import kotlinx.android.synthetic.main.item_tweet.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.repository.entity.TweetDetail
import net.komunan.komunantw.repository.entity.User
import net.komunan.komunantw.service.TwitterService
import net.komunan.komunantw.string
import java.text.SimpleDateFormat
import java.util.*

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
            @SuppressLint("ConstantLocale")
            private val format1 = SimpleDateFormat("HH:mm", Locale.getDefault())
            @SuppressLint("ConstantLocale")
            private val format2 = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
            @SuppressLint("ConstantLocale")
            private val format3 = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        }

        fun bind(tweet: TweetDetail?) {
            if (tweet == null) {
                return
            }
            GlobalScope.launch(Dispatchers.Main) {
                // ユーザ情報
                val user = withContext(Dispatchers.Default) { User.find(tweet.userId) }
                if (user == null) {
                    // TODO: 取得を試みてダメならダミー画像とかを設定
                    itemView.tweet_user_icon.setImageResource(R.mipmap.ic_launcher_round)
                    itemView.tweet_user_name.text = R.string.dummy.string()
                    itemView.tweet_user_screen_name.text = R.string.dummy.string()
                } else {
                    itemView.tweet_user_icon.run {
                        setImageURI(Uri.parse(user.imageUrl))
                        setOnClickListener { TwitterService.doOfficialProfile(user.id) }
                    }
                    itemView.tweet_user_name.text = user.name
                    itemView.tweet_user_screen_name.text = R.string.format_screen_name.string(user.screenName)
                }
                itemView.tweet_user_screen_name.setTextColor(Color.GRAY)

                // テキストおよび日時, クライント
                itemView.tweet_text.text = tweet.text
                itemView.tweet_date_time.run {
                    text = TwitterService.makeStatusPermalink(formatTime(tweet.timestamp), user?.screenName, tweet.id)
                    movementMethod = LinkMovementMethod.getInstance()
                }
                itemView.tweet_via.run {
                    text = Html.fromHtml(R.string.format_via.string(tweet.via))
                    movementMethod = LinkMovementMethod.getInstance()
                }

                // ボタン
                itemView.tweet_action_reply.run {
                    text = R.string.gmd_chat_bubble_outline.string()
                    setTextColor(Color.GRAY)
                    setOnClickListener { TwitterService.doOfficialTweet(tweet.id) }
                }
                itemView.tweet_action_retweet.run {
                    text = R.string.format_gmd_repeat.string(tweet.retweetCount.toString())
                    setTextColor(if (tweet.retweeted) Color.GREEN else Color.GRAY)
                    setOnClickListener { TwitterService.doOfficialRetweet(tweet.id) }
                }
                itemView.tweet_action_like.run {
                    text = R.string.format_gmd_favorite_border.string(tweet.likeCount.toString())
                    setTextColor(if (tweet.liked) Color.RED else Color.GRAY)
                    setOnClickListener { TwitterService.doOfficialLike(tweet.id) }
                }

                // リツイート
                if (tweet.isRetweet) {
                    val retweetedBy = withContext(Dispatchers.Default) { User.find(tweet.retweetedBy) }
                    if (retweetedBy == null) {
                        // TODO: 取得を試みてダメならダミーとかを設定
                        itemView.retweet_by.text = R.string.format_retweeted_by.string(R.string.dummy.string())
                    } else {
                        itemView.retweet_by.text = R.string.format_retweeted_by.string(retweetedBy.name)
                    }
                    itemView.retweet_by.visibility = View.VISIBLE
                    itemView.retweet_by.setCompoundDrawables(makeIcon(itemView.context, GoogleMaterial.Icon.gmd_repeat).color(Color.GREEN).sizeDp(12), null, null, null)
                } else {
                    itemView.retweet_by.visibility = View.GONE
                }
            }
        }

        private fun makeIcon(context: Context, icon: IIcon): IconicsDrawable {
            return IconicsDrawable(context).icon(icon)
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
