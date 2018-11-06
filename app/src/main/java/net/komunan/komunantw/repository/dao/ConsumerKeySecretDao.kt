package net.komunan.komunantw.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.github.ajalt.timberkt.d
import net.komunan.komunantw.repository.entity.ConsumerKeySecret

@Suppress("FunctionName")
@Dao
abstract class ConsumerKeySecretDao {
    fun findAllAsync() = __findAllAsync()
    fun findDefault() = __findDefault()
    fun count() = __count()
    fun save(consumerKeySecret: ConsumerKeySecret) = __save__(consumerKeySecret)
    fun delete(consumerKeySecret: ConsumerKeySecret) = __delete__(consumerKeySecret)

    /* ==================== SQL Definitions. ==================== */

    @Query("SELECT * FROM consumer_key_secret ORDER BY is_default DESC, name ASC")
    protected abstract fun __findAllAsync(): LiveData<List<ConsumerKeySecret>>
    @Query("SELECT * FROM consumer_key_secret ORDER BY is_default DESC, name ASC")
    protected abstract fun __findAll(): List<ConsumerKeySecret>
    @Query("SELECT * FROM consumer_key_secret WHERE is_default = 1")
    protected abstract fun __findDefault(): ConsumerKeySecret?
    @Query("SELECT COUNT(*) FROM consumer_key_secret")
    protected abstract fun __count(): Int
    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract fun __insert(consumerKeySecret: ConsumerKeySecret): Long
    @Update
    protected abstract fun __update(consumerKeySecret: ConsumerKeySecret)
    @Delete
    protected abstract fun __delete(consumerKeySecret: ConsumerKeySecret)

    /* ==================== Private Functions. ==================== */

    private fun __save__(consumerKeySecret: ConsumerKeySecret): ConsumerKeySecret {
        if (consumerKeySecret.id == 0L) {
            consumerKeySecret.id = __insert(consumerKeySecret.apply {
                createAt = System.currentTimeMillis()
            })
        } else {
            __update(consumerKeySecret.apply {
                updateAt = System.currentTimeMillis()
            })
        }
        d { "save: $consumerKeySecret" }
        return consumerKeySecret
    }

    private fun __delete__(consumerKeySecret: ConsumerKeySecret) {
        if (consumerKeySecret.default) {
            return
        }
        __delete(consumerKeySecret)
    }
}
