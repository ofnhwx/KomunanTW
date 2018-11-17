package net.komunan.komunantw.ui.common.view

import android.content.Context
import android.text.Html
import android.text.format.DateUtils
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.klinker.android.link_builder.Link
import com.klinker.android.link_builder.applyLinks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.komunan.komunantw.R
import net.komunan.komunantw.common.AppColor
import net.komunan.komunantw.common.service.TwitterService
import net.komunan.komunantw.repository.entity.Tweet
import net.komunan.komunantw.repository.entity.User
import java.util.*

class TweetFooterContainer: LinearLayout {
    companion object {
        private val TIMEZONE_UTC = TimeZone.getTimeZone("UTC")
        private const val FLAGS1: Int = DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_ALL
        private const val FLAGS2: Int = DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_ALL
        private const val FLAGS3: Int = DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_ALL
    }

    private val time: TextView
    private val via: TextView
    private val client: TextView

    constructor(context: Context) : this(context, attrs = null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, defStyleAttr = 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.container_tweet_footer, this, true)

        time = findViewById(R.id.time)
        via = findViewById(R.id.via)
        client = findViewById(R.id.client)

        textColor = AppColor.GRAY
        linkColor = AppColor.LINK
    }

    var textColor: Int
        set(value) {
            time.setTextColor(textColor)
            via.setTextColor(textColor)
            client.setTextColor(textColor)
            field = value
        }

    var linkColor: Int
        set(value) {
            time.setLinkTextColor(linkColor)
            via.setLinkTextColor(linkColor)
            client.setTextColor(textColor)
            field = value
        }

    fun bind(tweet: Tweet, user: User) = GlobalScope.launch(Dispatchers.Main) {
        // 時刻
        time.text = formatTime(tweet.timestamp)
        time.applyLinks(Link(time.text.toString())
                .setTextColor(linkColor)
                .setTextColorOfHighlightedLink(linkColor)
                .setOnClickListener { TwitterService.Official.showStatus(user.screenName, tweet.id) })

        // クライアント
        @Suppress("DEPRECATION")
        client.text = Html.fromHtml(tweet.via)
        client.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun formatTime(timestamp: Long): String {
        val today = Calendar.getInstance(Locale.getDefault())
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeZone = TIMEZONE_UTC
        calendar.timeInMillis = timestamp
        calendar.timeZone = TimeZone.getDefault()
        return when {
            calendar.get(Calendar.YEAR) != today.get(Calendar.YEAR) -> {
                DateUtils.formatDateTime(context, calendar.timeInMillis, FLAGS3)
            }
            calendar.get(Calendar.DAY_OF_YEAR) != today.get(Calendar.DAY_OF_YEAR) -> {
                DateUtils.formatDateTime(context, calendar.timeInMillis, FLAGS2)
            }
            else -> {
                DateUtils.formatDateTime(context, calendar.timeInMillis, FLAGS1)
            }
        }
    }
}
