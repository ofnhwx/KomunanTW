package net.komunan.komunantw.repository.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.komunan.komunantw.repository.entity.TweetSource
import net.komunan.komunantw.repository.entity.ext.TweetSourceExt

@Dao
abstract class TweetSourceDao {

    /* ==================== Functions. ==================== */

    fun addMissing(sourceId: Long, tweetId: Long) {
        save(TweetSource(sourceId, tweetId, true))
    }

    /* ==================== SQL Definitions. ==================== */

    companion object {
        private const val PARTS_WHERE_SOURCE_ID = "source_id = :sourceId AND is_missing = 0"

        private const val QUERY_COUNT_BY_SOURCE_ID   = "SELECT COUNT(*) FROM tweet_source WHERE $PARTS_WHERE_SOURCE_ID"
        private const val QUERY_MAX_ID_BY_SOURCE_ID  = "SELECT ifnull(MAX(tweet_id), -1) FROM tweet_source WHERE $PARTS_WHERE_SOURCE_ID"
        private const val QUERY_MIN_ID_BY_SOURCE_ID  = "SELECT ifnull(MIN(tweet_id), -1) FROM tweet_source WHERE $PARTS_WHERE_SOURCE_ID"
        private const val QUERY_PREV_ID_BY_SOURCE_ID = "SELECT ifnull(MAX(tweet_id), -1) FROM tweet_source WHERE $PARTS_WHERE_SOURCE_ID AND tweet_id < :tweetId"

        private const val QUERY_FIND_BY_SOURCE_IDS   = """SELECT
    ts.tweet_id,
    count(ts.source_id) AS source_id,
    ts.is_missing,
    group_concat(ts.source_id) AS source_ids
FROM tweet_source AS ts
WHERE source_id IN (:sourceIds)
GROUP BY ts.tweet_id, ts.is_missing
ORDER BY ts.tweet_id DESC, ts.is_missing ASC"""

        private const val QUERY_REMOVE_MISSING = "DELETE FROM tweet_source WHERE source_id = :sourceId AND tweet_id = :tweetId AND is_missing > 0"
    }

    @Query(QUERY_FIND_BY_SOURCE_IDS)
    abstract fun findBySourceIdsAsync(sourceIds: List<Long>): DataSource.Factory<Int, TweetSourceExt>

    @Query(QUERY_COUNT_BY_SOURCE_ID)
    abstract fun countBySourceId(sourceId: Long): Long

    @Query(QUERY_MAX_ID_BY_SOURCE_ID)
    abstract fun maxIdBySourceId(sourceId: Long): Long

    @Query(QUERY_MIN_ID_BY_SOURCE_ID)
    abstract fun minIdBySourceId(sourceId: Long): Long

    @Query(QUERY_PREV_ID_BY_SOURCE_ID)
    abstract fun prevIdBySourceId(sourceId: Long, tweetId: Long): Long

    @Query(QUERY_REMOVE_MISSING)
    abstract fun delMissing(sourceId: Long, tweetId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun save(tweetSource: TweetSource)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun save(tweetSources: List<TweetSource>)
}
