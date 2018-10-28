package net.komunan.komunantw.repository.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.github.ajalt.timberkt.d
import net.komunan.komunantw.R
import net.komunan.komunantw.common.string
import net.komunan.komunantw.repository.database.transaction
import net.komunan.komunantw.repository.entity.Account
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.repository.entity.Timeline
import net.komunan.komunantw.repository.entity.TimelineSource

@Dao
abstract class TimelineDao {
    @Query("SELECT * FROM timeline ORDER BY position ASC")
    abstract fun findAllAsync(): LiveData<List<Timeline>>

    @Query("SELECT COUNT(*) FROM Timeline")
    abstract fun count(): Int

    @Query("UPDATE timeline SET position = position - 1 WHERE position > :deleted")
    abstract fun _packPosition(deleted: Int)

    @Query("UPDATE timeline SET position = position - 1 WHERE position BETWEEN :begin AND :end")
    abstract fun _shiftLeft(begin: Int, end: Int)

    @Query("UPDATE timeline SET position = position + 1 WHERE position BETWEEN :begin AND :end")
    abstract fun _shiftRight(begin: Int, end: Int)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun _insert(timeline: Timeline): Long

    @Update
    abstract fun _update(timeline: Timeline)

    @Delete
    abstract fun _delete(timeline: Timeline)

    fun save(timeline: Timeline): Timeline {
        if (timeline.id == 0L) {
            timeline.id = _insert(timeline.apply {
                position = count() + 1
                createAt = System.currentTimeMillis()
            })
        } else {
            _update(timeline.apply {
                updateAt = System.currentTimeMillis()
            })
        }
        d { "save: $timeline" }
        return timeline
    }

    fun delete(timeline: Timeline) {
        transaction {
            _delete(timeline)
            _packPosition(timeline.position)
        }
    }

    fun moveTo(timeline: Timeline, position: Int) {
        transaction {
            when {
                position > timeline.position -> _shiftLeft(timeline.position + 1, position)
                position < timeline.position -> _shiftRight(position, timeline.position - 1)
            }
            timeline.position = position
            save(timeline)
        }
    }

    fun firstSetup(account: Account) {
        transaction {
            if (Timeline.count() > 0) {
                return@transaction
            }
            val timeline = save(Timeline(R.string.default_label.string()))
            val homeSource = Source.findByAccountId(account.id).first { Source.SourceType.valueOf(it.type) == Source.SourceType.HOME }
            timeline.addSource(homeSource)
        }
    }
}

@Dao
abstract class TimelineSourceDao {
    @Query("SELECT COUNT(*) FROM timeline_source")
    abstract fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun _save(timelineSource: TimelineSource)

    @Delete
    abstract fun _delete(timelineSource: TimelineSource)

    fun add(timeline: Timeline, source: Source) {
        _save(TimelineSource(timeline.id, source.id))
    }

    fun remove(timeline: Timeline, source: Source) {
        _delete(TimelineSource(timeline.id, source.id))
    }
}
