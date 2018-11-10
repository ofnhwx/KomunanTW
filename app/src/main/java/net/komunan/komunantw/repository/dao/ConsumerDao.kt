package net.komunan.komunantw.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.github.ajalt.timberkt.d
import net.komunan.komunantw.repository.entity.Consumer

@Suppress("unused")
@Dao
abstract class ConsumerDao {

    /* ==================== Functions. ==================== */

    fun save(consumer: Consumer): Consumer {
        if (consumer.id == 0L) {
            consumer.id = pInsert(consumer.apply {
                createAt = System.currentTimeMillis()
            })
        } else {
            pUpdate(consumer.apply {
                updateAt = System.currentTimeMillis()
            })
        }
        d { "save: $consumer" }
        return consumer
    }

    fun delete(consumer: Consumer) {
        //assert(!consumer.default)
        pDelete(consumer)
    }

    /* ==================== SQL Definitions. ==================== */

    companion object {
        private const val QUERY_COUNT        = "SELECT COUNT(*) FROM consumer"
        private const val QUERY_FIND         = "SELECT * FROM consumer WHERE id = :id"
        private const val QUERY_FIND_ALL     = "SELECT * FROM consumer ORDER BY default_key DESC, name ASC"
        private const val QUERY_FIND_DEFAULT = "SELECT * FROM consumer WHERE default_key = 1"
    }

    @Query(QUERY_COUNT)
    abstract fun count(): Int

    @Query(QUERY_FIND)
    abstract fun find(id: Long): Consumer?

    @Query(QUERY_FIND_ALL)
    abstract fun findAll(): List<Consumer>

    @Query(QUERY_FIND_ALL)
    abstract fun findAllAsync(): LiveData<List<Consumer>>

    @Query(QUERY_FIND_DEFAULT)
    abstract fun findDefault(): Consumer?

    /* ==================== Protected SQL Definitions. ==================== */

    @Delete
    protected abstract fun pDelete(consumer: Consumer)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract fun pInsert(consumer: Consumer): Long

    @Update
    protected abstract fun pUpdate(consumer: Consumer)
}
