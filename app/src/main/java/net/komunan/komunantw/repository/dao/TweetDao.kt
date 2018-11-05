@file:Suppress("FunctionName")

package net.komunan.komunantw.repository.dao

import androidx.paging.DataSource
import androidx.room.*
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.repository.entity.Tweet
import net.komunan.komunantw.repository.entity.TweetDetail
import net.komunan.komunantw.repository.entity.TweetSource

@Dao
abstract class TweetDao {
    fun findBySourcesAsync(sources: List<Source>) = __findBySourceIdsAsync(sources.map { it.id })
    fun save(tweets: List<Tweet>) = __save(tweets)

    /* ==================== SQL Definitions. ==================== */

    @Query("""SELECT
    ts.tweet_id AS id,
    ifnull(t.user_id, 0) AS user_id,
    ifnull(t.text, '') AS text,
    ifnull(t.via, '') AS via,
    ifnull(t.timestamp, 0) AS timestamp,
    ifnull(t.retweeted_by, 0) AS retweeted_by,
    ifnull(t.retweeted_id, 0) AS retweeted_id,
    ts.is_missing,
    ts.source_ids
FROM (SELECT tweet_id, sum(is_missing) AS is_missing, group_concat(source_id) AS source_ids FROM tweet_source WHERE source_id in (:sourceIds) GROUP BY tweet_id) AS ts
LEFT OUTER JOIN tweet AS t ON t.id = ts.tweet_id
ORDER BY t.id DESC""")
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
    fun addMissingMark(source: Source, missTweetId: Long) = __save(TweetSource(source.id, missTweetId, _isMissing = 1))
    fun removeMissingMark(source: Source, missTweetId: Long) = __removeMissingMark(source.id, missTweetId)

    /* ==================== SQL Definitions. ==================== */

    @Query("SELECT COUNT(*) FROM tweet_source WHERE source_id = :sourceId")
    abstract fun __countBySourceId(sourceId: Long): Long
    @Query("SELECT MAX(tweet_id) FROM tweet_source WHERE source_id = :sourceId")
    abstract fun __maxIdBySourceId(sourceId: Long): Long
    @Query("SELECT MIN(tweet_id) FROM tweet_source WHERE source_id = :sourceId")
    abstract fun __minIdBySourceId(sourceId: Long): Long
    @Query("SELECT MAX(tweet_id) FROM tweet_source WHERE source_id = :sourceId AND tweet_id < :tweetId")
    abstract fun __prevIdBySourceId(sourceId: Long, tweetId: Long): Long
    @Query("DELETE FROM tweet_source WHERE source_id = :sourceId AND tweet_id = :missTweetId AND is_missing = 0")
    abstract fun __removeMissingMark(sourceId: Long, missTweetId: Long)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun __save(tweetSource: TweetSource)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun __save(tweetSources: List<TweetSource>)
}
