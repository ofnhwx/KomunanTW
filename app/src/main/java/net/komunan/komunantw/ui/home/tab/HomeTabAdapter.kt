package net.komunan.komunantw.ui.home.tab

import android.content.Context
import android.graphics.Paint
import android.net.Uri
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.github.ajalt.timberkt.i
import com.klinker.android.link_builder.Link
import com.klinker.android.link_builder.TouchableMovementMethod
import com.klinker.android.link_builder.applyLinks
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.stfalcon.frescoimageviewer.ImageViewer
import kotlinx.android.synthetic.main.item_tweet.view.*
import kotlinx.coroutines.*
import net.komunan.komunantw.R
import net.komunan.komunantw.common.*
import net.komunan.komunantw.core.repository.entity.Source
import net.komunan.komunantw.core.repository.entity.cache.OGP
import net.komunan.komunantw.core.repository.entity.cache.Tweet
import net.komunan.komunantw.core.repository.entity.cache.User
import net.komunan.komunantw.core.service.TwitterService

class HomeTabAdapter : PagedListAdapter<Tweet, HomeTabAdapter.TweetViewHolder>(DIFF_CALLBACK) {
    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<Tweet> = object : DiffUtil.ItemCallback<Tweet>() {
            override fun areItemsTheSame(oldItem: Tweet, newItem: Tweet): Boolean = oldItem.isTheSame(newItem)
            override fun areContentsTheSame(oldItem: Tweet, newItem: Tweet): Boolean = oldItem.isContentsTheSame(newItem)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TweetViewHolder(inflater.inflate(R.layout.item_tweet, parent, false))
    }

    override fun onBindViewHolder(holder: TweetViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it, position == (itemCount - 1))
        }
    }

    override fun onViewRecycled(holder: TweetViewHolder) {
        holder.unbind()
    }

    class TweetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var firstSetup: Boolean = true
        private var ogpJob: Job? = null

        fun bind(tweet: Tweet, isLast: Boolean) {
            bindHeader(tweet)
            bindUser(tweet)
            bindTweet(tweet)
            bindMedia(tweet)
            bindOgp(tweet)
            bindAction(tweet)
            bindFooter(tweet)
            bindNext(tweet)
            bindDebug(tweet)
            (itemView.layoutParams as? ViewGroup.MarginLayoutParams)?.also { params ->
                params.bottomMargin = if (isLast) (itemView.context.navigationBarHeight + 8.dp()) else 0
            }
            firstSetup = false
        }

        fun unbind() {
            ogpJob?.cancel()
            ogpJob = null
        }

        private fun bindHeader(tweet: Tweet) = itemView.apply {
            if (firstSetup) {
                header_text.setTextColor(AppColor.GRAY)
            }
            when {
                tweet.isRetweet -> {
                    val user = User.box.get(tweet.userId) ?: User.dummy
                    header_mark.setTextColor(AppColor.GREEN)
                    header_mark.text = string[R.string.gmd_retweet]()
                    header_text.text = string[R.string.retweeted_by](user.name)
                    header_container.visibility = View.VISIBLE
                }
                tweet.isReply -> {
                    val user = User.box.get(tweet.replyUserId) ?: User.dummy
                    header_mark.setTextColor(AppColor.RED)
                    header_mark.text = string[R.string.gmd_reply]()
                    header_text.text = string[R.string.reply_to](user.name)
                    header_container.visibility = View.VISIBLE
                }
                else -> {
                    header_container.visibility = View.GONE
                }
            }
        }

        private fun bindUser(tweet: Tweet) = itemView.apply {
            if (firstSetup) {
                user_name.setTextColor(AppColor.ORANGE)
                user_screen_name.setTextColor(AppColor.GRAY)
            }
            val user = User.box.get(tweet.mainUserId) ?: User.dummy
            user_icon.setImageURI(user.imageUrl)
            user_icon.setOnClickListener {
                TwitterService.Official.showProfile(user.id)
            }
            user_name.text = user.name
            user_screen_name.text = string[R.string.screen_name](user.screenName)
        }

        private fun bindTweet(tweet: Tweet) = itemView.apply {
            if (firstSetup) {
                tweet_text.setTextColor(AppColor.WHITE)
                tweet_text.movementMethod = TouchableMovementMethod.instance
            }
            tweet_text.text = tweet.displayText
            val links = tweet.ext.mentions.map { mention ->
                makeLink(string[R.string.screen_name](mention)) {
                    TwitterService.Official.showProfile(it)
                }
            }.plus(tweet.ext.hashtags.map { hashtag ->
                makeLink(string[R.string.hashtag](hashtag)) {
                    TwitterService.Official.showHashtag(it)
                }
            }).plus(tweet.ext.urls.map { url ->
                makeLink(url.display) {
                    url.expanded.openUrl()
                }
            })
            if (links.any()) {
                tweet_text.applyLinks(links)
            }
        }

        private fun bindMedia(tweet: Tweet) = itemView.apply {
            media_container.visibility = View.GONE
            if (tweet.extraType == Tweet.ExtraType.MEDIA) {
                val context = itemView.media_container.context
                val medias = tweet.ext.medias
                val mediaViews = listOf(media1, media2, media3, media4)
                if (medias.size == 1 && medias.first().let { it.isVideo || it.isAnimatedGif }) {
                    bindVideo(context, medias.first(), mediaViews)
                } else {
                    bindPhotos(context, medias, mediaViews)
                }
                media_container.visibility = View.VISIBLE
            }
        }

        private fun bindVideo(context: Context, media: Tweet.Extension.Media, mediaViews: List<SimpleDraweeView>) {
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
                        media.videoVariants.first().url.openUrl()
                    }
                    mediaView.setOnLongClickListener {
                        media.videoVariants.first().url.openUrl()
                        return@setOnLongClickListener true
                    }
                } else {
                    mediaView.visibility = View.GONE
                }
            }
        }

        private fun bindPhotos(context: Context, medias: List<Tweet.Extension.Media>, mediaViews: List<SimpleDraweeView>) {
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
                        imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(media.url)).apply {
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
                        media.url.openUrl()
                        return@setOnLongClickListener true
                    }
                }
            }
        }

        private fun bindOgp(tweet: Tweet) = itemView.apply {
            if (firstSetup) {
                ogp_title.setTextColor(AppColor.WHITE)
                ogp_description.setTextColor(AppColor.GRAY)
            }
            ogp_container.visibility = View.GONE
            if (tweet.extraType == Tweet.ExtraType.OGP) {
                ogpJob = GlobalScope.launch(Dispatchers.Main) {
                    try {
                        val ogp = OGP.get(tweet)
                        if (ogp.hasError) {
                            return@launch
                        }
                        if (ogp.imageUrl == null) {
                            ogp_image.visibility = View.GONE
                        } else {
                            ogp_image.visibility = View.VISIBLE
                            ogp_image.setImageURI(ogp.imageUrl)
                        }
                        ogp_title.text = ogp.title
                        ogp_description.text = ogp.description
                        ogp_container.setOnClickListener {
                            ogp.url.openUrl()
                        }
                        ogp_container.visibility = View.VISIBLE
                    } catch (cancel: CancellationException) {
                        i(cancel)
                    }
                }
            }
        }

        private fun bindAction(tweet: Tweet) = itemView.apply {
            if (firstSetup) {
                action_reply.setTextColor(AppColor.GRAY)
            }
            // 返信
            action_reply.setOnClickListener {
                TwitterService.Official.doTweet(tweet.id)
            }

            // リツイート
            action_retweet.setTextColor(if (tweet.rtBy.any()) AppColor.RETWEETED else AppColor.GRAY)
            action_retweet.text = string[R.string.gmd_retweet_count](tweet.rtCount.toString())
            action_retweet.setOnClickListener {
                TwitterService.Official.doRetweet(tweet.id)
            }

            // お気に入り
            action_like.setTextColor(if (tweet.likeBy.any()) AppColor.LIKED else AppColor.GRAY)
            action_like.text = string[R.string.gmd_like_count](tweet.likeCount.toString())
            action_like.setOnClickListener {
                TwitterService.Official.doLike(tweet.id)
            }
        }

        private fun bindFooter(tweet: Tweet) = itemView.apply {
            if (firstSetup) {
                footer_time.setTextColor(AppColor.GRAY)
                footer_time.setLinkTextColor(AppColor.LINK)
                footer_via.setTextColor(AppColor.GRAY)
                footer_via.setLinkTextColor(AppColor.LINK)
                footer_client.setTextColor(AppColor.GRAY)
                footer_client.setLinkTextColor(AppColor.LINK)
                footer_client.movementMethod = LinkMovementMethod.getInstance()
            }
            footer_time.text = tweet.displayTime
            footer_time.applyLinks(Link(footer_time.text.toString())
                    .setTextColor(AppColor.LINK)
                    .setTextColorOfHighlightedLink(AppColor.LINK)
                    .setOnClickListener {
                        val user = User.box.get(tweet.mainUserId)
                        TwitterService.Official.showStatus(user.screenName, tweet.id)
                    })
            @Suppress("DEPRECATION")
            footer_client.text = Html.fromHtml(tweet.via)
        }

        private fun bindNext(tweet: Tweet) = itemView.apply {
            if (firstSetup) {
                tweet_missing.setTextColor(AppColor.LINK)
                tweet_missing.paintFlags = tweet_missing.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            }
            if (tweet.hasMissing) {
                tweet_missing.visibility = View.VISIBLE
                tweet_missing.setOnClickListener {
                    TODO()
                    // TwitterService.fetchTweets(tweetSource, true)
                }
            } else {
                tweet_missing.visibility = View.GONE
            }
        }

        private fun bindDebug(tweet: Tweet) = itemView.apply {
            if (firstSetup) {
                debug_id.setTextColor(AppColor.GRAY)
                debug_sources.setTextColor(AppColor.GRAY)
            }
            if (Preference.debugMode) {
                debug_container.visibility = View.VISIBLE
                debug_id.text = String.format("ID: %s", tweet.id.commaSeparated())
                debug_sources.text = String.format("Sources: %s", tweet.sources.map(Source::id).joinToString(", "))
            } else {
                debug_container.visibility = View.GONE
            }
        }

        private fun makeLink(text: String, listener: (String) -> Unit): Link {
            return Link(text).apply {
                setTextColor(AppColor.LINK)
                setTextColorOfHighlightedLink(AppColor.LINK_PRESSED)
                setOnClickListener { listener.invoke(it) }
            }
        }
    }
}
