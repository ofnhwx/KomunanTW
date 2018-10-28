package net.komunan.komunantw.repository.dao

import android.arch.paging.DataSource
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import net.komunan.komunantw.repository.entity.Tweet
import net.komunan.komunantw.repository.entity.TweetDetail
import net.komunan.komunantw.repository.entity.TweetSource

@Dao
interface TweetDao {
    @Query("""
SELECT t1.id, t1.user_id, t1.text, t2.has_missing, t2.source_ids
FROM tweet AS t1
INNER JOIN (SELECT tweet_id, sum(has_missing) AS has_missing, group_concat(source_id) AS source_ids FROM tweet_source WHERE source_id in (:sourceIds) GROUP BY tweet_id) AS t2
ON t1.id = t2.tweet_id
ORDER BY t1.id DESC
""")
    fun findBySourceIdsAsync(sourceIds: List<Long>): DataSource.Factory<Int, TweetDetail>

    @Query("SELECT * FROM tweet WHERE id = :id")
    fun find(id: Long): Tweet

    @Query("SELECT COUNT(*) FROM tweet")
    fun count(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(tweets: Collection<Tweet>)
}

@Dao
interface TweetSourceDao {
    @Query("SELECT COUNT(*) FROM tweet_source WHERE source_id = :sourceId")
    fun countBySourceId(sourceId: Long): Long

    @Query("SELECT MAX(tweet_id) FROM tweet_source WHERE source_id = :sourceId")
    fun maxIdBySourceId(sourceId: Long): Long

    @Query("SELECT MIN(tweet_id) FROM tweet_source WHERE source_id = :sourceId")
    fun minIdBySourceId(sourceId: Long): Long

    @Query("SELECT MIN(tweet_id) FROM tweet_source WHERE source_id = :sourceId AND tweet_id < :tweetId")
    fun prevIdBySourceId(sourceId: Long, tweetId: Long): Long

    @Query("UPDATE tweet_source SET has_missing = :hasMissing WHERE source_id = :sourceId AND tweet_id = :tweetId")
    fun updateHasMissing(sourceId: Long, tweetId: Long, hasMissing: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(tweetSources: Collection<TweetSource>)
}
