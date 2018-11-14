package net.komunan.komunantw.repository.entity

import androidx.room.*
import net.komunan.komunantw.extension.TransactionTarget
import net.komunan.komunantw.extension.gson
import net.komunan.komunantw.extension.transaction
import net.komunan.komunantw.repository.database.TWCacheDatabase
import org.apache.commons.lang3.builder.ToStringBuilder
import twitter4j.MediaEntity
import twitter4j.Status
import twitter4j.URLEntity

@Suppress("PropertyName")
@Entity(tableName = "tweet", primaryKeys = ["id"])
open class Tweet() {
    @ColumnInfo(name = "id")            var id          : Long = 0L
    @ColumnInfo(name = "user_id")       var userId      : Long = 0L
    @ColumnInfo(name = "text")          var text        : String = ""
    @ColumnInfo(name = "retweet_count") var retweetCount: Int = 0
    @ColumnInfo(name = "like_count")    var likeCount   : Int = 0
    @ColumnInfo(name = "timestamp")     var timestamp   : Long = 0L
    @ColumnInfo(name = "via")           var via         : String = ""
    @ColumnInfo(name = "reply_id")      var replyId     : Long = 0L
    @ColumnInfo(name = "reply_user_id") var replyUserId : Long = 0L
    @ColumnInfo(name = "rt_id")         var rtId        : Long = 0L
    @ColumnInfo(name = "rt_user_id")    var rtUserId    : Long = 0L
    @ColumnInfo(name = "qt_id")         var qtId        : Long = 0L
    @ColumnInfo(name = "qt_user_id")    var qtUserId    : Long = 0L
    @ColumnInfo(name = "ext")           var ext         : TweetExtension = TweetExtension()

    companion object {
        @JvmStatic val INVALID_ID: Long = -1L
        @JvmStatic val dao = TWCacheDatabase.instance.tweetDao()
        @JvmStatic val accountDao = TWCacheDatabase.instance.tweetAccountDao()
        @JvmStatic val sourceDao = TWCacheDatabase.instance.tweetSourceDao()

        @JvmStatic
        fun createCache(source: Source, statuses: List<Status>) = transaction(TransactionTarget.WITH_CACHE) {
            val account = Account.dao.find(source.accountId)!!
            statuses.forEach { dao.save(Tweet(it)) }
            accountDao.save(statuses.map { TweetAccount(account.id, it) })
            sourceDao.save(statuses.map { TweetSource(source.id, it.id) })
        }
    }

    @Ignore
    constructor(status: Status): this() {
        (status.retweetedStatus ?: status).also {
            this.id = status.id
            this.userId = status.user.id
            this.text = it.text
            this.retweetCount = it.retweetCount
            this.likeCount = it.favoriteCount
            this.via = it.source
            this.timestamp = it.createdAt.time
            this.ext = TweetExtension(it)
            // 返信に関する情報
            this.replyId = it.inReplyToStatusId
            this.replyUserId = it.inReplyToUserId
            // リツイートに関する情報
            if (status.isRetweet) {
                this.rtId = it.id
                this.rtUserId = it.user.id
            }
            // 引用ツイートに関する情報
            it.quotedStatus?.let { qt ->
                this.qtId = qt.id
                this.qtUserId = qt.user.id
            }
        }
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

    val isRetweet: Boolean
        get() = rtId != 0L

    val hasQuote: Boolean
        get() = qtId != 0L

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this)
    }

    fun save() = dao.save(this)
}

class TweetExtension() {
    var urls: List<TweetUrl> = emptyList()
    var medias: List<TweetMedia> = emptyList()

    constructor(status: Status): this() {
        urls = TweetUrl.fromUrlEntities(status.urlEntities)
        medias = TweetMedia.fromMediaEntities(status.mediaEntities)
    }

    fun isEmpty(): Boolean {
        return urls.isEmpty() && medias.isEmpty()
    }

    class Converter {
        @TypeConverter fun fromJson(value: String): TweetExtension = if (value.isEmpty()) TweetExtension() else gson.fromJson(value, TweetExtension::class.java)
        @TypeConverter fun toJson(value: TweetExtension): String = if (value.isEmpty()) "" else gson.toJson(value)
    }

    class TweetUrl() {
        var shorten : String = ""
        var display : String = ""
        var expanded: String = ""

        companion object {
            @JvmStatic
            fun fromUrlEntities(urlEntities: Array<URLEntity>): List<TweetUrl> {
                return urlEntities.filter { it.url != null && it.displayURL != null && it.expandedURL != null }
                        .map { TweetUrl(it) }
            }
        }

        constructor(urlEntity: URLEntity): this() {
            this.shorten = urlEntity.url
            this.display = urlEntity.displayURL
            this.expanded = urlEntity.expandedURL
        }
    }

    class TweetMedia() {
        var shorten : String = ""
        var display : String = ""
        var expanded: String = ""
        var url: String = ""
        var type: String = ""
        var videoAspectRatioHeight: Int = 0
        var videoAspectRatioWidth: Int = 0
        var videoDurationMillis: Long = 0
        var videoVariants: List<Video> = emptyList()

        companion object {
            @JvmStatic
            fun fromMediaEntities(mediaEntities: Array<MediaEntity>): List<TweetMedia> {
                return mediaEntities
                        .map { TweetMedia(it) }
            }
        }

        constructor(mediaEntity: MediaEntity): this() {
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

            constructor(videoVariant: MediaEntity.Variant): this() {
                this.bitrate = videoVariant.bitrate
                this.contentType = videoVariant.contentType
                this.url = videoVariant.url
            }
        }
    }
}
