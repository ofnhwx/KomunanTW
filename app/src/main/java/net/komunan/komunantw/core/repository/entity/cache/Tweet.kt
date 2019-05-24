package net.komunan.komunantw.core.repository.entity.cache

import android.text.format.DateUtils
import io.objectbox.Box
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.converter.PropertyConverter
import io.objectbox.kotlin.boxFor
import io.objectbox.query.QueryBuilder
import io.objectbox.relation.ToMany
import net.komunan.komunantw.TWContext
import net.komunan.komunantw.common.gson
import net.komunan.komunantw.core.repository.ObjectBox
import net.komunan.komunantw.core.repository.entity.Account
import net.komunan.komunantw.core.repository.entity.Source
import net.komunan.komunantw.ui.common.base.Diffable
import twitter4j.HashtagEntity
import twitter4j.MediaEntity
import twitter4j.URLEntity
import twitter4j.UserMentionEntity
import java.util.*
import twitter4j.Status as TwitterStatus

@Entity
class Tweet() : Diffable {
    @Id(assignable = true)
    var id: Long = 0L
    var text: String = ""
    var userId: Long = 0L
    var rtCount: Int = 0
    var likeCount: Int = 0
    var timestamp: Long = 0L
    var via: String = ""
    var replyId: Long = 0L
    var replyUserId: Long = 0L
    var rtId: Long = 0L
    var rtUserId: Long = 0L
    var qtId: Long = 0L
    var qtUserId: Long = 0L
    var cachedAt: Long = 0L
    @Convert(converter = Extension.Converter::class, dbType = String::class)
    var ext: Extension = Extension()

    lateinit var sources: ToMany<Source>
    lateinit var missings: ToMany<Source>
    lateinit var rtBy: ToMany<Account>
    lateinit var likeBy: ToMany<Account>

    companion object {
        @JvmStatic
        val INVALID_ID: Long = -1L

        @JvmStatic
        val box: Box<Tweet>
            get() = ObjectBox.get().boxFor(Tweet::class)

        @JvmStatic
        fun query(): QueryBuilder<Tweet> = box.query()

        @JvmStatic
        fun createCache(source: Source, statuses: List<TwitterStatus>) {
            box.put(statuses.map {
                Tweet(source, it).apply {
                    cachedAt = System.currentTimeMillis()
                }
            })
        }

        private val TIMEZONE_UTC = TimeZone.getTimeZone("UTC")
        private const val FLAGS1: Int = DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_ALL
        private const val FLAGS2: Int = DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_ALL
        private const val FLAGS3: Int = DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_ALL
    }

    constructor(source: Source, status: TwitterStatus) : this() {
        val tweet = status.retweetedStatus ?: status
        Tweet.box.attach(this)
        // 標準の情報
        this.id = status.id
        this.sources.add(source)
        this.userId = status.user.id
        this.text = tweet.text
        this.rtCount = tweet.retweetCount
        this.likeCount = tweet.favoriteCount
        this.via = tweet.source
        this.timestamp = tweet.createdAt.time
        //this.ext = Extension(tweet)
        // リツイート, お気に入りの状態
        if (status.isRetweeted) {
            this.rtBy.add(source.account.target)
        }
        if (status.isFavorited) {
            this.likeBy.add(source.account.target)
        }
        // 返信に関する情報
        this.replyId = tweet.inReplyToStatusId
        this.replyUserId = tweet.inReplyToUserId
        // リツイートに関する情報
        if (status.isRetweet) {
            this.rtId = tweet.id
            this.rtUserId = tweet.user.id
        }
        // 引用ツイートに関する情報
        tweet.quotedStatus?.let { qt ->
            this.qtId = qt.id
            this.qtUserId = qt.user.id
        }
        // その他拡張情報
        ext = Extension(tweet)
    }

    val mainUserId: Long
        get() = if (isRetweet) rtUserId else userId

    val displayText: String
        get() {
            var result = text
            ext.urls.forEach { result = result.replace(it.shorten, it.display) }
            ext.medias.forEach { result = result.replace(it.shorten, "") }
            return result
        }

    val displayTime: String
        get() {
            val today = Calendar.getInstance(Locale.getDefault())
            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.timeZone = TIMEZONE_UTC
            calendar.timeInMillis = timestamp
            calendar.timeZone = TimeZone.getDefault()
            return when {
                calendar.get(Calendar.YEAR) != today.get(Calendar.YEAR) -> {
                    DateUtils.formatDateTime(TWContext, calendar.timeInMillis, FLAGS3)
                }
                calendar.get(Calendar.DAY_OF_YEAR) != today.get(Calendar.DAY_OF_YEAR) -> {
                    DateUtils.formatDateTime(TWContext, calendar.timeInMillis, FLAGS2)
                }
                else -> {
                    DateUtils.formatDateTime(TWContext, calendar.timeInMillis, FLAGS1)
                }
            }
        }

    val isReply: Boolean
        get() = replyId != -1L

    val isRetweet: Boolean
        get() = rtId != 0L

    val hasQuote: Boolean
        get() = qtId != 0L

    val hasMissing: Boolean
        get() = missings.isNotEmpty()

    val extraType: ExtraType
        get() {
            when {
                ext.medias.any() -> return ExtraType.MEDIA
                hasQuote -> return ExtraType.QUOTE
                ext.urls.size == 1 -> return ExtraType.OGP
                else -> return ExtraType.UNKNOWN
            }
        }

    override fun equals(other: Any?): Boolean {
        return other is Tweet
                && this.id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun isTheSame(other: Diffable): Boolean {
        return this == other
    }

    override fun isContentsTheSame(other: Diffable): Boolean {
        return false
    }

    class Extension() {
        var urls: List<Url> = emptyList()
        var medias: List<Media> = emptyList()
        var mentions: List<String> = emptyList()
        var hashtags: List<String> = emptyList()

        constructor(tweet: TwitterStatus) : this() {
            urls = Url.fromUrlEntities(tweet.urlEntities)
            medias = Media.fromMediaEntities(tweet.mediaEntities)
            mentions = tweet.userMentionEntities.map(UserMentionEntity::getScreenName)
            hashtags = tweet.hashtagEntities.map(HashtagEntity::getText)
        }

        class Url() {
            var shorten: String = ""
            var display: String = ""
            var expanded: String = ""

            companion object {
                @JvmStatic
                fun fromUrlEntities(urlEntities: Array<URLEntity>): List<Url> {
                    return urlEntities.map { Url(it) }
                }
            }

            constructor(urlEntity: URLEntity) : this() {
                this.shorten = urlEntity.url
                this.display = urlEntity.displayURL
                this.expanded = urlEntity.expandedURL
            }
        }

        class Media() {
            var shorten: String = ""
            var display: String = ""
            var expanded: String = ""
            var url: String = ""
            var type: String = ""
            var videoAspectRatioHeight: Int = 0
            var videoAspectRatioWidth: Int = 0
            var videoDurationMillis: Long = 0
            var videoVariants: List<Video> = emptyList()

            companion object {
                @JvmStatic
                fun fromMediaEntities(mediaEntities: Array<MediaEntity>): List<Media> {
                    return mediaEntities.map { Media(it) }
                }
            }

            constructor(mediaEntity: MediaEntity) : this() {
                this.shorten = mediaEntity.url
                this.display = mediaEntity.displayURL
                this.expanded = mediaEntity.expandedURL
                this.url = mediaEntity.mediaURLHttps ?: mediaEntity.mediaURL
                this.type = mediaEntity.type
                if (this.isVideo) {
                    this.videoAspectRatioHeight = mediaEntity.videoAspectRatioHeight
                    this.videoAspectRatioWidth = mediaEntity.videoAspectRatioWidth
                    this.videoDurationMillis = mediaEntity.videoDurationMillis
                    this.videoVariants = mediaEntity.videoVariants.map { Video(it) }
                }
            }

            val isPhoto: Boolean
                get() = type == "photo"

            val isVideo: Boolean
                get() = type == "video"

            val isAnimatedGif: Boolean
                get() = type == "animated_gif"

            class Video() {
                var bitrate: Int = 0
                var contentType: String = ""
                var url: String = ""

                constructor(videoVariant: MediaEntity.Variant) : this() {
                    this.bitrate = videoVariant.bitrate
                    this.contentType = videoVariant.contentType
                    this.url = videoVariant.url
                }
            }
        }

        class Converter : PropertyConverter<Extension, String> {
            override fun convertToEntityProperty(value: String): Extension {
                return gson.fromJson(value, Extension::class.java)
            }

            override fun convertToDatabaseValue(value: Extension): String {
                return gson.toJson(value)
            }
        }
    }

    enum class ExtraType {
        UNKNOWN,
        MEDIA,
        QUOTE,
        OGP,
    }
}
