package net.komunan.komunantw.ui.common.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.mikepenz.iconics.view.IconicsButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.common.AppColor
import net.komunan.komunantw.common.extension.string
import net.komunan.komunantw.common.service.TwitterService
import net.komunan.komunantw.repository.entity.Tweet
import net.komunan.komunantw.repository.entity.TweetAccount

class TweetActionContainer: LinearLayout {
    private val actionReply  : IconicsButton
    private val actionRetweet: IconicsButton
    private val actionLike   : IconicsButton

    constructor(context: Context): this(context, attrs = null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, defStyleAttr = 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.container_tweet_action, this, true)

        actionReply   = findViewById(R.id.action_reply)
        actionRetweet = findViewById(R.id.action_retweet)
        actionLike    = findViewById(R.id.action_like)

        actionReply.text = string[R.string.gmd_reply]()

        textColor      = AppColor.GRAY
        retweetedColor = AppColor.RETWEETED
        likedColor     = AppColor.LIKED
    }

    var textColor: Int
        set(value) {
            actionReply.setTextColor(value)
            field = value
        }

    var retweetedColor: Int
    var likedColor: Int


    fun bind(tweet: Tweet) = GlobalScope.launch(Dispatchers.Main) {
        val tweetAccounts = withContext(Dispatchers.Default) { Tweet.accountDao.find(tweet.id) }

        // リツイート
        val isRetweeted = tweetAccounts.map(TweetAccount::retweeted).any { it }
        actionRetweet.setTextColor(if (isRetweeted) retweetedColor else textColor)
        actionRetweet.text = string[R.string.format_gmd_retweet_count](tweet.retweetCount.toString())

        // お気に入り
        val isLiked = tweetAccounts.map(TweetAccount::liked).any { it }
        actionLike.setTextColor(if (isLiked) likedColor else textColor)
        actionLike.text = string[R.string.format_gmd_like_count](tweet.likeCount.toString())

        // アクション
        actionReply.setOnClickListener   { TwitterService.Official.doTweet(tweet.id) }
        actionRetweet.setOnClickListener { TwitterService.Official.doRetweet(tweet.id) }
        actionLike.setOnClickListener    { TwitterService.Official.doLike(tweet.id) }
    }
}
