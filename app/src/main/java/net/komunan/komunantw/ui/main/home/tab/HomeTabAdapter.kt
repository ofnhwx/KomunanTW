package net.komunan.komunantw.ui.main.home.tab

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
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
                } else {
                    itemView.tweet_user_icon.setImageURI(Uri.parse(user.imageUrl))
                    itemView.tweet_user_name.text = user.name
                    itemView.tweet_user_screen_name.text = R.string.format_screen_name.string(user.screenName)
                }
                itemView.tweet_date_time.text = formatTime(tweet.timestamp)
                itemView.tweet_text.text = tweet.text
            }
        }

        private fun formatTime(timestamp: Long): String {
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
