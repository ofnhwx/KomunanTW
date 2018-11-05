package net.komunan.komunantw.ui.main.home.tab

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.text.Html
import android.text.Spanned
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
import kotlinx.android.synthetic.main.item_tweet.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.repository.entity.TweetDetail
import net.komunan.komunantw.repository.entity.User
import net.komunan.komunantw.string
import java.text.SimpleDateFormat
import java.util.*

class HomeTabAdapter: PagedListAdapter<TweetDetail, HomeTabAdapter.TweetViewHolder>(DIFF_CALLBACK) {
    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<TweetDetail> = object: DiffUtil.ItemCallback<TweetDetail>() {
            override fun areItemsTheSame(oldItem: TweetDetail, newItem: TweetDetail): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TweetDetail, newItem: TweetDetail): Boolean {
                return oldItem.id == newItem.id
            }
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
                val user = withContext(Dispatchers.Default) { User.find(tweet.userId) }
                if (user == null) {
                    // TODO: 取得を試みてダメならダミー画像とかを設定
                    itemView.tweet_user_icon.setImageResource(R.mipmap.ic_launcher_round)
                    itemView.tweet_user_name.text = R.string.dummy.string()
                    itemView.tweet_user_screen_name.text = R.string.dummy.string()
                } else {
                    itemView.tweet_user_icon.setImageURI(Uri.parse(user.imageUrl))
                    itemView.tweet_user_name.text = user.name
                    itemView.tweet_user_screen_name.text = R.string.format_screen_name.string(user.screenName)
                }
                itemView.tweet_text.text = tweet.text
                itemView.tweet_date_time.run {
                    text = makePermLink(formatTime(tweet.timestamp), user?.screenName, tweet.id)
                    movementMethod = LinkMovementMethod.getInstance()
                }
                itemView.tweet_via.run {
                    text = Html.fromHtml(R.string.format_via.string(tweet.via))
                    movementMethod = LinkMovementMethod.getInstance()
                }
                if (tweet.isRetweet) {
                    val retweetedBy = withContext(Dispatchers.Default) { User.find(tweet.retweetedBy) }
                    if (retweetedBy == null) {
                        // TODO: 取得を試みてダメならダミーとかを設定
                        itemView.retweet_by.text = R.string.format_retweeted_by.string(R.string.dummy.string())
                    } else {
                        itemView.retweet_by.text = R.string.format_retweeted_by.string(retweetedBy.name)
                    }
                    itemView.retweet_by.visibility = View.VISIBLE
                    itemView.retweet_by.setCompoundDrawables(IconicsDrawable(itemView.context).icon(GoogleMaterial.Icon.gmd_repeat).color(Color.GREEN).sizeDp(12), null, null, null)
                } else {
                    itemView.retweet_by.visibility = View.GONE
                }
            }
        }

        private fun makePermLink(text: String?, screenName: String?, tweetId: Long?): Spanned? {
            if (text == null || screenName == null || tweetId == null) {
                return null
            }
            return Html.fromHtml(R.string.format_twitter_permalink.string(screenName, tweetId.toString(), text))
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
