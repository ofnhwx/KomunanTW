@file:Suppress("FunctionName")

package net.komunan.komunantw.repository.dao

import androidx.paging.DataSource
import androidx.room.*
import net.komunan.komunantw.repository.entity.*

@Dao
abstract class TweetDao {
    fun findBySourcesAsync(sources: List<Source>) = __findBySourceIdsAsync(sources.map { it.id })
    fun save(tweets: List<Tweet>) = __save(tweets)

    /* ==================== SQL Definitions. ==================== */

    @Query("""SELECT
    ts.tweet_id AS id,
    ifnull(t.user_id, 0) AS user_id,
    ifnull(t.text, '') AS text,
    ifnull(t.retweet_count, 0) AS retweet_count,
    ifnull(t.like_count, 0) AS like_count,
    ifnull(t.timestamp, 0) AS timestamp,
    ifnull(t.via, '') AS via,
    ifnull(t.reply_id, 0) AS reply_id,
    ifnull(t.reply_user_id, 0) AS reply_user_id,
    ifnull(t.rt_id, 0) AS rt_id,
    ifnull(t.rt_user_id, 0) AS rt_user_id,
    ifnull(t.qt_id, 0) AS qt_id,
    ifnull(t.qt_user_id, 0) AS qt_user_id,
    ifnull(t.ext, '') AS ext,
    ts.retweeted,
    ts.liked,
    ts.is_missing,
    ts.source_ids
FROM (SELECT tweet_id, sum(retweeted) retweeted,  sum(liked) as liked, sum(is_missing) AS is_missing, group_concat(source_id) AS source_ids FROM tweet_source WHERE source_id in (:sourceIds) GROUP BY tweet_id) AS ts
LEFT OUTER JOIN tweet AS t ON t.id = ts.tweet_id
ORDER BY ts.tweet_id DESC""")
    protected abstract fun __findBySourceIdsAsync(sourceIds: List<Long>): DataSource.Factory<Int, TweetDetail>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun __save(tweets: List<Tweet>)
}

@Dao
abstract class TweetSourceDao {
    fun countBySource(source: Source) = __countBySourceId(source.id)
    fun maxIdBySource(source: Source) = __maxIdBySourceId(source.id)
    fun minIdBySource(source: Source) = __minIdBySourceId(source.id)
    fun prevIdBySource(source: Source, tweetId: Long) = __prevIdBySourceId(source.id, tweetId)
    fun save(tweetSource: TweetSource) = __save(tweetSource)
    fun save(tweetSources: List<TweetSource>) = __save(tweetSources)
    fun addMissingMark(source: Source, missTweetId: Long) = __save(TweetSource(source, missTweetId, true))
    fun removeMissingMark(source: Source, missTweetId: Long) = __removeMissingMark(source.id, missTweetId)

    /* ==================== SQL Definitions. ==================== */

    @Query("SELECT COUNT(*) FROM tweet_source WHERE source_id = :sourceId AND is_missing = 0")
    abstract fun __countBySourceId(sourceId: Long): Long
    @Query("SELECT ifnull(MAX(tweet_id), -1) FROM tweet_source WHERE source_id = :sourceId AND is_missing = 0")
    abstract fun __maxIdBySourceId(sourceId: Long): Long
    @Query("SELECT ifnull(MIN(tweet_id), -1) FROM tweet_source WHERE source_id = :sourceId AND is_missing = 0")
    abstract fun __minIdBySourceId(sourceId: Long): Long
    @Query("SELECT ifnull(MAX(tweet_id), -1) FROM tweet_source WHERE source_id = :sourceId AND tweet_id < :tweetId AND is_missing = 0")
    abstract fun __prevIdBySourceId(sourceId: Long, tweetId: Long): Long
    @Query("DELETE FROM tweet_source WHERE source_id = :sourceId AND tweet_id = :missTweetId AND is_missing > 0")
    abstract fun __removeMissingMark(sourceId: Long, missTweetId: Long)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun __save(tweetSource: TweetSource)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun __save(tweetSources: List<TweetSource>)
}
