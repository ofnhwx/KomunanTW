package net.komunan.komunantw.ui.common.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.stfalcon.frescoimageviewer.ImageViewer
import net.komunan.komunantw.R
import net.komunan.komunantw.common.AppColor
import net.komunan.komunantw.common.extension.intentActionView
import net.komunan.komunantw.common.extension.make
import net.komunan.komunantw.common.extension.uri
import net.komunan.komunantw.repository.entity.ext.TweetExtension

class TweetMediaContainer: LinearLayout {
    private val mediaViews: List<SimpleDraweeView>

    constructor(context: Context): this(context, attrs = null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, defStyleAttr = 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.container_tweet_media, this, true)
        mediaViews = listOf(findViewById(R.id.media1), findViewById(R.id.media2), findViewById(R.id.media3), findViewById(R.id.media4))
    }

    fun bind(medias: List<TweetExtension.Media>) {
        if (medias.size == 1 && medias.first().isVideo) {
            bindVideo(medias.first())
        } else {
            bindPhotos(medias)
        }
        visibility = View.VISIBLE
    }

    private fun bindVideo(media: TweetExtension.Media) {
        mediaViews.forEachIndexed { index, mediaView ->
            if (index == 0) {
                mediaView.visibility = View.VISIBLE
                mediaView.hierarchy = GenericDraweeHierarchyBuilder(context.resources).apply {
                    setOverlay(GoogleMaterial.Icon.gmd_play_circle_outline.make(context).color(AppColor.BLACK).paddingDp(12))
                }.build()
                mediaView.controller = Fresco.newDraweeControllerBuilder().apply {
                    imageRequest = ImageRequest.fromUri(media.url)
                }.build()
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

    private fun bindPhotos(medias: List<TweetExtension.Media>) {
        mediaViews.forEachIndexed { index, mediaView ->
            val media = medias.elementAtOrNull(index)
            if (media == null) {
                mediaView.visibility = View.GONE
            } else {
                mediaView.visibility = View.VISIBLE
                mediaView.hierarchy = GenericDraweeHierarchyBuilder(context.resources).apply {
                    //
                }.build()
                mediaView.controller = Fresco.newDraweeControllerBuilder().apply {
                    imageRequest = ImageRequestBuilder.newBuilderWithSource(media.url.uri()).apply {
                        isProgressiveRenderingEnabled = true
                    }.build()
                    autoPlayAnimations = true
                }.build()
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
}
