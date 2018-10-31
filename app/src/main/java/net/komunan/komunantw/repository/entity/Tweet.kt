package net.komunan.komunantw.repository.entity

import android.arch.paging.LivePagedListBuilder
import android.arch.persistence.room.*
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
        var text: String
) {
    companion object {
        private val dao = TWCacheDatabase.instance.tweetDao()
        private val sourceDao = TWCacheDatabase.instance.tweetSourceDao()

        @JvmStatic fun findBySourceIdsAsync(sourceIds: List<Long>)
                = LivePagedListBuilder(dao.findBySourceIdsAsync(sourceIds), 20).build()
        @JvmStatic fun countBySourceId(sourceId: Long) = sourceDao.countBySourceId(sourceId)
        @JvmStatic fun maxIdBySourceId(sourceId: Long) = sourceDao.maxIdBySourceId(sourceId)
        @JvmStatic fun minIdBySourceId(sourceId: Long) = sourceDao.minIdBySourceId(sourceId)
        @JvmStatic fun prevIdBySourceId(sourceId: Long, tweetId: Long) = sourceDao.prevIdBySourceId(sourceId, tweetId)
        @JvmStatic fun addMissingMark(sourceId: Long, missTweetId: Long) = sourceDao.addMissingMark(sourceId, missTweetId)
        @JvmStatic fun removeMissingMark(sourceId: Long, missTweetId: Long) = sourceDao.removeMissingMark(sourceId, missTweetId)

        @JvmStatic
        fun save(tweets: Collection<Tweet>, source: Source) = transaction(TransactionTarget.CACHE_ONLY) {
            dao.save(tweets)
            sourceDao.save(tweets.map { TweetSource(source.id, it.id) })
        }
    }

    @Ignore
    constructor(status: Status): this(status.id, status.user.id, status.text)
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
    constructor(sourceId: Long, tweetId: Long): this(sourceId, tweetId, 0)
}

class TweetDetail {
    @ColumnInfo(name = "id")
    var id: Long = 0
    @ColumnInfo(name = "user_id")
    var userId: Long = 0
    @ColumnInfo(name = "text")
    lateinit var text: String
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
