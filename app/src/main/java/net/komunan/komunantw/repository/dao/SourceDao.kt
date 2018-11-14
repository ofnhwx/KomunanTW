package net.komunan.komunantw.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.github.ajalt.timberkt.d
import net.komunan.komunantw.repository.entity.Source
import net.komunan.komunantw.repository.entity.ext.SourceWithActive

@Suppress("unused")
@Dao
abstract class SourceDao {

    /* ==================== Functions. ==================== */

    fun save(source: Source): Source {
        if (source.id == 0L) {
            source.id = pInsert(source.apply {
                createAt = System.currentTimeMillis()
            })
        } else {
            pUpdate(source.apply {
                updateAt = System.currentTimeMillis()
            })
        }
        d { "save: $source" }
        return source
    }

    /* ==================== SQL Definitions. ==================== */

    companion object {
        private const val PARTS_JOIN_ACCOUNT         = "LEFT OUTER JOIN account AS a ON a.id = s.account_id"
        private const val PARTS_JOIN_TIMELINE_SOURCE = "LEFT OUTER JOIN timeline_source AS ts ON ts.source_id = s.id AND ts.timeline_id = :timelineId"
        private const val PARTS_DEFAULT_ORDER        = "ORDER BY a.name ASC, s.ordinal ASC, s.label ASC"

        private const val QUERY_FIND                 = "SELECT s.* FROM source AS s WHERE s.id = :id"
        private const val QUERY_FIND_IDS             = "SELECT s.* FROM source AS s $PARTS_JOIN_ACCOUNT WHERE s.id IN (:ids) $PARTS_DEFAULT_ORDER"
        private const val QUERY_FIND_ALL             = "SELECT s.* FROM source AS s $PARTS_JOIN_ACCOUNT $PARTS_DEFAULT_ORDER"
        private const val QUERY_FIND_ALL_WITH_ACTIVE = "SELECT s.*, ifnull(ts.source_id, 0) AS is_active FROM source AS s $PARTS_JOIN_ACCOUNT $PARTS_JOIN_TIMELINE_SOURCE $PARTS_DEFAULT_ORDER"
        private const val QUERY_FIND_BY_ACCOUNT_ID   = "SELECT s.* FROM source AS s WHERE s.account_id = :accountId"
        private const val QUERY_FIND_SOURCE_IDS      = "SELECT DISTINCT s.id FROM source AS s ORDER BY s.id ASC"
    }

    @Query(QUERY_FIND)
    abstract fun find(id: Long): Source?

    @Query(QUERY_FIND_IDS)
    abstract fun find(ids: List<Long>): List<Source>

    @Query(QUERY_FIND_ALL)
    abstract fun findAll(): List<Source>

    @Query(QUERY_FIND_ALL)
    abstract fun findAllAsync(): LiveData<List<Source>>

    @Query(QUERY_FIND_ALL_WITH_ACTIVE)
    abstract fun findAllWithActiveAsync(timelineId: Long): LiveData<List<SourceWithActive>>

    @Query(QUERY_FIND_BY_ACCOUNT_ID)
    abstract fun findByAccountId(accountId: Long): List<Source>

    @Query(QUERY_FIND_SOURCE_IDS)
    abstract fun findSourceIds(): List<Long>

    @Delete
    abstract fun delete(source: Source)

    /* ==================== SQL Definitions. ==================== */

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract fun pInsert(source: Source): Long

    @Update
    protected abstract fun pUpdate(source: Source)
}
