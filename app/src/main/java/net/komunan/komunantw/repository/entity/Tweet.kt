package net.komunan.komunantw.repository.entity

import androidx.paging.LivePagedListBuilder
import androidx.room.*
import net.komunan.komunantw.common.Diffable
import net.komunan.komunantw.repository.database.TWCacheDatabase
import net.komunan.komunantw.repository.database.TransactionTarget
import net.komunan.komunantw.repository.database.transaction
import net.komunan.komunantw.extension.toBoolean
import net.komunan.komunantw.extension.toInt
import twitter4j.MediaEntity
import twitter4j.Status
import twitter4j.URLEntity

@Suppress("PropertyName")
@Entity(tableName = "tweet")
open class Tweet(): Diffable {
    @PrimaryKey
    @ColumnInfo(name = "id")            var id          : Long = 0L
    @ColumnInfo(name = "user_id")       var userId      : Long = 0L
    @ColumnInfo(name = "text")          var text        : String = ""
    @ColumnInfo(name = "via")           var via         : String = ""
    @ColumnInfo(name = "retweeted")     var _retweeted  : Int = 0
    @ColumnInfo(name = "retweet_count") var retweetCount: Int = 0
    @ColumnInfo(name = "liked")         var _liked      : Int = 0
    @ColumnInfo(name = "like_count")    var likeCount   : Int = 0
    @ColumnInfo(name = "timestamp")     var timestamp   : Long = 0L
    @ColumnInfo(name = "retweeted_by")  var retweetedBy : Long = 0L
    @ColumnInfo(name = "retweeted_id")  var retweetedId : Long = 0L
    @ColumnInfo(name = "urls")          var urls        : List<TweetUrl> = emptyList()
    @ColumnInfo(name = "medias")        var medias      : List<TweetMedia> = emptyList()

    companion object {
        @JvmStatic
        val INVALID_ID: Long = -1L

        private val dao = TWCacheDatabase.instance.tweetDao()
        private val sourceDao = TWCacheDatabase.instance.tweetSourceDao()

        @JvmStatic fun findBySourcesAsync(sources: List<Source>) = LivePagedListBuilder(dao.findBySourcesAsync(sources), 20).build()
        @JvmStatic fun countBySource(source: Source) = sourceDao.countBySource(source)
        @JvmStatic fun maxIdBySource(source: Source) = sourceDao.maxIdBySource(source)
        @JvmStatic fun minIdBySource(source: Source) = sourceDao.minIdBySource(source)
        @JvmStatic fun prevIdBySource(source: Source, tweetId: Long) = sourceDao.prevIdBySource(source, tweetId)
        @JvmStatic fun addMissingMark(source: Source, missTweetId: Long) = sourceDao.addMissingMark(source, missTweetId)
        @JvmStatic fun removeMissingMark(source: Source, missTweetId: Long) = sourceDao.removeMissingMark(source, missTweetId)

        @JvmStatic
        fun save(source: Source, tweets: List<Tweet>) = transaction(TransactionTarget.CACHE_ONLY) {
            dao.save(tweets)
            sourceDao.save(tweets.map { TweetSource(source, it) })
        }
    }

    @Ignore
    constructor(status: Status): this() {
        this.id = status.id
        if (status.isRetweet) {
            // 基本的にリツィート元の内容に差し替えつつ、
            // <code>retweetedBy</code>に誰がリツイートしたかを保存
            val retweeted = status.retweetedStatus
            this.userId = retweeted.user.id
            this.text = retweeted.text
            this.via = retweeted.source
            this.retweeted = retweeted.isRetweeted
            this.retweetCount = retweeted.retweetCount
            this.liked = retweeted.isFavorited
            this.likeCount = retweeted.favoriteCount
            this.timestamp = retweeted.createdAt.time
            this.retweetedBy = status.user.id
            this.retweetedId = retweeted.id
            this.urls = TweetUrl.fromUrlEntities(retweeted.urlEntities)
            this.medias = TweetMedia.fromMediaEntities(retweeted.mediaEntities)
        } else {
            this.userId = status.user.id
            this.text = status.text
            this.via = status.source
            this.retweeted = status.isRetweeted
            this.retweetCount = status.retweetCount
            this.liked = status.isFavorited
            this.likeCount = status.favoriteCount
            this.timestamp = status.createdAt.time
            this.urls = TweetUrl.fromUrlEntities(status.urlEntities)
            this.medias = TweetMedia.fromMediaEntities(status.mediaEntities)
        }
    }

    var retweeted: Boolean
        get() = _retweeted.toBoolean()
        set(value) {
            _retweeted = value.toInt()
        }

    var liked: Boolean
        get() = _liked.toBoolean()
        set(value) {
            _liked = value.toInt()
        }

    val isRetweet: Boolean
        get() = retweetedBy != 0L

    override fun toString(): String {
        return "${Tweet::class.simpleName}{ " +
                "id=$id, " +
                "userId=$userId, " +
                "text=$text, " +
                "via=$via, " +
                "timestamp=$timestamp, " +
                "retweeted=$retweeted, " +
                "liked=$liked, " +
                "retweetCount=$retweetCount, " +
                "likeCount=$likeCount, " +
                "retweetedBy=$retweetedBy, " +
                "retweetedId=$retweetedId" +
                "urls=$urls" +
                "medias=$medias }"
    }

    override fun isTheSame(other: Diffable): Boolean {
        return other is Tweet
                && this.id == other.id
    }

    override fun isContentsTheSame(other: Diffable): Boolean {
        return other is Tweet
                && this.id == other.id
                && this.retweetCount == other.retweetCount
                && this.likeCount == other.likeCount
    }
}

@Suppress("PropertyName")
@Entity(tableName = "tweet_source", primaryKeys = ["source_id", "tweet_id"])
class TweetSource() {
    @ColumnInfo(name = "source_id")  var sourceId  : Long = 0L
    @ColumnInfo(name = "tweet_id")   var tweetId   : Long = 0L
    @ColumnInfo(name = "is_missing") var _isMissing: Int = 0

    @Ignore
    constructor(source: Source, tweet: Tweet): this(source, tweet.id, false)

    @Ignore
    constructor(source: Source, tweetId: Long, isMissing: Boolean): this() {
        this.sourceId = source.id
        this.tweetId = tweetId
        this._isMissing = isMissing.toInt()
    }

    override fun toString(): String {
        return "${TweetSource::class.simpleName}{ " +
                "sourceId=$sourceId, " +
                "tweetId=$tweetId, " +
                "isMissing=$_isMissing }"
    }
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

@Suppress("PropertyName")
class TweetDetail: Tweet() {
    @ColumnInfo(name = "is_missing") var _isMissing: Int = 0
    @ColumnInfo(name = "source_ids") var _sourceIds: String = ""

    val displayText: String
        get() {
            var result = text
            urls.forEach { result = result.replace(it.shorten, it.display) }
            medias.forEach { result = result.replace(it.shorten, "") }
            return result
        }

    val isMissing: Boolean
        get() = _isMissing.toBoolean()

    val sourceIds: List<Long>
        get() = _sourceIds.split(',').map { it.toLong() }

    override fun toString(): String {
        return "${TweetDetail::class.simpleName}{ " +
                "base=${super.toString()}, " +
                "isMissing=$isMissing, " +
                "sourceIds=[$_sourceIds] }"
    }
}
