package net.komunan.komunantw.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.facebook.drawee.view.SimpleDraweeView
import com.stfalcon.frescoimageviewer.ImageViewer
import net.komunan.komunantw.R
import net.komunan.komunantw.extension.intentActionView
import net.komunan.komunantw.repository.entity.TweetExtension

class TweetMediaContainer: LinearLayout {
    private val mediaViews: List<SimpleDraweeView>
    private lateinit var medias: List<TweetExtension.TweetMedia>

    constructor(context: Context): this(context, attrs = null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, defStyleAttr = 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.container_tweet_media, this, true)
        mediaViews = listOf(findViewById(R.id.media1), findViewById(R.id.media2), findViewById(R.id.media3), findViewById(R.id.media4))
    }

    fun bindVideo(media: TweetExtension.TweetMedia) {
        this.medias = listOf(media)
        mediaViews.forEachIndexed { index, mediaView ->
            if (index == 0) {
                mediaView.visibility = View.VISIBLE
                mediaView.setImageURI(media.url)
                mediaView.setOnClickListener {
                    // TODO: 内部での動画再生
                    media.videoVariants.first().url.intentActionView()
                }
                mediaView.setOnLongClickListener {
                    media.videoVariants.first().url.intentActionView()
                    return@setOnLongClickListener true
                }
            } else {
                mediaView.visibility = View.GONE
            }
        }
    }

    fun bindPhotos(medias: List<TweetExtension.TweetMedia>) {
        this.medias = medias
        mediaViews.forEachIndexed { index, mediaView ->
            val media = medias.elementAtOrNull(index)
            if (media == null) {
                mediaView.visibility = View.GONE
            } else {
                mediaView.visibility = View.VISIBLE
                mediaView.setImageURI(media.url)
                mediaView.setOnClickListener {
                    ImageViewer.Builder(context, medias)
                            .setFormatter { media -> media.url }
                            .setStartPosition(index)
                            .show()
                }
                mediaView.setOnLongClickListener {
                    media.url.intentActionView()
                    return@setOnLongClickListener true
                }
            }
        }
    }

    fun setOnClickListener(listener: (index: Int, medias: List<TweetExtension.TweetMedia>) -> Unit) {
        for (i in IntRange(0, medias.size - 1)) {
            mediaViews[i].setOnClickListener { listener.invoke(i, medias) }
        }
    }

    fun setOnLongClickListener(listener: (index: Int, medias: List<TweetExtension.TweetMedia>) -> Unit) {
        for (i in IntRange(0, medias.size - 1)) {
            mediaViews[i].setOnLongClickListener { listener.invoke(i, medias); true }
        }
    }
}
