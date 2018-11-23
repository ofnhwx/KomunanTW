@file:Suppress("unused")

package net.komunan.komunantw.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.github.ajalt.timberkt.d
import net.komunan.komunantw.common.extension.transaction
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.repository.entity.ext.TimelineExt

@Dao
abstract class TimelineDao {

    /* ==================== Functions. ==================== */

    fun save(timeline: Timeline): Timeline {
        if (timeline.id == 0L) {
            timeline.id = pInsert(timeline.apply {
                position = count()
                createAt = System.currentTimeMillis()
            })
        } else {
            pUpdate(timeline.apply {
                updateAt = System.currentTimeMillis()
            })
        }
        d { "save: $timeline" }
        return timeline
    }

    fun delete(timeline: Timeline): Unit = transaction {
        pDelete(timeline)
        pPackPosition(timeline.position)
    }

    fun moveTo(timeline: Timeline, position: Int): Unit = transaction {
        d { "move: $timeline to position:$position" }
        when {
            position == timeline.position -> return@transaction
            position > timeline.position -> pShiftLeft(timeline.position + 1, position)
            position < timeline.position -> pShiftRight(position, timeline.position - 1)
        }
        timeline.position = position
        save(timeline)
    }

    /* ==================== SQL Definitions. ==================== */

    companion object {
        private const val QUERY_FIND = "SELECT * FROM timeline WHERE id = :id"
        private const val QUERY_FIND_ALL = """SELECT
    t.*,
    ts.source_count
FROM timeline AS t
LEFT OUTER JOIN (SELECT timeline_id, count(*) AS source_count FROM timeline_source) as ts ON t.id = ts.timeline_id
ORDER BY t.position ASC"""
    }

    @Query("SELECT COUNT(*) FROM timeline")
    abstract fun count(): Int

    @Query(QUERY_FIND)
    abstract fun find(id: Long): Timeline?

    @Query(QUERY_FIND)
    abstract fun findAsync(id: Long): LiveData<Timeline?>

    @Query(QUERY_FIND_ALL)
    abstract fun findAll(): List<TimelineExt>

    @Query(QUERY_FIND_ALL)
    abstract fun findAllAsync(): LiveData<List<TimelineExt>>

    /* ==================== Protected SQL Definitions. ==================== */

    @Query("UPDATE timeline SET position = position - 1 WHERE position > :deleted")
    protected abstract fun pPackPosition(deleted: Int)

    @Query("UPDATE timeline SET position = position - 1 WHERE position BETWEEN :begin AND :end")
    protected abstract fun pShiftLeft(begin: Int, end: Int)

    @Query("UPDATE timeline SET position = position + 1 WHERE position BETWEEN :begin AND :end")
    protected abstract fun pShiftRight(begin: Int, end: Int)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract fun pInsert(timeline: Timeline): Long

    @Update
    protected abstract fun pUpdate(timeline: Timeline)

    @Delete
    protected abstract fun pDelete(timeline: Timeline)
}
