package net.komunan.komunantw.repository.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.komunan.komunantw.repository.entity.Tweet
import net.komunan.komunantw.repository.entity.TweetDetail
import net.komunan.komunantw.repository.entity.TweetSource

@Dao
interface TweetDao {
@Query("""
SELECT ts.tweet_id AS id, ifnull(t.user_id, 0) AS user_id, ifnull(t.text, '') AS text, ts.is_missing, ts.source_ids
FROM (SELECT tweet_id, sum(is_missing) AS is_missing, group_concat(source_id) AS source_ids FROM tweet_source WHERE source_id in (:sourceIds) GROUP BY tweet_id) AS ts
LEFT OUTER JOIN tweet AS t ON t.id = ts.tweet_id
ORDER BY t.id DESC
""")
    fun findBySourceIdsAsync(sourceIds: List<Long>): DataSource.Factory<Int, TweetDetail>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(tweets: Collection<Tweet>)
}

@Dao
abstract class TweetSourceDao {
    @Query("SELECT COUNT(*) FROM tweet_source WHERE source_id = :sourceId")
    abstract fun countBySourceId(sourceId: Long): Long

    @Query("SELECT MAX(tweet_id) FROM tweet_source WHERE source_id = :sourceId")
    abstract fun maxIdBySourceId(sourceId: Long): Long

    @Query("SELECT MIN(tweet_id) FROM tweet_source WHERE source_id = :sourceId")
    abstract fun minIdBySourceId(sourceId: Long): Long

    @Query("SELECT MIN(tweet_id) FROM tweet_source WHERE source_id = :sourceId AND tweet_id < :tweetId")
    abstract fun prevIdBySourceId(sourceId: Long, tweetId: Long): Long

    @Query("DELETE FROM tweet_source WHERE source_id = :sourceId AND tweet_id = :missTweetId AND is_missing = 0")
    abstract fun removeMissingMark(sourceId: Long, missTweetId: Long)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun _save(tweetSource: TweetSource)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun save(tweetSources: Collection<TweetSource>)

    fun addMissingMark(sourceId: Long, missTweetId: Long) {
        _save(TweetSource(sourceId, missTweetId, 1))
    }
}
