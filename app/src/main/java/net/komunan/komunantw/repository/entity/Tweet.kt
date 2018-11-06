package net.komunan.komunantw.repository.entity

import androidx.room.*
import androidx.paging.LivePagedListBuilder
import net.komunan.komunantw.common.Diffable
import net.komunan.komunantw.toBoolean
import net.komunan.komunantw.toInt
import net.komunan.komunantw.repository.database.TWCacheDatabase
import net.komunan.komunantw.repository.database.TransactionTarget
import net.komunan.komunantw.repository.database.transaction
import twitter4j.Status

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

    companion object {
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
        } else {
            this.userId = status.user.id
            this.text = status.text
            this.via = status.source
            this.retweeted = status.isRetweeted
            this.retweetCount = status.retweetCount
            this.liked = status.isFavorited
            this.likeCount = status.favoriteCount
            this.timestamp = status.createdAt.time
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
                "retweetedId=$retweetedId }"
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

@Suppress("PropertyName")
class TweetDetail: Tweet() {
    @ColumnInfo(name = "is_missing") var _isMissing: Int = 0
    @ColumnInfo(name = "source_ids") var sourceIds : String = ""

    var isMissing: Boolean
        get() = _isMissing.toBoolean()
        set(value) {
            _isMissing = value.toInt()
        }

    override fun toString(): String {
        return "${TweetDetail::class.simpleName}{ " +
                "base=${super.toString()}, " +
                "isMissing=$isMissing, " +
                "sourceIds=[$sourceIds] }"
    }
}
