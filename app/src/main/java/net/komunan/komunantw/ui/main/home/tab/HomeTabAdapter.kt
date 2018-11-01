package net.komunan.komunantw.ui.main.home.tab

import android.arch.paging.PagedListAdapter
import android.databinding.DataBindingUtil
import android.net.Uri
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.*
import net.komunan.komunantw.R
import net.komunan.komunantw.databinding.ItemTweetBinding
import net.komunan.komunantw.repository.entity.TweetDetail
import net.komunan.komunantw.repository.entity.User
import net.komunan.komunantw.string

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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TweetViewHolder(ItemTweetBinding.inflate(inflater, parent, false).root)
    }

    override fun onBindViewHolder(holder: TweetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TweetViewHolder(view: View): RecyclerView.ViewHolder(view) {
        fun bind(tweet: TweetDetail?) {
            if (tweet == null) {
                return
            }
            DataBindingUtil.bind<ItemTweetBinding>(itemView)?.let {
                GlobalScope.launch(Dispatchers.Main) {
                    val user = async { User.find(tweet.userId) }.await()
                    if (user == null) {
                        // TODO: 取得を試みてダメならダミー画像とかを設定
                    } else {
                        it.tweetUserIcon.setImageURI(Uri.parse(user.imageUrl))
                        it.tweetUserName.text = user.name
                        it.tweetUserScreenName.text = R.string.format_screen_name.string(user.screenName)
                    }
                    it.tweetDateTime.text = "{{時間}}" // TODO: 時間をフォーマットして設定
                    it.tweetText.text = tweet.text
                }
            }
        }
    }
}
