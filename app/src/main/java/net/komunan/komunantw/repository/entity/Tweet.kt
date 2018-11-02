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
data class Tweet(
        @PrimaryKey
        @ColumnInfo(name = "id")
        var id: Long,
        @ColumnInfo(name = "user_id")
        var userId: Long,
        @ColumnInfo(name = "text")
        var text: String,
        @ColumnInfo(name = "timestamp")
        var timestamp: Long
) {
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
    constructor(status: Status): this(
            id = status.id,
            userId = status.user.id,
            text = status.text,
            timestamp = status.createdAt.time
    )
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

class TweetDetail {
    @ColumnInfo(name = "id")
    var id: Long = 0
    @ColumnInfo(name = "user_id")
    var userId: Long = 0
    @ColumnInfo(name = "text")
    lateinit var text: String
    @ColumnInfo(name = "timestamp")
    var timestamp: Long = 0
    @ColumnInfo(name = "is_missing")
    var _isMissing: Int = 0
    @ColumnInfo(name = "source_ids")
    lateinit var sourceIds: String

    var isMissing: Boolean
        get() = _isMissing.toBoolean()
        set(value) {
            _isMissing = value.toInt()
        }
}
