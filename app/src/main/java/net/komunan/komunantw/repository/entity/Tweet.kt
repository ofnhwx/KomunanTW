package net.komunan.komunantw.repository.entity

import androidx.room.*
import androidx.paging.LivePagedListBuilder
import net.komunan.komunantw.toBoolean
import net.komunan.komunantw.toInt
import net.komunan.komunantw.repository.database.TWCacheDatabase
import net.komunan.komunantw.repository.database.TransactionTarget
import net.komunan.komunantw.repository.database.transaction
import twitter4j.Status

@Entity(tableName = "tweet")
open class Tweet() {
    @PrimaryKey
    @ColumnInfo(name = "id")           var id         : Long = 0
    @ColumnInfo(name = "user_id")      var userId     : Long = 0
    @ColumnInfo(name = "text")         var text       : String = ""
    @ColumnInfo(name = "via")          var via        : String = ""
    @ColumnInfo(name = "timestamp")    var timestamp  : Long = 0
    @ColumnInfo(name = "retweeted_by") var retweetedBy: Long = 0
    @ColumnInfo(name = "retweeted_id") var retweetedId: Long = 0

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
            sourceDao.save(tweets.map { TweetSource(source.id, it.id) })
        }
    }

    @Ignore
    constructor(status: Status): this() {
        this.id = status.id
        if (status.isRetweet) {
            val retweeted = status.retweetedStatus
            this.userId = retweeted.user.id
            this.text = retweeted.text
            this.via = retweeted.source
            this.timestamp = retweeted.createdAt.time
            this.retweetedBy = status.user.id
            this.retweetedId = retweeted.id
        } else {
            this.userId = status.user.id
            this.text = status.text
            this.via = status.source
            this.timestamp = status.createdAt.time
        }
    }

    val isRetweet: Boolean
        get() = retweetedBy != 0L

    override fun toString(): String {
        return "${Tweet::class.simpleName}{ id=$id, userId=$userId, text=$text, via=$via, timestamp=$timestamp }"
    }
}

@Entity(
        tableName = "tweet_source",
        primaryKeys = ["source_id", "tweet_id"]
)
data class TweetSource(
        @ColumnInfo(name = "source_id")
        var sourceId: Long,
        @ColumnInfo(name = "tweet_id")
        var tweetId: Long,
        @ColumnInfo(name = "is_missing")
        var _isMissing: Int
) {
    @Ignore
    constructor(sourceId: Long, tweetId: Long): this(
            sourceId = sourceId,
            tweetId = tweetId,
            _isMissing = 0
    )
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
        return "${TweetDetail::class.simpleName}{ base=${super.toString()}, isMissing=$isMissing, sourceIds=[$sourceIds] }"
    }
}
