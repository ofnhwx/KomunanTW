package net.komunan.komunantw.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.facebook.drawee.view.SimpleDraweeView
import net.komunan.komunantw.R

class TweetMediaContainer: LinearLayout {
    val mediaViews: List<SimpleDraweeView>

    constructor(context: Context): this(context, attrs = null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, defStyleAttr = 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.container_tweet_media, this, true)
        mediaViews = listOf(findViewById(R.id.media1), findViewById(R.id.media2), findViewById(R.id.media3), findViewById(R.id.media4))
    }
}
