package net.komunan.komunantw.ui.common.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.mikepenz.iconics.view.IconicsTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.komunan.komunantw.R
import net.komunan.komunantw.common.AppColor
import net.komunan.komunantw.common.extension.string
import net.komunan.komunantw.repository.entity.Credential
import net.komunan.komunantw.repository.entity.Tweet
import net.komunan.komunantw.repository.entity.User

class TweetHeaderContainer: LinearLayout {
    private val mark: IconicsTextView
    private val text: TextView

    constructor(context: Context): this(context, attrs = null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, defStyleAttr = 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.container_tweet_header, this, true)

        mark = findViewById(R.id.mark)
        text = findViewById(R.id.text)

        retweetedMarkColor = AppColor.GREEN
        replyToMarkColor = AppColor.RED
        textColor = AppColor.GRAY
    }

    var retweetedMarkColor: Int
    var replyToMarkColor: Int

    var textColor: Int
        set(value) {
            text.setTextColor(textColor)
            field = value
        }

    fun bind(tweet: Tweet, credentials: List<Credential>) = GlobalScope.launch(Dispatchers.Main) {
        when {
            tweet.isRetweet -> {
                val user = withContext(Dispatchers.Default) { User.dao.find(tweet.userId, credentials) ?: User.dummy() }
                mark.setTextColor(retweetedMarkColor)
                mark.text = string[R.string.gmd_retweet]()
                text.text = string[R.string.format_retweeted_by](user.name)
                visibility = View.VISIBLE
            }
            tweet.isReply -> {
                val user = withContext(Dispatchers.Default) { User.dao.find(tweet.replyUserId, credentials) ?: User.dummy() }
                mark.setTextColor(replyToMarkColor)
                mark.text = string[R.string.gmd_reply]()
                text.text = string[R.string.format_reply_to](user.name)
                visibility = View.VISIBLE
            }
            else -> {
                visibility = View.GONE
            }
        }
    }
}
