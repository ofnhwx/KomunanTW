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
    }

    @Query("""SELECT
    ts.tweet_id,
    count(ts.source_id) AS source_id,
    ts.is_missing,
    group_concat(ts.source_id) AS source_ids
FROM tweet_source AS ts
WHERE source_id IN (:sourceIds)
GROUP BY ts.tweet_id, ts.is_missing
ORDER BY ts.tweet_id DESC, ts.is_missing ASC""")
    abstract fun findBySourceIdsAsync(sourceIds: List<Long>): DataSource.Factory<Int, TweetSourceExt>

    @Query("SELECT COUNT(*) FROM tweet_source WHERE $PARTS_WHERE_SOURCE_ID")
    abstract fun countBySourceId(sourceId: Long): Long

    @Query("SELECT ifnull(MAX(tweet_id), -1) FROM tweet_source WHERE $PARTS_WHERE_SOURCE_ID")
    abstract fun maxIdBySourceId(sourceId: Long): Long

    @Query("SELECT ifnull(MIN(tweet_id), -1) FROM tweet_source WHERE $PARTS_WHERE_SOURCE_ID")
    abstract fun minIdBySourceId(sourceId: Long): Long

    @Query("SELECT ifnull(MAX(tweet_id), -1) FROM tweet_source WHERE $PARTS_WHERE_SOURCE_ID AND tweet_id < :tweetId")
    abstract fun prevIdBySourceId(sourceId: Long, tweetId: Long): Long

    @Query("SELECT DISTINCT source_id FROM tweet_source ORDER BY source_id ASC")
    abstract fun findSourceIds(): List<Long>

    @Query("DELETE FROM tweet_source WHERE source_id = :sourceId")
    abstract fun deleteBySourceId(sourceId: Long): Int

    @Query("DELETE FROM tweet_source WHERE source_id = :sourceId AND tweet_id <= (SELECT tweet_id FROM tweet_source WHERE source_id = :sourceId ORDER BY tweet_id DESC LIMIT 1 OFFSET :keepCount)")
    abstract fun deleteOld(sourceId: Long, keepCount: Int): Int

    @Query("DELETE FROM tweet_source WHERE source_id = :sourceId AND tweet_id = :tweetId AND is_missing > 0")
    abstract fun delMissing(sourceId: Long, tweetId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun save(tweetSource: TweetSource)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun save(tweetSources: List<TweetSource>)
}
