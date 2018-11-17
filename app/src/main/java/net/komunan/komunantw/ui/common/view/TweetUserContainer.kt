package net.komunan.komunantw.ui.common.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import net.komunan.komunantw.R
import net.komunan.komunantw.common.AppColor
import net.komunan.komunantw.common.extension.string
import net.komunan.komunantw.repository.entity.User

class TweetUserContainer: LinearLayout {
    private val name: TextView
    private val screenName: TextView

    constructor(context: Context): this(context, attrs = null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, defStyleAttr = 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.container_tweet_user, this, true)

        name = findViewById(R.id.name)
        screenName = findViewById(R.id.screen_name)

        nameColor = AppColor.ORANGE
        screenNameColor = AppColor.GRAY
    }

    var nameColor: Int
        set(value) {
            name.setTextColor(value)
            field = value
        }

    var screenNameColor: Int
        set(value) {
            screenName.setTextColor(value)
            field = value
        }

    fun bind(user: User) {
        name.text = user.name
        screenName.text = string[R.string.format_screen_name](user.screenName)
    }
}
