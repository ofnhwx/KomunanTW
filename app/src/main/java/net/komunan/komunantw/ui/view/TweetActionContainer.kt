package net.komunan.komunantw.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.mikepenz.iconics.view.IconicsButton
import net.komunan.komunantw.R
import net.komunan.komunantw.common.AppColor
import net.komunan.komunantw.extension.string

class TweetActionContainer: LinearLayout {
    enum class Action {
        REPLY,
        RETWEET,
        LIKE,
    }

    private val actionReply  : IconicsButton
    private val actionRetweet: IconicsButton
    private val actionLike   : IconicsButton

    constructor(context: Context): this(context, attrs = null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, defStyleAttr = 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.container_tweet_action, this, true)

        actionReply   = findViewById<IconicsButton>(R.id.action_reply).apply {
            text = string[R.string.gmd_chat_bubble_outline]()
            setTextColor(AppColor.GRAY)
        }
        actionRetweet = findViewById(R.id.action_retweet)
        actionLike    = findViewById(R.id.action_like)

        isRetweeted = false
        isLiked     = false
        retweetCount = 0
        likeCount    = 0
    }

    var isRetweeted: Boolean = false
        set(value) {
            actionRetweet.setTextColor(AppColor.RETWEETED(value))
            field = value
        }

    var retweetCount: Int = 0
        set(value) {
            actionRetweet.text = string[R.string.format_gmd_repeat](value.toString())
            field = value
        }

    var isLiked: Boolean = false
        set(value) {
            actionLike.setTextColor(AppColor.LIKED(value))
            field = value
        }

    var likeCount: Int = 0
        set(value) {
            actionLike.text = string[R.string.format_gmd_favorite_border](value.toString())
            field = value
        }

    fun setOnClickListener(listener: (action: Action) -> Unit) {
        actionReply.setOnClickListener   { listener.invoke(Action.REPLY) }
        actionRetweet.setOnClickListener { listener.invoke(Action.RETWEET) }
        actionLike.setOnClickListener    { listener.invoke(Action.LIKE) }
    }
}
