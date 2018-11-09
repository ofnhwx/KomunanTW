@file:Suppress("FunctionName")

package net.komunan.komunantw.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.github.ajalt.timberkt.d
import net.komunan.komunantw.R
import net.komunan.komunantw.extension.string
import net.komunan.komunantw.repository.database.transaction
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.repository.entity.TimelineSource

@Dao
abstract class TimelineDao {
    fun findAllAsync() = __findAllAsync()
    fun findAsync(id: Long) = __findAsync(id)
    fun find(id: Long) = __find(id)
    fun count() = __count()
    fun save(timeline: Timeline) = __save__(timeline)
    fun delete(timeline: Timeline) = __delete__(timeline)
    fun moveTo(timeline: Timeline, position: Int) = __moveTo__(timeline, position)
    fun firstSetup(account: Account) = __firstSetup__(account)

    /* ==================== SQL Definitions. ==================== */

    @Query("SELECT * FROM timeline ORDER BY position ASC")
    abstract fun __findAllAsync(): LiveData<List<Timeline>>
    @Query("SELECT * FROM timeline WHERE id = :id")
    abstract fun __findAsync(id: Long): LiveData<Timeline?>
    @Query("SELECT * FROM timeline WHERE id = :id")
    abstract fun __find(id: Long): Timeline?
    @Query("SELECT COUNT(*) FROM Timeline")
    abstract fun __count(): Int
    @Query("UPDATE timeline SET position = position - 1 WHERE position > :deleted")
    abstract fun __packPosition(deleted: Int)
    @Query("UPDATE timeline SET position = position - 1 WHERE position BETWEEN :begin AND :end")
    abstract fun __shiftLeft(begin: Int, end: Int)
    @Query("UPDATE timeline SET position = position + 1 WHERE position BETWEEN :begin AND :end")
    abstract fun __shiftRight(begin: Int, end: Int)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun __insert(timeline: Timeline): Long
    @Update
    abstract fun __update(timeline: Timeline)
    @Delete
    abstract fun __delete(timeline: Timeline)

    /* ==================== Private Functions. ==================== */

   private fun __save__(timeline: Timeline): Timeline {
        if (timeline.id == 0L) {
            timeline.id = __insert(timeline.apply {
                position = __count() + 1
                createAt = System.currentTimeMillis()
            })
        } else {
            __update(timeline.apply {
                updateAt = System.currentTimeMillis()
            })
        }
        d { "save: $timeline" }
        return timeline
    }

    private fun __delete__(timeline: Timeline) {
        transaction {
            __delete(timeline)
            __packPosition(timeline.position)
        }
    }

    private fun __moveTo__(timeline: Timeline, position: Int) {
        transaction {
            d { "move: $timeline to position=$position" }
            when {
                position == timeline.position -> return@transaction
                position > timeline.position -> __shiftLeft(timeline.position + 1, position)
                position < timeline.position -> __shiftRight(position, timeline.position - 1)
            }
            timeline.position = position
            __save__(timeline)
        }
    }

    private fun __firstSetup__(account: Account) {
        transaction {
            if (Timeline.count() > 0) {
                return@transaction
            }
            val timeline = __save__(Timeline(string[R.string.default_label]()))
            val homeSource = Source.findByAccount(account).first { Source.SourceType.valueOf(it.type) == Source.SourceType.HOME }
            timeline.addSource(homeSource)
        }
    }
}

@Dao
abstract class TimelineSourceDao {
    fun count() = __count()
    fun add(timeline: Timeline, source: Source) = __save(TimelineSource(timeline, source))
    fun remove(timeline: Timeline, source: Source) = __delete(TimelineSource(timeline, source))

    /* ==================== SQL Definitions. ==================== */

    @Query("SELECT COUNT(*) FROM timeline_source")
    protected abstract fun __count(): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun __save(timelineSource: TimelineSource)
    @Delete
    protected abstract fun __delete(timelineSource: TimelineSource)
}
