package net.komunan.komunantw.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import net.komunan.komunantw.repository.entity.TimelineSource

@Dao
abstract class TimelineSourceDao {

    /* ==================== SQL Definitions. ==================== */

    @Query("SELECT COUNT(*) FROM timeline_source")
    abstract fun count(): Int

    @Query("SELECT COUNT(*) FROM timeline_source WHERE timeline_id = :timelineId")
    abstract fun countByTimelineId(timelineId: Long): Int

    @Query("SELECT * FROM timeline_source WHERE timeline_id = :timelineId AND source_id = :sourceId")
    abstract fun find(timelineId: Long, sourceId: Long): TimelineSource?

    @Query("SELECT * FROM timeline_source ORDER BY timeline_id ASC, source_id ASC")
    abstract fun findAll(): List<TimelineSource>

    @Query("SELECT * FROM timeline_source WHERE timeline_id = :timelineId ORDER BY source_id ASC")
    abstract fun findByTimelineIdAsync(timelineId: Long): LiveData<List<TimelineSource>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun save(timelineSource: TimelineSource)

    @Delete
    abstract fun delete(timelineSource: TimelineSource)
}
