package net.komunan.komunantw.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import net.komunan.komunantw.repository.entity.TimelineSource

@Dao
abstract class TimelineSourceDao {

    /* ==================== SQL Definitions. ==================== */

    companion object {
        private const val QUERY_COUNT                = "SELECT COUNT(*) FROM timeline_source"
        private const val QUERY_COUNT_BY_TIMELIEN_ID = "SELECT COUNT(*) FROM timeline_source WHERE timeline_id = :timelineId"
        private const val QUERY_FIND                 = "SELECT * FROM timeline_source WHERE timeline_id = :timelineId AND source_id = :sourceId"
        private const val QUERY_FIND_ALL             = "SELECT * FROM timeline_source ORDER BY timeline_id ASC, source_id ASC"
        private const val QUERY_FIND_BY_TIMELINE_ID  = "SELECT * FROM timeline_source WHERE timeline_id = :timelineId ORDER BY source_id ASC"
    }

    @Query(QUERY_COUNT)
    abstract fun count(): Int

    @Query(QUERY_COUNT_BY_TIMELIEN_ID)
    abstract fun countByTimelineId(timelineId: Long): Int

    @Query(QUERY_FIND)
    abstract fun find(timelineId: Long, sourceId: Long): TimelineSource?

    @Query(QUERY_FIND_ALL)
    abstract fun findAll(): List<TimelineSource>

    @Query(QUERY_FIND_BY_TIMELINE_ID)
    abstract fun findByTimelineIdAsync(timelineId: Long): LiveData<List<TimelineSource>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun save(timelineSource: TimelineSource)

    @Delete
    abstract fun delete(timelineSource: TimelineSource)
}
