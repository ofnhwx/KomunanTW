package net.komunan.komunantw.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.github.ajalt.timberkt.d
import net.komunan.komunantw.repository.entity.Source

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
        private const val PARTS_JOIN_ACCOUNT  = "LEFT OUTER JOIN account ON account.id = source.account_id"
        private const val PARTS_DEFAULT_ORDER = "ORDER BY account.name ASC, source.ordinal ASC, source.label ASC"

        private const val QUERY_FIND               = "SELECT source.* FROM source WHERE source.id = :id"
        private const val QUERY_FIND_IDS           = "SELECT source.* FROM source $PARTS_JOIN_ACCOUNT WHERE source.id IN (:ids) $PARTS_DEFAULT_ORDER"
        private const val QUERY_FIND_ALL           = "SELECT source.* FROM source $PARTS_JOIN_ACCOUNT $PARTS_DEFAULT_ORDER"
        private const val QUERY_FIND_BY_ACCOUNT_ID = "SELECT source.* FROM source WHERE account_id = :accountId"
    }

    @Query(QUERY_FIND)
    abstract fun find(id: Long): Source?

    @Query(QUERY_FIND_IDS)
    abstract fun find(ids: List<Long>): List<Source>

    @Query(QUERY_FIND_ALL)
    abstract fun findAll(): List<Source>

    @Query(QUERY_FIND_ALL)
    abstract fun findAllAsync(): LiveData<List<Source>>

    @Query(QUERY_FIND_BY_ACCOUNT_ID)
    abstract fun findByAccountId(accountId: Long): List<Source>

    @Delete
    abstract fun delete(source: Source)

    /* ==================== SQL Definitions. ==================== */

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract fun pInsert(source: Source): Long

    @Update
    protected abstract fun pUpdate(source: Source)
}
